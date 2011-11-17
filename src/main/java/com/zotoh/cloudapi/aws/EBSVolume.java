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
import static com.zotoh.cloudapi.aws.AWSAPI.toVolState;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.CoreUte.tstPosIntArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.isEmpty;

import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.Volume;
import org.dasein.cloud.compute.VolumeSupport;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class EBSVolume implements VolumeSupport {
	
    private Logger ilog() {  return _log=getLogger(EBSVolume.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    
    private final AWSComputeSvcs _svc;


    /**
     * @param s
     */
    protected EBSVolume(AWSComputeSvcs s) {
        tstObjArg("compute-service",s);
    	_svc=s;
    }

	@Override
	public void attach(String vol, String server, String dev)
					throws InternalException, CloudException {
		tstEStrArg("instance-id", server);
		tstEStrArg("volume-id", vol);
		tstEStrArg("dev-id", dev);
		_svc.getCloud().getEC2().attachVolume(
				new AttachVolumeRequest().withVolumeId(vol)
				.withInstanceId(server)	.withDevice(dev));
	}

	@Override
	public String create(String snapId, int sizeGB, String zone)
					throws InternalException, CloudException {
		tstEStrArg("datacenter/zone", zone);
		tstPosIntArg("sizeGB", sizeGB);
		CreateVolumeRequest req=new CreateVolumeRequest()
		.withAvailabilityZone(zone).withSize(sizeGB); 
		if (!isEmpty(snapId)) {
			req.withSnapshotId(snapId);
		}
		CreateVolumeResult res=_svc.getCloud().getEC2().createVolume(req);
		com.amazonaws.services.ec2.model.Volume v=res==null ? null : res.getVolume();
		return v==null ? null : v.getVolumeId();
	}

	@Override
	public void detach(String vol) throws InternalException, CloudException {
	    tstEStrArg("volume-id", vol);
		_svc.getCloud().getEC2().detachVolume(
						new DetachVolumeRequest()
						.withVolumeId(vol)) ;
	}

	@Override
	public String getProviderTermForVolume(Locale loc) {
		return "volume";
	}

	@Override
	public Volume getVolume(String vol) throws InternalException,
					CloudException {
		tstEStrArg("volume-id", vol);
		DescribeVolumesResult res= _svc.getCloud().getEC2().describeVolumes(
					new DescribeVolumesRequest().withVolumeIds(vol));
		List<com.amazonaws.services.ec2.model.Volume> lst= res==null ? null : res.getVolumes();
		com.amazonaws.services.ec2.model.Volume v= isNil(lst) ? null : lst.get(0);
		return toVol(v);
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
        try {
            _svc.getCloud().getEC2().describeVolumes();
            return true;
        }
        catch (AmazonServiceException e) {
            if (AWSAPI.testForNotSubError(e)) { return false; }
            else {
                throw new CloudException(e);                            
            }
        }
        catch (AmazonClientException e) {
            throw new InternalException(e);
        }
	}

	@Override
	public Iterable<String> listPossibleDeviceIds(Platform platform)
					throws InternalException, CloudException {
		tstObjArg("platform", platform);		
        List<String> lst;        
        if( platform.isWindows() ) {
            lst=AWSAPI.toObjList("xvdf","xvdg", "xvdh", "xvdi", "xvdj") ;
        }
        else {
            lst= AWSAPI.toObjList("/dev/sdf", "/dev/sdg",  "/dev/sdh", "/dev/sdi", "/dev/sdj");
        }
        return lst;
	}

	@Override
	public Iterable<Volume> listVolumes() throws InternalException,
					CloudException {
		DescribeVolumesResult res= _svc.getCloud().getEC2().describeVolumes();
		List<com.amazonaws.services.ec2.model.Volume> lst = res==null ? null : res.getVolumes();
		List<Volume> rc= LT();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {
			rc.add(toVol( lst.get(i)));
		}
		return rc;
	}

	@Override
	public void remove(String vol) throws InternalException, CloudException {
		tstEStrArg("volume-id", vol);
		try {
			_svc.getCloud().getEC2().deleteVolume(
					new DeleteVolumeRequest().withVolumeId(vol)) ;
		}
		catch (AmazonServiceException e) {
			if (! testSafeNonExistError(e, "InvalidVolume.NotFound")) {
				throw new CloudException(e); 				
			}
		}
	}
    
	/**/
    private Volume toVol( com.amazonaws.services.ec2.model.Volume v) {
    	Volume vol= null;
    	if (v != null) {
    		vol= new Volume();
    		vol.setSizeInGigabytes(v.getSize());
    		vol.setName(v.getVolumeId());
    		vol.setProviderVolumeId(vol.getName());
    		vol.setCreationTimestamp(v.getCreateTime().getTime());
    		vol.setProviderSnapshotId(v.getSnapshotId());
    		vol.setCurrentState( toVolState(v.getState()));
    		vol.setProviderDataCenterId(v.getAvailabilityZone()) ;
    		vol.setProviderRegionId(_svc.getCloud().getContext().getRegionId());
    		vol.setProviderVirtualMachineId("") ;
    		List<VolumeAttachment> atts=v.getAttachments();
    		if (!isNil(atts)) {
    		    vol.setProviderVirtualMachineId( atts.get(0).getInstanceId());
                vol.setDeviceId( atts.get(0).getDevice());
    		}
    	}
    	return vol;
    }
	
}
