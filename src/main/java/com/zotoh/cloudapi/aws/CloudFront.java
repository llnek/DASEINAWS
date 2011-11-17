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

import java.util.Collection;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.platform.CDNSupport;
import org.dasein.cloud.platform.Distribution;

import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class CloudFront  implements CDNSupport {

    private Logger ilog() {  return _log=getLogger(CloudFront.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    @SuppressWarnings("unused")
    private final AWSPlatformSvcs _svc;
    
    /**
     * @param s
     */
    protected CloudFront(AWSPlatformSvcs s) {
        tstObjArg("platform-service",s);
        _svc=s;
    }
    
    @Override
    public String create(String bucket, String name, boolean active, String... cnames)
            throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String arg0) throws InternalException, CloudException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Distribution getDistribution(String arg0) throws InternalException,
            CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProviderTermForDistribution(Locale arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSubscribed() throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<Distribution> list() throws InternalException,
            CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(String arg0, String arg1, boolean arg2, String... arg3)
            throws InternalException, CloudException {
        // TODO Auto-generated method stub
        
    }

}
