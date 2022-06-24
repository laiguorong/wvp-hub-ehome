package com.wvp.hk.module.service.alarm;

import com.wvp.hk.module.common.CommonClass.NET_EHOME_DEV_SESSIONKEY;
import com.wvp.hk.module.common.CommonClass.NET_EHOME_IPADDRESS;



import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

public interface ISUPAMSByJNA extends Library {

	public static final int MAX_DEVICE_ID_LEN = 256;
	public static final int NET_EHOME_SERIAL_LEN = 12;
	public static final int EHOME_ALARM_UNKNOWN = 0;
	public static final int EHOME_ALARM = 1;
	public static final int EHOME_ALARM_HEATMAP_REPORT = 2;
	public static final int EHOME_ALARM_FACESNAP_REPORT = 3;
	public static final int EHOME_ALARM_GPS = 4;
	public static final int EHOME_ALARM_CID_REPORT = 5;
	public static final int EHOME_ALARM_NOTICE_PICURL = 6;
	public static final int EHOME_ALARM_NOTIFY_FAIL = 7;
	public static final int EHOME_ALARM_SELFDEFINE = 9;
	public static final int EHOME_ALARM_DEVICE_NETSWITCH_REPORT = 10;
	public static final int EHOME_ALARM_ACS = 11;
	public static final int EHOME_ALARM_WIRELESS_INFO = 12;
	public static final int EHOME_ISAPI_ALARM = 13;
	public static final int EHOME_INFO_RELEASE_PRIVATE = 14;
	public static final int EHOME_ALARM_MPDCDATA = 15;

	public static final int MAX_TIME_LEN = 32;
	public static final int MAX_REMARK_LEN = 64;
	public static final int MAX_URL_LEN = 512;
	public static final int CID_DES_LEN = 32;
	public static final int MAX_FILE_PATH_LEN = 256;
	public static final int MAX_UUID_LEN = 64;
	public static final int CID_DES_LEN_EX = 256;

