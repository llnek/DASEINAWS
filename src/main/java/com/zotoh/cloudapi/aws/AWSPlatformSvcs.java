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

import org.dasein.cloud.platform.AbstractPlatformServices;
import org.dasein.cloud.platform.CDNSupport;
import org.dasein.cloud.platform.KeyValueDatabaseSupport;
import org.dasein.cloud.platform.MessageQueueSupport;
import org.dasein.cloud.platform.PushNotificationSupport;
import org.dasein.cloud.platform.RelationalDatabaseSupport;

import com.zotoh.core.util.Logger;
import static com.zotoh.core.util.CoreUte.*;

/**
 * @author kenl
 *
 */
public class AWSPlatformSvcs extends AbstractPlatformServices 
implements AWSService {

    private Logger ilog() {  return _log=getLogger(AWSPlatformSvcs.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    private final AWSCloud _aws;
    
    /**
     * @param c
     */
    protected AWSPlatformSvcs(AWSCloud c) {
        tstObjArg("aws-cloud",c);
        _aws=c;
    }
        
    @Override
    public CDNSupport getCDNSupport() {
        return new CloudFront(this);
    }

    @Override
    public KeyValueDatabaseSupport getKeyValueDatabaseSupport() {
        return new SDB(this);
    }

    @Override
    public MessageQueueSupport getMessageQueueSupport() {
        return new SQS(this);
    }

    @Override
    public PushNotificationSupport getPushNotificationSupport() {
        return new SNS(this);
    }

    @Override
    public RelationalDatabaseSupport getRelationalDatabaseSupport() {
        return new RDS(this);
    }

    @Override
    public boolean hasCDNSupport() {
        return true;
    }

    @Override
    public boolean hasKeyValueDatabaseSupport() {
        return true;
    }

    @Override
    public boolean hasMessageQueueSupport() {
        return true;
    }

    @Override
    public boolean hasPushNotificationSupport() {
        return true;
    }

    @Override
    public boolean hasRelationalDatabaseSupport() {
        return true;
    }


    @Override
    public AWSCloud getCloud() {
        return _aws;
    }

}
