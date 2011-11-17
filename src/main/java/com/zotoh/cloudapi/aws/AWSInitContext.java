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
import static com.zotoh.core.util.StrUte.isEmpty;
import static com.zotoh.core.util.StrUte.nsb;

import java.util.Properties;

import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.ProviderContext;

import com.zotoh.cloudapi.core.Vars;
import com.zotoh.core.crypto.PwdFactory;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public enum AWSInitContext implements AWSVars , Vars {
	
	INSTANCE ;
	
    private Logger ilog() {  return _log=getLogger(AWSInitContext.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
	
	/**
	 * @return
	 */
	public static AWSInitContext getInstance() { return INSTANCE; }
	
	
	/**
	 * @param props
	 * @return
	 */
	@SuppressWarnings("serial")
	public CloudProvider configure(final Properties props) {
		tstObjArg("config-props", props);
//		String acctno= nsb( props.getProperty(P_ACCT)), 
//			accessKey= nsb(props.getProperty(P_ID)), 
//			secretKey= nsb(props.getProperty(P_PWD));
//		tstEStrArg("cred-secret-key", secretKey); 
//		tstEStrArg("cred-access-key", accessKey);
//		tstEStrArg("cred-acct-n#", acctno);
		
		return configure0(new Properties() {{  putAll(props); }});
	}
	
	
	/**
	 * @param props
	 * @param acctno
	 * @param accessKey
	 * @param secretKey
	 * @return
	 */
	@SuppressWarnings("serial")
	public CloudProvider configure(final Properties props, final String acctno, final String accessKey, final String secretKey) {
//		tstEStrArg("cred-secret-key", secretKey);
//		tstEStrArg("cred-access-key", accessKey);
//		tstEStrArg("cred-acct-n#", acctno);
		tstObjArg("config-props", props);
		
		return configure0(new Properties() {{  
			putAll(props);
	        put(P_ID, accessKey);
	        put(P_PWD, secretKey);
	        put(P_ACCT, acctno);
		}});
	}
	
	private CloudProvider configure0(final Properties props) {
        tlog().debug("AWSInitContext: configuring for AWS()");        
	    
        String pwd= props.getProperty(P_PWD);
        if (!isEmpty(pwd)) 
        try {
        	pwd=PwdFactory.getInstance().create(pwd).getAsClearText();
        	props.put(P_PWD, pwd);
        }
        catch (Exception e) {}
        
        String rg= nsb( props.remove(P_REGION) );
		ProviderContext x= new ProviderContext(props.getProperty(P_ACCT).replaceAll("-", ""), "us-east-1");
        x.setProviderName("Amazon");
        x.setCloudName("AWS");
		x.setCustomProperties( props);
		
		AWSCloud c = new AWSCloud();
		c.initialize(x);
		if (!isEmpty(rg)) { c.setAWSSite(rg) ; }
		
		return c;				
	}
}
