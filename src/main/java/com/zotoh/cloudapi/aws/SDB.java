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

import static com.zotoh.cloudapi.aws.AWSAPI.testForNotSubError;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstNEArray;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LangUte.MP;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.isEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.platform.KeyValueDatabase;
import org.dasein.cloud.platform.KeyValueDatabaseSupport;
import org.dasein.cloud.platform.KeyValuePair;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.DomainMetadataRequest;
import com.amazonaws.services.simpledb.model.DomainMetadataResult;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class SDB implements KeyValueDatabaseSupport {

    private Logger ilog() {  return _log=getLogger(SDB.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
	
	private final AWSPlatformSvcs _svc;
	
	/**
	 * @param s
	 */
	protected SDB(AWSPlatformSvcs s) {
	    tstObjArg("platform-service",s);
		_svc=s;
	}
	
	@Override
	public void addKeyValuePairs(String domain, String item, KeyValuePair... vals)
					throws CloudException, InternalException {
		modifyKVPairs(false, domain, item, vals);
	}

	@Override
	public String createDatabase(String domain, String desc)
					throws CloudException, InternalException {
		tstEStrArg("domain-name", domain);
		_svc.getCloud().getSDB().createDomain(
						new CreateDomainRequest()
						.withDomainName(domain)) ;
		return domain;
	}

	@Override
	public KeyValueDatabase getDatabase(String domain) throws CloudException,
					InternalException {
		tstEStrArg("domain-name", domain);
		DomainMetadataResult res=_svc.getCloud().getSDB().domainMetadata(
				new DomainMetadataRequest().withDomainName(domain));
		return toKVD(domain,res);
	}

	@Override
	public Iterable<KeyValuePair> getKeyValuePairs(String domain, String item,
					boolean consistentRead) throws CloudException, InternalException {
		tstEStrArg("domain-name", domain);
		tstEStrArg("item-name", item);
		GetAttributesResult res=_svc.getCloud().getSDB().getAttributes(
				new GetAttributesRequest()
				.withDomainName(domain)
				.withItemName(item)
				.withConsistentRead(consistentRead));
		List<Attribute> lst=res==null ? null : res.getAttributes() ;
		return toKPs(lst) ;
	}

	@Override
	public String getProviderTermForDatabase(Locale loc) {
		return "simpledb";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		try {
			_svc.getCloud().getSDB().listDomains(
						new ListDomainsRequest().withMaxNumberOfDomains(10)) ;
			return true;
		}
		catch (AmazonServiceException e) {
			if (testForNotSubError(e,  "SubscriptionCheckFailed", "AuthFailure" 
            				,"SignatureDoesNotMatch" 
            				,"InvalidClientTokenId", "OptInRequired"))  {
                return false;
            }
			else { throw new CloudException(e); }
		}
		catch (AmazonClientException e) {
			throw new InternalException(e);
		}
	}

	@Override
	public boolean isSupportsKeyValueDatabases() throws CloudException,
					InternalException {
		return true;
	}

	@Override
	public Iterable<String> list() throws CloudException, InternalException {
		List<String> rc= LT();
		ListDomainsRequest req;
		ListDomainsResult res;
		List<String> lst;
		String token=null;
		do {
			req=new ListDomainsRequest().withMaxNumberOfDomains(100);
			if (!isEmpty(token)) { req.setNextToken(token); }
			res= _svc.getCloud().getSDB().listDomains(req);
			lst= res==null ? null : res.getDomainNames();
			if (lst != null) {
				rc.addAll(lst) ;
			}
			token=res.getNextToken();			
		}
		while ( !isEmpty(token));
		return rc;
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Set<KeyValuePair>> query(String query, boolean consistentRead)
					throws CloudException, InternalException {
	    tstEStrArg("query-string", query);
		Map<String,Set<KeyValuePair>> rc= MP();
		List<Item> lst;
		SelectRequest req;
		SelectResult res;
		String token=null;
		do {
			req=new SelectRequest()
			.withSelectExpression(query)
			.withConsistentRead(consistentRead);
			if (!isEmpty(token)) { req.setNextToken(token); }
			res=_svc.getCloud().getSDB().select(req) ;
			lst= res==null ? null : res.getItems();
			if (lst != null) for (int i=0; i < lst.size(); ++i) {
				final Item itm=lst.get(i);
				rc.put(itm.getName(), 
					 new HashSet<KeyValuePair>(){{ 
						addAll(toKPs(itm.getAttributes()));
					}}
				);
			}
			token= res.getNextToken();
		}
		while (!isEmpty(token));
		return Collections.unmodifiableMap(rc) ;
	}

	@Override
	public void removeDatabase(String domain) throws CloudException,
					InternalException {
		tstEStrArg("domain-name", domain);
		_svc.getCloud().getSDB().deleteDomain(
		        new DeleteDomainRequest().withDomainName(domain)) ;
	}

	@Override
	public void removeKeyValuePairs(String domain, String item,
					KeyValuePair... vals) throws CloudException,
					InternalException {
		tstEStrArg("domain-name", domain);
		tstEStrArg("item-name", item);
		_svc.getCloud().getSDB().deleteAttributes(
			new DeleteAttributesRequest()
			.withAttributes(toAtts(vals))
			.withItemName(item)
			.withDomainName(domain)) ;
	}

	@Override
	public void removeKeyValuePairs(String domain, String item, String... pairs)
					throws CloudException, InternalException {
		tstEStrArg("domain-name", domain);
		tstEStrArg("item-name", item);
		_svc.getCloud().getSDB().deleteAttributes(
			new DeleteAttributesRequest()			
			.withAttributes(toAtts(pairs))
			.withItemName(item)
			.withDomainName(domain)) ;
	}

	@Override
	public void replaceKeyValuePairs(String domain, String item,
					KeyValuePair... vals) throws CloudException,
					InternalException {
		modifyKVPairs(true, domain, item, vals);
	}

	/**/
	private void modifyKVPairs(boolean replace, String domain, String item,
					KeyValuePair... vals) throws CloudException,
					InternalException {
		tstEStrArg("domain-name", domain);
		tstEStrArg("item-name", item);
		tstNEArray("keyvalue-pairs", vals);
		_svc.getCloud().getSDB().putAttributes(
				new PutAttributesRequest()
				.withAttributes( toRAtts(replace, vals))
				.withItemName(item)
				.withDomainName(domain));		
	}
	
	/**/
	private List<ReplaceableAttribute> toRAtts(boolean replace, KeyValuePair[] vals) {
		List<ReplaceableAttribute> lst= LT();
		KeyValuePair kp;
		if (vals != null) for(int i=0; i < vals.length; ++i) {
			kp=vals[i];
			lst.add(new ReplaceableAttribute()
			.withValue(kp.getValue())
			.withName(kp.getKey())
			.withReplace(replace));
		}
		return lst;
	}

	/**/
	private List<Attribute> toAtts(KeyValuePair[] vals) {
		List<Attribute> lst= new ArrayList<Attribute>();
		KeyValuePair kp;
		if (vals != null) for(int i=0; i < vals.length; ++i) {
			kp=vals[i];
			lst.add(new Attribute()
			.withValue(kp.getValue())
			.withName(kp.getKey()));
		}
		return lst;
	}
	
	/**/
	private List<Attribute> toAtts(String[] keys) {
		List<Attribute> lst= LT();
		if (keys != null) for(int i=0; i < keys.length; ++i) {
			lst.add(new Attribute()
			.withName(keys[i]));
		}
		return lst;
	}
	
	/**/	
	private List<KeyValuePair> toKPs(List<Attribute> atts) {
		List<KeyValuePair> rc= LT();
		Attribute a;
		KeyValuePair p;
		if (atts != null) for (int i=0; i < atts.size(); ++i) {
			a=atts.get(i);
			p=new KeyValuePair();
			p.setValue(a.getValue()) ;
			p.setKey(a.getName());
			rc.add(p);
		}
		return rc;
	}
	
    /**/
    private KeyValueDatabase toKVD(String domain,DomainMetadataResult res) {
        KeyValueDatabase db=null;
        if (res != null) {
            db=new KeyValueDatabase();
            db.setDescription(domain);
            db.setItemCount(res.getItemCount());
            db.setItemSize( new Long(res.getItemNamesSizeBytes()).intValue());
            db.setKeyCount(res.getAttributeNameCount()) ;
            db.setKeySize( new Long(res.getAttributeNamesSizeBytes()).intValue());
            db.setKeyValueCount(res.getAttributeValueCount()) ;
            db.setKeyValueSize(new Long(res.getAttributeValuesSizeBytes()).intValue()) ;
            db.setName(domain);
            db.setProviderDatabaseId(db.getName()) ;
            db.setProviderOwnerId(_svc.getCloud().getContext().getAccountNumber()) ;
            db.setProviderRegionId(_svc.getCloud().getContext().getRegionId());         
        }
        return db;      
    }
}
