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

import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LoggerFactory.getLogger;

import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.network.DNSRecord;
import org.dasein.cloud.network.DNSRecordType;
import org.dasein.cloud.network.DNSSupport;
import org.dasein.cloud.network.DNSZone;

import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class DNSRoute53 implements DNSSupport {

    private Logger ilog() {  return _log=getLogger(DNSRoute53.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
	
    @SuppressWarnings("unused")
    private final AWSNetworkSvcs _svc;
    
    /**
     * @param s
     */
    protected DNSRoute53(AWSNetworkSvcs s) {
        tstObjArg("network-service",s);
    	_svc=s;
    }
    
	@Override
	public DNSRecord addDnsRecord(String arg0, DNSRecordType arg1, String arg2,
					int arg3, String... arg4) throws CloudException,
					InternalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createDnsZone(String domain, String name, String desc)
					throws CloudException, InternalException {
		// TODO Auto-generated method stub
		tstEStrArg("domain", domain);
		tstEStrArg("name", name);
		tstEStrArg("description", desc);
		return null;
	}

	@Override
	public void deleteDnsRecords(DNSRecord... arg0) throws CloudException,
					InternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteDnsZone(String arg0) throws CloudException,
					InternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DNSZone getDnsZone(String arg0) throws CloudException,
					InternalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProviderTermForRecord(Locale arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProviderTermForZone(Locale arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<DNSRecord> listDnsRecords(String arg0, DNSRecordType arg1,
					String arg2) throws CloudException, InternalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<DNSZone> listDnsZones() throws CloudException,
					InternalException {
		// TODO Auto-generated method stub
		return null;
	}

}
