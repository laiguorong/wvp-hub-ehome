package com.wvp.hk.module.service.stream;


import com.wvp.hk.module.common.CommonClass;
import com.wvp.hk.module.common.CommonClass.NET_EHOME_IPADDRESS;
import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

public interface ISUPStreamByJNA extends Library {

    public static final int NET_DVR_SYSHEAD = 1;
    public static final int NET_DVR_STREAMDATA = 2;

    public static final int MAX_FRAME_LENTH = 64;
    public static final int NET_EHOME_SERIAL_LEN = 12;

    public class NET_EHOME_LISTEN_PREVIEW_CFG extends Structure
    {
        public NET_EHOME_IPADDRESS struIPAdress;
        public PREVIEW_NEWLINK_CB    fnNewLinkCB; //回调函数，用于接收预览请求的响应报文。
        public Pointer pUser;
        public byte byLinkMode;   //监听请求的接入方式：0-TCP，1-UDP.2-HRUDP
        public byte[] byRes = new byte[127];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("struIPAdress", "fnNewLinkCB", "pUser", "byLinkMode", "byRes");
        }
    }

    public class NET_EHOME_STOPSTREAM_PARAM extends Structure
    {
        public int lSessionID;
        public int lHandle;
        public byte[] byRes = new byte[120];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("lSessionID", "lHandle", "byRes");
        }
    }

    public class NET_EHOME_NEWLINK_CB_MSG extends Structure
    {
        public byte[] szDeviceID = new byte[CommonClass.MAX_DEVICE_ID_LEN];   //设备ID。最大长度为256字节（对应宏定义为“MAX_DEVICE_ID_LEN”）
        public int iSessionID;     //设备取流会话ID
        public int dwChannelNo;    //通道号
        public byte byStreamType;   //码流类型：0-主码流，1-子码流
        public byte[] byRes1 = new byte[2];   //保留，最大长度为2字节
        public byte byStreamFormat;         //码流封装格式：0-PS，1-标准流格式。
        public byte[] sDeviceSerial = new byte[CommonClass.NET_EHOME_SERIAL_LEN];        //设备序列号
        public PREVIEW_DATA_CB  fnPreviewDataCB;
        public Pointer pUserData;
        public byte[] byRes = new byte[96];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("szDeviceID", "iSessionID", "dwChannelNo", "byStreamType", "byRes1", "byStreamFormat",
                    "sDeviceSerial", "fnPreviewDataCB", "pUserData", "byRes");
        }
    }

    public class NET_EHOME_PLAYBACK_NEWLINK_CB_INFO extends Structure
    {
        public byte[] szDeviceID = new byte[CommonClass.MAX_DEVICE_ID_LEN];   //设备ID。最大长度为256字节（对应宏定义为“MAX_DEVICE_ID_LEN”）
        public int lSessionID;     //设备取流会话ID
        public int dwChannelNo;    //通道号
        public byte[] sDeviceSerial = new byte[NET_EHOME_SERIAL_LEN];
        public byte byStreamFormat;         //码流封装格式：0-PS，1-RTP。
        public byte[] byRes1 = new byte[3];   //保留，最大长度为3字节
        public PLAYBACK_DATA_CB  fnPlayBackDataCB;
        public Pointer pUserData;
        public byte[] byRes = new byte[88];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("szDeviceID", "lSessionID", "dwChannelNo", "sDeviceSerial", "byStreamFormat", "byRes1",
                    "fnPlayBackDataCB", "pUserData", "byRes");
        }
    }

    public class NET_EHOME_PREVIEW_CB_MSG extends Structure
    {
        public byte byDataType;       //数据类型：1-NET_DVR_SYSHEAD（码流头部）, 2-NET_DVR_STREAMDATA（码流数据）
        public byte[] byRes1 = new byte[3];
        public Pointer pRecvdata;      //保存码流头部或码流数据的缓冲区
        public int dwDataLen;      //码流头部或码流数据缓冲区大小
        public byte[] byRes2 = new byte[128];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("byDataType", "byRes1", "pRecvdata", "dwDataLen", "byRes2");
        }
    }

    public class NET_EHOME_PLAYBACK_DATA_CB_INFO extends Structure
    {
        public byte byType;
        public Pointer pData;
        public int dwDataLen;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("byType", "pData", "dwDataLen", "byRes");
        }
    }

    public class NET_EHOME_PREVIEW_DATA_CB_PARAM extends Structure
    {
        public PREVIEW_DATA_CB fnPreviewDataCB;    //预览的回调函数
        public Pointer pUserData;
        public byte byStreamFormat;     //码流封装格式：0-PS
        public byte[] byRes = new byte[127];
        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("fnPreviewDataCB", "pUserData", "byStreamFormat", "byRes");
        }
    }

    public class NET_EHOME_PLAYBACK_DATA_CB_PARAM extends Structure
    {
        public PLAYBACK_DATA_CB fnPlayBackDataCB;
        public Pointer pUserData;
        public byte byStreamFormat;
        public byte byRes;

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("fnPlayBackDataCB", "pUserData", "byStreamFormat", "byRes");
        }
    }

    public class NET_EHOME_VOICETALK_DATA_CB_INFO extends Structure
    {
        public Pointer pData;
        public int dwDataLen;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("pData", "dwDataLen", "byRes");
        }
    }

    public class NET_EHOME_PLAYBACK_LISTEN_PARAM extends Structure
    {
        public NET_EHOME_IPADDRESS struIPAddress = new NET_EHOME_IPADDRESS();
        public PLAYBACK_NEWLINK_CB fnNewLinkCB;
        public Pointer pUserData;
        public byte byLinkMode;
        public byte[] byRes = new byte[127];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("struIPAddress", "fnNewLinkCB", "pUserData", "byLinkMode", "byRes");
        }
    }

    public class NET_EHOME_VOICETALK_NEWLINK_CB_INFO extends Structure
    {
        public String szDeviceID;
        public int dwEncodeType;
        public String sDeviceSerial;
        public int dwAudioChan;
        public long lSessionID;
        public byte[] byToken = new byte[64];
        public VOICETALK_DATA_CB fnVoiceTalkDataCB;
        public Pointer pUserData;
        public byte[] byRes = new byte[48];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("szDeviceID", "dwEncodeType", "sDeviceSerial", "dwAudioChan", "lSessionID",
                    "byToken", "fnVoiceTalkDataCB", "pUserData", "byRes");
        }
    }

    public static class NET_EHOME_LISTEN_VOICETALK_CFG extends Structure
    {
        public NET_EHOME_IPADDRESS struIPAddress = new NET_EHOME_IPADDRESS();
        public VOICETALK_NEWLINK_CB fnNewLinkCB;
        public Pointer pUserData;
        public byte byLinkMode;
        public byte byLinkEncrypt;
        public byte[] byRes = new byte[126];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("struIPAddress", "fnNewLinkCB", "pUserData", "byLinkMode", "byLinkEncrypt", "byRes");
        }
    }

    public static class NET_EHOME_VOICETALK_DATA_CB_PARAM extends Structure
    {
        public VOICETALK_DATA_CB fnVoiceTalkDataCB;
        public Pointer pUserData;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("fnVoiceTalkDataCB", "pUserData", "byRes");
        }
    }

    public static class NET_EHOME_VOICETALK_DATA extends Structure
    {
        public Pointer pSendBuf;
        public int dwDataLen;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder(){
            return Arrays.asList("pSendBuf", "dwDataLen", "byRes");
        }
    }

    public interface PREVIEW_NEWLINK_CB extends Callback {
        public boolean invoke(int lLinkHandle, NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData);
    }

    public interface PLAYBACK_NEWLINK_CB extends Callback {
        public boolean invoke(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBMsg,
                              Pointer pUserData);
    }

    public interface VOICETALK_NEWLINK_CB extends Callback {
        public boolean invoke(int lHandle, NET_EHOME_VOICETALK_NEWLINK_CB_INFO pNewLinkCBMsg, Pointer pUserData);
    }

    public interface PREVIEW_DATA_CB extends Callback {
        public boolean invoke(int iPreviewHandle, Pointer pPreviewCBMsg, Pointer pUserData);
    }

    public interface PLAYBACK_DATA_CB extends Callback {
        public boolean invoke(int iPlayBackLinkHandle, Pointer pDataCBInfo, Pointer pUserData);
    }

    public interface VOICETALK_DATA_CB extends Callback {
        public boolean invoke(int lHandle, NET_EHOME_VOICETALK_DATA_CB_INFO pDataCBInfo, Pointer pUserData);
    }

    public interface fExceptionCallBack extends Callback {
        public void invoke(int dwType, int iUserID, int iHandle, Pointer pUser);
    }

    public boolean NET_ESTREAM_Init();

    public boolean NET_ESTREAM_Fini();

    public int NET_ESTREAM_GetLastError();

    public boolean NET_ESTREAM_SetExceptionCallBack(int dwMessage, int hWnd, fExceptionCallBack cbExceptionCallBack,
                                                    Pointer pUser);

    public boolean NET_ESTREAM_SetLogToFile(long iLogLevel, String strLogDir, boolean bAutoDel);

    public boolean NET_ESTREAM_GetSDKLocalCfg(int enumType, Pointer lpOutBuff);
    public boolean NET_ESTREAM_SetSDKLocalCfg(int enumType, Pointer lpOutBuff);

    public int NET_ESTREAM_GetBuildVersion();

    public int NET_ESTREAM_StartListenPreview(NET_EHOME_LISTEN_PREVIEW_CFG pListenParam);

    public int NET_ESTREAM_StartListenPlayBack(NET_EHOME_PLAYBACK_LISTEN_PARAM pListenParam);

    public boolean NET_ESTREAM_StopListenPlayBack(int iListenHandle);

    public boolean NET_ESTREAM_StopListenPreview(int iListenHandle);

    public boolean NET_ESTREAM_StopPreview(int iPreviewHandle);

    public boolean NET_ESTREAM_StopPlayBack(int iPlayBackLinkHandle);

    public boolean NET_ESTREAM_SetPreviewDataCB(int iHandle, NET_EHOME_PREVIEW_DATA_CB_PARAM pStruCBParam);

    public boolean NET_ESTREAM_SetPlayBackDataCB(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_DATA_CB_PARAM pDataCBParam);

    public int NET_ESTREAM_StartListenVoiceTalk(NET_EHOME_LISTEN_VOICETALK_CFG pListenParam);

    public boolean NET_ESTREAM_SetVoiceTalkDataCB(long lHandle, NET_EHOME_VOICETALK_DATA_CB_PARAM pStruCBParam);

    public int NET_ESTREAM_SendVoiceTalkData(int lHandle, NET_EHOME_VOICETALK_DATA pVoicTalkData);

    public boolean NET_ESTREAM_StopVoiceTalk(long lHanlde);

    public boolean NET_ESTREAM_StopListenVoiceTalk(int lListenHandle);
}



