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
 
package com.zotoh.cloudapi.core;

import static com.zotoh.core.util.StrUte.nsb;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.dasein.cloud.compute.VirtualMachineProduct;
import org.json.JSONException;
import org.json.JSONObject;

import com.zotoh.cloudapi.aws.AWSAPI;

/**
 * @author kenl
 *
 */
public abstract class CloudAPI implements Vars {


    /**
     * @param vendor
     * @return
     */
    public static CloudAPI create(String vendor) {
        vendor= nsb(vendor).toLowerCase();
        if ("aws".equals(vendor) || "amazon".equals(vendor)) {
            return new AWSAPI();
        }
        
        return null;
    }

    /**
     * @param hint
     * @param target
     * @param props
     */
    public abstract void setProperties(String hint, JSONObject target, Properties props) throws JSONException ;
    
    
    /**
     * @param regions
     * @throws JSONException
     */
    public abstract void setRegionsAndZones(JSONObject regions) throws JSONException;
    
    /**
     * @return
     */
    public abstract Set<String> listDatacenters(String region);
    
    /**
     * @return
     */
    public abstract Set<String> listRegions();
    
    /**
     * @param bits 32 or 64
     * @return
     */
    public abstract Collection<VirtualMachineProduct> listProducts(int bits) ;

    /**
     * @param bits 32 or 64
     * @return
     */
    public abstract List<String> listProductIds(int bits);
    
    /**
     * @param pid
     * @return
     */
    public abstract VirtualMachineProduct findProduct(String pid);

    
    
    
    
    
    /**
     * 
     */
    protected CloudAPI() {}
  
    
    protected static Collection<VirtualMachineProduct> sort(Collection<VirtualMachineProduct> cs) {
        Map<VirtualMachineProduct, VirtualMachineProduct> m= 
                new TreeMap<VirtualMachineProduct, VirtualMachineProduct>(
                new AAA());
        for (VirtualMachineProduct p : cs) {            
            m.put(p, p);
        }
        return m.values();
    }
    
    protected static class AAA implements Comparator<VirtualMachineProduct> {
        public int compare(VirtualMachineProduct o1, VirtualMachineProduct o2) {
            int c1= o1.getCpuCount(), c2= o2.getCpuCount();
//            return  c1 == c2 ? 0 : ( (c1 > c2) ? 1 : -1 );
            return c1 > c2 ? 1 : -1;
        }        
    }
    
    
}
