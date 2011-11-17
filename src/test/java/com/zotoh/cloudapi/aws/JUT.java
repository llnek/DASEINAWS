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
import static com.zotoh.core.util.StrUte.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.JUnit4TestAdapter;

import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.Snapshot;
import org.dasein.cloud.compute.SnapshotSupport;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.compute.Volume;
import org.dasein.cloud.compute.VolumeSupport;
import org.dasein.cloud.dc.DataCenter;
import org.dasein.cloud.dc.DataCenterServices;
import org.dasein.cloud.dc.Region;
import org.dasein.cloud.identity.IdentityServices;
import org.dasein.cloud.identity.ShellKeySupport;
import org.dasein.cloud.network.DNSSupport;
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallSupport;
import org.dasein.cloud.network.IpAddressSupport;
import org.dasein.cloud.network.LoadBalancerSupport;
import org.dasein.cloud.network.NetworkServices;
import org.dasein.cloud.network.Protocol;
import org.dasein.cloud.platform.KeyValueDatabase;
import org.dasein.cloud.platform.KeyValueDatabaseSupport;
import org.dasein.cloud.platform.KeyValuePair;
import org.dasein.cloud.platform.PlatformServices;
import org.dasein.cloud.platform.PushNotificationSupport;
import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.StorageServices;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import com.zotoh.cloudapi.core.Vars;
import com.zotoh.core.util.Logger;

/**/
public class JUT implements AWSVars, Vars {

