package com.wvp.hk.module.service.storage;

import com.wvp.hk.module.common.CommonClass.IntPointer;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

public interface ISUPSSByJNA extends Library {

	public static final int PIC_URI_LEN = 128;
	public static final int MAX_URL_LEN_SS = 4096;
	public static final int MAX_KMS_USER_LEN = 512;
	public static final int MAX_KMS_PWD_LEN = 512;
	public static final int MAX_CLOUD_AK_SK_LEN = 64;
	public static final int MAX_PATH = 260;
	public static final int NET_EHOME_SERIAL_LEN = 12;

	public static final int NET_EHOME_SS_INIT_CFG_SDK_PATH        = 1; //Picture storage database path setting, please end the path with \\
	public static final int NET_EHOME_SS_INIT_CFG_CLOUD_TIME_DIFF = 2; //Set the request time difference of cloud storage, the default is 15 minutes, the minimum is 5 minutes, and the maximum is 60 minutes
	public static final int NET_EHOME_SS_INIT_CFG_PUBLIC_IP_PORT  = 3; //Set the public network address (when there is internal and external network mapping)
	public static final int NET_EHOME_SS_INIT_CFG_LIBEAY_PATH     = 4; //Set OpenSSL encryption library path
	public static final int NET_EHOME_SS_INIT_CFG_SSLEAY_PATH     = 5; //Set OpenSSL communication library path
	public static final int NET_EHOME_SS_INIT_CFG_SQLITE3_PATH    = 6; //Set Sqlite3.dll | libsqlite3.so path

	public static final int NET_EHOME_SS_MSG_TOMCAT = 1;
	public static final int NET_EHOME_SS_MSG_KMS_USER_PWD = 2;
	public static final int NET_EHOME_SS_MSG_CLOUD_AK = 3;


	public static final int NET_EHOME_SS_CLIENT_TYPE_TOMCAT = 1;
	public static final int NET_EHOME_SS_CLIENT_TYPE_VRB = 2;
	public static final int NET_EHOME_SS_CLIENT_TYPE_KMS = 3;
	public static final int NET_EHOME_SS_CLIENT_TYPE_CLOUD = 4;

	public enum NET_EHOME_SS_MSG_TYPE {
		NET_EHOME_SS_MSG_TOMCAT ,
		NET_EHOME_SS_MSG_KMS_USER_PWD,
		NET_EHOME_SS_MSG_CLOUD_AK
	}

	public enum NET_EHOME_SS_CLIENT_TYPE {
		NET_EHOME_SS_CLIENT_TYPE_TOMCAT,
		NET_EHOME_SS_CLIENT_TYPE_VRB,
		NET_EHOME_SS_CLIENT_TYPE_KMS,
		NET_EHOME_SS_CLIENT_TYPE_CLOUD
	}

	public enum NET_EHOME_SS_INIT_CFG_TYPE {
		NET_EHOME_SS_INIT_CFG_SDK_PATH,
		NET_EHOME_SS_INIT_CFG_CLOUD_TIME_DIFF
	}


