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
import static com.zotoh.cloudapi.aws.AWSAPI.testSafeNonExistError;
import static com.zotoh.cloudapi.aws.AWSAPI.toArch;
import static com.zotoh.cloudapi.aws.AWSAPI.toImageState;
import static com.zotoh.cloudapi.aws.AWSAPI.toImageType;
import static com.zotoh.cloudapi.aws.AWSAPI.toObjList;
import static com.zotoh.core.util.CoreUte.asDouble;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.ProcessUte.asyncExec;
import static com.zotoh.core.util.ProcessUte.safeThreadWait;
import static com.zotoh.core.util.StrUte.hasWithin;
import static com.zotoh.core.util.StrUte.isEmpty;
import static com.zotoh.core.util.StrUte.nsb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.dasein.cloud.AsynchronousTask;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageFormat;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.Platform;
import org.dasein.util.CalendarWrapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.BundleInstanceRequest;
import com.amazonaws.services.ec2.model.BundleInstanceResult;
import com.amazonaws.services.ec2.model.BundleTask;
import com.amazonaws.services.ec2.model.BundleTaskError;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeBundleTasksRequest;
import com.amazonaws.services.ec2.model.DescribeBundleTasksResult;
import com.amazonaws.services.ec2.model.DescribeImageAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeImageAttributeResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ImageAttribute;
import com.amazonaws.services.ec2.model.LaunchPermission;
import com.amazonaws.services.ec2.model.LaunchPermissionModifications;
import com.amazonaws.services.ec2.model.ModifyImageAttributeRequest;
import com.amazonaws.services.ec2.model.RegisterImageRequest;
import com.amazonaws.services.ec2.model.RegisterImageResult;
import com.amazonaws.services.ec2.model.S3Storage;
import com.amazonaws.services.ec2.model.Storage;
import com.amazonaws.services.ec2.util.S3UploadPolicy;
import com.zotoh.cloudapi.core.Vars;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class AMImage implements MachineImageSupport, AWSVars , Vars{

    private Logger ilog() {  return _log=getLogger(AMImage.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
        
    private final AWSComputeSvcs _svc;
    
    /**
     * @param s
     */
    protected AMImage(AWSComputeSvcs s) {
        tstObjArg("compute-service",s);
        _svc=s;
    }
    
    @Override
    public void downloadImage(String ami, OutputStream out)
            throws CloudException, InternalException {
        throw new OperationNotSupportedException();
    }

    @Override
    public MachineImage getMachineImage(String ami) throws CloudException,
            InternalException {
        tstEStrArg("image-id", ami);
        DescribeImagesResult res= _svc.getCloud().getEC2()
        .describeImages(new DescribeImagesRequest().withImageIds(ami));
        List<Image> lst= res==null ? null : res.getImages();
        Image m= isNil(lst) ? null : lst.get(0);        
        return toMI(m);
    }

    @Override
    public String getProviderTermForImage(Locale loc) {        return "image";    }

    @Override
    public boolean hasPublicLibrary() {        return true;    }

    @Override
    public AsynchronousTask<String> imageVirtualMachine(final String vmId,
            final String name, final String desc) throws CloudException, InternalException {
        tstEStrArg("description", desc);
    	tstEStrArg("vm-id", vmId);
    	tstEStrArg("name", name);
        final AsynchronousTask<String> task = new AsynchronousTask<String>();
        asyncExec(new Runnable() {
            public void run() {
                try {
                    CreateImageResult res= _svc.getCloud().getEC2()
                                    .createImage(new CreateImageRequest().withInstanceId(vmId)
                                    .withName(name).withDescription(desc));
                    task.completeWithResult( res==null ? null : res.getImageId() );
                }
                catch( Throwable t ) {
                    task.complete(t);
                }
                return;
            }            
        });
        return task;
    }

    @Override
    public AsynchronousTask<String> imageVirtualMachineToStorage(String vmId,
            String name, String desc, String directory) throws CloudException,
            InternalException {
    	tstEStrArg("directory", directory);
    	tstEStrArg("name", name);
    	tstEStrArg("instance-id", vmId);
        Properties props= _svc.getCloud().getContext().getCustomProperties();
        String uid= props.getProperty(P_ID);
        String pwd= props.getProperty(P_PWD);
        S3UploadPolicy p= new S3UploadPolicy(uid, pwd, directory, name, 60*12);
        S3Storage s3= new S3Storage()        
        .withAWSAccessKeyId(uid).withBucket(directory).withPrefix(name)
        .withUploadPolicy(p.getPolicyString())
        .withUploadPolicySignature(p.getPolicySignature());
        BundleInstanceResult res= _svc.getCloud().getEC2()
        .bundleInstance(
            new BundleInstanceRequest().withInstanceId(vmId)
            .withStorage(new Storage().withS3(s3)));
        BundleTask t= res==null ? null : res.getBundleTask();        
        final String bid= t==null ? null : t.getBundleId();     
        if (isEmpty(bid)) {
            throw new CloudException("Bundle Id is empty");
        }
        final AsynchronousTask<String> task = new AsynchronousTask<String>();
        final String manifest = (directory + "/" + name + ".manifest.xml");
        asyncExec( new Runnable() {   
            public void run() {
                try { waitForBundle(bid, manifest, task); } catch (Throwable t) {
                    task.complete(t);
                }
        }});        
        return task;
    }

    @Override
    public String installImageFromUpload(MachineImageFormat format,
            InputStream imageStream) throws CloudException, InternalException {
    	throw new OperationNotSupportedException();
    }

    @Override
    public boolean isImageSharedWithPublic(String ami) throws CloudException,
            InternalException {
    	MachineImage m= getMachineImage(ami);
    	return m==null ? false : "true".equals(m.getTag("public"));
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            _svc.getCloud().getEC2().describeImages(
                    new DescribeImagesRequest()
                    .withOwners(_svc.getCloud().getContext().getAccountNumber()));
            return true;
        }
        catch (AmazonServiceException e) {
            if (testForNotSubError(e)) { return false; } else {  throw new CloudException(e); }
        }
        catch (AmazonClientException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public Iterable<MachineImage> listMachineImages() throws CloudException,
            InternalException {
        DescribeImagesResult res=_svc.getCloud().getEC2()
        .describeImages(new DescribeImagesRequest());
        return listAMIs(res);
    }

    @Override
    public Iterable<MachineImage> listMachineImagesOwnedBy(String owner)
            throws CloudException, InternalException {
    	tstEStrArg("owner-id", owner);
        DescribeImagesResult res= _svc.getCloud().getEC2()
        .describeImages(
                new DescribeImagesRequest().withOwners(owner));
        return listAMIs(res);
    }

    @Override
    public Iterable<String> listShares(String ami) throws CloudException,
            InternalException {
    	tstEStrArg("image-id", ami);
    	DescribeImageAttributeResult res=_svc.getCloud().getEC2()
        .describeImageAttribute(
        				new DescribeImageAttributeRequest().withImageId(ami));
    	ImageAttribute attr= res==null ? null : res.getImageAttribute();
		List<LaunchPermission> lst= attr==null ? null : attr.getLaunchPermissions();
		List<String> rc= LT();
    	if (lst != null) for (int i=0; i < lst.size(); ++i) {
    		rc.add( lst.get(i).getUserId());
    	}
        return rc;
    }

    @Override
    public Iterable<MachineImageFormat> listSupportedFormats()
            throws CloudException, InternalException {
        return Collections.singletonList(MachineImageFormat.AWS);
    }

    @Override
    public String registerMachineImage(String location) throws CloudException,
            InternalException {
        tstEStrArg("image-location", location);
        RegisterImageResult res= _svc.getCloud().getEC2()
        .registerImage(
                new RegisterImageRequest().withImageLocation(location));
        return res==null ? null : res.getImageId();
    }

    @Override
    public void remove(String ami) throws CloudException, InternalException {
    	tstEStrArg("image-id", ami);
    	try {
	        _svc.getCloud().getEC2()
	        .deregisterImage(
	                new DeregisterImageRequest().withImageId(ami));
    	}
    	catch (AmazonServiceException e) {
    	    if ( ! testSafeNonExistError(e, "InvalidAMIID.NotFound")) {
                throw new CloudException(e);    	        
    	    }
    	}
    }

    @Override
    public Iterable<MachineImage> searchMachineImages(String keyword,
            Platform platform, final Architecture arch) throws CloudException,
            InternalException {
        List<Filter> fs= LT();
        
        fs.add( new Filter("state", toObjList("available")));
        
        if (platform != null && platform.isWindows()) {
            fs.add( new Filter("platform", toObjList("windows") ));            
        }
        
        if (arch != null) {
            fs.add( new Filter("architecture", 
                    toObjList(   Architecture.I32.equals(arch)  ? "i386" : "x86_64" )));            
        }
        
        keyword=nsb(keyword);

        List<MachineImage> rc= LT();
        DescribeImagesResult res= _svc.getCloud().getEC2()
        .describeImages(
                new DescribeImagesRequest().withFilters(fs));
        List<Image> lst= res==null ? null : res.getImages();
        Image g;
        boolean ok;
        if (lst != null) for (int i=0;  i < lst.size(); ++i) {
            g=lst.get(i);
            if (!isEmpty(keyword)) {
                ok=hasWithin(keyword, nsb(g.getDescription()), nsb(g.getName()), nsb(g.getImageId())) ;
            } else { ok=true; }
            if (ok) {
                rc.add(  toMI(g)) ;
            }
        }
        return rc;
    }

    @Override
    public void shareMachineImage(String ami, final String acct, boolean allow)
            throws CloudException, InternalException {
        tstEStrArg("image-id", ami);
        LaunchPermissionModifications perms= new LaunchPermissionModifications();
        List<LaunchPermission> lst;
        ModifyImageAttributeRequest req= new ModifyImageAttributeRequest()
        .withImageId(ami);
        LaunchPermission lp=new LaunchPermission();
        lst= toObjList(   isEmpty(acct) ?   lp.withGroup("all") : lp.withUserId(acct) );
        if (allow) {   perms.setAdd(lst);   }
        else { perms.setRemove(lst); }        
        req.setLaunchPermission(perms);
        _svc.getCloud().getEC2().modifyImageAttribute(req);
    }

    @Override
    public boolean supportsCustomImages() {
        return true;
    }

    @Override
    public boolean supportsImageSharing() {
        return true;
    }

    @Override
    public boolean supportsImageSharingWithPublic() {
        return true;
    }

    @Override
    public String transfer(CloudProvider fromCloud, String imageId)
            throws CloudException, InternalException {
        throw new OperationNotSupportedException();
    }

    /**/
    private List<MachineImage> listAMIs(DescribeImagesResult res) {
        final List<MachineImage> rc= LT();
        final List<Image> lst= res.getImages();
        if (lst != null) for (int i=0; i < lst.size(); ++i) {
            rc.add( toMI(lst.get(i)) );
        }
        return rc;
    }
    
    /**/
    private MachineImage toMI(Image i) {
        MachineImage m= null;
        if (i != null) {
            m= new MachineImage();            
            m.setProviderRegionId(_svc.getCloud().getContext().getRegionId());
            m.setArchitecture( toArch(i.getArchitecture()) );            
            m.setCurrentState( toImageState(i.getState() ));
            
            m.setProviderMachineImageId(i.getImageId());
            m.setName(i.getName());
            m.setProviderOwnerId(i.getOwnerId());
            m.setSoftware("");            
            m.setType( toImageType(i.getRootDeviceType() ));

            m.addTag("manifest-location", nsb( i.getImageLocation())) ;
            m.addTag("hypervisor", nsb(i.getHypervisor()));
            m.addTag("alias", nsb(i.getImageOwnerAlias()));
            m.addTag("kernel", nsb(i.getKernelId()));
            m.addTag("public", i.getPublic()?"true":"false");
            m.addTag("ramdisk", nsb(i.getRamdiskId()));
            m.addTag("root-dev-name", nsb(i.getRootDeviceName()));
            m.addTag("state-reason", nsb(i.getStateReason()));
            m.addTag("virtualization-type", nsb(i.getVirtualizationType()));
            
            m.setDescription(i.getDescription());
            m.setPlatform(
                    nsb(i.getPlatform()).toLowerCase().indexOf("windows")>=0 
                    ? Platform.WINDOWS : Platform.UBUNTU);
        }
        
        return m;
    }
    
    /**/
    private void waitForBundle(String bid, String manifest, AsynchronousTask<String> task) 
    throws Exception {
        long failurePoint = -1L;            
        while( ! task.isComplete() ) {            
            DescribeBundleTasksResult res= _svc.getCloud().getEC2().describeBundleTasks(
                    new DescribeBundleTasksRequest()
                    .withBundleIds(bid));
            List<BundleTask> lst= res==null ? null : res.getBundleTasks();
            BundleTask t= isNil(lst) ? null : lst.get(0);
            if (t != null) {
                double bar=asDouble(t.getProgress(), 0.0);
                // pending | waiting-for-shutdown | storing | canceling | complete | failed                
                String s=t.getState();
                if( "pending".equals(s) || "waiting-for-shutdown".equals(s) ) {
                    task.setPercentComplete(0.0);
                }
                else if ("complete".equals(s)) { onBundleComplete(manifest, task); }
                else if( "bundling".equals(s) ) {
                    task.setPercentComplete(Math.min(50.0, bar/2));
                }
                else if( "storing".equals(s) ) {
                    task.setPercentComplete(Math.min(100.0, 50.0 + bar/2));
                }
                else if( "failed".equals(s) ) {
                    failurePoint= onBundleFailure(failurePoint, t, task);
                }            
                else {
                    task.setPercentComplete(0.0);
                }
            }
            
            if (!task.isComplete()) {
                safeThreadWait(1500);
            }
        }
    }
    
    /**/
    private long onBundleFailure(long lastFailed, BundleTask t, AsynchronousTask<String> task) {
        BundleTaskError e= t.getBundleTaskError();
        String msg= e==null ? null : e.getMessage();
        
        if( isEmpty(msg)) {
            if( lastFailed == -1L ) {
                lastFailed = System.currentTimeMillis();
            }
            if( (System.currentTimeMillis() - lastFailed) > (CalendarWrapper.MINUTE * 2) ) {
                msg = "Bundle failed without further information.";
            }
        }
        if( !isEmpty(msg) ) {
            task.complete(new CloudException(msg));
        }        
        
        return lastFailed;
    }
    
    /**/
    private void onBundleComplete(String manifest, AsynchronousTask<String> task)
            throws CloudException,
            InternalException {
        String imageId;        
        task.setPercentComplete(99.0);
        imageId = registerMachineImage(manifest);
        task.setPercentComplete(100.00);
        task.completeWithResult(imageId);        
    }
}
