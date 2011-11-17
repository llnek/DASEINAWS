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
import static com.zotoh.core.util.CoreUte.*;

import java.util.Collection;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.TimeWindow;
import org.dasein.cloud.platform.ConfigurationParameter;
import org.dasein.cloud.platform.Database;
import org.dasein.cloud.platform.DatabaseConfiguration;
import org.dasein.cloud.platform.DatabaseEngine;
import org.dasein.cloud.platform.DatabaseProduct;
import org.dasein.cloud.platform.DatabaseSnapshot;
import org.dasein.cloud.platform.RelationalDatabaseSupport;

import com.zotoh.core.util.Logger;


public class RDS implements RelationalDatabaseSupport {

    private Logger ilog() {  return _log=getLogger(RDS.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    @SuppressWarnings("unused")
    private final AWSPlatformSvcs _svc;
    
    /**
     * @param s
     */
    protected RDS(AWSPlatformSvcs s) {
        tstObjArg("platform-service",s);
        _svc=s;
    }
    
    @Override
    public void addAccess(String arg0, String arg1) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void alterDatabase(String arg0, boolean arg1, String arg2, int arg3,
            String arg4, String arg5, String arg6, int arg7, int arg8,
            TimeWindow arg9, TimeWindow arg10) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String createFromLatest(String arg0, String arg1, String arg2,
            String arg3, int arg4) throws InternalException, CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createFromScratch(String arg0, DatabaseProduct arg1,
            String arg2, String arg3, int arg4) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createFromSnapshot(String arg0, String arg1, String arg2,
            String arg3, String arg4, int arg5) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createFromTimestamp(String arg0, String arg1, long arg2,
            String arg3, String arg4, int arg5) throws InternalException,
            CloudException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatabaseConfiguration getConfiguration(String arg0)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Database getDatabase(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<DatabaseEngine> getDatabaseEngines() throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<DatabaseProduct> getDatabaseProducts(DatabaseEngine arg0)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProviderTermForDatabase(Locale arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProviderTermForSnapshot(Locale arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatabaseSnapshot getSnapshot(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupportsFirewallRules() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupportsHighAvailability() throws CloudException,
            InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupportsLowAvailability() throws CloudException,
            InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupportsMaintenanceWindows() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupportsSnapshots() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<String> listAccess(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<DatabaseConfiguration> listConfigurations()
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Database> listDatabases() throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ConfigurationParameter> listParameters(String arg0)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<DatabaseSnapshot> listSnapshots(String arg0)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeConfiguration(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeDatabase(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeSnapshot(String arg0) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetConfiguration(String arg0, String... arg1)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restart(String arg0, boolean arg1) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokeAccess(String arg0, String arg1) throws CloudException,
            InternalException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public DatabaseSnapshot snapshot(String arg0, String arg1)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateConfiguration(String arg0, ConfigurationParameter... arg1)
            throws CloudException, InternalException {
        // TODO Auto-generated method stub
        
    }

}
