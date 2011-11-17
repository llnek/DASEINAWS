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
import static com.zotoh.cloudapi.aws.AWSAPI.toSnapState;
import static com.zotoh.core.util.CoreUte.isNil;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.isEmpty;
import static com.zotoh.core.util.StrUte.nsb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.compute.Snapshot;
import org.dasein.cloud.compute.SnapshotSupport;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateVolumePermission;
import com.amazonaws.services.ec2.model.CreateVolumePermissionModifications;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotAttributeResult;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.ModifySnapshotAttributeRequest;
import com.zotoh.core.util.Logger;

import static com.zotoh.core.util.LangUte.*;



/**
 * @author kenl
 *
 */
public class EBSSnapshot implements SnapshotSupport {

    private Logger ilog() {  return _log=getLogger(EBSSnapshot.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
    	
	private final AWSComputeSvcs _svc;
	
	/**
	 * @param s
	 */
	protected EBSSnapshot(AWSComputeSvcs s) {
	    tstObjArg("compute-service",s);
		_svc=s;
	}
	
	@Override
	public String create(String volId, String desc) throws InternalException,
					CloudException {
		tstEStrArg("volume-id", volId);
		desc=nsb(desc);
		CreateSnapshotResult res = _svc.getCloud().getEC2().createSnapshot(
				new CreateSnapshotRequest().withVolumeId(volId)
				.withDescription(desc)) ;
		com.amazonaws.services.ec2.model.Snapshot s= res==null ? null : res.getSnapshot();
		return s==null ? null : s.getSnapshotId();
	}

	@Override
	public String getProviderTermForSnapshot(Locale loc) {
		return "snapshot";
	}

	@Override
	public Snapshot getSnapshot(String snap) throws InternalException,
					CloudException {
		tstEStrArg("snapshot-id", snap);
		DescribeSnapshotsResult res= _svc.getCloud().getEC2().describeSnapshots(
					new DescribeSnapshotsRequest()
					.withSnapshotIds(snap)) ;
		List<com.amazonaws.services.ec2.model.Snapshot> lst= res==null ? null : res.getSnapshots();
		return isNil(lst) ? null : toSnap(lst.get(0));
	}

	@Override
	public boolean isPublic(String snap) throws InternalException,
					CloudException {
		DescribeSnapshotAttributeResult res= _svc.getCloud().getEC2().describeSnapshotAttribute(
						new DescribeSnapshotAttributeRequest()
						.withAttribute("createVolumePermission")
						.withSnapshotId(snap));
		List<CreateVolumePermission> lst= res==null ? null : res.getCreateVolumePermissions();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {
			if ( "all".equals( lst.get(i).getGroup())) { return true; }
		}
		return false;
	}

	@Override
	public boolean isSubscribed() throws InternalException, CloudException {
	    try {
	        _svc.getCloud().getEC2().describeSnapshots(
	                new DescribeSnapshotsRequest()
	                .withOwnerIds(_svc.getCloud().getContext().getAccountNumber()));
	        return true;
	    }
	    catch (AmazonServiceException e) {
	        if ( testForNotSubError(e)) { return false; } else {
	            throw new CloudException(e);            	            
	        }
	    }
        catch (AmazonClientException e) {
            throw new InternalException(e);
        }
	}

	@Override
	public Iterable<String> listShares(String snap) throws InternalException,
					CloudException {
		DescribeSnapshotAttributeResult res= _svc.getCloud().getEC2().describeSnapshotAttribute(
				new DescribeSnapshotAttributeRequest()
				.withAttribute("createVolumePermission")
				.withSnapshotId(snap));
		List<CreateVolumePermission> lst= res==null ? null : res.getCreateVolumePermissions();
		List<String> rc= LT();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {
			rc.add( lst.get(i).getUserId());
		}
		return rc;
	}

	@Override
	public Iterable<Snapshot> listSnapshots() throws InternalException,
					CloudException {
		DescribeSnapshotsResult res= _svc.getCloud().getEC2().describeSnapshots();
		List<com.amazonaws.services.ec2.model.Snapshot> lst= res==null ? null : res.getSnapshots();
		List<Snapshot> rc= LT();
		if (lst != null) for (int i=0; i < lst.size(); ++i) {
			rc.add( toSnap(lst.get(i)));
		}
		return rc;
	}

	@Override
	public void remove(String snap) throws InternalException, CloudException {
		tstEStrArg("snapshot-id", snap);
		try {
			_svc.getCloud().getEC2().deleteSnapshot(
				new DeleteSnapshotRequest()
				.withSnapshotId(snap));
		}
		catch (AmazonServiceException e) {
		    if ( !testSafeNonExistError(e, "InvalidSnapshot.NotFound")) {  
                throw new CloudException(e);		        
		    }
		}
	}

	@Override
	public void shareSnapshot(String snapId, String acct, boolean share)
					throws InternalException, CloudException {
		tstEStrArg("snapshot-id", snapId);
		List<CreateVolumePermission> lst= new ArrayList<CreateVolumePermission>();
		CreateVolumePermission cp=new CreateVolumePermission();
		CreateVolumePermissionModifications perms
		    = new CreateVolumePermissionModifications();		
        lst.add( isEmpty(acct) ?  cp.withGroup("all") : cp.withUserId(acct));		
		if (share) { 	perms.setAdd(lst);}
		else { perms.setRemove(lst); }		
		
		_svc.getCloud().getEC2().modifySnapshotAttribute(
				new ModifySnapshotAttributeRequest()
				.withCreateVolumePermission(perms)
				.withSnapshotId(snapId));
	}

	@Override
	public boolean supportsSnapshotSharing() throws InternalException,
					CloudException {
		return true;
	}

	@Override
	public boolean supportsSnapshotSharingWithPublic()
					throws InternalException, CloudException {
		return true;
	}

	/**/
	private Snapshot toSnap(com.amazonaws.services.ec2.model.Snapshot s) {
		Snapshot ss= null;
		if (s != null) {
			ss= new Snapshot();
			ss.setCurrentState(toSnapState(s.getState()));
			ss.setDescription(s.getDescription()); 
			ss.setName(s.getSnapshotId());
			ss.setOwner(s.getOwnerId());
			ss.setProviderSnapshotId(ss.getName());
			ss.setRegionId(_svc.getCloud().getContext().getRegionId());
			ss.setSizeInGb(s.getVolumeSize());
			ss.setSnapshotTimestamp(s.getStartTime().getTime());
			ss.setVolumeId(s.getVolumeId());
			ss.setProgress(s.getProgress());
		}
		return ss;
	}
	
}
