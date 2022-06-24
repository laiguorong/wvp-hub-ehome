package com.wvp.hk.module.service.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wvp.common.utils.StringUtils;
import com.wvp.domain.WvpGpsInfo;
import com.wvp.hk.module.communication.WebSocketService;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.alarm.ISUPAMSByJNA.*;
import com.wvp.hk.module.common.CommonClass.StringPointer;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.mapper.WvpGpsInfoMapper;
import com.wvp.service.IWvpGpsInfoService;
import com.wvp.service.impl.WvpGpsInfoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class EAlarm {

	//AMS报警服务结构体
	private static ISUPAMSByJNA m_AMSInstance = null;
	//AMS服务运行状态
	private boolean m_bIsRunning = false;
	//AMS报警回调函数
	private static ISUPAMSByJNA.EHomeMsgCallBack m_fEHomeMsgCallBack = null;
	public int m_lAlarmListenHandle;

	@Autowired
	private IWvpGpsInfoService wvpGpsInfoService;


	@Value("${ehome.pu-ip}")
	private String ehomePuIp;

	@Value("${ehome.in-ip}")
	private String ehomeInIp;

	@Value("${ehome.ams-prot}")
	private short ehomeAmsProt;



	//日志文件
	protected static final Logger logger = LoggerFactory.getLogger(EAlarm.class);

	//报警服务初始化
	@PostConstruct
	public void EAlarmInit() {
		if(m_AMSInstance == null)
		{
			if(!CreateAMSInstance())
			{
				logger.error("Load AMS fail");
				return;
			}
		}

		m_lAlarmListenHandle = -1;

		if(m_AMSInstance.NET_EALARM_Init())
		{
			logger.info("[NET_EALARM_Init]->EHomeAMS initialize successfully");
			//Open alarm module log
			m_AMSInstance.NET_EALARM_SetLogToFile(3, "./EHomeSdkLog", true);

			String sLibComPath = "";
			if(CommonMethod.isLinux())
			{
				sLibComPath = "/home/hik/LinuxSDK/HCAapSDKCom/";
				StringPointer strPointer = new StringPointer(sLibComPath);
				strPointer.write();
				if(m_AMSInstance.NET_EALARM_SetSDKLocalCfg(5, strPointer.getPointer())) {
					logger.info("[Alarm]ComPath Load Successfully:" + sLibComPath);
				}else {
					System.out.println("[Alarm]ComPath Load Failed!:" + sLibComPath +
							", errorCode: " + m_AMSInstance.NET_EALARM_GetLastError());
					logger.info("[Alarm]ComPath Load Successfully:" + sLibComPath);
				}
				strPointer.read();
			}

			if (m_fEHomeMsgCallBack == null) {
				m_fEHomeMsgCallBack = new FEHomeMsgCallBack();
			}

			NET_EHOME_ALARM_LISTEN_PARAM struAlarmListenParam = new NET_EHOME_ALARM_LISTEN_PARAM();
			byte[] byIP = ehomeInIp.getBytes();
			System.arraycopy(byIP, 0, struAlarmListenParam.struAddress.szIP, 0, byIP.length);

			struAlarmListenParam.struAddress.wPort = ehomeAmsProt;  //和注册回调函数下发给设备的端口需要一致
			struAlarmListenParam.pUserData= null;
			struAlarmListenParam.fnMsgCb = m_fEHomeMsgCallBack;
			struAlarmListenParam.byProtocolType = 2; //协议类型：0- TCP，1- UDP，2- MQTT
			struAlarmListenParam.byUseCmsPort = 0; //是否复用CMS端口：0- 不复用，非0- 复用
			m_lAlarmListenHandle = m_AMSInstance.NET_EALARM_StartListen(struAlarmListenParam);
			if (m_lAlarmListenHandle < 0) {
				logger.error("[NET_EALARM_StartListen]->AMS start listen failed,errorCode is " +
						m_AMSInstance.NET_EALARM_GetLastError());
			} else {
				m_bIsRunning = true;
				logger.info("[NET_EALARM_StartListen]->AMS start listen successfully->" +
						"listenProctol: " + (int)struAlarmListenParam.byProtocolType + "\t" +
						"listenAddress: " + CommonMethod.byteToString(struAlarmListenParam.struAddress.szIP) + "\t" +
						"listenPort: "    + struAlarmListenParam.struAddress.wPort);
			}


		}else{
			logger.error("[NET_EALARM_Init]->EALARM initialize Failed, errorCode:" +
					m_AMSInstance.NET_EALARM_GetLastError());
		}
	}

	//停止报警监听
	public boolean StopListen() {

		boolean bIsStopAlarmListen = m_AMSInstance.NET_EALARM_StopListen(m_lAlarmListenHandle);
		if (bIsStopAlarmListen) {
			m_bIsRunning = false;
			logger.info("[NET_EALARM_StopListen]->AMS stop listen successfully");
			m_lAlarmListenHandle=-1;
		} else {
			logger.error("[NET_EALARM_StopListen]->AMS stop listen failed, errorCode is" +
					m_AMSInstance.NET_EALARM_GetLastError());
		}

		return bIsStopAlarmListen;
	}

	//停止报警服务
	public void StopElarm() {
		StopListen();

		m_AMSInstance.NET_EALARM_Fini();

		logger.info("[NET_EALARM_Fini]->EHomeAMS release resource successfully");
	}

	//报警消息回调
	private class FEHomeMsgCallBack implements EHomeMsgCallBack {
		@Override
		public boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser) {
			StringBuffer strOutBuff = new StringBuffer();
			com.alibaba.fastjson.JSONObject objAlarmInfo = new com.alibaba.fastjson.JSONObject();

			//Only the ISAPI alarm will have httpUrl
			if (pAlarmMsg.pHttpUrl != null) {
				pAlarmMsg.dwAlarmType = ISUPAMSByJNA.EHOME_ISAPI_ALARM;
			}

			try {
				switch (pAlarmMsg.dwAlarmType) {
					// 未知报警信息
					case ISUPAMSByJNA.EHOME_ALARM_UNKNOWN :
					{
						objAlarmInfo.put("AlarmType", "UNKNOWN");
						break;
					}
					// 基本报警信息：移动侦测、视频遮盖、视频丢失、PIR 报警、人脸侦测、区域入侵等
					case ISUPAMSByJNA.EHOME_ALARM :
					{
						objAlarmInfo.put("AlarmType", "DEVICE_STATUS_REPORT");
						ProcessEhomeXMLAlarm(pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 热度图报告上传
					case ISUPAMSByJNA.EHOME_ALARM_HEATMAP_REPORT :
					{
						objAlarmInfo.put("AlarmType", "HEATMAP_REPORT");
						ProcessEhomeHeatMapReport(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, strOutBuff);
						break;
					}
					// 人脸抓拍报告上传
					case ISUPAMSByJNA.EHOME_ALARM_FACESNAP_REPORT :
					{
						objAlarmInfo.put("AlarmType", "FACESNAP_REPORT");
						ProcessEhomeFaceSnapReport(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, strOutBuff);
						break;
					}
					// GPS 信息上传
					case ISUPAMSByJNA.EHOME_ALARM_GPS :
					{
						objAlarmInfo.put("AlarmType", "GPS");
						ProcessEhomeXMLAlarm(pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 报警主机 CID 报警信息
					case ISUPAMSByJNA.EHOME_ALARM_CID_REPORT :
					{
						objAlarmInfo.put("AlarmType", "CID_REPORT");
						ProcessEhomeCid(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 图片 URL 报警
					case ISUPAMSByJNA.EHOME_ALARM_NOTICE_PICURL : {
						objAlarmInfo.put("AlarmType", "PictureURL_REPORT");
						ProcessEhomeNoticePicUrl(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 异步失败通知报警
					case ISUPAMSByJNA.EHOME_ALARM_NOTIFY_FAIL : {
						objAlarmInfo.put("AlarmType", "AsyncFail_REPORT");
						ProcessEhomeNotifyFail(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 门禁事件报警
					case ISUPAMSByJNA.EHOME_ALARM_ACS : {
						objAlarmInfo.put("AlarmType", "ACS_Event_REPORT");
						ProcessEhomeXMLAlarm(pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 无线网络报警
					case ISUPAMSByJNA.EHOME_ALARM_WIRELESS_INFO : {
						objAlarmInfo.put("AlarmType", "WIRELESS_INFO_REPORT");
						ProcessEhomeWirelessInfo(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen, strOutBuff);
						break;
					}
					// 通过 HTTP 上传的报警
					case ISUPAMSByJNA.EHOME_ISAPI_ALARM : {
						objAlarmInfo.put("AlarmType", "ISAPI_REPORT");
						ProcessHttpAlarmInfo(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.pHttpUrl, pAlarmMsg.dwHttpUrlLen, strOutBuff);
						break;
					}
					//车载客流统计报警
					case ISUPAMSByJNA.EHOME_ALARM_MPDCDATA : {
						objAlarmInfo.put("AlarmType", "MPDC_DATA_REPORT");
						ProcessEhomeMPDCData(pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.pHttpUrl, pAlarmMsg.dwHttpUrlLen, strOutBuff);
						break;
					}
					default :
						break;
				}

				//Package the alarm content and send it to the front end via Websocket
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
				String sDateTimeNow = df.format(new Date());
				String sDevSerial = CommonMethod.byteToString(pAlarmMsg.sSerialNumber);
				objAlarmInfo.put("DevNumber", sDevSerial);
				objAlarmInfo.put("DateTime", sDateTimeNow);
				objAlarmInfo.put("AlarmContent", strOutBuff.toString());
				System.out.println(strOutBuff.toString());
				if(getJSONType(strOutBuff.toString())){
					com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(strOutBuff.toString());

					if("GPSUpload".equals(jsonObject.getString("eventType"))){

						com.alibaba.fastjson.JSONObject gpsJsonObject=jsonObject.getJSONObject("GPS");
						String longitude=gpsJsonObject.getString("longitude");
						String latitude=gpsJsonObject.getString("latitude");
						logger.info("收到GPS报警上报信息，设备序列号："+jsonObject.getString("deviceID")+"，longitude："+longitude+",latitude:"+latitude);
						if(!"0".equals(longitude)||!"0".equals(latitude)){
							WvpGpsInfo wvpGpsInfo=new WvpGpsInfo();
							wvpGpsInfo.setDeviceId(jsonObject.getString("deviceID"));
							wvpGpsInfo.setLongitude(gpsConvert(Long.valueOf(longitude)));
							wvpGpsInfo.setLatitude(gpsConvert(Long.valueOf(latitude)));
							wvpGpsInfo.setCreateTime(new Date());
							wvpGpsInfoService.insertWvpGpsInfo(wvpGpsInfo);
						}
					}
				}


			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return true;
		}


		public boolean getJSONType(String str) {
			boolean result = false;
			if (StringUtils.isNotBlank(str)) {
				str = str.trim();
				if (str.startsWith("{") && str.endsWith("}")) {
					result = true;
				} else if (str.startsWith("[") && str.endsWith("]")) {
					result = true;
				}
			}
			return result;
		}


		public String gpsConvert(long data){
			long lon_d = data / (3600 * 100);

			long lon_f = (data - (lon_d * 3600 * 100)) / (60 * 100);

			double lon_m = (double)(data - (lon_d * 3600 * 100) - (lon_f * 60 *100)) / 100;

			double r = lon_d + lon_f / 60.0 + lon_m / 3600.0;
			return r+"";
		}
	}

	private static void ProcessEhomeXMLAlarm(Pointer pXml, int dwXmlLen, StringBuffer strOutBuffer) {
		try{
			//1. Get the class name of the alarm content
			String sClassName = NET_EHOME_XML_DATA.class.getName();
			//2. Pull the alarm message in Pointer into this class through reflection
			NET_EHOME_XML_DATA struXMLData = (NET_EHOME_XML_DATA) CommonMethod.WritePointerDataToClass(pXml, sClassName);

			String strXML = new String(struXMLData.byXMLData, 0, dwXmlLen);
			strOutBuffer.append(strXML);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private static void ProcessEhomeHeatMapReport(Pointer pStru, int dwStruLen, StringBuffer strOutBuffer) {
		try{
			//Same as above（Comment）
			String sClassName = NET_EHOME_HEATMAP_REPORT.class.getName();
			NET_EHOME_HEATMAP_REPORT struHeatMapData = (NET_EHOME_HEATMAP_REPORT) CommonMethod.WritePointerDataToClass(pStru, sClassName);

			String strData = "[HEATMAPREPORT]\nDeviceID: "+ CommonMethod.byteToString(struHeatMapData.byDeviceID) +
					"\nChannel: "+ struHeatMapData.dwVideoChannel +"\nStartTime: "+ CommonMethod.byteToString(struHeatMapData.byStartTime) +
					"\nStopTime: "+ CommonMethod.byteToString(struHeatMapData.byStopTime) +"\nHeatMapValue: "+ struHeatMapData.struHeatmapValue.dwMaxHeatMapValue +
					"  "+ struHeatMapData.struHeatmapValue.dwMinHeatMapValue +"  "+ struHeatMapData.struHeatmapValue.dwTimeHeatMapValue +
					"\nSize: "+ struHeatMapData.struPixelArraySize.dwLineValue +"  "+ struHeatMapData.struPixelArraySize.dwColumnValue +"\n";
			strOutBuffer.append(strData);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private static void ProcessEhomeFaceSnapReport(Pointer pStru, int dwStruLen, StringBuffer strOutBuffer) {
		com.alibaba.fastjson.JSONObject jsonFaceSnapReport = new com.alibaba.fastjson.JSONObject();
		try {
			String sClassName = NET_EHOME_FACESNAP_REPORT.class.getName();
			NET_EHOME_FACESNAP_REPORT struFaceSnapReport = (NET_EHOME_FACESNAP_REPORT) CommonMethod.WritePointerDataToClass(pStru, sClassName);

			com.alibaba.fastjson.JSONObject jsonInParam = new com.alibaba.fastjson.JSONObject();
			jsonInParam.put("DeviceID", CommonMethod.byteToString(struFaceSnapReport.byDeviceID));
			jsonInParam.put("Channel", struFaceSnapReport.dwVideoChannel);
			jsonInParam.put("Time",    CommonMethod.byteToString(struFaceSnapReport.byAlarmTime));
			jsonInParam.put("PicID",   struFaceSnapReport.dwFacePicID);
			jsonInParam.put("Score",   struFaceSnapReport.dwFaceScore);
			jsonInParam.put("TargetID", struFaceSnapReport.dwTargetID);

			com.alibaba.fastjson.JSONObject jsonTargetZone = new com.alibaba.fastjson.JSONObject();
			jsonTargetZone.put("x",      struFaceSnapReport.struTarketZone.dwX);
			jsonTargetZone.put("y",      struFaceSnapReport.struTarketZone.dwY);
			jsonTargetZone.put("width",  struFaceSnapReport.struTarketZone.dwWidth);
			jsonTargetZone.put("height", struFaceSnapReport.struTarketZone.dwHeight);
			jsonInParam.put("TargetZone", jsonTargetZone);

			com.alibaba.fastjson.JSONObject jsonFacePictureZone = new com.alibaba.fastjson.JSONObject();
			jsonFacePictureZone.put("x",      struFaceSnapReport.struFacePicZone.dwX);
			jsonFacePictureZone.put("y",      struFaceSnapReport.struFacePicZone.dwY);
			jsonFacePictureZone.put("width",  struFaceSnapReport.struFacePicZone.dwWidth);
			jsonFacePictureZone.put("height", struFaceSnapReport.struFacePicZone.dwHeight);
			jsonInParam.put("FacePictureZone", jsonFacePictureZone);

			com.alibaba.fastjson.JSONObject jsonHumanFeature = new com.alibaba.fastjson.JSONObject();
			jsonHumanFeature.put("AgeGroup",      struFaceSnapReport.struHumanFeature.byAgeGroup);
			jsonHumanFeature.put("Sex",      struFaceSnapReport.struHumanFeature.bySex);
			jsonHumanFeature.put("IsEyeGlass",  struFaceSnapReport.struHumanFeature.byEyeGlass);
			jsonHumanFeature.put("IsMask", struFaceSnapReport.struHumanFeature.byMask);
			jsonInParam.put("HumanFeature", jsonHumanFeature);

			jsonInParam.put("Duration", struFaceSnapReport.dwStayDuration);
			jsonInParam.put("FacePicLen", struFaceSnapReport.dwFacePicLen);
			jsonInParam.put("FacePicUrl", CommonMethod.byteToString(struFaceSnapReport.byFacePicUrl));
			jsonInParam.put("BackGroundPicLen", struFaceSnapReport.dwBackgroudPicLen);
			jsonInParam.put("BackGroundPicUrl", CommonMethod.byteToString(struFaceSnapReport.byBackgroudPicUrl));

			jsonFaceSnapReport.put("FaceSnapReport", jsonInParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		strOutBuffer.append(jsonFaceSnapReport.toString());
	}

	private static void ProcessEhomeCid(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen, StringBuffer strOutBuffer) {

	}

	private static void ProcessEhomeNoticePicUrl(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen, StringBuffer strOutBuffer) {
		com.alibaba.fastjson.JSONObject jsonNoticePicReport = new com.alibaba.fastjson.JSONObject();
		try{
			String sClassName = NET_EHOME_NOTICE_PICURL.class.getName();
			NET_EHOME_NOTICE_PICURL struNoticePicReport = (NET_EHOME_NOTICE_PICURL) CommonMethod.WritePointerDataToClass(pStru, sClassName);

			com.alibaba.fastjson.JSONObject jsonInParam = new JSONObject();
			jsonInParam.put("DeviceID", CommonMethod.byteToString(struNoticePicReport.byDeviceID));
			jsonInParam.put("PictureType", struNoticePicReport.wPicType);
			jsonInParam.put("Time",    CommonMethod.byteToString(struNoticePicReport.byAlarmTime));
			jsonInParam.put("AlarmType",   struNoticePicReport.wAlarmType);
			jsonInParam.put("AlarmChan",   struNoticePicReport.dwAlarmChan);
			jsonInParam.put("CaptureChan", struNoticePicReport.dwCaptureChan);
			jsonInParam.put("PicTime",    CommonMethod.byteToString(struNoticePicReport.byPicTime));
			jsonInParam.put("PicUrl",    CommonMethod.byteToString(struNoticePicReport.byPicUrl));
			jsonInParam.put("ManualSnapSeq", struNoticePicReport.dwManualSnapSeq);
			jsonNoticePicReport.put("NoticePicReport", jsonInParam);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		strOutBuffer.append(jsonNoticePicReport.toString());
	}

	private static void ProcessEhomeNotifyFail(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen, StringBuffer strOutBuffer) {

	}

	private static void ProcessEhomeWirelessInfo(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen, StringBuffer strOutBuffer) {

	}

	private static void ProcessEhomeMPDCData(Pointer pStru, int dwStruLen, Pointer pUrl, int dwUrlLen, StringBuffer strOutBuffer) {

	}

	private static void ProcessHttpAlarmInfo(Pointer pStru, int dwStruLen, Pointer pUrl, int dwUrlLen, StringBuffer strOutBuffer) {
		if(pStru == null) {
			return;
		}

		try{
			String sClassName = NET_EHOME_ALARM_ISAPI_INFO.class.getName();
			NET_EHOME_ALARM_ISAPI_INFO struISAPIStru = (NET_EHOME_ALARM_ISAPI_INFO) CommonMethod.WritePointerDataToClass(pStru, sClassName);
			strOutBuffer.append(struISAPIStru.pAlarmData);

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private boolean CreateAMSInstance()
	{
		if(m_AMSInstance == null)
		{
			synchronized (ISUPAMSByJNA.class)
			{
				String strDllPath = "";
				try
				{
					//System.setProperty("jna.debug_load", "true");
					if(CommonMethod.isWindows()) {
						strDllPath = System.getProperty("user.dir") + "\\lib\\HCISUPAlarm";
					}
					else if(CommonMethod.isLinux()) {
						strDllPath = "/home/hik/LinuxSDK/libHCISUPAlarm.so";
					}
					logger.info("[EAlarm]->"+strDllPath);
					m_AMSInstance = (ISUPAMSByJNA) Native.loadLibrary(strDllPath, ISUPAMSByJNA.class);

				}catch (Exception ex) {
					System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
					return false;
				}
			}
		}
		return true;
	}

	public static ISUPAMSByJNA GetAlarmInstance()
	{
		return m_AMSInstance;
	}

	public boolean IsRunning(){return m_bIsRunning;}
}
