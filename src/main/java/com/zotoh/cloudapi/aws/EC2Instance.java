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
import static com.zotoh.cloudapi.aws.AWSAPI.toObjList;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.isNilArray;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.CoreUte.tstPosLongArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.isEmpty;
import static com.zotoh.core.util.StrUte.nsb;
import static com.zotoh.core.util.StrUte.trim;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.Tag;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VmStatistics;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.GetConsoleOutputRequest;
import com.amazonaws.services.ec2.model.GetConsoleOutputResult;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.MonitorInstancesRequest;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.UnmonitorInstancesRequest;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class EC2Instance implements VirtualMachineSupport {

    private Logger ilog() {  return _log=getLogger(EC2Instance.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    	
	private final AWSComputeSvcs _svc;
	

	/**
	 * @param s
	 */
	protected EC2Instance(AWSComputeSvcs s) {
		tstObjArg("compute-service", s);
		_svc=s;
	}
	
	@Override
	public void boot(String server) throws InternalException, CloudException {
		tstEStrArg("instance-id", server);
		_svc.getCloud().getEC2().startInstances(
				new StartInstancesRequest().withInstanceIds(server));
	}

	@Override
	public VirtualMachine clone(String server, String toDcId, String name,
					String desc, boolean powerOn, String... firewalls)
					throws InternalException, CloudException {
		throw new OperationNotSupportedException("EC2 instance cannot be cloned.");
	}

	@Override
	public void disableAnalytics(String server) throws InternalException,
					CloudException {
		tstEStrArg("instance-id", server);
		_svc.getCloud().getEC2().unmonitorInstances(
						new UnmonitorInstancesRequest().withInstanceIds(server));
	}

	@Override
	public void enableAnalytics(String server) throws InternalException,
					CloudException {
		tstEStrArg("instance-id", server);
		_svc.getCloud().getEC2().monitorInstances(
						new MonitorInstancesRequest().withInstanceIds(server));
	}

	@Override
	public String getConsoleOutput(String server) throws InternalException,
					CloudException {
		tstEStrArg("instance-id", server);
		GetConsoleOutputResult res=_svc.getCloud().getEC2().getConsoleOutput(
					new GetConsoleOutputRequest().withInstanceId(server)) ;
		return res==null ? null : res.getOutput();
	}

	@Override
	public VirtualMachineProduct getProduct(String productId)
					throws InternalException, CloudException {
	    return new AWSAPI().findProduct(productId);
	}

	@Override
	public String getProviderTermForServer(Locale loc) {
		return "instance";
	}

	@Override
	public VmStatistics getVMStatistics(final String server, long from, long to)
					throws InternalException, CloudException {
		tstPosLongArg("from-timestamp", from);
		tstPosLongArg("to-timestamp", to);		
		tstEStrArg("instance-id", server);
		GetMetricStatisticsRequest req= new GetMetricStatisticsRequest();
		GregorianCalendar cal= new GregorianCalendar();
		cal.setTimeInMillis(from);
		req.setStartTime(cal.getTime()) ;
		cal.setTimeInMillis(to);
		req.setEndTime(cal.getTime());
		req.setDimensions(toObjList( new Dimension().withName("InstanceId").withValue(server) ));
		req.setNamespace("AWS");
		req.setPeriod(60);
		req.setMetricName("");
		req.setStatistics(toObjList("Average", "Minimum", "Maximum"));
		GetMetricStatisticsResult res= _svc.getCloud().getCW().getMetricStatistics(req);
		List<Datapoint> lst= res==null ? null : res.getDatapoints();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {			
		}
		return null;
	}

	@Override
	public Iterable<VmStatistics> getVMStatisticsForPeriod(String server,
					long begin, long end) throws InternalException,
					CloudException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine getVirtualMachine(String server)
					throws InternalException, CloudException {
	    tstEStrArg("instance-id", server);
	    DescribeInstancesResult res= _svc.getCloud().getEC2().describeInstances(
	            new DescribeInstancesRequest().withInstanceIds(server));
	    List<Reservation > lst=res==null ? null : res.getReservations();
	    Reservation r= isNil(lst) ? null : lst.get(0);
	    VirtualMachine vm=null;
	    if (r != null) {
	        List<Instance> li= r==null ? null : r.getInstances();
	        vm= toVM( r.getOwnerId(), isNil(li) ? null : li.get(0));	        
	    }
		return vm;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
	    try {
	        _svc.getCloud().getEC2().describeInstances();
	        return true;
	    }
	    catch (AmazonServiceException e) {
	    	if ( testForNotSubError(e, "SignatureDoesNotMatch")) { return false; }
	    	else {
	            throw new CloudException(e);	    		
	    	}
	    }
        catch (AmazonClientException e){
            throw new InternalException(e);
        }
	}

	// we embed the region as part of zone => region|zone
	@Override
	public VirtualMachine launch(String ami, VirtualMachineProduct type,
            String zone, String name, String descOrUserData, String keypair,
            String vpcId, boolean monitoring, boolean asImageSandbox,String... firewalls)
					throws InternalException, CloudException {
		return launch(ami,type,zone,name,descOrUserData,keypair,vpcId,monitoring,asImageSandbox, firewalls, new Tag[0]);
	}

    // we embed the region as part of zone => region|zone
	@Override
	public VirtualMachine launch(String ami, VirtualMachineProduct type,
					String zone, String name, String descOrUserData, String keypair,
					String vpcId, boolean monitoring, boolean asImageSandbox, String[] firewalls,
					Tag... tags) throws InternalException, CloudException {
		tstEStrArg("image-id", ami);
		tstObjArg("product-type", type);
		tstEStrArg("keypair", keypair);
        tstEStrArg("zone", zone);
	    RunInstancesRequest req=new RunInstancesRequest()
	    .withInstanceType(type.getProductId())
	    .withImageId(ami)
	    .withKeyName(keypair)
	    .withMaxCount(1)
	    .withMinCount(1)
	    .withMonitoring(monitoring);
	    if (!isNilArray(firewalls)) {
		    req.withSecurityGroups(firewalls);	    	
	    }
	    String[] ss= zone.split("\\|");
	    _svc.getCloud().setAWSSite(ss[0]);
	    if (ss.length > 1) {
		    req.withPlacement(new Placement().withAvailabilityZone( trim( ss[1])) );	    	
	    }
	    if (!isEmpty(descOrUserData)) {
	        req.withUserData(descOrUserData);
	    }
	    RunInstancesResult res=_svc.getCloud().getEC2().runInstances(req);
	    Reservation r= res==null ? null : res.getReservation();
	    VirtualMachine vm=null;
	    if (r != null) {
	        List<Instance> lst= r.getInstances();
	        vm= toVM( r.getOwnerId(), isNil(lst) ? null : lst.get(0));
	    }
		return vm;
	}

	@Override
	public Iterable<String> listFirewalls(String server)
					throws InternalException, CloudException {
	    tstEStrArg("instance-id", server);
	    DescribeInstancesResult res= _svc.getCloud().getEC2().describeInstances(
	            new DescribeInstancesRequest().withInstanceIds(server));
	    List<Reservation> lst=res==null ? null : res.getReservations();
	    Reservation r= isNil(lst) ? null : lst.get(0);
	    List<GroupIdentifier> gs= r==null ? null : r.getGroups();
	    List<String> rc= LT();
	    if (gs != null) for (int i=0; i < gs.size(); ++i) {
	        rc.add(gs.get(i).getGroupName());
	    }
		return rc;
	}

	@SuppressWarnings("unchecked")
    @Override
	public Iterable<VirtualMachineProduct> listProducts(Architecture arch)
					throws InternalException, CloudException {
	    AWSAPI api= new AWSAPI();
	    if (Architecture.I32.equals(arch)) return api.listProducts(32);
        if (Architecture.I64.equals(arch)) return api.listProducts(64);
		return Collections.EMPTY_LIST;
	}

	@Override
	public Iterable<VirtualMachine> listVirtualMachines()
					throws InternalException, CloudException {
	    DescribeInstancesResult res=_svc.getCloud().getEC2().describeInstances();
	    List<Reservation> lst=res==null ? null : res.getReservations();
	    List<Instance> li;
	    Reservation rr;
	    List<VirtualMachine> rc= LT();
	    if (lst != null) for (int i=0; i < lst.size(); ++i) {
	        rr=lst.get(i);
	        li=rr.getInstances();
	        if (li != null) for (int j=0; j < li.size(); ++j) {
	            rc.add( toVM(rr.getOwnerId(), li.get(j)));	            
	        }
	    }
		return rc;
	}

	// beware: you can only stop an EBS-backed ami(vm)
	@Override
	public void pause(String server) throws InternalException, CloudException {
	    tstEStrArg("instance-id", server);
	    _svc.getCloud().getEC2().stopInstances(
	            new StopInstancesRequest().withInstanceIds(server));
	}

	@Override
	public void reboot(String server) throws CloudException, InternalException {
        tstEStrArg("instance-id", server);
        _svc.getCloud().getEC2().rebootInstances(
                new RebootInstancesRequest().withInstanceIds(server));
	}

	@Override
	public boolean supportsAnalytics() throws CloudException, InternalException {
		return true;
	}

	@Override
	public void terminate(String server) throws InternalException, CloudException {
        tstEStrArg("instance-id", server);
        _svc.getCloud().getEC2().terminateInstances(
                new TerminateInstancesRequest().withInstanceIds(server));		
	}

	/**/
	private VirtualMachine toVM(String owner, Instance r) {
        AWSAPI api= new AWSAPI();
        String s;
	    VirtualMachine vm= null;
	    if (r != null) {
	        vm=new VirtualMachine();
	        vm.setPersistent(true);// if EBS backed, yes (i think)
	        vm.setProviderOwnerId(owner);
	        vm.setCurrentState(AWSAPI.toVmState(r.getState().getName()));	        
	        vm.setArchitecture(AWSAPI.toArch(r.getArchitecture()));
	        vm.setClonable(false);
	        vm.setCreationTimestamp(r.getLaunchTime().getTime());
	        vm.setDescription("");
	        vm.setImagable(true);
	        vm.setLastBootTimestamp(vm.getCreationTimestamp());
	        vm.setLastPauseTimestamp(0L);
	        vm.setName(r.getInstanceId());
	        vm.setPausable(false); // only if EBS backed
	        vm.setPlatform(AWSAPI.toPlat(r.getPlatform())) ;
	        vm.setPrivateDnsAddress(r.getPrivateDnsName());
	        vm.setPrivateIpAddresses( new String[]{nsb(r.getPrivateIpAddress())});
	        vm.setProduct( api.findProduct( r.getInstanceType()));
	        vm.setProviderAssignedIpAddressId(r.getPublicDnsName());
	        vm.setPublicDnsAddress(r.getPublicDnsName());
	        vm.setPublicIpAddresses(new String[] { nsb(r.getPublicIpAddress())});
	        vm.setRebootable(true);
	        vm.setTerminationTimestamp(0L);
	        vm.setProviderDataCenterId(
	                r.getPlacement()==null ? "" : r.getPlacement().getAvailabilityZone());
	        vm.setProviderMachineImageId(r.getImageId());
	        vm.setProviderRegionId( _svc.getCloud().getContext().getRegionId());
	        vm.setProviderVirtualMachineId(r.getInstanceId());
	        
	        s=r.getRootDeviceType();
	        if ("ebs".equals(s)) { vm.setPausable(true);}
	        vm.addTag("rootdevicetype", s) ;

	    }
	    return vm;
	}
		
}
