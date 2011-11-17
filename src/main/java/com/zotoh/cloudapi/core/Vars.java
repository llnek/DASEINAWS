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

package com.zotoh.cloudapi.core;


/**
 * @author kenl
 *
 */
public interface Vars {

    public static final String PT_WINDOWS="windows";
    public static final String PT_LINUX="linux";
    
    public static final String I64= "I64";
    public static final String I32= "I32";

    public static final String P_CUSTOM= "custom";

    public static final String P_CRED= "credential";
    public static final String P_KEYS= "sshkeys";
    public static final String P_FWALLS= "firewalls";
    public static final String P_IPS= "eips";
    public static final String P_VMS= "vms";
    public static final String P_SSHINFO= "sshinfo";

    public static final String P_PUBDNS= "pubdns";
    
    public static final String P_VENDOR= "provider";
    public static final String P_PWD= "pwd";
    public static final String P_ID= "id";
    public static final String P_ACCT= "account";

    public static final String P_IMAGES= "images";
    public static final String P_ARCH= "arch";
    public static final String P_PLATFORM= "platform";
    public static final String P_PRODUCT= "product";

    public static final String P_REGIONS= "regions";
    public static final String P_DFTS= "defaults";

    public static final String P_REGION= "region";
    public static final String P_ZONE= "zone";
    public static final String P_IMAGE= "image";
    public static final String P_KEY= "key";
    public static final String P_FWALL= "firewall";
    public static final String P_VM= "vm";

    public static final String P_PEM= "pem";
    public static final String P_IP= "ip";

    public static final String P_USER="user";
    
}