    private Logger ilog() {  return _log=getLogger(JUT.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
	
    private static CloudProvider _aws;
	private static Properties _props;
	
    /**/
    public static junit.framework.Test suite()     {
        return new JUnit4TestAdapter(JUT.class);
    }

    //@BeforeClass
    public static void iniz() throws Exception     {
        
        _props= new Properties();
    	_props.put(P_PWD, "");
    	_props.put(P_ACCT, "");
    	_props.put(P_ID, "");
    	
        _aws= AWSInitContext.getInstance().configure(_props);    	
    }
    
    @AfterClass
    public static void finz() throws Exception     {
    }
    
    /**/
    //@Before
    public void open() throws Exception     {
//        System.out.println("@Before open()");
        ((AWSCloud)_aws).setAWSSite("us-east-1") ;
    }

    /**/
    @After
    public void close() throws Exception     {
//        System.out.println("@Before close()");
    }

    //@Test
    public void testSortProducts() throws Exception {
        String s;
        s= Architecture.I32.name();
        s=null;
    }
    
    //@Test
    public void testBasicInfo() throws Exception {
        assertTrue("Amazon".equals(_aws.getProviderName()));
        assertTrue("AWS".equals(_aws.getCloudName()));
        ProviderContext x= _aws.getContext();
        assertNotNull(x);
        assertEquals(_props.getProperty(P_ACCT),x.getAccountNumber());        
        assertEquals(x.getAccountNumber(),x.getEffectiveAccountNumber());
    }

    //@Test
    public void testComputeSvcs() throws Exception {
        ComputeServices s=_aws.getComputeServices();
        //AutoScalingSupport s1= s.getAutoScalingSupport();
        MachineImageSupport s2=s.getImageSupport();
        VirtualMachineSupport s3=s.getVirtualMachineSupport();
        SnapshotSupport s4= s.getSnapshotSupport();
        VolumeSupport s5=s.getVolumeSupport();
        assertTrue(s2.isSubscribed());
        assertTrue(s3.isSubscribed());
        assertTrue(s4.isSubscribed());
        assertTrue(s5.isSubscribed());
    }

    //@Test
    public void testStorageSvcs() throws Exception {
        StorageServices svcs= _aws.getStorageServices();
        BlobStoreSupport s1= svcs.getBlobStoreSupport();
        assertNotNull(s1);
    }
    
    //@Test
    public void testPlatformSvcs() throws Exception {
        PlatformServices svc=_aws.getPlatformServices();
        svc.getCDNSupport();
        KeyValueDatabaseSupport s2=svc.getKeyValueDatabaseSupport();
        svc.getMessageQueueSupport();
        PushNotificationSupport s4=svc.getPushNotificationSupport() ;
        svc.getRelationalDatabaseSupport();
        assertTrue(s2.isSubscribed());
//        ((AWSCloud)_aws).setAWSSite("eu-west-1");
//        assertTrue(s2.isSubscribed());
        assertTrue(s4.isSubscribed());
    }
    
    //@Test
    public void testNetworkSvcs() throws Exception {
        NetworkServices svc= _aws.getNetworkServices();
        DNSSupport s1=svc.getDnsSupport();
        FirewallSupport s2=svc.getFirewallSupport();
        IpAddressSupport s3= svc.getIpAddressSupport();
        LoadBalancerSupport s4=svc.getLoadBalancerSupport();
        assertFalse(s1.isSubscribed());
        Collection<Firewall> lst=s2.list();
        assert(lst != null && lst.size() > 1); // at least should have "default" ?
        assertTrue(s3.isSubscribed());
        assertTrue(s4.isSubscribed());
    }
    
    //@Test
    public void testIdentitiesSvcs() throws Exception {
        IdentityServices svc=_aws.getIdentityServices();
        ShellKeySupport s1= svc.getShellKeySupport();
        Collection<String> lst= s1.list();
        assertNotNull(lst);
    }
    
    //@Test
    public void testDataCenterSvcs() throws Exception {
        DataCenterServices svcs= _aws.getDataCenterServices();
        Region r= svcs.getRegion("us-west-1");
        assertNotNull(r);
        assertEquals("us-west-1", r.getName());
        assertEquals(r.getProviderRegionId(), "ec2.us-west-1.amazonaws.com");
        
        Collection<Region>rs= svcs.listRegions();
        assertTrue(rs.size()>=5);
        Collection<DataCenter>cs=svcs.listDataCenters("eu-west-1");
        assertTrue(cs.size()>=1);
        
        ((AWSCloud)_aws).setAWSSite("us-west-1") ;
        DataCenter c=svcs.getDataCenter("us-west-1a");        
        assertEquals("us-west-1a",c.getProviderDataCenterId());
        assertEquals("us-west-1",c.getRegionId());
        assertEquals(c.getProviderDataCenterId(), c.getName());
    }
    
    //@Test
    public void testKeypairs() throws Exception {
        ShellKeySupport s= _aws.getIdentityServices().getShellKeySupport();
        String kp="zzzxxxyyy000777";
        String pem=s.createKeypair(kp);
        assertTrue(pem != null && pem.length() > 0);
        int sz= s.list().size();
        assertTrue(sz > 0);
        String fp=s.getFingerprint(kp);
        assertTrue(fp != null && fp.length() > 0);
        s.deleteKeypair(kp);
        fp=s.getFingerprint(kp);
        assertNull(fp);
        // amazon doesn't throw error even if you delete a non-existent keypair, up till now
        s.deleteKeypair("qqqsdfsdgsdgiuyiyyusdd");
    }
    
    //@Test
    public void testSecGroups() throws Exception {
        FirewallSupport s= _aws.getNetworkServices().getFirewallSupport();
        String grp= "zzzxxxyyy000777";
        String gid=s.create(grp, "test");
        assertTrue(gid != null && gid.length() > 0);
        int sz=s.list().size();
        assertTrue(sz > 0);
        assertTrue(s.getRules(grp).size()==0);
        s.authorize(grp, "0.0.0.0/0", Protocol.TCP, 8080, 8080);
        assertTrue(s.getRules(grp).size()==1);
        s.revoke(grp, "0.0.0.0/0", Protocol.TCP, 8080,8080);
        assertTrue(s.getRules(grp).size()==0);
        s.delete(grp);
        // we've made it so it won't throw exception if delete a non-existent group
        s.delete("qqqsdfsdgsdgiuyiyyusdd") ;
    }
    
    //@Test
    public void testSimpleDB() throws Exception {
        KeyValueDatabaseSupport db=_aws.getPlatformServices().getKeyValueDatabaseSupport();
        String domain="zzzxxxyyy000777";
        db.removeDatabase(domain);
        
        String id=db.createDatabase(domain, "test");
        assertEquals(id, domain) ;
        
        KeyValueDatabase kv= db.getDatabase(domain);
        assertNotNull(kv);
        assertEquals("us-east-1", kv.getProviderRegionId()) ;
        assertEquals(kv.getProviderDatabaseId(), kv.getName());
        assertEquals(kv.getItemCount(), 0);
        assertEquals(domain,kv.getName());        
        assertTrue(db.list().iterator().hasNext());
        
        db.addKeyValuePairs(domain, "item1",
                new KeyValuePair("a1", "hello"),
                new KeyValuePair("a2", "world"));
        db.addKeyValuePairs(domain, "item2",
                new KeyValuePair("a1", "xyz"),
                new KeyValuePair("a2", "abc"));
        
        Iterator<KeyValuePair> it= db.getKeyValuePairs(domain, "item1", true).iterator();
        it.next();
        it.next();
        assertFalse(it.hasNext());
        
        db.replaceKeyValuePairs(domain, "item1",
                new KeyValuePair("a1", "crazy"),
                new KeyValuePair("a3", "night"));
        
        Map<String,Set<KeyValuePair>> rows=db.query("select * from "+domain+ " where a1='crazy' ", true);
        assertEquals(rows.size(),1);   
        it=rows.values().iterator().next().iterator();
        it.next(); it.next(); it.next();   // expect 3 attrs
        assertFalse(it.hasNext());
        
        db.removeKeyValuePairs(domain, "item2", new String[0]);

        it= db.getKeyValuePairs(domain, "item2", true).iterator();
        assertFalse(it.hasNext());
        it= db.getKeyValuePairs(domain, "item1", true).iterator();
        assertTrue(it.hasNext());
        
        db.removeDatabase(domain);
        // amazon doesn't throw error when you try to delete a non-existent domain
        db.removeDatabase("shfiirweogvm1sdsdgqqqqqq");
    }

    //@Test
    public void testVolumes() throws Exception {
        VolumeSupport vs=_aws.getComputeServices().getVolumeSupport();
        String vid;
        
        vid=vs.create("", 10, "us-east-1c");
        assertTrue(vid != null && vid.length() > 0);        
        // should at least have 1...
        assertTrue(vs.listVolumes().iterator().hasNext());        
        Volume v= vs.getVolume(vid);
        assertNotNull(v);
        assertEquals(v.getName(), v.getProviderVolumeId());
        assertEquals(v.getProviderVolumeId(), vid);
        assertEquals(v.getSizeInGigabytes(),10);
        
        vs.remove(vid);        
        // we made it so deleting a non-existent volume is ok - no error
        vs.remove("vol-f1292c9a");
    }
    
    //@Test
    public void testSnapshots() throws Exception {
    	SnapshotSupport ss= _aws.getComputeServices().getSnapshotSupport() ;
        VolumeSupport vs=_aws.getComputeServices().getVolumeSupport();
        String v2, vid=vs.create("", 10, "us-east-1c");
        assertTrue(vid != null && vid.length() > 0);
        
        Thread.sleep(5000);
        
        String snap= ss.create(vid, "test");
        assertTrue(snap != null && snap.length() >0);
        Iterator<Snapshot> it= ss.listSnapshots().iterator();
        // at least 1
        assertTrue(it.hasNext());        
        Snapshot s= ss.getSnapshot(snap) ;
        assertNotNull(s);
        assertEquals(s.getVolumeId(), vid);
        assertEquals(s.getProviderSnapshotId(), snap);
        assertEquals(s.getSizeInGb(), 10);
        
        v2=vs.create(snap, 20, "us-east-1c");
        assertTrue(v2 != null && v2.length() > 0);
        
        Thread.sleep(5000);
        
        ss.remove(snap);
        vs.remove(vid);
        vs.remove(v2);

        // we made it so deleting a non-existent snapshot is ok - no error
        ss.remove("snap-89bd16e8");        
    }

    //@Test
    public void testInstances() throws Exception {
    	VirtualMachineSupport vm=_aws.getComputeServices().getVirtualMachineSupport();
    	VirtualMachineProduct t=vm.getProduct("m1.large");
    	//751355128135/ubuntu-lucid-10gb
    	String pn="zzzxxxyyy000777";
    	_aws.getIdentityServices().getShellKeySupport().createKeypair(pn);
    	_aws.getNetworkServices().getFirewallSupport().create(pn, "test");
    	String iid, ami="ami-221fec4b";
    	VirtualMachine m= vm.launch(ami,t, "us-east-1c", "","",pn, "",false,false, pn);
    	assertNotNull(m);
    	iid=m.getProviderVirtualMachineId();
    	while (true) {
        	tlog().debug("wait 3 secs, then check state of instance...");
        	Thread.sleep(3000);
    		m=vm.getVirtualMachine(iid);
        	assertNotNull(m);
        	if ( VmState.RUNNING.equals(m.getCurrentState())) { break; }
    	}

    	assertEquals(m.getArchitecture(), Architecture.I64);
    	//m.getProviderAssignedIpAddressId()
    	assertTrue(m.getPrivateDnsAddress().startsWith("ip-"));
    	assertFalse(isEmpty(m.getPrivateIpAddresses()[0]));
    	assertEquals(ami,m.getProviderMachineImageId());
    	assertEquals(m.getProviderRegionId(),_aws.getContext().getRegionId());
    	assertFalse(isEmpty(iid));
    	assertTrue(m.getPublicDnsAddress().startsWith("ec2-"));
    	assertFalse( isEmpty(m.getPublicIpAddresses()[0]));    
//    	assertFalse(isEmpty(vm.getConsoleOutput(iid)));

    	tlog().debug("stopping the instance...");
    	vm.pause(iid);

    	while (true) {
        	tlog().debug("wait 8 secs, then check state of instance...");
        	Thread.sleep(8000);
    		m=vm.getVirtualMachine(iid);
        	assertNotNull(m);
        	if ( VmState.PAUSED.equals(m.getCurrentState())) { break; }
    	}
    	
    	vm.boot(iid);
    	
    	while (true) {
        	tlog().debug("wait 5 secs, then check state of instance...");
        	Thread.sleep(5000);
    		m=vm.getVirtualMachine(iid);
        	assertNotNull(m);
        	if ( VmState.RUNNING.equals(m.getCurrentState())) { break; }
    	}
    	
    	vm.terminate(iid);
    	tlog().debug("after terminate, wait 10secs");
    	Thread.sleep(10000);
    	    	
    	_aws.getIdentityServices().getShellKeySupport().deleteKeypair(pn) ;
    	_aws.getNetworkServices().getFirewallSupport().delete(pn); 	
    	
    }
    
    //@Test
    public void testAMIages() throws Exception {
    	MachineImageSupport ms= _aws.getComputeServices().getImageSupport();
        // we made it so deleting a non-existent ami is ok - no error
    	ms.remove("ami-1bf91172");
    }
    
    @Test
    public void testDummy() throws Exception {}
    
    
}
