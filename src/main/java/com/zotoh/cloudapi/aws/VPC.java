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

import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LoggerFactory.getLogger;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.network.IpAddress;
import org.dasein.cloud.network.VLANSupport;
import org.dasein.cloud.network.Vlan;

import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class VPC  implements VLANSupport {

    private Logger ilog() {  return _log=getLogger(VPC.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    @SuppressWarnings("unused")
    private final AWSNetworkSvcs _svc;
    
    /**
     * @param s
     */
    protected VPC(AWSNetworkSvcs s) {
        tstObjArg("network-service", s);
        _svc=s;
    }
    
    @Override
    public boolean allowsNewVlanCreation() throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getMaxVlanCount() throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vlan getVlan(String arg0) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<IpAddress> listAddresses(String arg0)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Vlan> listVlans() throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String provisionVlan(String arg0, String arg1)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String provisionVlanWithSubnet(String arg0, String arg1,
            String arg2, String arg3, String... arg4) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeVlan(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean supportsCustomSubnets() {
        // TODO Auto-generated method stub
        return false;
    }

}
