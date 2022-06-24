package com.wvp.hk.module.common;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.sun.jna.Structure;
import com.wvp.hk.module.service.alarm.ISUPAMSByJNA.NET_EHOME_ALARM_LISTEN_PARAM;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.NET_EHOME_CMS_LISTEN_PARAM;
import com.wvp.hk.module.service.storage.ISUPSSByJNA;
import com.wvp.hk.module.service.stream.ISUPStreamByJNA.NET_EHOME_LISTEN_PREVIEW_CFG;
import com.wvp.hk.module.service.stream.ISUPStreamByJNA.NET_EHOME_PLAYBACK_LISTEN_PARAM;
import com.wvp.hk.module.service.stream.ISUPStreamByJNA.NET_EHOME_LISTEN_VOICETALK_CFG;
import com.wvp.hk.module.service.storage.ISUPSSByJNA.NET_EHOME_SS_LISTEN_PARAM;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class CommonClass {

    //Server listening parameters
    public static ListenInfo g_struListenInfo = new ListenInfo();
    //Used to send to the device when the device is online and callback. If the server is on the public network,
    // then you need to fill in the public network address and port here
    public static NET_EHOME_SERVER_INFO_V50 g_struServerInfoV50 = new NET_EHOME_SERVER_INFO_V50();
    public static Prop g_pLocalParam = PropKit.use("demoConfig.txt", "UTF-8");

    public static final int MAX_PASSWD_LEN             = 32;
    public static final int NAME_LEN                   = 32;
    public static final int MAX_DEVICE_ID_LEN          = 256;
    public static final int MAX_DEVICE_NAME_LEN 	   = 32;
    public static final int MAX_DEVNAME_LEN            = 32;
    public static final int MAX_VERSION_LEN            = 32;
    public static final int NET_EHOME_SERIAL_LEN       = 12;
    public static final int MAX_FULL_SERIAL_NUM_LEN    = 64;
    public static final int MAX_SERIALNO_LEN    	   = 128;
    public static final int MAX_MASTER_KEY_LEN         = 16;
    public static final int MAX_TIME_LEN               = 32;
    public static final int MAX_URL_LEN_SS             = 4096;
    public static final int REGISTER_LISTEN_MODE_ALL   = 0;
    public static final int REGISTER_LISTEN_MODE_UDP   = 1;
    public static final int REGISTER_LISTEN_MODE_TCP   = 2;

    public static class NET_EHOME_IPADDRESS extends Structure{
        public byte[] szIP = new byte[128];
        public short wPort;
        public byte[] byRes = new byte[2];

        @Override
        protected List<String> getFieldOrder() {

            return Arrays.asList("szIP", "wPort", "byRes");
        }
    }

    public static class NET_EHOME_ZONE extends Structure
    {
        public int dwX;
        public int dwY;
        public int dwWidth;
        public int dwHeight;
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("dwX","dwY","dwWidth","dwHeight");
        }
    }

    public static interface NET_EHOME_LOCAL_CFG_TYPE
    {
        public final int UNDEFINE                = -1;
        public final int ACTIVE_ACCESS_SECURITY  = 0;
        public final int AMS_ADDRESS             = 1;
        public final int SEND_PARAM              = 2;
        public final int SET_REREGISTER_MODE     = 3;
        public final int LOCAL_CFG_TYPE_GENERAL  = 4;
        public final int COM_PATH                = 5;
        public final int SESSIONKEY_REQ_MOD      = 6;
        public final int DEV_DAS_PINGREO_CALLBACK = 7;
        public final int REGISTER_LISTEN_MODE    = 8;
        public final int STREAM_PLAYBACK_PARAM   = 9;
    }

    public static class NET_EHOME_LOCAL_ACCESS_SECURITY extends Structure
    {
        public int   dwSize;
        public byte    byAccessSecurity;
        public byte[]    byRes = new byte[127];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("dwSize","byAccessSecurity","byRes");
        }
    }

    public static class NET_EHOME_SEND_PARAM extends Structure
    {
        public int dwSize;
        public int dwRecvTimeOut;
        public byte  bySendTimes;
        public byte[]  byRes2 = new byte[127];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("dwSize","dwRecvTimeOut","bySendTimes","byRes2");
        }
    }

    public static class NET_EHOME_TIME extends Structure{
        public short   wYear;
        public byte    byMonth;
        public byte    byDay;
        public byte    byHour;
        public byte    byMinute;
        public byte    bySecond;
        public byte    byRes1;
        public short   wMSecond;
        public byte[] byRes2 = new byte[2];

        public NET_EHOME_TIME(){}

        public NET_EHOME_TIME(short wYear, byte byMonth, byte byDay, byte byHour, byte byMinute, byte bySecond){
            this.wYear = wYear;
            this.byMonth = byMonth;
            this.byDay = byDay;
            this.byHour = byHour;
            this.byMinute = byMinute;
            this.bySecond = bySecond;
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("wYear","byMonth","byDay","byHour","byMinute",
                    "bySecond","byRes1", "wMSecond", "byRes2");
        }
    }

    public static class NET_EHOME_BLACKLIST_SEVER extends Structure {
        public NET_EHOME_IPADDRESS struAdd;
        public byte[] byServerName = new byte[32];
        public byte[] byUserName = new byte[32];
        public byte[] byPassWord = new byte[32];
        public byte[] byRes = new byte[64];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("struAdd", "byServerName","byUserName", "byPassWord", "byRes");
        }
    }

    public static class NET_EHOME_SERVER_INFO_V50 extends Structure{
        public int                   dwSize;
        public int                   dwKeepAliveSec;
        public int                   dwTimeOutCount;
        public NET_EHOME_IPADDRESS     struTCPAlarmSever;
        public NET_EHOME_IPADDRESS     struUDPAlarmSever;
        public int                   dwAlarmServerType;
        public NET_EHOME_IPADDRESS     struNTPSever;
        public int                   dwNTPInterval;
        public NET_EHOME_IPADDRESS     struPictureSever;
        public int                   dwPicServerType;
        public NET_EHOME_BLACKLIST_SEVER   struBlackListServer;
        public NET_EHOME_IPADDRESS     struRedirectSever;
        public byte[] byClouldAccessKey = new byte[64];
        public byte[] byClouldSecretKey = new byte[64];
        public byte                    byClouldHttps;
        public byte[] byRes1 = new byte[3];
        public int                   dwAlarmKeepAliveSec;
        public int                   dwAlarmTimeOutCount;
        public byte[] byRes = new byte[372];
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize","dwKeepAliveSec","dwTimeOutCount","struTCPAlarmSever","struUDPAlarmSever",
                    "dwAlarmServerType","struNTPSever", "dwNTPInterval", "struPictureSever", "dwPicServerType", "struBlackListServer",
                    "struRedirectSever", "byClouldAccessKey", "byClouldSecretKey", "byClouldHttps", "byRes1", "dwAlarmKeepAliveSec",
                    "dwAlarmTimeOutCount", "byRes");
        }
    }

    public static class ListenInfo{
        public NET_EHOME_CMS_LISTEN_PARAM struCMSListenParam = new NET_EHOME_CMS_LISTEN_PARAM();
        public NET_EHOME_ALARM_LISTEN_PARAM struAMSListenParam = new NET_EHOME_ALARM_LISTEN_PARAM();
        public ISUPSSByJNA.NET_EHOME_SS_LISTEN_PARAM struSSListenParam = new NET_EHOME_SS_LISTEN_PARAM();
        public NET_EHOME_LISTEN_PREVIEW_CFG struPreviewListenParam = new NET_EHOME_LISTEN_PREVIEW_CFG();
        public NET_EHOME_PLAYBACK_LISTEN_PARAM struPlaybackListenParam = new NET_EHOME_PLAYBACK_LISTEN_PARAM();
        public NET_EHOME_LISTEN_VOICETALK_CFG struVoiceTalkListenParam = new NET_EHOME_LISTEN_VOICETALK_CFG();
        public NET_EHOME_IPADDRESS struDASParam = new NET_EHOME_IPADDRESS();
        public boolean bSMSUsePortMapping;
        public byte byCallbackType;   //0-storage  1-RW
        public NET_EHOME_IPADDRESS struOuterPreviewAddress = new NET_EHOME_IPADDRESS();
        public NET_EHOME_IPADDRESS struOuterPlaybackAddress = new NET_EHOME_IPADDRESS();
        public NET_EHOME_IPADDRESS struOuterVoiceTalkAddress = new NET_EHOME_IPADDRESS();
    }

    public static class LocalConfigInfo{

    }

    public static class  NET_EHOME_DEV_SESSIONKEY extends Structure
    {
        public byte[]   sDeviceID = new byte[MAX_DEVICE_ID_LEN];
        public byte[]   sSessionKey = new byte[MAX_MASTER_KEY_LEN];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("sDeviceID","sSessionKey");
        }
    }

    public static class NET_EHOME_SET_REREGISTER_MODE extends Structure
    {
        public int dwSize;
        public int dwReRegisterMode;
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("dwSize","dwReRegisterMode");
        }
    }

    public static class NET_EHOME_REGISTER_LISTEN_MODE extends Structure
    {
        public int dwSize;
        public int dwRegisterListenMode;
        public byte[]  byRes = new byte[128];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("dwSize","dwRegisterListenMode","byRes");
        }
    }

    public static class NET_EHOME_LOCAL_GENERAL_CFG extends Structure
    {
        public byte byAlarmPictureSeparate;
        public byte[] byRes = new byte[127];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("byAlarmPictureSeparate","byRes");
        }
    }

    public static class NET_EHOME_LOCAL_PLAYBACK_PARAM extends Structure
    {
        public int dwSize;
        public byte  byPlayBackSync;
        public byte[]  byRes = new byte[131];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("dwSize","byPlayBackSync","byRes");
        }
    }

    public static class StringPointer extends Structure{
    	public byte[]  sData;

    	public StringPointer(){}

    	public StringPointer(int dwLength){
    		if(dwLength == 0){
    			throw new NullPointerException("Data length can`t be zero");
    		}

    		this.sData = new byte[dwLength];
    	}

    	public StringPointer(String sContent) {

    		if(sContent == null){
    			throw new NullPointerException("Content can`t be null");
    		}

    		this.sData = new byte[sContent.length() + 128];
    		try {
                int sDataLen_utf8 = sContent.getBytes("utf-8").length;
                System.arraycopy(sContent.getBytes("utf-8"), 0, this.sData, 0, sDataLen_utf8);
                System.out.println("sdata:" + CommonMethod.byteToString(this.sData));
            }catch(UnsupportedEncodingException ex){
    		    ex.printStackTrace();
            }
		}

		public StringPointer(byte[] byData){
    	    this.sData = new byte[byData.length + 128];
            System.arraycopy(byData, 0, this.sData, 0, byData.length);
        }

        public String GetString(){
    	    return CommonMethod.byteToString(this.sData);
    	}

        public byte[] GetByteArray(){
            return this.sData;
        }

    	@Override
    	protected List<String> getFieldOrder() {
    		return Arrays.asList("sData");
    	}
    }

    public static class IntPointer extends Structure {
        public int dwData;

		public IntPointer() {}
		public IntPointer(int dwTemp){
			this.dwData = dwTemp;
		}

		public int GetData(){return this.dwData;}
        public void SetData(int dwData){this.dwData = dwData;}
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dwData");
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
}
