package com.wvp.hk.module.service.cms;

import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import com.sun.jna.NativeLong;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* 在线设备管理类
*/
public class OnlineDevManager {

    public static List<Device> g_Devs = null;

    public static class Device{
        public NativeLong m_lLoginID = new NativeLong(-1);
        public NET_EHOME_DEV_REG_INFO_V12 m_struDevInfo;
        public NET_EHOME_DEVICE_CFG m_struDeviceCfg;
        public NET_EHOME_DEVICE_INFO m_struDeviceInfo;
        public NET_EHOME_VERSION_INFO m_struVersionInfo;
        public NET_EHOME_COMPRESSION_CFG m_struCompressionInfo;
        public NET_EHOME_DEVICE_CFG m_struDevCfg;
        public NET_EHOME_PIC_CFG m_struPicCfg;
        public NET_EHOME_NETWORK_CFG m_struNetworkCfg;
        public int dwVersion;
        public byte[] byEhomeKey = new byte[32];

    }

    public static void DevAdd(){
        g_Devs.add(new Device());
    }

    public static long GetUserID(String DeviceID){
        for(Device iDev : g_Devs)
        {
            if(CommonMethod.byteToString(iDev.m_struDevInfo.struRegInfo.byDeviceID).equals(DeviceID))
            {
                return iDev.m_lLoginID.longValue();
            }
        }
        return -1;
    }

    public static Device GetDevObj(long lUserID){
        for(Device iDev : g_Devs)
        {
            if(iDev.m_lLoginID.longValue() == lUserID)
            {
                return iDev;
            }
        }
        return null;
    }
}
