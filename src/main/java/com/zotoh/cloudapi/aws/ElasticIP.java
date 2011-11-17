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

import static com.zotoh.cloudapi.aws.AWSAPI.testForNotSubError;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.network.AddressType;
import org.dasein.cloud.network.IpAddress;
import org.dasein.cloud.network.IpAddressSupport;
import org.dasein.cloud.network.IpForwardingRule;
import org.dasein.cloud.network.Protocol;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class ElasticIP implements IpAddressSupport {

    private Logger ilog() {  return _log=getLogger(ElasticIP.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    
    private final AWSNetworkSvcs _svc;
    
    /**
     * @param s
     */
    protected ElasticIP(AWSNetworkSvcs s) {
    	tstObjArg("network-service",s);
        _svc=s;
    }
    
    @Override
    public void assign(String addr, String server) throws InternalException,
            CloudException {
    	tstEStrArg("instance-id", server);
    	tstEStrArg("ip-addr", addr);
        _svc.getCloud().getEC2()
            .associateAddress(
                    new AssociateAddressRequest()
                    .withInstanceId(server)
                    .withPublicIp(addr)) ;
    }

    @Override
    public String forward(String addr, int pubPort, Protocol p, int prvPort,
            String server) throws InternalException, CloudException {
        throw new OperationNotSupportedException();
    }

    @Override
    public IpAddress getIpAddress(String ipAddr) throws InternalException,
            CloudException {
    	tstEStrArg("ip-addr", ipAddr);
        DescribeAddressesResult res=_svc.getCloud().getEC2()
            .describeAddresses(new DescribeAddressesRequest().withPublicIps(ipAddr)) ;
        List<Address> lst= res==null ? null : res.getAddresses();
        Address a= isNil(lst) ? null : lst.get(0);
        return toIPAddr(a) ;
    }

    @Override
    public String getProviderTermForIpAddress(Locale loc) {
        return "elastic-ip";
    }

    @Override
    public boolean isAssigned(AddressType t) {
        return t != null && t.equals(AddressType.PUBLIC);
    }

    @Override
    public boolean isForwarding() {
        return false;
    }

    @Override
    public boolean isRequestable(AddressType t) {
        return t != null && t.equals(AddressType.PUBLIC);
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            _svc.getCloud().getEC2().describeAddresses();
            return true;
        }
        catch (AmazonServiceException e) {
        	if ( testForNotSubError(e, "SignatureDoesNotMatch")) { return false; }
        	else {
                throw new CloudException(e);                    		
        	}
        }
        catch (AmazonClientException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public Iterable<IpAddress> listPrivateIpPool(boolean unassignedOnly)
            throws InternalException, CloudException {
        return Collections.emptyList();
    }

    @Override
    public Iterable<IpAddress> listPublicIpPool(boolean unassignedOnly)
            throws InternalException, CloudException {
        DescribeAddressesResult res= _svc.getCloud().getEC2()
            .describeAddresses(new DescribeAddressesRequest());
        List<Address> lst= res==null ? null : res.getAddresses();
        Address a;
        List<IpAddress> rc= LT();
        if (lst != null) for (int i=0; i < lst.size(); ++i) {
        	a=lst.get(i);
        	if ( unassignedOnly && !isEmpty(a.getInstanceId())) {}
        	else
            rc.add( toIPAddr( a));
        }
        return rc;
    }

    @Override
    public Iterable<IpForwardingRule> listRules(String addr)
            throws InternalException, CloudException {
    	throw new OperationNotSupportedException();
    }

    @Override
    public void releaseFromPool(String addr) throws InternalException,
            CloudException {
        tstEStrArg("public-ip", addr) ;
        _svc.getCloud().getEC2()
            .releaseAddress(new ReleaseAddressRequest().withPublicIp(addr)) ;
    }

    @Override
    public void releaseFromServer(String addr) throws InternalException,
            CloudException {
        tstEStrArg("public-ip", addr) ;
        _svc.getCloud().getEC2()
            .disassociateAddress(new DisassociateAddressRequest().withPublicIp(addr)) ;
    }

    @Override
    public String request(AddressType t) throws InternalException,
            CloudException {
        if (!AddressType.PUBLIC.equals(t)) {
            throw new IllegalArgumentException("Expecting type: PUBLIC, got: " + t);
        }
        String rg= _svc.getCloud().getContext().getRegionId();
        _svc.getCloud().setAWSSite(rg) ;
        AllocateAddressResult res= _svc.getCloud().getEC2()
        .allocateAddress(new AllocateAddressRequest());
        return res==null ? null : res.getPublicIp();
    }

    @Override
    public void stopForward(String addr) throws InternalException,
            CloudException {
        throw new OperationNotSupportedException();
    }

    /**/
    private IpAddress toIPAddr(Address a) {
        IpAddress p= null;
        if (a != null) {
            p=new IpAddress();
            p.setRegionId( _svc.getCloud().getContext().getRegionId()) ;
            p.setAddressType(AddressType.PUBLIC);
            p.setServerId(a.getInstanceId());
            p.setAddress(a.getPublicIp());
            p.setIpAddressId(p.getAddress());
        }
        return p;
    }
    
}
