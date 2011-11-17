/*??
 * COPYRIGHT (C) 2011 CHERIMOIA LLC. ALL RIGHTS RESERVED.
 *
 * THIS IS FREE SOFTWARE; YOU CAN REDISTRIBUTE IT AND/OR
 * MODIFY IT UNDER THE TERMS OF THE APACHE LICENSE, 
 * VERSION 2.0 (THE "LICENSE").
 *
 * THIS LIBRARY IS DISTRIBUTED IN THE HOPE THAT IT WILL BE USEFUL,
 * BUT WITHOUT ANY WARRANTY; WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 *   
 * SEE THE LICENSE FOR THE SPECIFIC LANGUAGE GOVERNING PERMISSIONS 
 * AND LIMITATIONS UNDER THE LICENSE.
 *
 * You should have received a copy of the Apache License
 * along with this distribution; if not, you may obtain a copy of the 
 * License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 ??*/


package com.zotoh.cloudapi.aws;

import static com.zotoh.cloudapi.aws.AWSAPI.strProtocol;
import static com.zotoh.cloudapi.aws.AWSAPI.testForNotSubError;
import static com.zotoh.cloudapi.aws.AWSAPI.toLbProtocol;
import static com.zotoh.cloudapi.aws.AWSAPI.toObjList;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.network.LbAlgorithm;
import org.dasein.cloud.network.LbListener;
import org.dasein.cloud.network.LbProtocol;
import org.dasein.cloud.network.LoadBalancer;
import org.dasein.cloud.network.LoadBalancerAddressType;
import org.dasein.cloud.network.LoadBalancerState;
import org.dasein.cloud.network.LoadBalancerSupport;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.DisableAvailabilityZonesForLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.EnableAvailabilityZonesForLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.zotoh.core.util.Logger;

import static com.zotoh.core.util.LangUte.*;


/**
 * @author kenl
 *
 */
public class ElasticLoadBalancer implements LoadBalancerSupport {

