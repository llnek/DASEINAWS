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
import static com.zotoh.cloudapi.aws.AWSAPI.toObjList;
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
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallRule;
import org.dasein.cloud.network.FirewallSupport;
import org.dasein.cloud.network.Protocol;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.zotoh.core.util.Logger;
import com.zotoh.core.util.Tuple;


/**
 * @author kenl
 *
 */
public class SecurityGroup implements FirewallSupport {

    private Logger ilog() {  return _log=getLogger(SecurityGroup.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    private final AWSNetworkSvcs _svc;
    
    /**
     * @param s
     */
    protected SecurityGroup(AWSNetworkSvcs s) {
    	tstObjArg("network-service", s);
        _svc=s;
    }
    
    @Override
    public void authorize(final String group, final String cidr, final Protocol p, final int fromPort,
            final int toPort) throws CloudException, InternalException {
        tstEStrArg("group-name", group);
        tstEStrArg("cidr", cidr);
        tstObjArg("protocol", p);

        List<IpPermission> lst= 
        toObjList(toPerm(group,cidr,p,fromPort,toPort) );
        
        _svc.getCloud().getEC2()
            .authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest(group, lst));
    }

/**
 * returns the Amazon group-id, which is different to group-name.    
 */
    @Override
    public String create(String group, String desc) throws InternalException,
            CloudException {
        tstEStrArg("group-description", desc);
        tstEStrArg("group-name", group);

        CreateSecurityGroupResult res= _svc.getCloud().getEC2()
                .createSecurityGroup(new CreateSecurityGroupRequest(group,desc)) ;
        return res==null ? null : res.getGroupId();
    }

    @Override
    public void delete(String group) throws InternalException, CloudException {
        tstEStrArg("group-name", group);
        try {
            _svc.getCloud().getEC2()
                .deleteSecurityGroup( 
                    new DeleteSecurityGroupRequest().withGroupName(group));
        }
        catch (AmazonServiceException e) {
        	if (!testSafeNonExistError(e, "InvalidGroup.NotFound")) {
                throw new CloudException(e);        		
        	}
        }
    }

    @Override
    public Firewall getFirewall(String group) throws InternalException,
            CloudException {
        return (Firewall) getOneFWall(group).get(0);
    }

    @Override
    public String getProviderTermForFirewall(Locale loc) {
        return "security-group";
    }

    @Override
    public Collection<FirewallRule> getRules(String group)
            throws InternalException, CloudException {        
        Tuple t= getOneFWall(group) ;
        com.amazonaws.services.ec2.model.SecurityGroup g= 
        (com.amazonaws.services.ec2.model.SecurityGroup) t.get(1);
        List<IpPermission> lst= g==null ? null : g.getIpPermissions();
        List<FirewallRule> rc= LT();
        
        if (lst != null) for (int i=0; i < lst.size(); ++i) {
            rc.addAll(toRules(group, lst.get(i)));
        }
        
        return rc;
    }

    @Override
    public Collection<Firewall> list() throws InternalException, CloudException {
        DescribeSecurityGroupsResult res= _svc.getCloud().getEC2()
                .describeSecurityGroups( new DescribeSecurityGroupsRequest());
        List<com.amazonaws.services.ec2.model.SecurityGroup> lst= res==null ? null : res.getSecurityGroups();

        List<Firewall> rc= LT();
        if (lst != null) for (int i=0; i < lst.size(); ++i) {
            rc.add( (Firewall) toFW(lst.get(i)).get(0) );
        }
        return rc;
    }

    @Override
    public void revoke(final String group, final String cidr, final Protocol p, final int fromPort,
            final int toPort) throws CloudException, InternalException {
        tstEStrArg("group-name", group);
        tstEStrArg("cidr", cidr);
        tstObjArg("protocol", p);
        
        List<IpPermission> lst= toObjList(toPerm(group,cidr,p,fromPort,toPort));
        
        _svc.getCloud().getEC2()
            .revokeSecurityGroupIngress(
                    new RevokeSecurityGroupIngressRequest(group, lst));
    }

    /**/
    private Tuple getOneFWall(String group) throws InternalException,
            CloudException {
        tstEStrArg("group-name", group);
        DescribeSecurityGroupsResult res= _svc.getCloud().getEC2()
                .describeSecurityGroups( new DescribeSecurityGroupsRequest().withGroupNames(group));
        List<com.amazonaws.services.ec2.model.SecurityGroup> lst= res==null ? null : res.getSecurityGroups();
        com.amazonaws.services.ec2.model.SecurityGroup g= isNil(lst) ? null : lst.get(0);
        return toFW(g);
    }
    
    /**/
    private Tuple toFW(com.amazonaws.services.ec2.model.SecurityGroup g) {
        Firewall w= null;
        if (g != null) {
            w= new Firewall();
            w.setActive(true);
            w.setAvailable(true);
            w.setProviderFirewallId(g.getGroupId());
            w.setName( g.getGroupName());
            w.setDescription(g.getDescription());
            w.setRegionId(_svc.getCloud().getContext().getRegionId());            
        }
        return new Tuple(w,g);
    }

    /**/
    private List<FirewallRule> toRules(String group, IpPermission p) {
        List<FirewallRule> lst= LT();
        if (p != null) {
            for (String s : p.getIpRanges()) {
                lst.add( new FirewallRule(
                        group, 
                        s, 
                        Protocol.valueOf(p.getIpProtocol().toUpperCase()), 
                        p.getFromPort(), 
                        p.getToPort()));                
            }
        }
        return lst;
    }
    
    /**/
    private IpPermission toPerm(String grp, String cidr, Protocol p, int fromPort, int toPort) {
        return new IpPermission().withIpProtocol(p.name().toLowerCase())
        .withFromPort(fromPort).withToPort(toPort)
        .withIpRanges(cidr) ;        
    }
    
}
