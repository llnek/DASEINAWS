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
import static com.zotoh.cloudapi.aws.AWSAPI.testSafeNonExistError;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.identity.ShellKeySupport;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class Keypair implements ShellKeySupport {

    private Logger ilog() {  return _log=getLogger(Keypair.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    private final AWSIdentitySvcs _svc;
    
    /**
     * @param s
     */
    protected Keypair(AWSIdentitySvcs s) {
    	tstObjArg("identity-service",s);
        _svc=s;
    }
    
    @Override
    public String createKeypair(String name) throws InternalException,
            CloudException {
        tstEStrArg("keypair-name", name);
        CreateKeyPairResult res= _svc.getCloud().getEC2()
            .createKeyPair( new CreateKeyPairRequest().withKeyName(name));
        KeyPair p= res==null ? null : res.getKeyPair();
        return p==null ? null : p.getKeyMaterial() ;
    }

    @Override
    public void deleteKeypair(String name) throws InternalException,
            CloudException {
        tstEStrArg("keypair-name", name);
        _svc.getCloud().getEC2()
                .deleteKeyPair(
                        new DeleteKeyPairRequest().withKeyName(name));
    }

    @Override
    public String getFingerprint(String name) throws InternalException,
            CloudException {
        tstEStrArg("keypair-name", name);
        String fp=null;
        try {
            DescribeKeyPairsResult res= _svc.getCloud().getEC2()
                    .describeKeyPairs(
                            new DescribeKeyPairsRequest().withKeyNames(name));            
            List<KeyPairInfo> lst= res==null ? null : res.getKeyPairs();
            KeyPairInfo p= isNil(lst) ? null : lst.get(0);
            fp= p==null ? null : p.getKeyFingerprint() ;
        }
        catch (AmazonServiceException e) {
        	if (!testSafeNonExistError(e, "InvalidKeyPair.NotFound")) {
                throw new CloudException(e);        		
        	} 
        }
        return fp;
    }    
    
    @Override
    public String getProviderTermForKeypair(Locale loc) {
        return "keypair";
    }

    @Override
    public Collection<String> list() throws InternalException, CloudException {
        DescribeKeyPairsResult res= _svc.getCloud().getEC2()
                .describeKeyPairs(new DescribeKeyPairsRequest());
        List<KeyPairInfo> lst= res==null ? null : res.getKeyPairs();
        List<String> rc= LT();
        if (lst != null) for (int i=0; i < lst.size(); ++i ) {
            rc.add( lst.get(i).getKeyName());
        }
        return rc;
    }

}
