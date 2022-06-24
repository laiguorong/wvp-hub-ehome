package com.wvp.hk.module.service.cms;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sun.jna.*;
import com.wvp.domain.WvpDevice;
import com.wvp.hk.module.common.CommonClass;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.communication.WebSocketService;

import com.wvp.hk.module.common.CommonClass.BYTE_ARRAY;

import com.wvp.hk.module.service.alarm.EAlarm;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.NET_EHOME_CMS_LISTEN_PARAM;

import com.wvp.hk.module.service.storage.ESS;
import com.wvp.hk.module.service.stream.Preview;
import com.wvp.service.IWvpDeviceService;
import com.wvp.websocket.WebSocketUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ECMS {

	@Autowired
	private IWvpDeviceService wvpDeviceService;

	private boolean m_bIsRunning = false;
	private static ISUPCMSByJNA m_CMSInstance = null;


	//设备注册回调函数
	private static FRegisterCallBack m_fRegisterCallBack = null;

	//CMS 监听参数
	public static NET_EHOME_CMS_LISTEN_PARAM m_struCMSListenPara = new NET_EHOME_CMS_LISTEN_PARAM();

	//CMS 监听状态
	private int m_lCMSListenHandle;

	//日志文件
	protected static final Logger logger = LoggerFactory.getLogger(ECMS.class);

	@Value("${ehome.pu-ip}")
	private String ehomePuIp;

	@Value("${ehome.in-ip}")
	private String ehomeInIp;

	@Value("${ehome.cms-prot}")
	private short ehomeCmsProt;

	@Value("${ehome.ams-prot}")
	private short ehomeAmsProt;

	@Value("${ehome.secret-key}")
	private String secretKey;

	//初始化CMS服务 开启注册监听
	@PostConstruct
	public void ECMSInit() {

		//服务启动时将数据库中的所有设备初始化
		wvpDeviceService.updateAllDeviceInit();
		if(m_CMSInstance == null) {
			if(!CreateCMSInstance())
			{
				logger.error("Load CMS module fail");
				return;
			}
		}

		BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
		String strPathCrypto = null;
		if(CommonMethod.isLinux()){
			strPathCrypto = "/home/hik/LinuxSDK/libcrypto.so";
		}else{
			strPathCrypto = System.getProperty("user.dir")+"\\lib\\libeay32.dll";
		}
		//Linux版本是libcrypto.so库文件的路径
		System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
		ptrByteArrayCrypto.write();
		m_CMSInstance.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());
		System.out.println("初始化库文件路径：" + strPathCrypto);

		BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
		String strPathSsl = null;	//Linux版本是libssl.so库文件的路径

		if(CommonMethod.isLinux()){
			strPathSsl = "/home/hik/LinuxSDK/libssl.so";
		}else{
			strPathSsl = System.getProperty("user.dir")+"\\lib\\ssleay32.dll";
		}
		System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
		ptrByteArraySsl.write();
		m_CMSInstance.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
		System.out.println("初始化库文件路径：" + strPathSsl);

		//CMS服务器相关配置
		{
			CommonMethod.GetListenInfo().struCMSListenParam.struAddress.szIP=ehomeInIp.getBytes();
			CommonMethod.GetListenInfo().struCMSListenParam.struAddress.wPort=ehomeCmsProt;
		}

		//报警服务器相关配置
		{
			CommonMethod.GetListenInfo().struAMSListenParam.struAddress.szIP=ehomeInIp.getBytes();
			CommonMethod.GetListenInfo().struAMSListenParam.struAddress.wPort=ehomeAmsProt;
			CommonMethod.GetServerInfo().dwAlarmServerType=2;
		}


		if(!m_CMSInstance.NET_ECMS_Init()) {
			logger.error("[NET_ECMS_Init]->EHomeCMS initiate failed, errCode: " + m_CMSInstance.NET_ECMS_GetLastError());
		}else{
			logger.info("[NET_ECMS_Init]->EHomeCMS initiate Successfully!");
			// open sdk log
			m_CMSInstance.NET_ECMS_SetLogToFile(3, "./EHomeSdkLog", true);

			CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struCMSListenParam.struAddress.szIP, m_struCMSListenPara.struAddress.szIP);
			m_struCMSListenPara.struAddress.wPort = CommonMethod.GetListenInfo().struCMSListenParam.struAddress.wPort;
			m_lCMSListenHandle = -1;

			if (m_fRegisterCallBack == null) {
				m_fRegisterCallBack = new FRegisterCallBack();
				m_struCMSListenPara.fnCB = m_fRegisterCallBack;
			}

			//由于Linux环境中的动态库路径是默认路径（/usr/lib），因此建议
			//在此处配置组件库路径
			if (CommonMethod.isLinux()) {
				String sLibComPath = "/home/hik/LinuxSDK/HCAapSDKCom/";
				CommonClass.StringPointer strPointer = new CommonClass.StringPointer(sLibComPath);
				strPointer.write();
				if (m_CMSInstance.NET_ECMS_SetSDKLocalCfg(5, strPointer.getPointer())) {
					logger.info("CMS ComPath:"+ sLibComPath);
				}else {
					logger.info("CMS ComPath:"+ sLibComPath);
				}
				strPointer.read();
			}

			//配置设备访问安全级别
			//“byAccessSecurity=0”表示可以访问所有协议版本的设备
			CommonClass.NET_EHOME_LOCAL_ACCESS_SECURITY struAccessSecure = new CommonClass.NET_EHOME_LOCAL_ACCESS_SECURITY();
			struAccessSecure.byAccessSecurity = 0;
			struAccessSecure.dwSize = struAccessSecure.size();
			struAccessSecure.write();
			m_CMSInstance.NET_ECMS_SetSDKLocalCfg(0, struAccessSecure.getPointer());

			m_lCMSListenHandle = m_CMSInstance.NET_ECMS_StartListen(m_struCMSListenPara);
			if (m_lCMSListenHandle == -1) {
				m_CMSInstance.NET_ECMS_StopListen(m_lCMSListenHandle);
				logger.error("[NET_ECMS_StartListen]->CMS start listen failed, errorCode is"
						+ m_CMSInstance.NET_ECMS_GetLastError());
			} else {
				m_bIsRunning = true;
				logger.info("[NET_ECMS_StartListen]->CMS start listen Successfully");
			}
		}
	}

	//关闭CMS 注册监听
	public void StopEcms() {
		if (m_lCMSListenHandle >= 0)
		{
			boolean bStopListen = m_CMSInstance.NET_ECMS_StopListen(m_lCMSListenHandle);
			if (!bStopListen) {
				logger.error("[NET_ECMS_StopListen]->CMS stop listen failed, errorCode is:"
						+ m_CMSInstance.NET_ECMS_GetLastError());
			} else {
				m_bIsRunning = false;
				logger.info("[NET_ECMS_StopListen]->CMS stop listen successfully");
			}
		}
		m_CMSInstance.NET_ECMS_Fini();

	}

	//EhomeKey是设备和CMS之间进行相互身份验证的工具，仅当设备协议版本是5.0
	public boolean Ehome50Auth(String sDeviceID, String sEhomeKey){

		WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByDeviceId(sDeviceID);
		if(wvpDevice!=null){
			wvpDevice.setEhomeKey(sEhomeKey);
			wvpDeviceService.updateWvpDevice(wvpDevice);
		}

		//NET_ESS_HAMSHA256的功能是通过SHA256对DeviceID和EhomeKey进行加密，生成一个字符串密钥，当图像服务器是云存储时将用作SecretKey。其他时间不使用。
		CommonClass.StringPointer sSecKeyOut = new CommonClass.StringPointer(255);
		sSecKeyOut.write();
		boolean bRet = ESS.GetSSInstance().NET_ESS_HAMSHA256(sDeviceID, sEhomeKey, sSecKeyOut.getPointer(), 255);
		if(bRet){
			sSecKeyOut.read();
			System.arraycopy(sSecKeyOut.sData, 0, CommonMethod.GetListenInfo().struSSListenParam.szSecretKey, 0, 64);
			logger.info("[NET_ESS_HAMSHA256]->Cloud SerectKey Config Successfully" +
					CommonMethod.byteToString(sSecKeyOut.sData));
		}else{
			logger.error("[NET_ESS_HAMSHA256]->Cloud SerectKey Config Error: " +
					ESS.GetSSInstance().NET_ESS_GetLastError());
		}
		return bRet;
	}

	//设备的注册状态将通过此回调函数返回
	public class FRegisterCallBack implements ISUPCMSByJNA.DEVICE_REGISTER_CB {
		@Override
		public boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
							  int dwInLen, Pointer pUser) {
			logger.info("--------------------------注册监听回调函数执行------------------------");
			try {
				//通过NET_EHOME_DEV_REG_INFO_V12加载回调设备信息
				// 通过反射将设备注册表信息复制到本地
				String sClassName = ISUPCMSByJNA.NET_EHOME_DEV_REG_INFO_V12.class.getName();
				ISUPCMSByJNA.NET_EHOME_DEV_REG_INFO_V12 strDevRegInfo = null;
				if(pOutBuffer!=null){
					strDevRegInfo=(ISUPCMSByJNA.NET_EHOME_DEV_REG_INFO_V12) CommonMethod.WritePointerDataToClass(pOutBuffer, sClassName);
				}
				logger.info("dwDataType:"+dwDataType);
				switch (dwDataType) {
					//如果不允许注册设备，请根据设备ID筛选要注册的设备 注册后，它将返回FALSE

					//设备在线信息
					case ISUPCMSByJNA.ENUM_DEV_ON: {
						//当前注册的设备序列号
						String sDevInDevID = CommonMethod.byteToString(strDevRegInfo.struRegInfo.byDeviceID);
						// 获取设备协议版本
						int dwDevProVersion = strDevRegInfo.struRegInfo.byDevProtocolVersion[0];
						logger.info("New Device is online, DevID is " + sDevInDevID);
						// 如果list已包含此设备了，则仅替换IUserID
						boolean bDevAdd = true;


						strDevRegInfo.write();
						Pointer pDevRegInfo = strDevRegInfo.getPointer();
						pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
						strDevRegInfo.read();




						CommonClass.NET_EHOME_SERVER_INFO_V50 strEhomeServerInfo = new CommonClass.NET_EHOME_SERVER_INFO_V50();
						strEhomeServerInfo.read();
						strEhomeServerInfo.dwSize = strEhomeServerInfo.size();
						byte[] byIP = ehomePuIp.getBytes();
						System.arraycopy(byIP, 0, strEhomeServerInfo.struUDPAlarmSever.szIP, 0, byIP.length);
						System.arraycopy(byIP, 0, strEhomeServerInfo.struTCPAlarmSever.szIP, 0, byIP.length);

						System.out.println("FRegisterCallBack--byDevProtocolVersion:" + new String(strDevRegInfo.struRegInfo.byDevProtocolVersion));

						if(strDevRegInfo.struRegInfo.byDevProtocolVersion[0] == '5')
						{
							strEhomeServerInfo.dwAlarmServerType = 2; //报警服务器类型：0- 只支持UDP协议上报，1- 支持UDP、TCP两种协议上报 2-MQTT
							strEhomeServerInfo.struTCPAlarmSever.wPort = ehomeAmsProt;
							System.out.println("FRegisterCallBack--dwAlarmServerType:" + strEhomeServerInfo.dwAlarmServerType + ", Port:" + strEhomeServerInfo.struTCPAlarmSever.wPort);
						}

						strEhomeServerInfo.write();
						dwInLen = strEhomeServerInfo.size();
						pInBuffer.write(0,strEhomeServerInfo.getPointer().getByteArray(0, dwInLen),0,dwInLen);

						//获取所有设备信息
						List<WvpDevice> wvpDeviceList=wvpDeviceService.selectWvpDeviceList(null);
						for(WvpDevice wvpDevice:wvpDeviceList){
							String slocalDevID = wvpDevice.getDeviceId();
							if(slocalDevID.equals(sDevInDevID)){
								//设用户id 由SDK分配
								wvpDevice.setLuserId((long) lUserID);
								//更新设备在线状态
								wvpDevice.setDeviceOnline(1);

								wvpDevice.setVoiceHandle(-1L);
								wvpDevice.setLLinkHandle(-1L);
								wvpDevice.setDeviceVersion(0);
								wvpDevice.setPushState(0);
								//更新ip
								wvpDevice.setDeviceIp(CommonMethod.byteToString(strDevRegInfo.struRegInfo.struDevAdd.szIP));
								//更新注册协议版本号
								wvpDevice.setDeviceVersion(dwDevProVersion);
								//更新上线时间
								wvpDevice.setUpdateTime(new Date());
								wvpDeviceService.updateWvpDevice(wvpDevice);
								bDevAdd = false;
							}
						}

						// 否则将当前设备添加到数据库存储
						if (bDevAdd) {
							WvpDevice wvpDevice=new WvpDevice();
							//设用户id 由SDK分配
							wvpDevice.setLuserId((long)lUserID);

							logger.info("注册用户句柄："+lUserID);
							//设备第一次注册时间
							wvpDevice.setCreateTime(new Date());
							//设备上线时间
							wvpDevice.setUpdateTime(new Date());
							//设备在线状态
							wvpDevice.setDeviceOnline(1);
							//设备ip
							wvpDevice.setDeviceIp(CommonMethod.byteToString(strDevRegInfo.struRegInfo.struDevAdd.szIP));
							//设备注册协议版本号
							wvpDevice.setDeviceVersion(dwDevProVersion);
							//设备序列号
							wvpDevice.setDeviceId( CommonMethod.byteToString(strDevRegInfo.struRegInfo.byDeviceID));
							wvpDevice.setEhomeKey(secretKey);
							wvpDeviceService.insertWvpDevice(wvpDevice);
						}
						break;
					}
					//Sessionkey 交互异常。
					case ISUPCMSByJNA.ENUM_DEV_SESSIONKEY_ERROR:{
						logger.error("SessionKey active Error");
					}
					//设备离线信息。
					case ISUPCMSByJNA.ENUM_DEV_OFF: {
						//获取所有设备信息
						List<WvpDevice> wvpDeviceList=wvpDeviceService.selectWvpDeviceList(null);
						for(WvpDevice wvpDevice:wvpDeviceList){
							if(wvpDevice.getLuserId().equals((long)lUserID)){
								wvpDevice.setDeviceOnline(0);
								wvpDevice.setVoiceHandle(-1L);
								wvpDevice.setLLinkHandle(-1L);
								wvpDevice.setLuserId(-1L);
								wvpDevice.setDeviceVersion(0);
								wvpDevice.setPushState(0);
								wvpDeviceService.updateWvpDevice(wvpDevice);
								logger.info("设备："+wvpDevice.getDeviceId()+"已掉线");
								Preview.StopPreview(wvpDevice.getLuserId()+"");
								//WebSocketUsers.sendMessageToUserByLUserId(lUserID.longValue(),"101505");
							}
						}
						//Note: You must reopen a thread to offline the device, otherwise it may cause a crash
						new Thread(){
							@Override
							public void run() {
								m_CMSInstance.NET_ECMS_ForceLogout(lUserID);
								logger.info("NET_ECMS_ForceLogout Enter");
							}
						}.start();

						break;
					}

					//用于验证支持ISUP5的设备。0，必须配置EHomeKey
					case ISUPCMSByJNA.ENUM_DEV_AUTH: {
						Pointer pDevRegInfo = strDevRegInfo.getPointer();
						strDevRegInfo = new ISUPCMSByJNA.NET_EHOME_DEV_REG_INFO_V12();
						strDevRegInfo.write();
						pDevRegInfo = strDevRegInfo.getPointer();
						pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
						strDevRegInfo.read();
						String szEHomeKey = secretKey;
						byte[] bs = szEHomeKey.getBytes();
						pInBuffer.write(0, bs, 0, szEHomeKey.length());
						break;
					}
					//校验密码失败信息。
					case ISUPCMSByJNA.ENUM_DEV_DAS_EHOMEKEY_ERROR: {
						logger.error("EhomeKey is Error");
						break;
					}
					//支持 5.0 版本 ISUP 设备的会话密钥信息
					case ISUPCMSByJNA.ENUM_DEV_SESSIONKEY: {
						strDevRegInfo = new ISUPCMSByJNA.NET_EHOME_DEV_REG_INFO_V12();
						strDevRegInfo.write();
						strDevRegInfo.getPointer().write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
						strDevRegInfo.read();

						CommonClass.NET_EHOME_DEV_SESSIONKEY struSessionKey = new CommonClass.NET_EHOME_DEV_SESSIONKEY();
						System.arraycopy(strDevRegInfo.struRegInfo.byDeviceID, 0, struSessionKey.sDeviceID, 0, 256);
						System.arraycopy(strDevRegInfo.struRegInfo.bySessionKey, 0, struSessionKey.sSessionKey, 0, 16);
						struSessionKey.write();
						Pointer pSessionKey = struSessionKey.getPointer();

						m_CMSInstance.NET_ECMS_SetDeviceSessionKey(pSessionKey);
						boolean bKey = EAlarm.GetAlarmInstance().NET_EALARM_SetDeviceSessionKey(pSessionKey);
						logger.info("NET_EALARM_SetDeviceSessionKey:" + bKey + ",IDlength:" + strDevRegInfo.struRegInfo.byDeviceID.length
								+ ",KeyLen:" + strDevRegInfo.struRegInfo.bySessionKey.length);
						break;
					}
					//支持 5.0 版本 ISUP 设备的重定向请求信息。
					case ISUPCMSByJNA.ENUM_DEV_DAS_REQ: {
						try {
							JSONObject jsonDas = new JSONObject();
							jsonDas.put("Type", "DAS");
							JSONObject jsonDasInfo = new JSONObject();
							jsonDasInfo.put("Address", ehomePuIp);
							jsonDasInfo.put("Domain", "");
							jsonDasInfo.put("ServerID", "das_" + ehomePuIp + "_" + ehomeCmsProt);
							jsonDasInfo.put("Port", ehomeCmsProt);
							jsonDasInfo.put("UdpPort", ehomeCmsProt);
							jsonDas.put("DasInfo", jsonDasInfo);
							String sDasInfo = jsonDas.toString();
							pInBuffer.write(0, sDasInfo.getBytes(), 0, sDasInfo.length());
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					}
					//当设备信息发生变化时，重新注册已注册的设备
					case ISUPCMSByJNA.ENUM_DEV_DAS_REREGISTER: {
						if(pInBuffer == null) {
							return false;
						}
						String sDevProtocolVersion = CommonMethod.byteToString(strDevRegInfo.struRegInfo.byDevProtocolVersion);
						if(sDevProtocolVersion.equals("2")) {
							break;
						}
						break;
					}
					//设备地址改变信息
					case ISUPCMSByJNA.ENUM_DEV_ADDRESS_CHANGED: {
						//print Device info
						logger.info("[Device IP Changed] DeviceID" + CommonMethod.byteToString(strDevRegInfo.struRegInfo.byDeviceID) +
								" New IP" + CommonMethod.byteToString(strDevRegInfo.struRegInfo.struDevAdd.szIP));
						break;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return true;
		}
	}

	//加载库文件
	private boolean CreateCMSInstance() {
		if(m_CMSInstance == null)
		{
			synchronized (ISUPCMSByJNA.class)
			{
				String strDllPath = "";
				try
				{
					if(CommonMethod.isWindows()) {
						strDllPath = System.getProperty("user.dir") + "\\lib\\HCISUPCMS";
					}else if(CommonMethod.isLinux()) {
						strDllPath = "/home/hik/LinuxSDK/libHCISUPCMS.so";
					}
					logger.info("[ECMS]->"+strDllPath);
					m_CMSInstance = (ISUPCMSByJNA) Native.loadLibrary(strDllPath, ISUPCMSByJNA.class);
				}catch (Exception ex) {
					logger.error("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
					return false;
				}
			}
		}
		return true;
	}

	public static ISUPCMSByJNA GetCMSInstance()
	{
		return m_CMSInstance;
	}

	public boolean IsRunning(){return m_bIsRunning;}

	public static boolean ptz(long lUserId, int dwCommand, ISUPCMSByJNA.NET_EHOME_REMOTE_CTRL_PARAM lpCtrlParam){
		boolean result=m_CMSInstance.NET_ECMS_RemoteControl(lUserId,dwCommand,lpCtrlParam);
		System.out.println(result);
		System.out.println(m_CMSInstance.NET_ECMS_GetLastError());
		return true;
	}
}
