package com.wvp.hk.module.service.cms;



import com.wvp.hk.module.common.CommonClass;
import com.wvp.hk.module.common.CommonClass.NET_EHOME_IPADDRESS;

import com.wvp.hk.module.common.CommonClass.NET_EHOME_DEV_SESSIONKEY;
import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

public interface ISUPCMSByJNA extends Library {


    public static final int ENUM_DEV_ON = 0;
    public static final int ENUM_DEV_OFF = 1;
    public static final int ENUM_DEV_ADDRESS_CHANGED = 2;
    public static final int ENUM_DEV_AUTH = 3;
    public static final int ENUM_DEV_SESSIONKEY = 4;
    public static final int ENUM_DEV_DAS_REQ = 5;
    public static final int ENUM_DEV_DAS_REREGISTER = 7;
    public static final int ENUM_DEV_DAS_EHOMEKEY_ERROR = 9;
    public static final int ENUM_DEV_SESSIONKEY_ERROR = 10;
    public static final int ACTIVE_ACCESS_SECURITY = 0;
    public static final int AMS_ADDRESS = 1;
    public static final int SEND_PARAM = 2;

    public static final int ENUM_GET_NEXT_STATUS_SUCCESS = 1000;
    public static final int ENUM_GET_NETX_STATUS_NO_FILE = 1001;
    public static final int ENUM_GET_NETX_STATUS_NEED_WAIT = 1002;
    public static final int ENUM_GET_NEXT_STATUS_FINISH = 1003;
    public static final int ENUM_GET_NEXT_STATUS_FAILED = 1004;
    public static final int ENUM_GET_NEXT_STATUS_NOT_SUPPORT = 1005;

    public static final int NET_EHOME_GET_DEVICE_INFO = 1;
    public static final int NET_EHOME_GET_VERSION_INFO = 2;
    public static final int NET_EHOME_GET_DEVICE_CFG = 3;
    public static final int NET_EHOME_SET_DEVICE_CFG = 4;
    public static final int NET_EHOME_GET_COMPRESSION_CFG = 7;
    public static final int NET_EHOME_SET_COMPRESSION_CFG = 7;

    public static final int NET_EHOME_GET_GPS_CFG = 20;
    public static final int NET_EHOME_SET_GPS_CFG = 21;
    public static final int NET_EHOME_GET_PIC_CFG = 22;
    public static final int NET_EHOME_SET_PIC_CFG = 23;
    public static final int NET_EHOME_GET_NETWORK_CFG = 5;
    public static final int NET_EHOME_SET_NETWORK_CFG = 6;
    public static final int NET_EHOME_GET_WIRELESSINFO_CFG = 24;
    public static final int NET_EHOME_SET_WIRELESSINFO_CFG = 25;

    public static final int ENUM_SEARCH_TYPE_ERR = -1;
    public static final int ENUM_SEARCH_RECORD_FILE = 0;
    public static final int ENUM_SEARCH_PICTURE_FILE = 1;
    public static final int ENUM_SEARCH_FLOW_INFO = 2;
    public static final int ENUM_SEARCH_DEV_LOG = 3;
    public static final int ENUM_SEARCH_ALARM_HOST_LOG = 4;

    public static interface NET_EHOME_REGISTER_TYPE {
        public final int ENUM_UNKNOWN = -1;
        public final int ENUM_DEV_ON = 0;
        public final int ENUM_DEV_OFF = 1;
        public final int ENUM_DEV_ADDRESS_CHANGED = 2;
        public final int ENUM_DEV_AUTH = 3;
        public final int ENUM_DEV_SESSIONKEY = 4;
        public final int ENUM_DEV_DAS_REQ = 5;
        public final int ENUM_DEV_SESSIONKEY_REQ = 6;
        public final int ENUM_DEV_DAS_REREGISTER = 7;
        public final int ENUM_DEV_DAS_PINGREO = 8;
        public final int ENUM_DEV_DAS_EHOMEKEY_ERROR = 9;
    }

