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

import static com.zotoh.core.util.LoggerFactory.getLogger;

import org.dasein.cloud.compute.AbstractComputeServices;
import org.dasein.cloud.compute.AutoScalingSupport;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.SnapshotSupport;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VolumeSupport;

import static com.zotoh.core.util.CoreUte.*;
import com.zotoh.core.util.Logger;


/**
 * @author kenl
 *
 */
public class AWSComputeSvcs extends AbstractComputeServices 
implements AWSService {

    private Logger ilog() {  return _log=getLogger(AWSComputeSvcs.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
        
    private final AWSCloud _aws;
    

    /**
     * @param c
     */
    protected AWSComputeSvcs(AWSCloud c) {
        tstObjArg("aws-cloud",c) ;
        _aws=c;
    }
    
    @Override
    public AutoScalingSupport getAutoScalingSupport() {
        return new EC2AutoScale(this);
    }

    @Override
    public MachineImageSupport getImageSupport() {
        return new AMImage(this);
    }

    @Override
    public SnapshotSupport getSnapshotSupport() {
        return new EBSSnapshot(this);
    }

    @Override
    public VirtualMachineSupport getVirtualMachineSupport() {
        return new EC2Instance(this);
    }

    @Override
    public VolumeSupport getVolumeSupport() {
        return new EBSVolume(this);
    }

    @Override
    public boolean hasAutoScalingSupport() {
        return true;
    }

    @Override
    public boolean hasImageSupport() {
        return true;
    }

    @Override
    public boolean hasSnapshotSupport() {
        return true;
    }

    @Override
    public boolean hasVirtualMachineSupport() {
        return true;
    }

    @Override
    public boolean hasVolumeSupport() {
        return true;
    }

    @Override
    public AWSCloud getCloud() {
        return _aws;
    }

}
