# About
Dasein-AWS is an amazon-aws implementation of the [dasein-cloud](http://dasein-cloud.sourceforge.net/) api, utilizing the amazon-aws java sdk.  Dasein-cloud defines a set of cloud agnostic API.

<b>NOTE:</b> This is an independent implementation and is not endorsed or associated with
Dasein-cloud.

# Requirements & Dependencies

## Java
* &gt;= 1.6

## AWS JavaSDK (included)
* &gt;= 1.2.12

## Dasein-core SDK (included)
* dasein-cloud-2011.02.zip

# Initialization
<pre>
Properties _props= new Properties();
_props.put("pwd", "your aws secret key");
_props.put("account", "your aws account number");
_props.put("id", "your aws access key");
CloudProvider _aws= AWSInitContext.getInstance().configure(_props);  
</pre>

# Set AWS Region
<pre>
((AWSCloud)_aws).setAWSSite("us-east-1") ;
</pre>

# Compute Services
<pre>
ComputeServices s=_aws.getComputeServices();
MachineImageSupport s2=s.getImageSupport(); // AWS AMI
VirtualMachineSupport s3=s.getVirtualMachineSupport();  // AWS instances
SnapshotSupport s4= s.getSnapshotSupport(); // EBS snapshots
VolumeSupport s5=s.getVolumeSupport();  // EBS Volumes
</pre>

# Platform Services
<pre>
PlatformServices svc=_aws.getPlatformServices();
KeyValueDatabaseSupport s2=svc.getKeyValueDatabaseSupport(); // AWS SDB
PushNotificationSupport s4=svc.getPushNotificationSupport() ; // AWS SNS
</pre>

# Network Services
<pre>
NetworkServices svc= _aws.getNetworkServices();
FirewallSupport s2=svc.getFirewallSupport();  // Security Groups
IpAddressSupport s3= svc.getIpAddressSupport(); // Elastic IP
LoadBalancerSupport s4=svc.getLoadBalancerSupport();  // AWS ELB
</pre>

# Identity Services
<pre>
IdentityServices svc=_aws.getIdentityServices();
ShellKeySupport s1= svc.getShellKeySupport(); // Key Pairs
Collection<String> lst= s1.list(); // list all the keypairs
String result_PEM = s1.createKeypair("test key"); // the ssh key
s1.deleteKeypair("test key");
</pre>

# Datacenter Services
<pre>
DataCenterServices svcs= _aws.getDataCenterServices();
Region r= svcs.getRegion("us-west-1");
String p= r.getProviderRegionId(); // "ec2.us-west-1.amazonaws.com"

Collection<Region>rs= svcs.listRegions();
Collection<DataCenter>cs=svcs.listDataCenters("eu-west-1");
DataCenter c=svcs.getDataCenter("us-west-1a");          // AWS Availability Zones
String cid= c.getProviderDataCenterId(); // "us-west-1a"
String rid= c.getRegionId(); // "us-west-1"
</pre>

# Latest binary
Download the latest bundle [1.0.0](http://www.zotoh.com/packages/dasein-aws/stable/1.0.0/dasein-aws-1.0.0.zip)



