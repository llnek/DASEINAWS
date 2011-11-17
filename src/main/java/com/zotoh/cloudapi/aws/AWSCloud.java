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

import static com.zotoh.core.util.CoreUte.rc2Stream;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.MP;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.nsb;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dasein.cloud.AbstractCloud;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.admin.AdminServices;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.dc.DataCenterServices;
import org.dasein.cloud.identity.IdentityServices;
import org.dasein.cloud.network.NetworkServices;
import org.dasein.cloud.platform.PlatformServices;
import org.dasein.cloud.storage.StorageServices;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.zotoh.cloudapi.core.Vars;
import com.zotoh.core.io.StreamUte;
import com.zotoh.core.util.Logger;

/**
 * @author kenl
 *
 */
class AWSCloud extends AbstractCloud implements AWSVars , Vars {

    private Logger ilog() {  return _log=getLogger(AWSCloud.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    

    private final Map<String,AmazonWebServiceClient> _WEB= MP();
    private final Map<String,String> _ENDPTS= MP();
    
    /**
     * @param x
     */
    protected void initialize(ProviderContext x) {
        tstObjArg("provider-context", x);
    	connect(x);
        iniz();
    }
    
    
    /**
     * @param region
     * @return
     */
    public AmazonEC2Client newEC2(String region) {
        tstEStrArg("region-name", region);
        Properties ps= getContext().getCustomProperties();
        AWSCredentials cc= new BasicAWSCredentials(
                ps.getProperty(P_ID), ps.getProperty(P_PWD));
        AmazonEC2Client c= new AmazonEC2Client(cc);
        String s=getAWSSite(region);
        c.setEndpoint(s) ;
        return c;
    }
    
    /**/
    public void setAWSSite(String region) {
        String ptr=region==null ? null : _ENDPTS.get(region) ;
        String s,pt=ptr;
        if (pt != null) {
            for (Map.Entry<String,AmazonWebServiceClient> en : _WEB.entrySet()) {
                s= en.getKey();
                pt=ptr;
                if ("elb".equals(s)) { continue; }
                if ("sdb".equals(s)) { pt= jiggleSDBSite(region); }
                else if ("sns".equals(s)) { pt= jiggleSNSSite(region); }
                en.getValue().setEndpoint(pt) ;                
            }
            getContext().setRegionId(region) ;
            getContext().setEndpoint(ptr) ;
        }
    }
    
    /**/
    public String getAWSSite(String region) {
        return region==null ? null : _ENDPTS.get(region);
    }
    
    /**/
    public Set<String> getCachedRegions() {
        return Collections.unmodifiableSet(_ENDPTS.keySet());
    }
    
    @Override
    public AdminServices getAdminServices() {
        return new AWSAdminSvcs(this);
    }

    @Override
    public ComputeServices getComputeServices() {
        return new AWSComputeSvcs(this);
    }

    @Override
    public DataCenterServices getDataCenterServices() {
        return new AWSDataCenterSvcs(this);
    }

    @Override
    public IdentityServices getIdentityServices() {
        return new AWSIdentitySvcs(this);
    }

    @Override
    public NetworkServices getNetworkServices() {
        return new AWSNetworkSvcs(this);
    }

    @Override
    public PlatformServices getPlatformServices() {
        return new AWSPlatformSvcs(this);
    }

    @Override
    public synchronized StorageServices getStorageServices() {
        return new AWSCloudStorageSvcs(this);
    }

    @Override
    public boolean hasComputeServices() {
        return true;
    }

    @Override
    public boolean hasIdentityServices() {
        return true;
    }

    @Override
    public boolean hasNetworkServices() {
        return true;
    }

    @Override
    public boolean hasPlatformServices() {
        return true;
    }

    @Override
    public boolean hasStorageServices() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public String getCloudName() {
        return getContext().getCloudName();
    }

    @Override
    public String getProviderName() {
        return getContext().getProviderName();
    }

    /**/
    public AmazonSimpleDBClient getSDB() {
        return (AmazonSimpleDBClient) _WEB.get("sdb");
    }
    
    /**/
    public AmazonS3EncryptionClient getS3S() {
        return (AmazonS3EncryptionClient) _WEB.get("s3s");
    }
    
    /**/
    public AmazonS3Client getS3() {
        return (AmazonS3Client) _WEB.get("s3");
    }
    
    /**/
    public AmazonElasticLoadBalancingClient getELB() {
        return (AmazonElasticLoadBalancingClient) _WEB.get("elb");
    }
    
    /**/
    public AmazonCloudWatchClient getCW() {
        return (AmazonCloudWatchClient) _WEB.get("cloudwatch");
    }
    
    /**/
    public AmazonAutoScalingClient getAutoScale() {
        return (AmazonAutoScalingClient) _WEB.get("autoscale");
    }
    
    /**/
    public AmazonEC2Client getEC2() {
        return (AmazonEC2Client) _WEB.get("ec2");
    }
    
    /**/
    public AmazonSNSClient getSNS() {
        return (AmazonSNSClient) _WEB.get("sns");
    }
    
    /**/
    public AmazonSQSClient getSQS() {
        return (AmazonSQSClient) _WEB.get("sqs");
    }
    
    /**/
    public AmazonRDSClient getRDS() {
        return (AmazonRDSClient) _WEB.get("rds");
    }
    
    /**/
    private void iniz() {
    	Properties ps= getContext().getCustomProperties();
    	filterEndpoints(ps);
        createAWSClients(ps);
        setAWSSite("us-east-1");
        AWSAPI.inizAvailableRegions(_ENDPTS.keySet());
    }
  
    /**/
    private void createAWSClients(Properties ps) {
        AWSCredentials cc= new BasicAWSCredentials(   
                ps.getProperty(P_ID), ps.getProperty(P_PWD));
        AmazonWebServiceClient c;
        
        _WEB.put("ec2", new AmazonEC2Client(cc));
        
        _WEB.put("s3", new AmazonS3Client(cc));
        
        // SIMPLE-DB
        c=new AmazonSimpleDBClient(cc);
        _WEB.put("sdb", c);
        c.setEndpoint("sdb.amazonaws.com");
        
        // LOAD BALANCER
        c=new AmazonElasticLoadBalancingClient(cc);
        _WEB.put("elb", c) ;
        c.setEndpoint("elasticloadbalancing.amazonaws.com");
        
        _WEB.put("cloudwatch", new AmazonCloudWatchClient(cc));
        _WEB.put("autoscale", new AmazonAutoScalingClient(cc));
        
        // NOTIFICATION
        c=new AmazonSNSClient(cc);
        _WEB.put("sns", c);
        c.setEndpoint("sns.us-east-1.amazonaws.com");
        
        _WEB.put("sqs", new AmazonSQSClient(cc));
        _WEB.put("rds", new AmazonRDSClient(cc));
        _WEB.put("s3s", new AmazonS3EncryptionClient(cc, 
                new EncryptionMaterials((KeyPair)null)));
        
    }
    
    /**/
    private void filterEndpoints(Properties props) {
        Properties ps= new Properties();
        InputStream inp=null;
        if (props==null) {
	        try {
	            inp= rc2Stream("com/zotoh/cloudapi/aws/aws.properties") ;
	            ps.load(inp) ;
	        }
	        catch (IOException e) {
	            tlog().error("", e);
	        }
	        finally {
	            StreamUte.close(inp);
	        }
        } else {
        	ps= props;
        }
        
        String region,key, ptr;
        for( Object obj : ps.keySet()) {
            key=obj.toString();
            if ( key.startsWith("aws.endpoint.")) {
                ptr=ps.getProperty(key) ;
                region=key.substring(13);
                tstEStrArg("aws-region", region);
                tstEStrArg("aws-endpoint", ptr);
                tlog().debug("Found region/endpoint: {} -> {}", region, ptr);     
                _ENDPTS.put(region, ptr);
            }
        }
    }
    
    /**/
    private String jiggleSDBSite(String region) {
        String pfx= "eu-west-1".equals(region) ? "sdb.eu-west-1" : "sdb";
        return pfx + ".amazonaws.com" ;
    }
    
    /**/
    private String jiggleSNSSite(String region) {
        return "sns." + nsb(region) + ".amazonaws.com" ;
    }
    
    /**/
    protected AWSCloud()
    {}
    
}