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
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LangUte.ST;
import static com.zotoh.core.util.StrUte.isEmpty;
import static com.zotoh.core.util.StrUte.nsb;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dasein.cloud.DataFormat;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.MachineImageType;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.SnapshotState;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.compute.VolumeState;
import org.dasein.cloud.network.LbProtocol;
import org.dasein.cloud.platform.EndpointType;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.zotoh.cloudapi.core.CloudAPI;
import com.zotoh.core.util.CoreUte;
import com.zotoh.core.util.JSONUte;
import com.zotoh.core.util.SMap;
import com.zotoh.core.util.Tuple;

/**
 * @author kenl
 *
 */
public class AWSAPI  extends CloudAPI implements AWSVars {

    private static JSONObject _regions;
    
    /**
     * 
     */
    public AWSAPI() {}

    /**
     * @param lp
     * @return
     */
    public static String strProtocol(LbProtocol lp) {
    	String s="";
		if (LbProtocol.HTTP.equals(lp) || LbProtocol.HTTPS.equals(lp)) { s= "HTTP"; }
		else if (LbProtocol.RAW_TCP.equals(lp)) { s= "TCP"; }
		return s;
    }
    
    
    /**
     * @param txt
     * @return
     */
    public static LbProtocol toLbProtocol(String txt) {
        if( "HTTP".equals(txt) || "HTTPS".equals(txt)) {
            return LbProtocol.HTTP;
        }
        else {
            return LbProtocol.RAW_TCP;
        }
    }
    
    
    /**
     * @param objs
     * @return
     */
    protected static <T> List<T> toObjList(final T ...objs ) {
        return CoreUte.asList(true, objs);
    }
    
    
    /**
     * @param e
     * @param codes
     * @return
     */
    public static boolean testSafeNonExistError(AmazonServiceException e, String ... codes) {
        String c=e.getErrorCode();
        for (int i=0; i < codes.length; ++i) {
            if (codes[i].equals(c)) { return true; }            
        }
        return false;
    }
    
    
    /**
     * @param e
     * @param codes
     * @return
     */
    public static boolean testForNotSubError(AmazonServiceException e, String ... codes) {
        String c=e.getErrorCode();
        int rc=e.getStatusCode();
        if (rc==401||rc==403|| "SignatureDoesNotMatch".equals(c) ) {
            return true;
        }        
        else if (codes.length > 0) {
            for (int i=0; i < codes.length; ++i) {
                if (codes[i].equals(c)) { return true; }
            }
        }
        
        return false;
    }
    
    /**
     * @param s
     * @return
     */
    public static Platform toPlat(String s) {
        if (PT_WINDOWS.equals(s)) { return Platform.WINDOWS; }
        else { return Platform.UBUNTU; }
    }
    
    
    /**
     * @param s
     * @return
     */
    public static Architecture toArch(String s) {
        if (AWS_32BIT.equals(s)) return Architecture.I32;
        else return Architecture.I64;
    }
    
    
    /**
     * @param s
     * @return
     */
    public static VmState toVmState(String s) {
        VmState rc=VmState.PENDING;
        if( "pending".equals(s) ) {}
        else if( "running".equals(s) ) {            rc= VmState.RUNNING;        }
        else if( "terminating".equals(s) || "stopping".equals(s) ) {
            rc= VmState.STOPPING;
        }
        else if( "stopped".equals(s) ) {            rc= VmState.PAUSED;        }
        else if( "shutting-down".equals(s) ) {            rc= VmState.STOPPING;        }
        else if( "terminated".equals(s) ) {            rc= VmState.TERMINATED;        }
        else if( "rebooting".equals(s) ) {            rc= VmState.REBOOTING;        }
        
        return rc;        
    }

    
    /**
     * @param str
     * @return
     */
    public static SnapshotState  toSnapState(String str) {
        SnapshotState st;
        if ("available".equals(str)) { st=SnapshotState.AVAILABLE; }
        if ("deleting".equals(str)||"deleted".equals(str)) { st=SnapshotState.DELETED; }
        else { st= SnapshotState.PENDING;}
        return st;
    }
    
    
    /**
     * @param str
     * @return
     */
    public static VolumeState  toVolState(String str) {
        VolumeState st;
        if ("available".equals(str)) { st=VolumeState.AVAILABLE; }
        if ("deleting".equals(str)||"deleted".equals(str)) { st=VolumeState.DELETED; }
        else { st= VolumeState.PENDING;}
        return st;
    }
    
    
    /**
     * @param s
     * @return
     */
    public static MachineImageType toImageType(String s) {
        if ( "ebs".equals( s)) { return MachineImageType.VOLUME; }
        else { return MachineImageType.STORAGE; }        
    }
    
    
    /**
     * @param s
     * @return
     */
    public static MachineImageState toImageState(String s) {
        MachineImageState ms;
        if ("available".equals(s)) { ms= MachineImageState.ACTIVE;}
        else 
        if ("deleting".equals(s) || "deleted".equals(s)) { ms= MachineImageState.DELETED;}
        else { ms= MachineImageState.PENDING; }
        return ms;
    }
    
    
    /**
     * @param fmt
     * @param t
     * @return
     */
    public static String toProtocol(DataFormat fmt, EndpointType t) {
        String p="";
        switch (t) {
            case AWS_SQS: 
                p= "sqs"; 
            break;
            case HTTPS: 
                p= "https"; 
            break;
            case HTTP: 
                p= "http"; 
            break;
            case EMAIL:
                if (DataFormat.JSON.equals(fmt)) { p = "email-json"; }
                else { p= "email";}
            break;
        }
        return p;
    }
    
    
    /**
     * @param protocol
     * @return
     */
    public static Tuple toDataFmt(String protocol) {
        Tuple t=null;
        if ("email-json".equals(protocol)) { t= new Tuple(DataFormat.JSON, EndpointType.EMAIL);}
        if ("email".equals(protocol))  { t= new Tuple(DataFormat.PLAINTEXT, EndpointType.EMAIL);}
        if ("https".equals(protocol))  { t= new Tuple(DataFormat.JSON, EndpointType.HTTPS);}
        if ("http".equals(protocol))  { t= new Tuple(DataFormat.JSON, EndpointType.HTTP);}
        if ("sqs".equals(protocol))  { t= new Tuple(DataFormat.JSON, EndpointType.AWS_SQS);}
        return t;
    }
        