    private Logger ilog() {  return _log=getLogger(ElasticLoadBalancer.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    private static List<LbAlgorithm> _algos= 
            Collections.unmodifiableList(toObjList(LbAlgorithm.ROUND_ROBIN) );
    
    private static List<LbProtocol> _protocols=
            Collections.unmodifiableList(toObjList(LbProtocol.HTTP, LbProtocol.RAW_TCP) );
    
    private final AWSNetworkSvcs _svc;
    
    /**
     * @param s
     */
    protected ElasticLoadBalancer(AWSNetworkSvcs s) {
    	tstObjArg("network-service",s);
        _svc=s;
    }

	@Override
    public void addDataCenters(String balancer, final String... zones)
            throws CloudException, InternalException {
    	tstEStrArg("load-balancer-name", balancer);
    	List<String> lst= toObjList(zones); 
        _svc.getCloud().getELB()
        .enableAvailabilityZonesForLoadBalancer(
				new EnableAvailabilityZonesForLoadBalancerRequest()
				.withAvailabilityZones(lst)
				.withLoadBalancerName(balancer));
    }

    @SuppressWarnings("serial")
	@Override
    public void addServers(String balancer, final String... servers) throws CloudException,
            InternalException {
    	tstEStrArg("load-balancer-name", balancer);
    	List<Instance> lst= new ArrayList<Instance>(){{ 
        	for (int i=0; i < servers.length; ++i) {
        		add(new Instance(servers[i]));
        	}    		
    	}};
    	_svc.getCloud().getELB()
    	.registerInstancesWithLoadBalancer(
				new RegisterInstancesWithLoadBalancerRequest()
				.withInstances(lst)
				.withLoadBalancerName(balancer));
    }

    @Override
    public String create(String name, String desc, String addrIgnoredByAWS, String[] zones,
            LbListener[] lis, String[] servers) throws CloudException,
            InternalException {
    	tstEStrArg("load-balancer-name", name) ;
    	tstObjArg("listeners", lis);
    	tstObjArg("zones", zones);
    	List<Listener> lst= LT();
    	Listener ln;
    	LbListener lb; 
    	for (int i=0; i < lis.length; ++i) {
    		ln= new Listener();    		
    		lb=lis[i];
    		ln.setProtocol(strProtocol( lb.getNetworkProtocol()));
    		ln.setInstancePort(lb.getPrivatePort());
    		ln.setLoadBalancerPort(lb.getPublicPort());
    		lst.add(ln);
    	}
    	CreateLoadBalancerResult res= _svc.getCloud().getELB()
    	.createLoadBalancer(
				new CreateLoadBalancerRequest()
				.withLoadBalancerName(name)
				.withListeners(lst)
				.withAvailabilityZones(zones));
    	return res==null ? null : res.getDNSName();
    }

    @Override
    public LoadBalancerAddressType getAddressType() throws CloudException,
            InternalException {
        return LoadBalancerAddressType.DNS;
    }

    @Override
    public LoadBalancer getLoadBalancer(String balancer) throws CloudException,
            InternalException {
    	tstEStrArg("load-balancer-name", balancer);
    	DescribeLoadBalancersResult res=_svc.getCloud().getELB()
    	.describeLoadBalancers(
				new DescribeLoadBalancersRequest().withLoadBalancerNames(balancer));
    	List<LoadBalancerDescription> lst = res==null ? null : res.getLoadBalancerDescriptions();
    	LoadBalancerDescription d = isNil(lst) ? null : lst.get(0);
    	return toELB(d);
    }

    @Override
    public int getMaxPublicPorts() throws CloudException, InternalException {
        return 0;
    }

    @Override
    public String getProviderTermForLoadBalancer(Locale loc) {
        return "load-balancer";
    }

    @Override
    public boolean isAddressAssignedByProvider() throws CloudException,
            InternalException {
        return true;
    }

    @Override
    public boolean isDataCenterLimited() throws CloudException,
            InternalException {
        return true;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            _svc.getCloud().getELB()
            .describeLoadBalancers(new DescribeLoadBalancersRequest());
            return true;
        }
        catch (AmazonServiceException e) {
        	if (testForNotSubError(e,"SubscriptionCheckFailed" 
        					,"AuthFailure","SignatureDoesNotMatch"
        					,"OptInRequired","InvalidClientTokenId" )) { return false; }
        	else {
            throw new CloudException(e); }            
        }
        catch (AmazonClientException e) {
            throw new InternalException(e);
        }
        
    }

    @Override
    public Iterable<LoadBalancer> listLoadBalancers() throws CloudException,
            InternalException {
        DescribeLoadBalancersResult res= _svc.getCloud().getELB()
        .describeLoadBalancers(new DescribeLoadBalancersRequest());
        List<LoadBalancerDescription> lst= res==null ? null : res.getLoadBalancerDescriptions();
        List<LoadBalancer> rc=LT();
        if (lst != null) for (int i=0; i < lst.size(); ++i) {
            rc.add( toELB(lst.get(i)));
        }
        return rc;
    }

    @Override
    public Iterable<LbAlgorithm> listSupportedAlgorithms()
            throws CloudException, InternalException {
        return _algos;
    }

    @Override
    public Iterable<LbProtocol> listSupportedProtocols() throws CloudException,
            InternalException {
        return _protocols;
    }

    @Override
    public void remove(String balancer) throws CloudException, InternalException {
        tstEStrArg("load-balancer-name", balancer);
        _svc.getCloud().getELB()
        .deleteLoadBalancer(new DeleteLoadBalancerRequest()
        .withLoadBalancerName(balancer)) ;
    }

    @Override
    public void removeDataCenters(String balancer, final String... zones)
            throws CloudException, InternalException {
        tstEStrArg("load-balancer-name", balancer);
        if (zones==null || zones.length==0) 
        { return; }
        List<String> lst= toObjList(zones);
        _svc.getCloud().getELB()
        .disableAvailabilityZonesForLoadBalancer(
                new DisableAvailabilityZonesForLoadBalancerRequest()
                .withLoadBalancerName(balancer)
                .withAvailabilityZones(lst)) ;
    }

    @SuppressWarnings("serial")
    @Override
    public void removeServers(String balancer, final String... servers)
            throws CloudException, InternalException {
        tstEStrArg("load-balancer-name", balancer);
        if (servers==null || servers.length==0) 
        { return; }
        List<Instance> lst= new ArrayList<Instance>() {{ 
            for (int i=0; i < servers.length; ++i) {
                add(new Instance(servers[i]));
            }            
        }};
        _svc.getCloud().getELB()
        .deregisterInstancesFromLoadBalancer(
                new DeregisterInstancesFromLoadBalancerRequest()
                .withLoadBalancerName(balancer)
                .withInstances(lst));
    }

    @Override
    public boolean requiresListenerOnCreate() throws CloudException,
            InternalException {
        return true;
    }

    @Override
    public boolean requiresServerOnCreate() throws CloudException,
            InternalException {
        return false;
    }

    @Override
    public boolean supportsMonitoring() throws CloudException,
            InternalException {
        return true;
    }

    /**/
    private LoadBalancer toELB(LoadBalancerDescription desc) {
        LoadBalancer b= null;
        if (desc != null) {
            ProviderContext x= _svc.getCloud().getContext();
            b= new LoadBalancer();
            
            b.setCreationTimestamp(desc.getCreatedTime().getTime());
            b.setProviderRegionId(x.getRegionId());
            b.setAddressType(LoadBalancerAddressType.DNS);
            b.setCurrentState(LoadBalancerState.ACTIVE);
            b.setProviderOwnerId(x.getAccountNumber());
            b.setName(desc.getLoadBalancerName());
            b.setDescription(b.getName());
            b.setProviderLoadBalancerId(b.getName());
            b.setAddress(desc.getDNSName());
            
            // zones
            {
                List<String> lst= desc.getAvailabilityZones();
                if (!isNil(lst)) {
                    b.setProviderDataCenterIds(
                            lst.toArray(new String[0]) );
                }
            }
            // servers
            {
                List<Instance> lst = desc.getInstances();
                List<String> s = LT();
                if (!isNil(lst)) for (int i=0; i < lst.size(); ++i) {
                    s.add( lst.get(i).getInstanceId());
                }                
                b.setProviderServerIds(s.toArray(new String[0]));
            }
            // listeners/ports
            {
                List<ListenerDescription> lst= desc.getListenerDescriptions();
                List<LbListener> rc= LT();
                int[] pports;
                if (lst != null) for (int i=0; i < lst.size(); ++i) {
                    rc.add( toLis( lst.get(i)));
                }                
                b.setListeners(rc.toArray(new LbListener[0]));
                pports= new int[ rc.size()] ;
                for (int i=0; i < pports.length; ++i) {
                    pports[i]=rc.get(i).getPublicPort();
                }
                b.setPublicPorts(pports) ;
            }
                        
            // unsupported
            desc.getHealthCheck();            
            desc.getPolicies();
            desc.getSourceSecurityGroup();
            desc.getCanonicalHostedZoneName();
        }
        
        return b;
    }
    
    /**/
    private LbListener toLis(ListenerDescription desc) {
        LbListener rc= null;
        if (desc != null) {
            rc= new LbListener();
            rc.setAlgorithm(LbAlgorithm.ROUND_ROBIN);
            rc.setNetworkProtocol(toLbProtocol( desc.getListener().getProtocol()));
            rc.setPublicPort(desc.getListener().getLoadBalancerPort());
            rc.setPrivatePort(desc.getListener().getInstancePort());                  
        }
        return rc;
    }
    
    
    
}
