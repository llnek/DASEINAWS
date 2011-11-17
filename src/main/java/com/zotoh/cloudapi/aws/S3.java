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

import java.io.File;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.encryption.Encryption;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;
import org.dasein.cloud.storage.FileTransfer;

import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class S3 implements BlobStoreSupport {

    private Logger ilog() {  return _log=getLogger(S3.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    @SuppressWarnings("unused")
    private final AWSCloudStorageSvcs _svc;
    
    /**
     * @param s
     */
    protected S3(AWSCloudStorageSvcs s) {
        tstObjArg("cloudstorage-service",s);
        _svc=s;
    }
    
    @Override
    public void clear(String bucket) throws CloudException, InternalException {
        //TODO
    }

    @Override
    public String createDirectory(String bucket, boolean findFreeName)
            throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileTransfer download(CloudStoreObject cloudFile, File diskFile)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileTransfer download(String arg0, String arg1, File arg2,
            Encryption arg3) throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists(String arg0) throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long exists(String arg0, String arg1, boolean arg2)
            throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getMaxFileSizeInBytes() throws InternalException,
            CloudException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getProviderTermForDirectory(Locale arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProviderTermForFile(Locale arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPublic(String arg0, String arg1) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<CloudStoreObject> listFiles(String arg0)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void makePublic(String arg0) throws InternalException,
            CloudException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void makePublic(String arg0, String arg1) throws InternalException,
            CloudException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void moveFile(String arg0, String arg1, String arg2)
            throws InternalException, CloudException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeDirectory(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeFile(String arg0, String arg1, boolean arg2)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String renameDirectory(String arg0, String arg1, boolean arg2)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void renameFile(String arg0, String arg1, String arg2)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void upload(File arg0, String arg1, String arg2, boolean arg3,
            Encryption arg4) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        
    }

}
