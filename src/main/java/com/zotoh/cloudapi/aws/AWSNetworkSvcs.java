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

import static com.zotoh.core.util.LoggerFactory.getLogger;

import org.dasein.cloud.network.AbstractNetworkServices;
import org.dasein.cloud.network.DNSSupport;
import org.dasein.cloud.network.FirewallSupport;
import org.dasein.cloud.network.IpAddressSupport;
import org.dasein.cloud.network.LoadBalancerSupport;
import org.dasein.cloud.network.VLANSupport;

import com.zotoh.core.util.Logger;
import static com.zotoh.core.util.CoreUte.*;


/**
 * @author kenl
 *
 */
public class AWSNetworkSvcs extends AbstractNetworkServices 
implements AWSService {

    private Logger ilog() {  return _log=getLogger(AWSNetworkSvcs.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
        
    private final AWSCloud _aws;
    
    /**
     * @param c
     */
    protected AWSNetworkSvcs(AWSCloud c) {
        tstObjArg("aws-cloud",c);
        _aws=c;
    }
    
    @Override
    public DNSSupport getDnsSupport() {
        return new DNSRoute53(this);
    }

    @Override
    public FirewallSupport getFirewallSupport() {
        return new SecurityGroup(this);
    }

    @Override
    public IpAddressSupport getIpAddressSupport() {
        return new ElasticIP(this);
    }

    @Override
    public LoadBalancerSupport getLoadBalancerSupport() {
        return new ElasticLoadBalancer(this);
    }

    @Override
    public VLANSupport getVlanSupport() {
        return new VPC(this);
    }

    @Override
    public boolean hasDnsSupport() {
        return true;
    }

    @Override
    public boolean hasFirewallSupport() {
        return true;
    }

    @Override
    public boolean hasIpAddressSupport() {
        return true;
    }

    @Override
    public boolean hasLoadBalancerSupport() {
        return true;
    }

    @Override
    public boolean hasVlanSupport() {
        return true;
    }

    @Override
    public AWSCloud getCloud() {
        return _aws;
    }

}