    public static class NET_EHOME_CMSCB_DATA extends Structure {
        public int lUserID;
        public int dwType;
        public int dwHandle;
        boolean bSucc;
        public Pointer pOutBuffer;
        public int dwOutLen;
        public int dwErrorNo;
        public byte[] byRes = new byte[32];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("lUserID", "dwType", "dwHandle", "bSucc", "pOutBuffer", "dwOutLen", "dwErrorNo", "byRes");
        }
    }

    public static class NET_EHOME_DEV_REG_INFO extends Structure {
        //??????????????????
        public int dwSize;
        //?????????
        public int dwNetUnitType;
        //?????? ID?????????????????? 256 ??????????????????????????????MAX_DEVICE_ID_LEN??????
        public byte[] byDeviceID = new byte[CommonClass.MAX_DEVICE_ID_LEN];
        //???????????????????????????????????? 24 ?????????
        public byte[] byFirmwareVersion = new byte[24];
        //????????????
        public NET_EHOME_IPADDRESS struDevAdd;
        //???????????????
        public int dwDevType;
        //???????????????
        public int dwManufacture;
        //????????????????????????????????????????????????????????????????????????????????????????????? 32 ??????
        public byte[] byPassWord = new byte[32];
        //????????????????????????????????? 12 ??????????????????????????????NET_EHOME_SERIAL_LEN?????????
        public byte[] sDeviceSerial = new byte[CommonClass.NET_EHOME_SERIAL_LEN];
        //???????????????
        public byte byReliableTransmission;
        //?????????????????????
        public byte byWebSocketTransmission;
        //????????????????????????0-??????1-??????
        public byte bySupportRedirect;
        //?????????????????????????????????????????? 6 ?????????
        public byte[] byDevProtocolVersion = new byte[6];
        //?????? 5.0 ?????? ISUP ??????????????????????????????????????? 16 ??????????????????????????????MAX_MASTER_KEY_LEN?????????
        public byte[] bySessionKey = new byte[16];
        //???????????????0-????????????????????????,1-????????????2-????????????
        public byte byMarketType;
        //???????????????????????? 25 ?????????
        public byte[] byRes = new byte[26];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwNetUnitType", "byDeviceID", "byFirmwareVersion",
                    "struDevAdd", "dwDevType", "dwManufacture", "byPassWord", "sDeviceSerial",
                    "byReliableTransmission", "byWebSocketTransmission", "bySupportRedirect",
                    "byDevProtocolVersion", "bySessionKey", "byMarketType", "byRes");
        }
    }

    //??????????????????????????????V12???
    public static class NET_EHOME_DEV_REG_INFO_V12 extends Structure {
        //????????????
        public NET_EHOME_DEV_REG_INFO struRegInfo;
        //??????????????????????????????
        public NET_EHOME_IPADDRESS struRegAddr;
        //?????????????????????????????? 64 ??????????????????????????????MAX_DEVNAME_LEN_EX?????????
        public byte[] sDevName = new byte[CommonClass.MAX_DEVNAME_LEN];
        //??????????????????????????????????????????????????????????????????????????????????????? 64 ??????????????????????????????MAX_FULL_SERIAL_NUM_LEN?????????
        public byte[] byDeviceFullSerial = new byte[CommonClass.MAX_FULL_SERIAL_NUM_LEN];
        //???????????????????????? 128 ?????????
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegInfo", "struRegAddr", "sDevName", "byDeviceFullSerial", "byRes");
        }
    }

    public static class NET_EHOME_BLACKLIST_SEVER extends Structure {
        public NET_EHOME_IPADDRESS struAdd;
        public byte[] byServerName = new byte[32];
        public byte[] byUserName = new byte[32];
        public byte[] byPassWord = new byte[32];
        public byte[] byRes = new byte[64];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("struAdd", "byServerName", "byUserName", "byPassWord", "byRes");
        }
    }

    public static class NET_EHOME_SERVER_INFO extends Structure {
        public int dwSize;
        public int dwKeepAliveSec;
        public int dwTimeOutCount;
        public NET_EHOME_IPADDRESS struTCPAlarmSever;
        public NET_EHOME_IPADDRESS struUDPAlarmSever;
        public int dwAlarmServerType;
        public NET_EHOME_IPADDRESS struNTPSever;
        public int dwNTPInterval;
        public NET_EHOME_IPADDRESS struPictureSever;
        public int dwPicServerType;
        public NET_EHOME_BLACKLIST_SEVER struBlackListServer;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwKeepAliveSec", "dwTimeOutCount", "struTCPAlarmSever",
                    "struUDPAlarmSever", "dwAlarmServerType", "struNTPSever", "dwNTPInterval", "struPictureSever",
                    "dwPicServerType", "struBlackListServer", "byRes");
        }
    }

    public static class NET_EHOME_VERSION_INFO extends Structure {
        public int dwSize;
        public byte[] sSoftwareVersion = new byte[CommonClass.MAX_VERSION_LEN];
        public byte[] sDSPSoftwareVersion = new byte[CommonClass.MAX_VERSION_LEN];
        public byte[] sPanelVersion = new byte[CommonClass.MAX_VERSION_LEN];
        public byte[] sHardwareVersion = new byte[CommonClass.MAX_VERSION_LEN];
        public byte[] byRes = new byte[124];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "sSoftwareVersion", "sDSPSoftwareVersion", "sPanelVersion", "sHardwareVersion", "byRes");
        }
    }

    public static class NET_EHOME_DEVICE_CFG extends Structure {
        public int dwSize;
        public byte[] sServerName = new byte[CommonClass.MAX_DEVICE_NAME_LEN];
        public int dwServerID;
        public int dwRecycleRecord;
        public int dwServerType;
        public int dwChannelNum;
        public int dwHardDiskNum;
        public int dwAlarmInNum;
        public int dwAlarmOutNum;
        public int dwRS232Num;
        public int dwRS485Num;
        public int dwNetworkPortNum;
        public int dwAuxoutNum;
        public int dwAudioNum;
        public byte[] sSerialNumber = new byte[CommonClass.MAX_SERIALNO_LEN];
        public int dwMajorScale;
        public int dwMinorScale;
        public byte[] byRes = new byte[292];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "sServerName", "dwServerID", "dwRecycleRecord", "dwServerType", "dwChannelNum",
                    "dwHardDiskNum", "dwAlarmInNum", "dwAlarmOutNum", "dwRS232Num", "dwRS485Num", "dwNetworkPortNum",
                    "dwAuxoutNum", "dwAudioNum", "sSerialNumber", "dwMajorScale", "dwMinorScale", "byRes");
        }
    }

    public static class NET_EHOME_COMPRESSION_CFG extends Structure {
        public int dwSize;
        public byte byStreamType;
        public byte byPicQuality;
        public byte byBitRateType;
        public byte byRes1;
        public int dwResolution;
        public int dwVideoBitRate;
        public int dwMaxBitRate;
        public int dwVideoFrameRate;
        public short wIntervalFrameI;
        public byte byIntervalBPFrame;
        public byte[] byRes = new byte[41];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byStreamType", "byPicQuality", "byBitRateType", "byRes1", "dwResolution",
                    "dwVideoBitRate", "dwMaxBitRate", "dwVideoFrameRate", "wIntervalFrameI", "byIntervalBPFrame", "byRes");
        }
    }

    public static class NET_EHOME_COMPRESSION_COND extends Structure {
        public int dwSize;
        public int dwChannelNum;
        public byte byCompressionType;
        public byte[] byRes = new byte[23];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannelNum", "byCompressionType", "byRes");
        }
    }

    public static class NET_EHOME_PIC_CFG extends Structure {
        public int dwSize;
        public byte[] byChannelName = new byte[32];
        public byte bIsShowChanName;
        public short wChanNameXPos;
        public short wChanNameYPos;
        public byte bIsShowOSD;
        public short wOSDXPos;
        public short wOSDYPos;
        public byte byOSDType;

        public byte byOSDAtrib;
        public byte[] byRes1 = new byte[2];
        public byte bIsShowWeek;
        public byte[] byRes2 = new byte[64];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byChannelName", "bIsShowChanName", "wChanNameXPos", "wChanNameYPos",
                    "bIsShowOSD", "wOSDXPos", "wOSDYPos", "byOSDType", "byOSDAtrib", "byRes1", "bIsShowWeek", "byRes2");
        }
    }

    public static class NET_EHOME_PREVIEWINFO_IN_V11 extends Structure {
        public int iChannel;
        public int dwStreamType;
        public int dwLinkMode;
        public NET_EHOME_IPADDRESS struStreamSever;
        public byte byDelayPreview;
        public byte[] byRes = new byte[31];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("iChannel", "dwStreamType", "dwLinkMode", "struStreamSever",
                    "byDelayPreview", "byRes");
        }
    }

    public static class NET_EHOME_PREVIEWINFO_OUT extends Structure {
        public int lSessionID;
        public int lHandle;
        public byte[] byRes = new byte[124];

        protected List<String> getFieldOrder() {
            return Arrays.asList("lSessionID", "lHandle", "byRes");
        }
    }

    public static class NET_EHOME_PUSHSTREAM_IN extends Structure {
        public int dwSize;
        public int lSessionID;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(
                    "dwSize",
                    "lSessionID",
                    "byRes"
            );
        }
    }

    public static class NET_EHOME_PUSHSTREAM_OUT extends Structure {
        public int dwSize;
//        public int lHandle;
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(
                    "dwSize",
                    "byRes"
            );
        }
    }

    public interface fVoiceDataCallBack extends Callback {
        public void invoke(int iVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, int dwEncodeType,
                           byte byAudioFlag, Pointer pUser);
    }

    public interface DEVICE_REGISTER_CB extends Callback {
        public boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer,
                              int dwOutLen, Pointer pInBuffer, int dwInLen, Pointer pUser);

    }

    public static class NET_EHOME_CMS_LISTEN_PARAM extends Structure {
        public NET_EHOME_IPADDRESS struAddress;
        public DEVICE_REGISTER_CB fnCB;
        public Pointer pUserData;
        public byte[] byRes = new byte[32];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("struAddress", "fnCB", "pUserData", "byRes");
        }
    }

    public static class NET_EHOME_DEV_SESSIONKEY_EHOME50 extends Structure {
        public byte[] sDeviceID = new byte[256];
        public byte[] sSessionKey = new byte[16];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("sDeviceID", "sSessionKey");
        }
    }


    public static class NET_EHOME_XML_CFG extends Structure {
        public Pointer pCmdBuf;
        public int dwCmdLen;
        public Pointer pInBuf;
        public int dwInSize;
        public Pointer pOutBuf;
        public int dwOutSize;
        public int dwSendTimeOut;
        public int dwRecvTimeOut;
        public Pointer pStatusBuf;
        public int dwStatusSize;
        public byte[] byRes = new byte[24];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pCmdBuf", "dwCmdLen", "pInBuf", "dwInSize", "pOutBuf", "dwOutSize", "dwSendTimeOut",
                    "dwRecvTimeOut", "pStatusBuf", "dwStatusSize", "byRes");
        }
    }

    /**
     * function:request playback stream data -- [InParam] structure
     */
    public class NET_EHOME_PUSHPLAYBACK_IN extends Structure {
        /**
         * structure size
         */
        public int dwSize;
        /**
         * Session ID???return by NET_ECMS_StartPlayBack
         */
        public int lSessionID;
        /**
         * stream encryption key ???get by 2 times MD5 calc
         */
        //public byte[] byKeyMD5 = new byte[32];

        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "lSessionID", "byRes");
        }

    }

    /**
     * function:request playback stream data -- [OutParam] structure
     */
    public class NET_EHOME_PUSHPLAYBACK_OUT extends Structure {

        /**
         * structure size
         */
        public int dwSize;
        /**
         * session handle of async CB
         */
        public long lHandle;

        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "lHandle", "byRes");
        }

    }

    /**
     * ????????????????????????????????????
     */
    public class NET_EHOME_PLAYBACK_INFO_IN extends Structure {
        /**
         * structure size
         */
        public int dwSize;
        /**
         * channel Number
         */
        public int dwChannel;
        /**
         * playback download mode???0- ByFilename???1- byTime
         */
        public byte byPlayBackMode;

        public byte byStreamPackage;

        public byte byLinkMode;

        public byte byLinkEncrypt;
        /**
         * playback download mode union
         */
        public UnionPlayBackMode unionPlayBackMode = new UnionPlayBackMode();
        /**
         * stream server address
         */
        public NET_EHOME_IPADDRESS struStreamSever = new NET_EHOME_IPADDRESS();

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannel", "byPlayBackMode", "byStreamPackage", "byLinkMode", "byLinkEncrypt",
                    "unionPlayBackMode", "struStreamSever");
        }

    }

    /**
     * ????????????????????????????????????
     */
    public class NET_EHOME_PLAYBACK_INFO_OUT extends Structure {

        /**
         * SessionID???return from device???0 -- invalid
         */
        public int lSessionID;
        /**
         *
         */
        public byte[] byRes = new byte[128];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("lSessionID", "byRes");
        }

    }

    public class UnionPlayBackMode extends Union {

        /**
         * union size, max size: 512 byte
         */
        public byte[] byLen = new byte[512];

        public StruPlayBackbyName struPlayBackbyName = new StruPlayBackbyName();

        public StruPlayBackbyTime struPlayBackbyTime = new StruPlayBackbyTime();

    }

    /**
     * ????????????????????????????????????
     */
    public class StruPlayBackbyName extends Structure {
        /**
         * ????????????????????????
         */
        public byte[] szFileName = new byte[100];
        /**
         * ????????????????????????0-??????????????????1-????????????????????????
         */
        public int dwSeekType;
        /**
         * ???????????????????????????????????????????????????
         */
        public int dwFileOffset;
        /**
         * ??????????????????????????? 0 ?????????????????????????????????
         */
        public int dwFileSpan;

        public byte byStreamType;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("szFileName", "dwSeekType", "dwFileOffset", "dwFileSpan", "byStreamType");
        }

    }

    /**
     * ????????????????????????????????????
     */
    public class StruPlayBackbyTime extends Structure {
        /**
         * ?????????????????????
         */
        public CommonClass.NET_EHOME_TIME struStartTime;
        /**
         * ?????????????????????
         */
        public CommonClass.NET_EHOME_TIME struStopTime;
        /**
         * time type???0-device local time -- device OSD time???1-UTC time???
         */
        public byte byLocalOrUTC;
        /**
         * ???????????????????????????0-???????????????????????????1-?????????????????????????????? byLocalOrUTC ??? 1?????????????????????
         */
        public byte byDuplicateSegment;

        public byte byStreamType;

        public byte byHls;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("struStartTime", "struStopTime", "byLocalOrUTC", "byDuplicateSegment", "byStreamType", "byHls");
        }

    }

    public static class NET_EHOME_PTXML_PARAM extends Structure {
        public Pointer pRequestUrl;
        public int dwRequestUrlLen;
        public Pointer pCondBuffer;
        public int dwCondSize;
        public Pointer pInBuffer;
        public int dwInSize;
        public Pointer pOutBuffer;
        public int dwOutSize;
        public int dwReturnedXMLLen;
        public int dwRecvTimeOut;
        public int dwHandle;
        public byte[] byRes = new byte[24];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pRequestUrl", "dwRequestUrlLen", "pCondBuffer", "dwCondSize", "pInBuffer", "dwInSize",
                    "pOutBuffer", "dwOutSize", "dwReturnedXMLLen", "dwRecvTimeOut", "dwHandle", "byRes");
        }
    }

    public static class NET_EHOME_XML_REMOTE_CTRL_PARAM extends Structure {
        public int dwSize;
        public Pointer lpInbuffer;
        public int dwInBufferSize;
        public int dwSendTimeOut;
        public int dwRecvTimeOut;
        public Pointer lpOutBuffer;
        public int dwOutBufferSize;
        public Pointer lpStatusBuffer;
        public int dwStatusBufferSize;
        public byte[] byRes = new byte[16];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "lpInbuffer", "dwInBufferSize", "dwSendTimeOut", "dwRecvTimeOut",
                    "lpOutBuffer", "dwOutBufferSize", "lpStatusBuffer", "dwStatusBufferSize", "byRes");
        }
    }

    public static class NET_EHOME_CONFIG extends Structure {
        public Pointer pCondBuf;
        public int dwCondSize;
        public Pointer pInBuf;
        public int dwInSize;
        public Pointer pOutBuf;
        public int dwOutSize;
        public byte[] byRes = new byte[40];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pCondBuf", "dwCondSize", "pInBuf", "dwInSize", "pOutBuf",
                    "dwOutSize", "byRes");
        }
    }

    public static class NET_EHOME_DEVICE_INFO extends Structure {
        //??????????????????
        public int dwSize;
        //?????????????????????
        public int dwChannelNumber;
        //??????????????????????????????????????????????????????
        public int dwChannelAmount;
        //???????????????1-????????????????????????3-????????????????????????30-??????????????????40-???????????????
        public int dwDevType;
        //???????????????????????????
        public int dwDiskNumber;
        //????????????????????????????????? 128 ??????????????????????????????MAX_SERIALNO_LEN?????????
        public byte[] sSerialNumber = new byte[128];
        //??????????????????????????????????????????
        public int dwAlarmInPortNum;
        //?????????????????????
        public int dwAlarmInAmount;
        //??????????????????????????????????????????
        public int dwAlarmOutPortNum;
        //?????????????????????
        public int dwAlarmOutAmount;
        //????????????????????????
        public int dwStartChannel;
        //???????????????????????????
        public int dwAudioChanNum;
        //???????????????????????????????????????
        public int dwMaxDigitChannelNum;
        //??????????????????????????????0-G.722???1-G.711U???2-G.711A???3-G.726???4-AAC???5-MP2L2???
        public int dwAudioEncType;
        //SIM ??????????????????????????????????????????????????????128 ??????????????????????????????MAX_SERIALNO_LEN??????
        public byte[] sSIMCardSN = new byte[128];
        //SIM ????????????????????????????????????????????????????????? 32 ??????????????????????????????MAX_PHOMENUM_LEN?????????
        public byte[] sSIMCardPhoneNum = new byte[32];
        //???????????????????????????0-????????????1-?????? 1 ??????2-?????? 2 ?????????????????????
        public int dwSupportZeroChan;
        //???????????????????????????????????? 10000???
        public int dwStartZeroChan;
        //0-?????????????????????1-????????????
        public int dwSmartType;

        public byte[] byRes = new byte[160];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannelNumber", "dwChannelAmount", "dwDevType", "dwDiskNumber",
                    "sSerialNumber", "dwAlarmInPortNum", "dwAlarmInAmount", "dwAlarmOutPortNum",
                    "dwAlarmOutAmount", "dwStartChannel", "dwAudioChanNum", "dwMaxDigitChannelNum",
                    "dwAudioEncType", "sSIMCardSN", "sSIMCardPhoneNum", "dwSupportZeroChan", "dwStartZeroChan",
                    "dwSmartType", "byRes");
        }
    }

    ;

    public static final int MACADDR_LEN = 6;
    public static final int NAME_LEN = 32;
    public static final int PASSWD_LEN = 16;

    public static class NET_EHOME_IPADDR extends Structure {
        public byte[] sIpV4 = new byte[16];
        public byte[] sIpV6 = new byte[128];

        protected List<String> getFieldOrder() {
            return Arrays.asList("sIpV4", "sIpV6");
        }
    }

    public static class NET_EHOME_ETHERNET extends Structure {
        public NET_EHOME_IPADDR struDevIP;
        public NET_EHOME_IPADDR struDevIPMask;
        public int dwNetInterface;
        public short wDevPort;
        public short wMTU;
        public byte[] byMACAddr = new byte[MACADDR_LEN];
        public byte[] byRes = new byte[2];

        protected List<String> getFieldOrder() {
            return Arrays.asList("struDevIP", "struDevIPMask", "dwNetInterface", "wDevPort", "wMTU", "byMACAddr", "byRes");
        }
    }

    public static class NET_EHOME_PPPOECFG extends Structure {
        public int dwPPPoE;
        public byte[] sPPPoEUser = new byte[NAME_LEN];
        public byte[] sPPPoEPassword = new byte[PASSWD_LEN];
        public NET_EHOME_IPADDR struPPPoEIP;

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwPPPoE", "sPPPoEUser", "sPPPoEPassword", "struPPPoEIP");
        }
    }

    public static class NET_EHOME_NETWORK_CFG extends Structure {
        public int dwSize;
        public NET_EHOME_ETHERNET struEtherNet;
        public NET_EHOME_IPADDR struGateWayIP;
        public NET_EHOME_IPADDR struMultiCastIP;
        public NET_EHOME_IPADDR struDDNSServer1IP;
        public NET_EHOME_IPADDR struDDNSServer2IP;
        public NET_EHOME_IPADDR struAlarmHostIP;
        public short wAlarmHostPort;
        public short wIPResolverPort;
        public NET_EHOME_IPADDR struIPResolver;
        public NET_EHOME_PPPOECFG struPPPoE;
        public short wHTTPPort;
        public byte[] byRes = new byte[674];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struEtherNet", "struGateWayIP", "struMultiCastIP", "struDDNSServer1IP",
                    "struDDNSServer2IP", "struAlarmHostIP", "wAlarmHostPort", "wIPResolverPort", "struIPResolver",
                    "struPPPoE", "wHTTPPort", "byRes");
        }
    }

    public static class NET_EHOME_VOICETALK_PARA extends Structure {
        //????????????????????????0-??????????????????1-????????????????????????????????????????????????
        public boolean bNeedCBNoEncData;
        //??????????????????????????????
        public fVoiceDataCallBack fVoiceDataCallBack;
        //??????????????????????????????0-G.722???1-G.711U???2-G.711A???3-G.726???4-AAC???5-MP2L2???6-PCM???
        public int dwEncodeType;
        //???????????????
        public Pointer pUser;
        //???????????????0-???????????????1-???????????????
        public byte byVoiceTalk;
        //??????????????????????????????0-G.722???1-G.711U???2-G.711A???3-G.726???4-AAC???5-MP2L2???6-PCM???
        public byte byDevAudioEnc;
        //??????????????? 0?????????????????? 2 ?????????
        public byte[] byRes1 = new byte[2];
        //??????????????????????????????????????????
        public int lHandle;
        //??????????????? 0?????????????????? 56 ?????????
        public byte[] byRes = new byte[56];

        protected List<String> getFieldOrder() {
            return Arrays.asList("bNeedCBNoEncData", "fVoiceDataCallBack", "dwEncodeType", "pUser", "byVoiceTalk",
                    "byDevAudioEnc", "byRes1", "lHandle", "byRes");
        }
    }

    public static class NET_EHOME_REC_FILE_COND extends Structure {
        public int dwChannel;
        public int dwRecType;
        public CommonClass.NET_EHOME_TIME struStartTime = new CommonClass.NET_EHOME_TIME();
        public CommonClass.NET_EHOME_TIME struStopTime = new CommonClass.NET_EHOME_TIME();
        public int dwStartIndex;
        public int dwMaxFileCountPer;
        public byte byLocalOrUTC;
        public byte[] byRes = new byte[63];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwChannel", "dwRecType", "struStartTime", "struStopTime", "dwStartIndex",
                    "dwMaxFileCountPer", "byLocalOrUTC", "byRes");
        }
    }

    public static class NET_EHOME_PIC_FILE_COND extends Structure {
        public int dwChannel;
        public int dwPicType;
        public CommonClass.NET_EHOME_TIME struStartTime = new CommonClass.NET_EHOME_TIME();
        public CommonClass.NET_EHOME_TIME struStopTime = new CommonClass.NET_EHOME_TIME();
        public int dwStartIndex;
        public int dwMaxFileCountPer;
        public int byLocalOrUTC;
        public byte[] byRes = new byte[63];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwChannel", "dwPicType", "struStartTime", "struStopTime", "dwStartIndex",
                    "dwMaxFileCountPer", "byLocalOrUTC", "byRes");
        }
    }

    public static class NET_EHOME_FLOW_COND extends Structure {
        public byte bySearchMode;
        public byte[] byRes = new byte[3];
        public CommonClass.NET_EHOME_TIME struStartTime = new CommonClass.NET_EHOME_TIME();
        public CommonClass.NET_EHOME_TIME struStopTime = new CommonClass.NET_EHOME_TIME();
        public int dwStartIndex;
        public int dwMaxFileCountPer;
        public int byLocalOrUTC;
        public byte[] byRes1 = new byte[63];

        protected List<String> getFieldOrder() {
            return Arrays.asList("bySearchMode", "byRes", "struStartTime", "struStopTime", "dwStartIndex",
                    "dwMaxFileCountPer", "byLocalOrUTC", "byRes");
        }
    }

    public static class NET_EHOME_REC_FILE extends Structure {
        public int dwSize;
        public byte[] szFileName = new byte[100];
        public CommonClass.NET_EHOME_TIME struStartTime = new CommonClass.NET_EHOME_TIME();
        public CommonClass.NET_EHOME_TIME struStopTime = new CommonClass.NET_EHOME_TIME();
        public int dwFileSize;
        public int dwFileMainType;
        public int dwFileSubType;
        public int dwFileIndex;
        public byte byTimeDiffH;
        public byte byTimeDiffM;
        public byte[] byRes = new byte[126];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "szFileName", "struStartTime", "struStopTime", "dwFileSize",
                    "dwFileMainType", "dwFileSubType", "dwFileIndex", "byTimeDiffH", "byTimeDiffM", "byRes");
        }
    }

    public static class NET_EHOME_PIC_FILE extends Structure {
        public int dwSize;
        public String szFileName;
        public CommonClass.NET_EHOME_TIME struPicTime = new CommonClass.NET_EHOME_TIME();
        public int dwFileSize;
        public int dwFileMainType;
        public int dwFileIndex;
        public byte byTimeDiffH;
        public byte byTimeDiffM;
        public byte[] byRes = new byte[126];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "szFileName", "struPicTime", "dwFileSize",
                    "dwFileMainType", "dwFileIndex", "byTimeDiffH", "byTimeDiffM", "byRes");
        }
    }

    public static class NET_EHOME_VOICE_TALK_IN extends Structure {
        //?????????
        public int dwVoiceChan;
        //???????????????
        public NET_EHOME_IPADDRESS struStreamSever = new NET_EHOME_IPADDRESS();
        //????????????????????????
        public int[] byEncodingType = new int[9];
        public byte byLinkEncrypt;  //
        //??????????????????,???????????????????????????1???????????????????????????????????????
        public byte byBroadcast;
        //???????????????????????????,0~15?????????????????????,?????????byBroadcast???1???,0?????????????????????????????????byBroadcast???0???????????????????????????????????????
        public byte byBroadLevel;
        //??????????????????,0~15??????????????????,?????????byBroadcast???1???,0??????????????????????????????byBroadcast???0???????????????????????????????????????
        public byte byBroadVolume;
        //??????????????? 0-??????, 1-16kHZ, 2-32kHZ, 3-48kHZ, 4-44.1kHZ, 5-8kHZ
        public byte byAudioSamplingRate;
        public byte[] byRes = new byte[114];

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(
                    "dwVoiceChan",
                    "struStreamSever",
                    "byEncodingType",
                    "byLinkEncrypt",
                    "byBroadcast",
                    "byBroadLevel",
                    "byBroadVolume",
                    "byAudioSamplingRate",
                    "byRes"
            );
        }
    }

    public static class NET_EHOME_VOICE_TALK_OUT extends Structure {
        public int lSessionID;
        public int lHandle;
        public byte[] byRes = new byte[124];

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(
                    "lSessionID",
                    "lHandle",
                    "byRes"
            );
        }
    }

    public static class NET_EHOME_PUSHVOICE_IN extends Structure {
        public int dwSize;
        public int lSessionID;
        public byte[] byToken = new byte[64];
        public byte[] byRes = new byte[64];

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(
                    "dwSize",
                    "lSessionID",
                    "byToken",
                    "byRes"
            );
        }
    }

    public static class NET_EHOME_PUSHVOICE_OUT extends Structure {
        public int dwSize;
        public int lHandle;
        public byte[] byRes = new byte[124];

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(
                    "dwSize",
                    "lHandle",
                    "byRes"
            );
        }
    }

    public static class NET_EHOME_REMOTE_CTRL_PARAM extends Structure
    {
        //???????????????
        public int dwSize;
        //????????????????????????????????????NET_ECMS_RemoteControl ???????????????(dwCommand)???????????????????????????????????????
        public Pointer lpCondBuffer;
        //??????????????????????????????
        public int dwCondBufferSize;
        //????????????????????????????????????NET_ECMS_RemoteControl ???????????????(dwCommand)???????????????????????????????????????
        public Pointer lpInbuffer;
        //???????????????????????????
        public int dwInBufferSize;
        //??????????????? 0?????????????????? 32 ?????????
        public byte[] byRes = new byte[32];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "lpCondBuffer", "dwCondBufferSize", "lpInbuffer", "dwInBufferSize", "byRes");
        }
    }

    public static class NET_EHOME_PTZ_PARAM extends Structure
    {
        //???????????????
        public int dwSize;
        //PTZ ??????????????????????????????
        public byte byPTZCmd;
        //PTZ ?????????0-?????????1-?????????
        public byte byAction;
        //PTZ ???????????????????????? 0 ??? 70????????????????????????????????????
        public byte bySpeed;
        //???????????????????????? 29 ?????????
        public byte[] byRes = new byte[29];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byPTZCmd", "byAction", "bySpeed", "byRes");
        }
    }


    public boolean NET_ECMS_Init();


    public int NET_ECMS_GetBuildVersion();


    public boolean NET_ECMS_SetSDKLocalCfg(int enumType, Pointer lpInBuff);

    public boolean NET_ECMS_SetSDKInitCfg(int enumType, Pointer lpInBuff);

    public boolean NET_ECMS_GetSDKLocalCfg(int enumType, Pointer lpOutBuff);

    public boolean NET_ECMS_GetDevConfig(int lUserID, int dwCommand, NET_EHOME_CONFIG lpConfig, int dwConfigSize);

    public boolean NET_ECMS_SetDevConfig(int lUserID, int dwCommand, Pointer lpConfig, int dwConfigSize);

    public int NET_ECMS_StartListen(NET_EHOME_CMS_LISTEN_PARAM lpCMSListenPara);

    public boolean NET_ECMS_StopListen(int lHandle);

    public boolean NET_ECMS_ForceLogout(int lUserID);

    public boolean NET_ECMS_SetLogToFile(long lLogLevel, String strLogDir, boolean bAutoDel);

    public int NET_ECMS_StartVoiceTalk(int lUserID, int dwVoiceChan,
                                              NET_EHOME_VOICETALK_PARA pVoiceTalkPara);

    public boolean NET_ECMS_SendVoiceTransData(int lUserID, Pointer pSendBuf, int dwBufSize);

    public boolean NET_ECMS_StopVoiceTalk(int iVoiceHandle);

    public int NET_ECMS_GetLastError();

    public boolean NET_ECMS_Fini();

    public boolean NET_ECMS_SetDeviceSessionKey(Pointer pDeviceKey);

    public boolean NET_ECMS_StartGetRealStreamV11(int lUserID, NET_EHOME_PREVIEWINFO_IN_V11 pPreviewInfoIn, Pointer pPreviewInfoOut);

    public boolean NET_ECMS_StartPushRealStream(int lUserID, NET_EHOME_PUSHSTREAM_IN pPushInfoIn, NET_EHOME_PUSHSTREAM_OUT pPushInfoOut);

    public boolean NET_ECMS_StartPushPlayBack(int lUserID, NET_EHOME_PUSHPLAYBACK_IN pPushInfoIn, Pointer pPushInfoOut);

    public boolean NET_ECMS_StopGetRealStream(long lUserID, int iSessionID);

    public boolean NET_ECMS_StartPlayBack(long lUserID, NET_EHOME_PLAYBACK_INFO_IN pPlayBackInfoIn, Pointer pPlayBackInfoOut);

    public boolean NET_ECMS_StopPlayBack(long lUserID, int iSessionID);

    public boolean NET_ECMS_StopVoiceTalkWithStmServer(long lUserID, long lSessionID);

    public boolean NET_ECMS_StartVoiceWithStmServer(int lUserID, Pointer lpVoiceTalkIn, Pointer lpVoiceTalkOut);

    public boolean NET_ECMS_StartPushVoiceStream(int lUserID, Pointer lpPushParamIn/*NET_EHOME_PUSHVOICE_IN*/, Pointer lpPushParamOut/*NET_EHOME_PUSHVOICE_OUT*/);

    public boolean NET_ECMS_XMLConfig(int lUserID, Pointer pXmlCfg, int dwConfigSize);

    public boolean NET_ECMS_GetPTXMLConfig(long lUserID, NET_EHOME_PTXML_PARAM pPTXMLParam);

    public boolean NET_ECMS_PutPTXMLConfig(long lUserID, NET_EHOME_PTXML_PARAM pPTXMLParam);

    public boolean NET_ECMS_PostPTXMLConfig(long lUserID, NET_EHOME_PTXML_PARAM pPTXMLParam);

    public boolean NET_ECMS_DeletePTXMLConfig(long lUserID, NET_EHOME_PTXML_PARAM pPTXMLParam);

    public boolean NET_ECMS_XMLRemoteControl(long lUserID, Pointer lpCtrlParam, int dwCtrlSize);

    public long NET_ECMS_StartFindFile_V11(long lUserID, long lSearchType, Pointer pFindCond, int dwCondSize);

    public long NET_ECMS_FindNextFile_V11(long lHandle, Pointer pFindData, int dwDataSize);

    public boolean NET_ECMS_StopFindFile(long lHandle);

    public boolean NET_ECMS_ISAPIPassThrough(long lUserID, Pointer lpParam);


    public boolean NET_ECMS_RemoteControl(long lUserId, int dwCommand, NET_EHOME_REMOTE_CTRL_PARAM lpCtrlParam);
}