	public static class NET_EHOME_SS_TOMCAT_MSG extends Structure {
		public byte[] szDevUri = new byte[MAX_URL_LEN_SS];
		public int dwPicNum;
		public String pPicURLs;
		public byte[] byRes = new byte[64];
		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("szDevUri", "dwPicNum", "pPicURLs", "byRes");
		}
	}


	public static class NET_EHOME_SS_LISTEN_PARAM extends Structure {
		//本地监听信息
		public NET_EHOME_IPADDRESS struAddress;
		public byte[] szKMS_UserName = new byte[MAX_KMS_USER_LEN];
		public byte[] szKMS_Password = new byte[MAX_KMS_PWD_LEN];
		public EHomeSSStorageCallBack fnSStorageCb;
		public EHomeSSMsgCallBack fnSSMsgCb;
		public byte[] szAccessKey = new byte[MAX_CLOUD_AK_SK_LEN];
		public byte[] szSecretKey = new byte[MAX_CLOUD_AK_SK_LEN];
		public Pointer pUserData;
		public byte byHttps;
		public byte[] byRes1 = new byte[3];
		public EHomeSSRWCallBack fnSSRWCb;
		public EHomeSSRWCallBackEx fnSSRWCbEx;
		public byte bySecurityMode;
		public byte[] byRes = new byte[51];

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("struAddress", "szKMS_UserName", "szKMS_Password", "fnSStorageCb",
					"fnSSMsgCb", "szAccessKey", "szSecretKey", "pUserData", "byHttps", "byRes1",
					"fnSSRWCb", "fnSSRWCbEx", "bySecurityMode", "byRes");
		}
	}


	public static class NET_EHOME_IPADDRESS extends Structure {
		public byte[] szIP = new byte[128];
		public short wPort;
		public byte[] byRes = new byte[2];
		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("szIP", "wPort", "byRes");
		}
	}


	public static class NET_EHOME_SS_LISTEN_HTTPS_PARAM extends Structure {
		public byte byHttps;
		public byte byVerifyMode;
		public byte byCertificateFileType;
		public byte byPrivateKeyFileType;
		public byte[] szUserCertificateFile = new byte[MAX_PATH];
		public byte[] szUserPrivateKeyFile = new byte[32];
		public int dwSSLVersion;


		public byte[] byRes3 = new byte[360];
		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("byHttps", "byVerifyMode", "byCertificateFileType", "byPrivateKeyFileType",
					"szUserCertificateFile", "szUserPrivateKeyFile", "dwSSLVersion", "byRes3");
		}
	}


	public static class NET_EHOME_SS_CLIENT_PARAM extends Structure {
		public int enumType;
		public NET_EHOME_IPADDRESS struAddress;
		public byte byHttps;
		public byte[] byRes = new byte[63];
		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("enumType", "struAddress", "byHttps", "byRes");
		}
	}

	public static class NET_EHOME_SS_LOCAL_SDK_PATH extends Structure {
		public byte[] sPath = new byte[MAX_PATH];
		public byte[] byRes = new byte[128];
		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("sPath", "byRes");
		}
	}

	public static class NET_EHOME_SS_RW_PARAM extends Structure {
		public String pFileName;
		public Pointer pFileBuf;
		public IntByReference dwFileLen;
		public Pointer pFileUrl;
		public Pointer pUser;
		public byte byAct;
		public byte[] byRes = new byte[63];

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("pFileName", "pFileBuf", "dwFileLen", "pFileUrl",
					"pUser", "byAct", "byRes");
		}
	}

	public static class NET_EHOME_SS_CLOUD_PARAM extends Structure {
		public String pPoolId;
		public byte byPoolIdLength;
		public int dwErrorCode;
		public byte[] byRes = new byte[503];

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("pPoolId", "byPoolIdLength", "dwErrorCode", "byRes");
		}
	}

	public static class NET_EHOME_SS_TOMCAT_PARAM extends Structure {
		public byte[] byRes = new byte[512];

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("byRes");
		}
	}

	public static class NET_EHOME_SS_KMS_PARAM extends Structure {
		public byte[] byRes = new byte[512];

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("byRes");
		}
	}

	public static class NET_EHOME_SS_VRB_PARAM extends Structure {
		public byte[] byRes = new byte[512];

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("byRes");
		}
	}

	public static class UnionStoreInfo extends Union {
		public NET_EHOME_SS_CLOUD_PARAM struCloud;
		public NET_EHOME_SS_TOMCAT_PARAM struTomcat;
		public NET_EHOME_SS_KMS_PARAM struKms;
		public NET_EHOME_SS_VRB_PARAM struVrb;
	}

	public static class NET_EHOME_SS_EX_PARAM extends Structure {
		public byte byProtoType;
		public byte[] byRes = new byte[23];
		public UnionStoreInfo unionStoreInfo = new UnionStoreInfo();

		@Override
		protected List<String> getFieldOrder(){
			return Arrays.asList("byProtoType", "byRes", "unionStoreInfo");
		}
	}

	public static class BYTE_ARRAY extends Structure
	{
		public byte[] byValue;

		public BYTE_ARRAY(int iLen) {
			byValue = new byte[iLen];
		}

		@Override
		protected List<String> getFieldOrder() {
			// TODO Auto-generated method stub
			return Arrays.asList("byValue");
		}
	}


	boolean NET_ESS_Init();

	boolean NET_ESS_Fini();



	public static interface EHomeSSMsgCallBack extends Callback {
		public boolean invoke(int iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
							  int dwInLen, Pointer pUser);
	}



	public static interface EHomeSSStorageCallBack extends Callback {
		public boolean invoke(int iHandle, String pFileName, Pointer pFileBuf, int dwFileLen, Pointer pFilePath,
							  Pointer pUser);
	}


	public static interface EHomeSSRWCallBack extends Callback {
		public boolean invoke(int iHandle, byte byAct, String pFileName, Pointer pFileBuf, IntPointer dwFileLen
				, Pointer pFileUrl,
							  Pointer pUser);
	}

	public static interface EHomeSSRWCallBackEx extends Callback {
		public boolean invoke(int iHandle, NET_EHOME_SS_RW_PARAM pRwParam, NET_EHOME_SS_EX_PARAM pExStruct);
	}


	int NET_ESS_GetLastError();


	boolean NET_ESS_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);


	int NET_ESS_GetBuildVersion();


	boolean NET_ESS_SetListenHttpsParam(NET_EHOME_SS_LISTEN_HTTPS_PARAM pSSHttpsParam);


	int NET_ESS_StartListen(NET_EHOME_SS_LISTEN_PARAM pSSListenParam);


	boolean NET_ESS_StopListen(int lListenHandle);


	boolean NET_ESS_SetSDKInitCfg(int enumType, Pointer lpInBuff);


	int NET_ESS_CreateClient(NET_EHOME_SS_CLIENT_PARAM pClientParam);


	boolean NET_ESS_ClientSetTimeout(int lHandle, int dwSendTimeout, int dwRecvTimeout);


	boolean NET_ESS_ClientSetParam(int lHandle, String strParamName, String strParamVal);


	boolean NET_ESS_ClientDoUpload(int lHandle, Pointer strUrl, int dwUrlLen);

	boolean NET_ESS_ClientDoUploadBuffer(int lHandle, Pointer strUrl, int dwUrlLen, Pointer pFileContent,
										 int dwContentLen);


	boolean NET_ESS_ClientDoDownload(int lHandle, byte[] strUrl, PointerByReference pFileContent,
									 IntByReference dwContentLen);


	boolean NET_ESS_DestroyClient(int lHandle);


	boolean NET_ESS_HAMSHA256(String pSrc, String pSecretKey, Pointer pSingatureOut, int dwSingatureLen);

}
