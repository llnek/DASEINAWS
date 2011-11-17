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

import org.dasein.cloud.identity.AbstractIdentityServices;
import org.dasein.cloud.identity.ShellKeySupport;

import com.zotoh.core.util.Logger;
import static com.zotoh.core.util.CoreUte.*;



/**
 * @author kenl
 *
 */
public class AWSIdentitySvcs extends AbstractIdentityServices 
implements AWSService {

    private Logger ilog() {  return _log=getLogger(AWSIdentitySvcs.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
        
    private final AWSCloud _aws;
    
    /**
     * @param c
     */
    protected AWSIdentitySvcs(AWSCloud c) {
        tstObjArg("aws-cloud",c);
        _aws=c;
    }
    
    @Override
    public ShellKeySupport getShellKeySupport() {
        return new Keypair(this);
    }

    @Override
    public boolean hasShellKeySupport() {
        return true;
    }

    @Override
    public AWSCloud getCloud() {
        return _aws;
    }

    
}