    /* (non-Javadoc)
     * @see com.zotoh.cloudapi.core.CloudAPI#listProducts(int)
     */
    public Collection<VirtualMachineProduct> listProducts(int bits) {
        if (64==bits) return sort(x86_64.values());
        else
        if (32 == bits) return sort(i386.values());
        else
        return sort(i386_x64.values());   // everything
    }

    /* (non-Javadoc)
     * @see com.zotoh.cloudapi.core.CloudAPI#listProductIds(int)
     */
    public List<String> listProductIds(int bits) {
    	Map<String,VirtualMachineProduct> c;
        if (64==bits) c= x86_64;
        else
        if (32 == bits) c= i386;
        else
        c= i386_x64;

        Collection<VirtualMachineProduct> cs= sort(c.values());
        List<String> lst= LT();
        for (VirtualMachineProduct p:cs) {
            lst.add(p.getProductId());
        }
        return Collections.unmodifiableList(lst);
    }
    
    /**
     * @param pid
     * @return
     */
    public  VirtualMachineProduct findProduct(String pid) {
        return pid==null ? null : i386_x64.get(pid); 
    }
    
    /**
     * @param rgs
     */
    protected static void inizAvailableRegions(Set<String> rgs) {
        tstObjArg("regions", rgs);
        try {
            _regions= new JSONObject();
            for (String s : rgs) {
                _regions.put(s, new JSONObject());
            }
        }
        catch (Exception e) 
        {}
    }
    
    @SuppressWarnings("serial")
    private static final SMap<VirtualMachineProduct> i386= new SMap<VirtualMachineProduct>() {{ 
           put("m1.small", new VirtualMachineProduct(){{ 
               setCpuCount(1);
               setDescription("i386/1CU/1.7GB/160GB");
               setDiskSizeInGb(160);
               setName("Small Instance");
               setProductId("m1.small");
               setRamInMb(1700);                           
           }}); 
           
           put("c1.medium", new VirtualMachineProduct(){{ 
               setCpuCount(5);
               setDescription("i386/5CU/1.7GB/350GB");
               setDiskSizeInGb(350);
               setName("High-CPU Medium Instance");
               setProductId("c1.medium");
               setRamInMb(1700);                           
           }});
           
           put("t1.micro",new VirtualMachineProduct(){{ 
               setCpuCount(2);
               setDescription("i386/2CU/613MB/EBS");
               setDiskSizeInGb(0);
               setName("Micro Instance");
               setProductId("t1.micro");
               setRamInMb(613);                           
           }}); 
           
    }};