	public static class NET_EHOME_XML_DATA extends Structure {
		public byte[] byXMLData = new byte[2048];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("byXMLData");
		}
	}

	public static class NET_EHOME_ALARM_MSG extends Structure {
		public int dwAlarmType;
		public Pointer pAlarmInfo;
		public int dwAlarmInfoLen;
		public Pointer pXmlBuf;
		public int dwXmlBufLen;
		public byte[] sSerialNumber = new byte[NET_EHOME_SERIAL_LEN];
		public Pointer pHttpUrl;
		public int dwHttpUrlLen;
		public byte[] byRes = new byte[12];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dwAlarmType", "pAlarmInfo", "dwAlarmInfoLen", "pXmlBuf", "dwXmlBufLen",
					"sSerialNumber", "pHttpUrl", "dwHttpUrlLen", "byRes");
		}
	}

	public static class NET_EHOME_DEV_STATUS_CHANGED extends Structure {
		public byte byDeviceStatus;
		public byte[] byRes = new byte[11];

		protected List<String> getFieldOrder() {
			return Arrays.asList("byDeviceStatus", "byRes");
		}
	}

	public static class NET_EHOME_CHAN_STATUS_CHANGED extends Structure {
		public short wChanNO;
		public byte byChanStatus;








		public byte[] byRes = new byte[9];

		protected List<String> getFieldOrder() {
			return Arrays.asList("wChanNO", "byChanStatus", "byRes");
		}
	}

	public static class NET_EHOME_HD_STATUS_CHANGED extends Structure {
		public int dwVolume;
		public short wHDNo;
		public byte byHDStatus;

		public byte[] byRes = new byte[5];

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwVolume", "wHDNo", "byHDStatus", "byRes");
		}
	}

	public static class NET_EHOME_DEV_TIMING_STATUS extends Structure {
		public int dwMemoryTotal;
		public int dwMemoryUsage;
		public byte byCPUUsage;
		public byte byMainFrameTemp;
		public byte byBackPanelTemp;
		public byte byRes;

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwMemoryTotal", "dwMemoryUsage", "byCPUUsage", "byMainFrameTemp", "byBackPanelTemp",
					"byRes");
		}
	}

	public static class NET_EHOME_CHAN_TIMING_STATUS_SINGLE extends Structure {
		public int dwBitRate;
		public short wChanNO;
		public byte byLinkNum;
		public byte[] byRes = new byte[5];

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwBitRate", "wChanNO", "byLinkNum", "byRes");
		}
	}

	public static class NET_EHOME_HD_TIMING_STATUS_SINGLE extends Structure {
		public int dwHDFreeSpace;
		public short wHDNo;
		public byte[] byRes = new byte[6];

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwHDFreeSpace", "wHDNo", "byRes");
		}
	}

	public static class NET_EHOME_ALARM_STATUS_UNION extends Structure {
		public byte[] byRes = new byte[12];
		public NET_EHOME_DEV_STATUS_CHANGED struDevStatusChanged;
		public NET_EHOME_CHAN_STATUS_CHANGED struChanStatusChanged;
		public NET_EHOME_HD_STATUS_CHANGED struHdStatusChanged;
		public NET_EHOME_DEV_TIMING_STATUS struDevTimeStatus;
		public NET_EHOME_CHAN_TIMING_STATUS_SINGLE struChanTimeStatus;
		public NET_EHOME_HD_TIMING_STATUS_SINGLE struHdTimeStatus;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("byRes", "struDevStatusChanged", "struChanStatusChanged", "struHdStatusChanged",
					"struDevTimeStatus", "struChanTimeStatus", "struHdTimeStatus");
		}
	}

	public static class NET_EHOME_HEATMAP_VALUE extends Structure {
		public int dwMaxHeatMapValue;
		public int dwMinHeatMapValue;
		public int dwTimeHeatMapValue;

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwMaxHeatMapValue", "dwMinHeatMapValue", "dwTimeHeatMapValue");
		}
	}

	public static class NET_EHOME_PIXEL_ARRAY_SIZE extends Structure {
		public int dwLineValue;
		public int dwColumnValue;

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwLineValue", "dwColumnValue");
		}
	}

	public static class NET_EHOME_HEATMAP_REPORT extends Structure {
		public int dwSize;
		public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
		public int dwVideoChannel;
		public byte[] byStartTime = new byte[MAX_TIME_LEN];

		public byte[] byStopTime = new byte[MAX_TIME_LEN];

		public NET_EHOME_HEATMAP_VALUE struHeatmapValue;
		public NET_EHOME_PIXEL_ARRAY_SIZE struPixelArraySize;
		public byte[] byPixelArrayData = new byte[MAX_URL_LEN];
		public byte byRetransFlag;
		public byte byTimeDiffH;

		public byte byTimeDiffM;

		public byte[] byRes = new byte[61];

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwSize", "byDeviceID", "dwVideoChannel", "byStartTime", "byStopTime",
					"struHeatmapValue", "struPixelArraySize", "byPixelArrayData", "byRetransFlag", "byTimeDiffH",
					"byTimeDiffM", "byRes");
		}
	}

	public static class NET_EHOME_NOTICE_PICURL extends Structure {
		public int dwSize;
		public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
		public short wPicType;


		public short wAlarmType;
		public int dwAlarmChan;
		public byte[] byAlarmTime = new byte[MAX_TIME_LEN];

		public int dwCaptureChan;
		public byte[] byPicTime = new byte[MAX_TIME_LEN];

		public byte[] byPicUrl = new byte[MAX_URL_LEN];
		public int dwManualSnapSeq;
		public byte byRetransFlag;
		public byte byTimeDiffH;

		public byte byTimeDiffM;

		public byte[] byRes = new byte[29];

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwSize", "byDeviceID", "wPicType", "wAlarmType", "dwAlarmChan", "byAlarmTime",
					"dwCaptureChan", "byPicTime", "byPicUrl", "dwManualSnapSeq", "byRetransFlag", "byTimeDiffH",
					"byTimeDiffM", "byRes");
		}
	}

	public static class NET_EHOME_ZONE extends Structure {
		public int dwX;
		public int dwY;
		public int dwWidth;
		public int dwHeight;

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwX", "dwY", "dwWidth", "dwHeight");
		}
	}

	public static class NET_EHOME_HUMAN_FEATURE extends Structure {
		public byte byAgeGroup;
		public byte bySex;
		public byte byEyeGlass;
		public byte byMask;
		public byte[] byRes = new byte[12];

		protected List<String> getFieldOrder() {
			return Arrays.asList("byAgeGroup", "bySex", "byEyeGlass", "byMask", "byRes");
		}
	}

	public static class NET_EHOME_FACESNAP_REPORT extends Structure {
		public int dwSize;
		public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
		public int dwVideoChannel;
		public byte[] byAlarmTime = new byte[MAX_TIME_LEN];

		public int dwFacePicID;
		public int dwFaceScore;
		public int dwTargetID;
		public NET_EHOME_ZONE struTarketZone;
		public NET_EHOME_ZONE struFacePicZone;
		public NET_EHOME_HUMAN_FEATURE struHumanFeature;
		public int dwStayDuration;
		public int dwFacePicLen;
		public byte[] byFacePicUrl = new byte[MAX_URL_LEN];
		public int dwBackgroudPicLen;
		public byte[] byBackgroudPicUrl = new byte[MAX_URL_LEN];
		public byte byRetransFlag;
		public byte byTimeDiffH;

		public byte byTimeDiffM;

		public byte[] byRes = new byte[61];

		protected List<String> getFieldOrder() {
			return Arrays.asList("dwSize", "byDeviceID", "dwVideoChannel", "byAlarmTime", "dwFacePicID", "dwFaceScore",
					"dwTargetID", "struTarketZone", "struFacePicZone", "struHumanFeature", "dwStayDuration",
					"dwFacePicLen", "byFacePicUrl", "dwBackgroudPicLen", "byBackgroudPicUrl", "byRetransFlag",
					"byTimeDiffH", "byTimeDiffM", "byRes");
		}
	}

	public static class NET_EHOME_ALARM_INFO extends Structure {
		public int dwSize;
		public byte[] szAlarmTime = new byte[MAX_TIME_LEN];

		public byte[] szDeviceID = new byte[MAX_DEVICE_ID_LEN];
		public int dwAlarmType;
		public int dwAlarmAction;
		public int dwVideoChannel;
		public int dwAlarmInChannel;
		public int dwDiskNumber;
		public byte[] byRemark = new byte[MAX_REMARK_LEN];
		public byte byRetransFlag;
		public byte byTimeDiffH;

		public byte byTimeDiffM;

		public byte byRes1;
		public byte[] szAlarmUploadTime = new byte[MAX_TIME_LEN];

		public NET_EHOME_ALARM_STATUS_UNION uStatusUnion;
		public byte[] byRes2 = new byte[16];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dwSize", "szAlarmTime", "szDeviceID", "dwAlarmType", "dwAlarmAction",
					"dwVideoChannel", "dwAlarmInChannel", "dwDiskNumber", "byRemark", "byRetransFlag", "byTimeDiffH",
					"byTimeDiffM", "byRes1", "szAlarmUploadTime", "uStatusUnion", "byRes2");
		}
	}

	public static class NET_EHOME_AMS_ADDRESS extends Structure {
		public int dwSize;
		public byte byEnable;
		public byte[] byRes1 = new byte[3];
		public NET_EHOME_IPADDRESS struAddress;
		public byte[] byRes2 = new byte[32];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dwSize", "byEnable", "byRes1", "struAddress", "byRes2");
		}
	}

	public static interface EHomeMsgCallBack extends Callback {
		public boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser);
	}

	public static class NET_EHOME_ALARM_LISTEN_PARAM extends Structure {
		public NET_EHOME_IPADDRESS struAddress;
		public EHomeMsgCallBack fnMsgCb;
		public Pointer pUserData;
		public byte byProtocolType;
		public byte byUseCmsPort;
		public byte byUseThreadPool;
		public byte  byRes1;
		public int dwKeepAliveSec;               //心跳间隔（单位：秒,0:默认为30S）
		public int dwTimeOutCount;               //心跳超时次数（0：默认为3）
		public byte byRes[] = new byte[20];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("struAddress", "fnMsgCb", "pUserData", "byProtocolType", "byUseCmsPort",
					"byUseThreadPool", "byRes1", "dwKeepAliveSec", "dwTimeOutCount", "byRes");
		}
	}

	public static class NET_EHOME_ALARM_ISAPI_INFO extends Structure {
		public String pAlarmData;
		public int dwAlarmDataLen;
		public byte byDataType;
		public byte byPicturesNumber;
		public byte[] byRes = new byte[2];
		public Pointer pPicPackData;
		public byte[] byRes1 = new byte[32];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("pAlarmData", "dwAlarmDataLen", "byDataType", "byPicturesNumber",
					"byRes", "pPicPackData", "byRes1");
		}
	}

	public int NET_EALARM_GetLastError();

	public boolean NET_EALARM_Init();

	public boolean NET_EALARM_Fini();

	public boolean NET_EALARM_SetLogToFile(long lLogLevel, String strLogDir, boolean bAutoDel);

	public boolean NET_EALARM_GetSDKLocalCfg(int enumType, Pointer lpOutBuff);

	public boolean NET_EALARM_SetSDKLocalCfg(int enumType, Pointer lpInBuff);

	public int NET_EALARM_StartListen(NET_EHOME_ALARM_LISTEN_PARAM pAlarmListenParam);

	public boolean NET_EALARM_StopListen(long lAlarmListenHandle);

	public boolean NET_EALARM_SetDeviceSessionKey(Pointer pDeviceKey);

	boolean NET_EALARM_SetSDKInitCfg(int enumType, Pointer lpInBuff);
}
