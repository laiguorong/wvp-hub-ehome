package com.wvp.hk.module.RemoteManage;

import com.wvp.hk.module.common.CommonClass.IntPointer;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import com.wvp.hk.module.service.cms.OnlineDevManager;
import com.sun.jna.NativeLong;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceConfig {
	//private final static int LOCALCONFIG = 1;
	private final static int SYSTEMCONFIG = 2;
	private final static int VIDEOCONFIG = 3;
	private final static int IMAGECONFIG = 4;
	private final static int ALARMCONFIG = 5;
	private final static int NETWORKCONFIG = 6;

	// 设备寄存器首先需要获取通道信息
	public static void getDevInfo(Long lUserID,String sDeviceID, StringBuffer strOutBuff) {
		JSONObject objRetParam = new JSONObject();
		NET_EHOME_CONFIG struEhomeConfig = new NET_EHOME_CONFIG();
		NET_EHOME_DEVICE_INFO struDevCfg = new NET_EHOME_DEVICE_INFO();
		struDevCfg.dwSize = struDevCfg.size();
		struDevCfg.write();
		struEhomeConfig.pOutBuf = struDevCfg.getPointer();
		struEhomeConfig.dwOutSize = struDevCfg.size();
		try {
			boolean bRet = ECMS.GetCMSInstance().NET_ECMS_GetDevConfig(lUserID.intValue(),
					ISUPCMSByJNA.NET_EHOME_GET_DEVICE_INFO, struEhomeConfig, struEhomeConfig.size());
			int dwErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();

			if (!bRet) {
				CommonMethod.logRecord("ERROR",
						"[NET_ECMS_GetDevConfig]->get Device Information failed, ErrorCode is" + dwErr);
				objRetParam.put("status", "OK");
				objRetParam.put("DeviceID", sDeviceID);
				objRetParam.put("ChannelNum", 0);
			} else {
				struDevCfg.read();
				CommonMethod.logRecord("INFO", "[NET_ECMS_GetDevConfig]->get Device Information successfully");
				OnlineDevManager.GetDevObj(lUserID).m_struDeviceInfo = struDevCfg;
				objRetParam.put("status", "OK");
				objRetParam.put("DeviceID", sDeviceID);
				objRetParam.put("ChannelNum", struDevCfg.dwChannelAmount);
				objRetParam.put("deviceType", struDevCfg.dwDevType);
				objRetParam.put("HardDiskNum", struDevCfg.dwDiskNumber);
				objRetParam.put("AlarmInNum ", struDevCfg.dwAlarmInAmount);
				objRetParam.put("dwAlarmOutNum", struDevCfg.dwAlarmOutAmount);
				objRetParam.put("sSerialNumber", CommonMethod.byteToString(struDevCfg.sSerialNumber));
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		strOutBuff.append(objRetParam);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	public static boolean getDevConfigInfo(long lUserID, int iConfigType, int iChanIndex, int iStreamType) {
		boolean bRet = false;
		NET_EHOME_CONFIG struEhomeConfig = new NET_EHOME_CONFIG();
		struEhomeConfig.pInBuf = null;
		struEhomeConfig.dwInSize = 0;

		switch (iConfigType) {
		case SYSTEMCONFIG: {
			struEhomeConfig.pCondBuf = null;
			struEhomeConfig.dwCondSize = 0;
			NET_EHOME_VERSION_INFO struVersionInfo = new NET_EHOME_VERSION_INFO();
			struVersionInfo.dwSize = struVersionInfo.size();
			struEhomeConfig.pOutBuf = struVersionInfo.getPointer();
			struVersionInfo.write();
			struEhomeConfig.dwOutSize = struVersionInfo.size();
			bRet = ECMS.GetCMSInstance().NET_ECMS_GetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_GET_VERSION_INFO, struEhomeConfig, struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR",
						"[NET_ECMS_GetDevConfig]->get Device Information failed��ErrorCode is��" + iErr);
			} else {
				struVersionInfo.read();
				OnlineDevManager.GetDevObj(lUserID).m_struVersionInfo = struVersionInfo;
				CommonMethod.logRecord("INFO", "[NET_ECMS_GetDevConfig]->get Device Information successfully");
			}
			break;
		}
		case VIDEOCONFIG: {
			NET_EHOME_COMPRESSION_COND struCompressCond = new NET_EHOME_COMPRESSION_COND();
			struCompressCond.byCompressionType = (byte) iStreamType;
			struCompressCond.dwChannelNum = iChanIndex;
			struCompressCond.dwSize = struCompressCond.size();
			struCompressCond.write();
			struEhomeConfig.pCondBuf = struCompressCond.getPointer();
			struEhomeConfig.dwCondSize = struCompressCond.dwSize;
			NET_EHOME_COMPRESSION_CFG struCompressCfg = new NET_EHOME_COMPRESSION_CFG();
			struCompressCfg.dwSize = struCompressCfg.size();
			struCompressCfg.write();
			struEhomeConfig.pOutBuf = struCompressCfg.getPointer();
			struEhomeConfig.dwOutSize = struCompressCfg.size();
			bRet = ECMS.GetCMSInstance().NET_ECMS_GetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_GET_COMPRESSION_CFG, struEhomeConfig, struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR", "[NET_ECMS_GetDevConfig]->get Device Stream Information failed��ErrorCode is��" + iErr);
			} else {
				struCompressCfg.read();
				OnlineDevManager.GetDevObj(lUserID).m_struCompressionInfo = struCompressCfg;
				CommonMethod.logRecord("INFO", "[NET_ECMS_GetDevConfig]->get Device Stream Information successfully");
			}
			break;
		}
		case IMAGECONFIG: {
			IntPointer pChanNum = new IntPointer(iChanIndex);
			struEhomeConfig.dwCondSize = pChanNum.size();
			pChanNum.write();
			struEhomeConfig.pCondBuf = pChanNum.getPointer();
			NET_EHOME_PIC_CFG struPicCfg = new NET_EHOME_PIC_CFG();
			struPicCfg.dwSize = struPicCfg.size();
			struPicCfg.write();
			struEhomeConfig.pOutBuf = struPicCfg.getPointer();
			struEhomeConfig.dwOutSize = struPicCfg.size();
			bRet = ECMS.GetCMSInstance().NET_ECMS_GetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_GET_PIC_CFG, struEhomeConfig, struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR", "[NET_ECMS_GetDevConfig]->get Device Display Information failed��ErrorCode is��" + iErr);
			} else {
				struPicCfg.read();
				pChanNum.read();
				OnlineDevManager.GetDevObj(lUserID).m_struPicCfg = struPicCfg;
				CommonMethod.logRecord("INFO", "[NET_ECMS_GetDevConfig]->get Device Channel Information successfully");
			}
			break;
		}
		case NETWORKCONFIG: {
			struEhomeConfig.pCondBuf = null;
			struEhomeConfig.dwCondSize = 0;
			NET_EHOME_NETWORK_CFG struNetworkInfo = new NET_EHOME_NETWORK_CFG();
			struNetworkInfo.dwSize = struNetworkInfo.size();
			struNetworkInfo.write();
			struEhomeConfig.pOutBuf = struNetworkInfo.getPointer();
			struEhomeConfig.dwOutSize = struNetworkInfo.size();
			bRet = ECMS.GetCMSInstance().NET_ECMS_GetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_GET_NETWORK_CFG, struEhomeConfig, struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR", "[NET_ECMS_GetDevConfig]->get Device network Information failed��ErrorCode is��" + iErr);
			} else {
				struNetworkInfo.read();
				OnlineDevManager.GetDevObj(lUserID).m_struNetworkCfg = struNetworkInfo;
				CommonMethod.logRecord("INFO", "[NET_ECMS_GetDevConfig]->get Device network Information successfully");
			}
		}
		case ALARMCONFIG: {

			break;
		}
		default:
			break;
		}
		return bRet;
	}

	public static boolean setDevConfigInfo(long lUserID, int iConfigType, JSONObject jsonInParam) {
		boolean bRet = false;
		NET_EHOME_CONFIG struEhomeConfig = new NET_EHOME_CONFIG();
		struEhomeConfig.pOutBuf = null;

		switch (iConfigType) {
		case VIDEOCONFIG: {
			NET_EHOME_COMPRESSION_COND struCompressCond = new NET_EHOME_COMPRESSION_COND();
			NET_EHOME_COMPRESSION_CFG struCompressCfg = new NET_EHOME_COMPRESSION_CFG();
			try {
				struCompressCond.byCompressionType = (byte)jsonInParam.getInt("inStreamVersion");
				struCompressCond.dwChannelNum = jsonInParam.getInt("iChannelIndex");
				struCompressCfg.byBitRateType = (byte)jsonInParam.getInt("inBitRateType");
				struCompressCfg.byIntervalBPFrame = (byte)jsonInParam.getInt("inFrameIntervalType");
				struCompressCfg.byPicQuality = (byte)jsonInParam.getInt("inPicQuality");
				struCompressCfg.byStreamType = (byte)jsonInParam.getInt("inStreamType");
				struCompressCfg.dwResolution = jsonInParam.getInt("inResolution");
				struCompressCfg.dwVideoBitRate = jsonInParam.getInt("inVideoBitRate");
				struCompressCfg.dwVideoFrameRate = jsonInParam.getInt("inVideoFPS");
				struCompressCfg.wIntervalFrameI = (short)jsonInParam.getInt("inIFrameInterval");
			} catch (Exception e) {
				e.printStackTrace();
			}
			struCompressCond.dwSize = struCompressCond.size();
			struCompressCond.write();
			struEhomeConfig.pCondBuf = struCompressCond.getPointer();
			struEhomeConfig.dwCondSize = struCompressCond.dwSize;
			struCompressCfg.dwSize = struCompressCfg.size();
			struCompressCfg.write();
			struEhomeConfig.pOutBuf = struCompressCfg.getPointer();
			struEhomeConfig.dwOutSize = struCompressCfg.size();
			bRet = ECMS.GetCMSInstance().NET_ECMS_GetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_SET_COMPRESSION_CFG, struEhomeConfig, struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR", "[NET_ECMS_SetDevConfig]->config Compress information failed��errorCode is��" + iErr);
			} else {
				struCompressCfg.read();
				OnlineDevManager.GetDevObj(lUserID).m_struCompressionInfo = struCompressCfg;
				CommonMethod.logRecord("INFO", "[NET_ECMS_SetDevConfig]->config Compress information successfully");
			}
			break;
		}
		case IMAGECONFIG: {
			IntPointer pChanNum = new IntPointer();
			NET_EHOME_PIC_CFG struPicCfg = new NET_EHOME_PIC_CFG();
			try {
				pChanNum.dwData = jsonInParam.getInt("iChannNum");
				struPicCfg.bIsShowChanName = (byte) (jsonInParam.getBoolean("bIsShowChanName") ? 1 : 0);
				struPicCfg.bIsShowOSD = (byte) (jsonInParam.getBoolean("bIsShowOSD") ? 1 : 0);
				struPicCfg.bIsShowWeek = (byte) (jsonInParam.getBoolean("bIsShowWeek") ? 1 : 0);
				String sChannName = jsonInParam.getString("byChannelName");
				System.arraycopy(sChannName.getBytes(), 0, struPicCfg.byChannelName, 0, sChannName.length());
				struPicCfg.byOSDAtrib = (byte) (jsonInParam.getInt("byOSDAtrib"));
				struPicCfg.byOSDType = (byte) (jsonInParam.getInt("byOSDType"));
				struPicCfg.wChanNameXPos = (short) jsonInParam.getInt("wChanNameXPos");
				struPicCfg.wChanNameYPos = (short) jsonInParam.getInt("wChanNameYPos");
				struPicCfg.wOSDXPos = (short) jsonInParam.getInt("wOSDXPos");
				struPicCfg.wOSDYPos = (short) jsonInParam.getInt("wOSDYPos");
			} catch (Exception e) {
				e.printStackTrace();
			}
			pChanNum.write();
			struEhomeConfig.pCondBuf = pChanNum.getPointer();
			struEhomeConfig.dwCondSize = 4;
			struPicCfg.dwSize = struPicCfg.size();
			struPicCfg.write();
			struEhomeConfig.dwInSize = struPicCfg.size();
			struEhomeConfig.pInBuf = struPicCfg.getPointer();
			struEhomeConfig.dwOutSize = 0;
			struEhomeConfig.write();
			bRet = ECMS.GetCMSInstance().NET_ECMS_SetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_SET_PIC_CFG, struEhomeConfig.getPointer(), struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR", "[NET_ECMS_SetDevConfig]->config display information failed��errorCode is��" + iErr);
			} else {
				struEhomeConfig.read();
				struPicCfg.read();
				OnlineDevManager.GetDevObj(lUserID).m_struPicCfg = struPicCfg;
				CommonMethod.logRecord("INFO", "[NET_ECMS_SetDevConfig]->config display information successfully");
			}
			break;
		}
		case NETWORKCONFIG:{
			NET_EHOME_NETWORK_CFG struNetworkInfo = new NET_EHOME_NETWORK_CFG();
			try {
				struNetworkInfo.struEtherNet.struDevIP.sIpV4 = jsonInParam.getString("DevAddress").getBytes();
				struNetworkInfo.struEtherNet.struDevIPMask.sIpV4 = jsonInParam.getString("DevIPMask").getBytes();
				struNetworkInfo.struEtherNet.wDevPort = (short)jsonInParam.getInt("SDKPort");
				struNetworkInfo.struEtherNet.byMACAddr = jsonInParam.getString("MACAddress").getBytes();
				struNetworkInfo.struEtherNet.dwNetInterface = jsonInParam.getInt("EthType");
				struNetworkInfo.struEtherNet.wMTU = (short)jsonInParam.getInt("MTUParam");
				struNetworkInfo.struGateWayIP.sIpV4 = jsonInParam.getString("GatewayAddr").getBytes();
				struNetworkInfo.struMultiCastIP.sIpV4 = jsonInParam.getString("MultiCastAddr").getBytes();
				struNetworkInfo.struDDNSServer1IP.sIpV4 = jsonInParam.getString("DDNS1Server").getBytes();
				struNetworkInfo.struDDNSServer2IP.sIpV4 = jsonInParam.getString("DDNS2Server").getBytes();
				struNetworkInfo.struAlarmHostIP.sIpV4 = jsonInParam.getString("AlarmHost").getBytes();
				struNetworkInfo.wAlarmHostPort = (short)jsonInParam.getInt("AlarmHostPort");
				struNetworkInfo.struIPResolver.sIpV4 = jsonInParam.getString("ParseServer").getBytes();
				struNetworkInfo.wIPResolverPort = (short)jsonInParam.getInt("ParseServerPort");
				struNetworkInfo.wHTTPPort = (short)jsonInParam.getInt("HttpPort");
				struNetworkInfo.struPPPoE.dwPPPoE = jsonInParam.getInt("EnablePPPoE");
				struNetworkInfo.struPPPoE.sPPPoEUser = jsonInParam.getString("PPPoEUserName").getBytes();
				struNetworkInfo.struPPPoE.sPPPoEPassword = jsonInParam.getString("PPPoEPwd").getBytes();
				struNetworkInfo.struPPPoE.struPPPoEIP.sIpV4 = jsonInParam.getString("PPPoEAddress").getBytes();
			} catch (Exception e) {
				e.printStackTrace();
			}
			struEhomeConfig.pCondBuf = null;
			struNetworkInfo.dwSize = struNetworkInfo.size();
			struNetworkInfo.write();
			struEhomeConfig.dwInSize = struNetworkInfo.size();
			struEhomeConfig.pInBuf = struNetworkInfo.getPointer();
			struEhomeConfig.dwOutSize = 0;
			struEhomeConfig.write();
			bRet = ECMS.GetCMSInstance().NET_ECMS_SetDevConfig((int) lUserID,
					ISUPCMSByJNA.NET_EHOME_SET_NETWORK_CFG, struEhomeConfig.getPointer(), struEhomeConfig.size());
			if (!bRet) // failed
			{
				int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
				CommonMethod.logRecord("ERR", "[NET_ECMS_SetDevConfig]->config network information failed��errorCode is��" + iErr);
			} else {
				struEhomeConfig.read();
				struNetworkInfo.read();
				OnlineDevManager.GetDevObj(lUserID).m_struNetworkCfg = struNetworkInfo;
				CommonMethod.logRecord("INFO", "[NET_ECMS_SetDevConfig]->config network information successfully");
			}
		}
		default:
			break;
		}
		return bRet;
	}
}
