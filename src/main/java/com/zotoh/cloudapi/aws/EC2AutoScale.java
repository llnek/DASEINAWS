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

import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstNEArray;
import static com.zotoh.core.util.CoreUte.tstNonNegIntArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.compute.AutoScalingSupport;
import org.dasein.cloud.compute.LaunchConfiguration;
import org.dasein.cloud.compute.ScalingGroup;
import org.dasein.cloud.compute.VirtualMachineProduct;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.zotoh.core.util.Logger;

import static com.zotoh.core.util.LangUte.*;


/**
 * @author kenl
 *
 */
public class EC2AutoScale implements AutoScalingSupport {

    private Logger ilog() {  return _log=getLogger(EC2AutoScale.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
	
	private final AWSComputeSvcs _svc;
	
	/**
	 * @param s
	 */
	protected EC2AutoScale(AWSComputeSvcs s) {
		tstObjArg("compute-service",s);
		_svc=s;
	}
	
	@Override
	public String createAutoScalingGroup(String name, String cfgId, int minServers,
					int maxServers, int coolDown, String... zones)
					throws InternalException, CloudException {
		tstEStrArg("autoscale-group-name", name);
		tstNonNegIntArg("max-servers", maxServers);
		tstNonNegIntArg("min-servers", minServers);
		tstNonNegIntArg("cool-down", coolDown);
		tstNEArray("zones", zones);
		
		_svc.getCloud().getAutoScale().createAutoScalingGroup(
				new CreateAutoScalingGroupRequest()
				.withLaunchConfigurationName(cfgId)
				.withAutoScalingGroupName(name)
				.withMaxSize(maxServers)
				.withMinSize(minServers)
				.withDefaultCooldown(coolDown)
				.withAvailabilityZones(zones));
		
		return name;
	}

	@Override
	public String createLaunchConfiguration(String name, String ami,
					VirtualMachineProduct type, String... firewalls)
					throws InternalException, CloudException {
		tstEStrArg("launch-config-name", name);
		tstEStrArg("image-id", ami);
		tstObjArg("product-type", type);
		tstNEArray("firewalls", firewalls);
		_svc.getCloud().getAutoScale().createLaunchConfiguration(
						new CreateLaunchConfigurationRequest()
						.withLaunchConfigurationName(name)
						.withInstanceType(type.getProductId())
						.withSecurityGroups(firewalls)
						.withImageId(ami));
		return name;
	}

	@Override
	public void deleteAutoScalingGroup(String name) throws CloudException,
					InternalException {
		tstEStrArg("autoscale-group-name", name);
		_svc.getCloud().getAutoScale().deleteAutoScalingGroup(
						new DeleteAutoScalingGroupRequest()
						.withAutoScalingGroupName(name));
	}

	@Override
	public void deleteLaunchConfiguration(String name) throws CloudException,
					InternalException {
		tstEStrArg("launch-config-name",name);
		_svc.getCloud().getAutoScale().deleteLaunchConfiguration(
						new DeleteLaunchConfigurationRequest()
						.withLaunchConfigurationName(name));
	}

	@Override
	public LaunchConfiguration getLaunchConfiguration(String name)
					throws CloudException, InternalException {
		tstEStrArg("launch-config-name", name);
		DescribeLaunchConfigurationsResult res=_svc.getCloud().getAutoScale().describeLaunchConfigurations(
				new DescribeLaunchConfigurationsRequest()
				.withLaunchConfigurationNames(name));
		List<com.amazonaws.services.autoscaling.model.LaunchConfiguration> lst= res==null ? null : res.getLaunchConfigurations();
		com.amazonaws.services.autoscaling.model.LaunchConfiguration c= isNil(lst) ? null : lst.get(0);		
		return toLCfg(c);
	}

	@Override
	public ScalingGroup getScalingGroup(String name) throws CloudException,
					InternalException {
		DescribeAutoScalingGroupsResult res=_svc.getCloud().getAutoScale().describeAutoScalingGroups(
						new DescribeAutoScalingGroupsRequest());
		List<AutoScalingGroup> lst=res==null ? null : res.getAutoScalingGroups() ;
		return toSG(isNil(lst) ? null : lst.get(0));
	}

	@Override
	public Collection<LaunchConfiguration> listLaunchConfigurations()
					throws CloudException, InternalException {
		DescribeLaunchConfigurationsResult res=_svc.getCloud().getAutoScale().describeLaunchConfigurations() ;
		List<com.amazonaws.services.autoscaling.model.LaunchConfiguration> lst=res==null ? null : res.getLaunchConfigurations() ;
		List<LaunchConfiguration> rc= LT();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {
			rc.add( toLCfg(lst.get(i)));
		}
		return rc;
	}

	@Override
	public Collection<ScalingGroup> listScalingGroups() throws CloudException,
					InternalException {
		DescribeAutoScalingGroupsResult res=_svc.getCloud().getAutoScale().describeAutoScalingGroups();
		List<AutoScalingGroup> lst=res==null ? null : res.getAutoScalingGroups();
		List<ScalingGroup> rc= LT();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {
			rc.add( toSG(lst.get(i)));
		}
		return rc;
	}

	@Override
	public void setDesiredCapacity(String group, int capacity)
					throws CloudException, InternalException {
		tstEStrArg("autoscale-group-name", group);
		tstNonNegIntArg("capacity", capacity);
		_svc.getCloud().getAutoScale().setDesiredCapacity(
					new SetDesiredCapacityRequest()
					.withDesiredCapacity(capacity)
					.withAutoScalingGroupName(group)) ;
	}

	@Override
	public String setTrigger(String name, String group, String statistic,
					String unitOfMeasure, String metric, int periodSecs, double lowerThreshold,
					double upperThreshold, int lowerIncr, boolean lowerIncrAbsolute, int upperIncr,
					boolean upperIncrAbsolute, int breachDuration) throws InternalException,
					CloudException {
		throw new OperationNotSupportedException();
	}

	@Override
	public void updateAutoScalingGroup(String group, String cfgId, int minServers,
					int maxServers, int coolDown, String... zones)
					throws InternalException, CloudException {
		_svc.getCloud().getAutoScale().updateAutoScalingGroup(
						new UpdateAutoScalingGroupRequest()
						.withAutoScalingGroupName(group)
						.withDefaultCooldown(coolDown)
						.withLaunchConfigurationName(cfgId)
						.withMaxSize(maxServers)
						.withMinSize(minServers)
						.withAvailabilityZones(zones));
	}

	/**/
	private ScalingGroup toSG(AutoScalingGroup g) {
		ScalingGroup s= null;
		if (g != null) {
			s= new ScalingGroup();
			s.setCooldown(g.getDefaultCooldown());
			s.setCreationTimestamp(g.getCreatedTime().getTime());
			s.setDescription("");
			s.setMaxServers(g.getMaxSize());
			s.setMinServers(g.getMinSize());
			s.setName(g.getAutoScalingGroupName());
			s.setProviderDataCenterIds(g.getAvailabilityZones().toArray(new String[0])) ;
			s.setProviderLaunchConfigurationId(g.getLaunchConfigurationName()) ;
			s.setProviderOwnerId(_svc.getCloud().getContext().getAccountNumber()) ;
			s.setProviderRegionId(_svc.getCloud().getContext().getRegionId()) ;
			s.setProviderScalingGroupId(s.getName()) ;
			List<com.amazonaws.services.autoscaling.model.Instance> lst=g.getInstances();
			List<String> ls= new ArrayList<String>();
			if (lst != null) for (int i=0; i < lst.size(); ++i) {
				ls.add(lst.get(i).getInstanceId());
			}
			s.setProviderServerIds(ls.toArray(new String[0]));
			s.setTargetCapacity(g.getDesiredCapacity());
		}
		return s;
	}
	
	/**/
	private LaunchConfiguration toLCfg(com.amazonaws.services.autoscaling.model.LaunchConfiguration c) {
		LaunchConfiguration g= null;
		if (c != null) {
			g= new LaunchConfiguration(); 
			g.setCreationTimestamp(c.getCreatedTime().getTime());
			g.setName(c.getLaunchConfigurationName());
			List<String> lst= c.getSecurityGroups();
			String[] ss;
			if (lst != null) {
				ss= lst.toArray(new String[0]);
			} else { ss= new String[0]; }
			g.setProviderFirewallIds(ss) ;
			g.setProviderImageId(c.getImageId()) ;
			g.setProviderLaunchConfigurationId(g.getName()) ;
			g.setServerSizeId(c.getInstanceType());
		}
		return g;
	}
	
}