    @SuppressWarnings("serial")
    private static final SMap<VirtualMachineProduct> x86_64= new SMap<VirtualMachineProduct>() {{ 
            put("t1.micro",new VirtualMachineProduct(){{ 
                setCpuCount(2);
                setDescription("x86_64/2CU/613MB/EBS");
                setDiskSizeInGb(0);
                setName("Micro Instance");
                setProductId("t1.micro");
                setRamInMb(613);                           
            }}); 
            put("m1.large", new VirtualMachineProduct(){{ 
                setCpuCount(4);
                setDescription("x86_64/4CU/7.5GB/850GB");
                setDiskSizeInGb(850);
                setName("Large Instance");
                setProductId("m1.large");
                setRamInMb(7500);                           
            }}); 
            put("m1.xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(8);
                setDescription("x86_64/8CU/15GB/1690GB");
                setDiskSizeInGb(1690);
                setName("Extra Large Instance");
                setProductId("m1.xlarge");
                setRamInMb(15000);                           
            }}); 
            put("m2.xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(7);
                setDescription("x86_64/6.5CU/17.1GB/420GB");
                setDiskSizeInGb(420);
                setName("High-Memory Extra Large Instance");
                setProductId("m2.xlarge");
                setRamInMb(17100);                           
            }}); 
            put("m2.2xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(13);
                setDescription("x86_64/13CU/34.2GB/850GB");
                setDiskSizeInGb(850);
                setName("High-Memory Double Extra Large Instance");
                setProductId("m2.2xlarge");
                setRamInMb(34200);                           
            }}); 
            put("m2.4xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(26);
                setDescription("x86_64/26CU/68.4GB/1690GB");
                setDiskSizeInGb(1690);
                setName("High-Memory Quadruple Extra Large Instance");
                setProductId("m2.4xlarge");
                setRamInMb(68400);                           
            }}); 
            put("c1.xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(20);
                setDescription("x86_64/20CU/7GB/1690GB");
                setDiskSizeInGb(1690);
                setName("High-CPU Extra Large Instance");
                setProductId( "c1.xlarge");
                setRamInMb(7000);                           
            }}); 
            put("cc1.4xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(34);
                setDescription("x86_64/33.5CU/23GB/1690GB (quad-core \"Nehalem\" architecture)");
                setDiskSizeInGb(1690);
                setName("Cluster Compute Quadruple Extra Large Instance");
                setProductId("cc1.4xlarge");
                setRamInMb(23000);                           
            }}); 
            put("cg1.4xlarge",new VirtualMachineProduct(){{ 
                setCpuCount(34);
                setDescription("x86_64/33.5CU/22GB/1690GB (quad-core \"Nehalem\" architecture)");
                setDiskSizeInGb(1690);
                setName("Cluster GPU Quadruple Extra Large Instance");
                setProductId("cg1.4xlarge");
                setRamInMb(23000);                           
            }}); 
        
    }};
        
    @SuppressWarnings("serial")
    private static final SMap<VirtualMachineProduct> i386_x64= new SMap<VirtualMachineProduct>() {{
        putAll(x86_64);
        putAll(i386);
    }};

    /* (non-Javadoc)
     * @see com.zotoh.cloudapi.core.CloudAPI#listRegions()
     */
    @Override
    public Set<String> listRegions() {
        Set<String> rc= ST();
        if (_regions != null) try {
            for (Iterator<?> it= _regions.keys(); it.hasNext(); ) {
                rc.add( nsb( it.next()));
            }
        }
        catch (Exception e)
        {}
        
        return Collections.unmodifiableSet(rc);
    }

	/* (non-Javadoc)
	 * @see com.zotoh.cloudapi.core.CloudAPI#setProperties(org.json.JSONObject, java.util.Properties)
	 */
	@Override
	public void setProperties(String hint, JSONObject target, Properties props) throws JSONException {
		
		if ("endpoint".equals(hint)) {
			for (Map.Entry<Object,Object> en : props.entrySet()) {
				target.put( "aws.endpoint." + nsb(en.getKey()) ,
				nsb(en.getValue()) );
			}
		}
		
		
	}

    /* (non-Javadoc)
     * @see com.zotoh.cloudapi.core.CloudAPI#setRegionsAndZones(org.json.JSONObject)
     */
    public void setRegionsAndZones(JSONObject regions) throws JSONException {
        tstObjArg("regions object", regions);
        _regions=JSONUte.read( JSONUte.asString(regions) );
    }
	
    /* (non-Javadoc)
     * @see com.zotoh.cloudapi.core.CloudAPI#listDatacenters(java.lang.String)
     */
    @Override
    public Set<String> listDatacenters(String region) {
        Set<String> rc= ST();
        if (!isEmpty(region) && _regions != null) 
        try {
            JSONObject j= _regions.optJSONObject(region) ;
            if (j != null) {
                for (Iterator<?> it2 = j.keys(); it2.hasNext(); ) {
                    rc.add( nsb(it2.next()));
                }                
            }
        }
        catch (Exception e)
        {}        
        return Collections.unmodifiableSet(rc);
    }
        
    
}
