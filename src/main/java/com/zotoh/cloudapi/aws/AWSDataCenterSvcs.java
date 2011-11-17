package com.zotoh.cloudapi.aws;

import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.dc.DataCenter;
import org.dasein.cloud.dc.DataCenterServices;
import org.dasein.cloud.dc.Region;

import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeRegionsRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class AWSDataCenterSvcs implements DataCenterServices 
, AWSService {

    private Logger ilog() {  return _log=getLogger(AWSDataCenterSvcs.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    
    private final AWSCloud _aws;
    
    /**
     * @param c
     */
    protected AWSDataCenterSvcs(AWSCloud c) {
        tstObjArg("aws-cloud",c) ;
        _aws=c;
    }
    
    @Override
    public DataCenter getDataCenter(String zone) throws InternalException,
            CloudException {
    	tstEStrArg("zone-name", zone);
    	DescribeAvailabilityZonesResult res= _aws.getEC2()
    	.describeAvailabilityZones(
				new DescribeAvailabilityZonesRequest()	.withZoneNames(zone));
    	List<AvailabilityZone> lst= res==null ? null : res.getAvailabilityZones();
    	AvailabilityZone z= isNil(lst) ? null : lst.get(0);    	
        return toDC(z);
    }

    @Override
    public String getProviderTermForDataCenter(Locale loc) {
        return "availability-zone";
    }

    @Override
    public String getProviderTermForRegion(Locale loc) {
        return "region";
    }

    @Override
    public Region getRegion(String name) throws InternalException,
            CloudException {
    	tstEStrArg("region-name", name);
    	DescribeRegionsResult res= _aws.getEC2().describeRegions(
    					new DescribeRegionsRequest().withRegionNames(name));
    	List<com.amazonaws.services.ec2.model.Region> lst= res==null ? null : res.getRegions();
    	com.amazonaws.services.ec2.model.Region r= isNil(lst) ? null : lst.get(0);    	
    	return toReg(r);
    }

    @Override
    public Collection<DataCenter> listDataCenters(String name)
            throws InternalException, CloudException {
    	DescribeAvailabilityZonesResult res= _aws.newEC2(name)
    	.describeAvailabilityZones(
				new DescribeAvailabilityZonesRequest());

    	List<AvailabilityZone> lst= res==null ? null : res.getAvailabilityZones();
    	List<DataCenter> rc= LT();
    	if (lst != null) for (int i=0; i < lst.size(); ++i) {
    		rc.add( toDC( lst.get(i)));
    	}

        return rc;
    }

    @Override
    public Collection<Region> listRegions() throws InternalException,
            CloudException {
    	DescribeRegionsResult res= _aws.getEC2().describeRegions();
    	List<com.amazonaws.services.ec2.model.Region> lst = res==null ? null : res.getRegions();
    	List<Region> rc= LT();
    	if (lst != null) for (int i=0; i < lst.size(); ++i) {
    		rc.add( toReg(lst.get(i)));    		
    	}
        return rc;
    }

    @Override
    public AWSCloud getCloud() {
        return _aws;
    }

    /**/
    private DataCenter toDC(AvailabilityZone z) {
    	DataCenter c= null;
    	if (z != null) {
    		c= new DataCenter();
    		c.setAvailable("available".equals(z.getState()));
    		c.setActive(c.isAvailable());
    		c.setRegionId(z.getRegionName());
    		c.setName(z.getZoneName());
    		c.setProviderDataCenterId(c.getName());
    	}
    	return c;
    }
    
    /**/
    private Region toReg(com.amazonaws.services.ec2.model.Region r) {
    	Region g= null;
    	if (r != null) {
    		g= new Region();
    		g.setProviderRegionId(r.getEndpoint());
    		g.setActive(true);
    		g.setAvailable(true);
    		g.setName(r.getRegionName());
    	}
    	return g;
    }
}
