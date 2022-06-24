package com.wvp.hk.module.service.stream;

import com.alibaba.fastjson.JSONObject;
import com.wvp.common.core.redis.RedisCache;
import com.wvp.domain.WvpDevice;
import com.wvp.hk.module.common.CommonClass;
import com.wvp.hk.module.common.CommonClass.StringPointer;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.storage.ISUPSSByJNA;
import com.wvp.hk.module.service.stream.ISUPStreamByJNA.*;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.wvp.hk.module.service.util.HaiKangSdkAccess;
import com.wvp.service.IWvpDeviceService;
import com.wvp.utils.G711Converter;
import com.wvp.websocket.WebSocketUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.websocket.Session;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;


import static com.wvp.hk.module.service.stream.ISUPStreamByJNA.*;


@Component
public class EStream {


    //CMS流媒体服务运行状态
    private boolean m_bIsRunning = false;

    //初始化CMS结构体到java类对象
    public static ISUPStreamByJNA m_StreamInstance = null;

    //CMS预览监听参数
    private static NET_EHOME_LISTEN_PREVIEW_CFG m_struPreviewListenParam = new NET_EHOME_LISTEN_PREVIEW_CFG();
    //CMS回放监听参数
    private static NET_EHOME_PLAYBACK_LISTEN_PARAM m_struPlaybackListenParam = new NET_EHOME_PLAYBACK_LISTEN_PARAM();
    //CMS语音对讲监听参数
    private static NET_EHOME_LISTEN_VOICETALK_CFG m_struVoiceTalkListenParam = new NET_EHOME_LISTEN_VOICETALK_CFG();


    //开启 SMS 预览监听服务并接收来自设备的连接请求 连接状态
    public static int m_lPreviewListenHandle = -1;
    //开启 SMS 回放监听服务并接收来自设备的连接请求 连接状态
    public static int m_lPlayBackListenHandle = -1;
    //开启 SMS 语音对讲监听服务并接收来自设备的连接请求 连接状态
    public static int m_lVoiceTalkListenHandle = -1;

    //预览请求的响应 句柄
    public static int m_lPreviewNewLinkHandle = -1;
    //语音对讲请求的响应 句柄
    public static int m_lVoiceTalkNewLinkHandle = -1;
    //回放请求的响应 句柄
    public static int m_lPlaybackNewLinkHandle = -1;

    public static int m_iSessionIDPlayBack = -1;

    //预览回调函数，用于接收预览请求的响应报文。
    private static FPreviewNewLinkCallBack m_fnPreviewNewLinkCallBack = null;

    private static FPreviewDataCallback fnPreviewDateCallBack = null;
    //预览回调函数，用于接收回放请求的响应报文。
    public static FPlaybackNewLinkCallBack m_fnPlaybackNewLinkCallBack = null;
    public static FPlaybackDataCallback m_fnPlaybackDataCallBack = null;
    //语音对讲回调函数，用于接收语音对讲请求的响应报文。
    public static FVoiceTalkNewLinkCallBack m_fnVoiceTalkNewLinkCallBack = null;

    //语音对讲的数据回调函数，用于接收语音对讲的数据
    public static FVoiceTalkDataCallBack m_fnVoiceTalkDataCallBack = null;
    public boolean bSendThread = false;

    //预览请求Map 预览句柄、推流实例
    public static HashMap<Integer, HaiKangSdkAccess> haiKangSdkAccessHashMap = new HashMap<>();

    //对讲Map LuserId->voiceHandle
    public static HashMap<String,String> voiceMap = new HashMap<>();

    @Autowired
    private RedisCache redisCache;

    public static RedisCache staticRedisCache;

    @Autowired
    private IWvpDeviceService wvpDeviceService;

    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(EStream.class);

    @Value("${ehome.in-ip}")
    private String ehomeInIp;

    @Value("${ehome.sms-preview-prot}")
    private short ehomeSmsPreViewProt;

    @Value("${ehome.sms-playback-prot}")
    private short ehomeSmsPlayBackProt;

    @Value("${ehome.sms-voicetalk-prot}")
    private short ehomeSmsVoiceTalkProt;

    @Value("${ehome.rtmp-url}")
    private String rtmpUrl;

    @Value("${ehome.hls-url}")
    private String hlsUrl;

    //初始化SMS服务
    @PostConstruct
    public void EStreamInit(){
        staticRedisCache=this.redisCache;
        if(m_StreamInstance == null)
        {
            if(!CreateStreamInstance())
            {
                logger.error("Load Stream module fail");
                return;
            }
        }
        /*
            配置SMS流媒体服务的端口、Ip、接入方式
         */
        {
            CommonMethod.GetListenInfo().struPreviewListenParam.struIPAdress.szIP=ehomeInIp.getBytes();
            CommonMethod.GetListenInfo().struPreviewListenParam.struIPAdress.wPort=ehomeSmsPreViewProt;
            ///监听请求的接入方式：0-TCP，1-UDP.2-HRUDP
            CommonMethod.GetListenInfo().struPreviewListenParam.byLinkMode=0;


            CommonMethod.GetListenInfo().struPlaybackListenParam.struIPAddress.szIP=ehomeInIp.getBytes();
            CommonMethod.GetListenInfo().struPlaybackListenParam.struIPAddress.wPort=ehomeSmsPlayBackProt;
            ///监听请求的接入方式：0-TCP，1-UDP.2-HRUDP
            CommonMethod.GetListenInfo().struPreviewListenParam.byLinkMode=0;


            CommonMethod.GetListenInfo().struVoiceTalkListenParam.struIPAddress.szIP=ehomeInIp.getBytes();
            CommonMethod.GetListenInfo().struVoiceTalkListenParam.struIPAddress.wPort=ehomeSmsVoiceTalkProt;
            ///监听请求的接入方式：0-TCP，1-UDP.2-HRUDP
            CommonMethod.GetListenInfo().struPreviewListenParam.byLinkMode=0;
        }


        if(!m_StreamInstance.NET_ESTREAM_Init()) {
            //SMS流媒体初始化失败
            logger.info("[NET_ESTREAM_Init]->ESTREAM initiate failed! errCode:" + m_StreamInstance.NET_ESTREAM_GetLastError());
        }else{
            //SMS流媒体初始化成功
            logger.info("[NET_ESTREAM_Init]->ESTREAM initiate Successfully!");
            m_bIsRunning = true;
            // open sdk log
            m_StreamInstance.NET_ESTREAM_SetLogToFile(3, "./EHomeSdkLog", true);

            String sLibComPath = "";
            //判断当前环境是否是linux环境
            if(CommonMethod.isLinux())
            {
                sLibComPath = "/home/hik/LinuxSDK/HCAapSDKCom";
                StringPointer strPointer = new StringPointer(sLibComPath);
                strPointer.write();
                if(m_StreamInstance.NET_ESTREAM_SetSDKLocalCfg(5, strPointer.getPointer())) {
                    logger.info("[Stream]ComPath Load Successfully:" + sLibComPath);
                }else {
                    logger.info("[Stream]ComPath Load Failed!:" + sLibComPath + ", errorCode: " + m_StreamInstance.NET_ESTREAM_GetLastError());
                }
                strPointer.read();
            }

            //设备接入服务器的安全参数
            try {
                CommonClass.NET_EHOME_LOCAL_ACCESS_SECURITY struAccessSecure = new CommonClass.NET_EHOME_LOCAL_ACCESS_SECURITY();
                struAccessSecure.byAccessSecurity = 0;
                struAccessSecure.dwSize = struAccessSecure.size();
                struAccessSecure.write();
                m_StreamInstance.NET_ESTREAM_SetSDKLocalCfg(0, struAccessSecure.getPointer());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //开始预览监听
            StartListenPreview();
            //开启回放监听
//            StartListenPlayback();
            //开启语音监听
            StartListenVoiceTalk();
        }
    }

    //停止SMS流媒体服务
    public boolean StopEStream() {
        StopListenPreview();
        StopListenPlayback();
        StopListenVoiceTalk();
        if(!m_StreamInstance.NET_ESTREAM_Fini())
        {
            logger.error("NET_ESTREAM_Fini failed, errCode:"
                    + m_StreamInstance.NET_ESTREAM_GetLastError());
            return false;
        }
        else
        {
            m_bIsRunning = false;
            logger.info("NET_ESTREAM_Fini Success!");
            return true;
        }
    }

    //开启 SMS 预览监听服务并接收来自设备的连接请求
    public boolean StartListenPreview() {
        /*-------------Preview Listen Part----------------*/
        CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struPreviewListenParam.struIPAdress.szIP, m_struPreviewListenParam.struIPAdress.szIP);
        m_struPreviewListenParam.struIPAdress.wPort = CommonMethod.GetListenInfo().struPreviewListenParam.struIPAdress.wPort;
        m_struPreviewListenParam.byLinkMode = CommonMethod.GetListenInfo().struPreviewListenParam.byLinkMode;

        //实例化预览回调函数，用于接收预览请求的响应报文。
        if(m_fnPreviewNewLinkCallBack == null) {
            m_fnPreviewNewLinkCallBack = new FPreviewNewLinkCallBack();
        }
        m_struPreviewListenParam.fnNewLinkCB = m_fnPreviewNewLinkCallBack;


        //开启 SMS 监听服务并接收来自设备的连接请求
        m_lPreviewListenHandle = m_StreamInstance.NET_ESTREAM_StartListenPreview(m_struPreviewListenParam);
        if(m_lPreviewListenHandle < 0)
        {
            logger.error("NET_ESTREAM_StartListenPreview Failed," +
                    " errCode:"+ m_StreamInstance.NET_ESTREAM_GetLastError() +
                    " IP: " + CommonMethod.byteToString(m_struPreviewListenParam.struIPAdress.szIP) +
                    " port: " + m_struPreviewListenParam.struIPAdress.wPort +
                    " linkMode: " + m_struPreviewListenParam.byLinkMode);
            return false;
        }else{
            logger.info("NET_ESTREAM_StartListenPreview Success," +
                    " IP: " + CommonMethod.byteToString(m_struPreviewListenParam.struIPAdress.szIP) +
                    " port: " + m_struPreviewListenParam.struIPAdress.wPort +
                    " linkMode: " + m_struPreviewListenParam.byLinkMode);
            return true;
        }
    }

    //停止 SMS 预览监听服务并断开其与设备的连接。
    public boolean StopListenPreview() {
        boolean bRet = true;
        if(m_lPreviewListenHandle >= 0) {
            if (!m_StreamInstance.NET_ESTREAM_StopListenPreview(m_lPreviewListenHandle)) {
                logger.error("NET_ESTREAM_StopListenPreview failed, errCode:"
                        + m_StreamInstance.NET_ESTREAM_GetLastError());
                bRet = false;
            } else {
                m_lPreviewListenHandle=-1;
                logger.info("NET_ESTREAM_StopListenPreview Success!");
            }
            return bRet;
        }

        logger.error("NET_ESTREAM_StopListenPreview failed, no listening preview port now");
        return false;
    }

    //开启 SMS 回放监听服务并接收来自设备的连接请求。
    public boolean StartListenPlayback() {
        if(m_lPlayBackListenHandle >= 0) {
            StopListenPlayback();
        }
        /*-------------Playback Listen Part----------------*/
        CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struPlaybackListenParam.struIPAddress.szIP, m_struPlaybackListenParam.struIPAddress.szIP);
        m_struPlaybackListenParam.struIPAddress.wPort = CommonMethod.GetListenInfo().struPlaybackListenParam.struIPAddress.wPort;
        m_struPlaybackListenParam.byLinkMode = CommonMethod.GetListenInfo().struPlaybackListenParam.byLinkMode;

        if(m_fnPlaybackNewLinkCallBack == null) {
            m_fnPlaybackNewLinkCallBack = new FPlaybackNewLinkCallBack();
        }
        m_struPlaybackListenParam.fnNewLinkCB = m_fnPlaybackNewLinkCallBack;

        m_lPlayBackListenHandle = m_StreamInstance.NET_ESTREAM_StartListenPlayBack(m_struPlaybackListenParam);
        if(m_lPlayBackListenHandle < 0)
        {
            logger.error("NET_ESTREAM_StartListenPlayBack Failed," +
                    " errCode:"+ m_StreamInstance.NET_ESTREAM_GetLastError() +
                    " IP: " + CommonMethod.byteToString(m_struPlaybackListenParam.struIPAddress.szIP) +
                    " port: " + m_struPlaybackListenParam.struIPAddress.wPort +
                    " linkMode: " + m_struPlaybackListenParam.byLinkMode);
            return false;
        }else{
            logger.info("NET_ESTREAM_StartListenPlayBack Success," +
                    " IP: " + CommonMethod.byteToString(m_struPlaybackListenParam.struIPAddress.szIP) +
                    " port: " + m_struPlaybackListenParam.struIPAddress.wPort +
                    " linkMode: " + m_struPlaybackListenParam.byLinkMode);
            return true;
        }
    }

    //停止 SMS 回放监听服务并断开其与设备的连接。
    public boolean StopListenPlayback() {
        boolean bRet = true;
        if(m_lPlayBackListenHandle >= 0) {
            if (!m_StreamInstance.NET_ESTREAM_StopListenPlayBack(m_lPlayBackListenHandle)) {
                logger.error("NET_ESTREAM_StopListenPlayBack failed, errCode:"
                        + m_StreamInstance.NET_ESTREAM_GetLastError());
                bRet = false;
            } else {
                m_lPlayBackListenHandle=-1;
                logger.info("NET_ESTREAM_StopListenPlayBack Success!");
            }
            return bRet;
        }
        logger.error("NET_ESTREAM_StopListenPlayBack failed, no listening playback port now");
        return false;
    }

    //开启 SMS 语音对讲监听服务并接收来自设备的连接请求。
    public boolean StartListenVoiceTalk() {
        if(m_lVoiceTalkListenHandle >= 0) {
            StopListenVoiceTalk();
        }
        /*-------------VoiceTalk Listen Part----------------*/
        CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struVoiceTalkListenParam.struIPAddress.szIP, m_struVoiceTalkListenParam.struIPAddress.szIP);
        m_struVoiceTalkListenParam.struIPAddress.wPort = CommonMethod.GetListenInfo().struVoiceTalkListenParam.struIPAddress.wPort;
        m_struVoiceTalkListenParam.byLinkMode = CommonMethod.GetListenInfo().struVoiceTalkListenParam.byLinkMode;

        if(m_fnVoiceTalkNewLinkCallBack == null)
        {
            m_fnVoiceTalkNewLinkCallBack = new FVoiceTalkNewLinkCallBack();
        }
        m_struVoiceTalkListenParam.fnNewLinkCB = m_fnVoiceTalkNewLinkCallBack;

        m_lVoiceTalkListenHandle = m_StreamInstance.NET_ESTREAM_StartListenVoiceTalk(m_struVoiceTalkListenParam);

        if(m_lVoiceTalkListenHandle < 0)
        {
            logger.error("NET_ESTREAM_StartListenVoiceTalk Failed," +
                    " errCode:"+ m_StreamInstance.NET_ESTREAM_GetLastError() +
                    " IP: " + CommonMethod.byteToString(m_struVoiceTalkListenParam.struIPAddress.szIP) +
                    " port: " + m_struVoiceTalkListenParam.struIPAddress.wPort +
                    " linkMode: " + m_struVoiceTalkListenParam.byLinkMode);
            return false;
        }else{
            logger.info("NET_ESTREAM_StartListenVoiceTalk Success," +
                    " IP: " + CommonMethod.byteToString(m_struVoiceTalkListenParam.struIPAddress.szIP) +
                    " port: " + m_struVoiceTalkListenParam.struIPAddress.wPort +
                    " linkMode: " + m_struVoiceTalkListenParam.byLinkMode);
            return true;
        }
    }

    //停止 SMS 语音对讲监听服务并断开其与设备的连接。
    public boolean StopListenVoiceTalk() {
        boolean bRet = true;
        if(m_lVoiceTalkListenHandle >= 0) {
            if (!m_StreamInstance.NET_ESTREAM_StopListenVoiceTalk(m_lVoiceTalkListenHandle)) {
                logger.error("NET_ESTREAM_StopListenVoiceTalk failed, errCode:"
                        + m_StreamInstance.NET_ESTREAM_GetLastError());
                bRet = false;
            } else {
                m_lVoiceTalkListenHandle=-1;
                logger.info("NET_ESTREAM_StopListenVoiceTalk Success!");
            }
            return bRet;
        }
        logger.error("NET_ESTREAM_StopListenVoiceTalk failed, no listening VoiceTalk port now");
        return false;
    }

    //语音对讲回调函数，用于接收语音对讲请求的响应报文。
    public class FVoiceTalkNewLinkCallBack implements VOICETALK_NEWLINK_CB{
        @Override
        public boolean invoke(int lHandle, NET_EHOME_VOICETALK_NEWLINK_CB_INFO pNewLinkCBMsg, Pointer pUserData){
            if(VoiceTalkWithServer.currentLuserId != -1){
                WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByLuserId((long)VoiceTalkWithServer.currentLuserId);
                wvpDevice.setVoiceHandle((long)lHandle);
                wvpDeviceService.updateWvpDevice(wvpDevice);

                voiceMap.put(VoiceTalkWithServer.currentLuserId.toString(),String.valueOf(lHandle));
                logger.info("语音对讲回调函数PUT之后:"+ JSONObject.toJSONString(voiceMap));
                VoiceTalkWithServer.currentLuserId=-1;

            }
            m_lVoiceTalkNewLinkHandle = lHandle;
            logger.info("Callback of two-way audio listening, Device ID:" + pNewLinkCBMsg.szDeviceID +
                    "Audio Channel:" + pNewLinkCBMsg.dwAudioChan + "AudioType: " + pNewLinkCBMsg.dwEncodeType);
            if(m_fnVoiceTalkDataCallBack == null) {
                m_fnVoiceTalkDataCallBack = new FVoiceTalkDataCallBack();
            }


            ISUPStreamByJNA.NET_EHOME_VOICETALK_DATA_CB_PARAM struVoiceTalkDataCBParam = new ISUPStreamByJNA.NET_EHOME_VOICETALK_DATA_CB_PARAM();
            struVoiceTalkDataCBParam.fnVoiceTalkDataCB  = m_fnVoiceTalkDataCallBack;
            struVoiceTalkDataCBParam.write();

            if(!EStream.m_StreamInstance.NET_ESTREAM_SetVoiceTalkDataCB(lHandle, struVoiceTalkDataCBParam)) {
                logger.error("NET_ESTREAM_SetVoiceTalkDataCB failed, errCode is:" + EStream.m_StreamInstance.NET_ESTREAM_GetLastError());
                return false;
            }
//            EhomeDevice ehomeDevice=ehomeDeviceService.selectEhomeDeviceByLuserId((long)lUserID);


            logger.info("NET_ESTREAM_SetVoiceTalkDataCB Success!");
            return true;
        }
    }

    //语音对讲的数据回调函数，用于接收语音对讲的数据
    public class FVoiceTalkDataCallBack implements VOICETALK_DATA_CB{
        @Override
        public boolean invoke(int lHandle, NET_EHOME_VOICETALK_DATA_CB_INFO  pDataCBInfo, Pointer pUserData){
            return InputVoiceDataHandle(lHandle, pDataCBInfo.pData, pDataCBInfo.dwDataLen);
        }
    }

    //对语音对讲数据进行转码g711转PCM格式，数据转发处理
    public boolean InputVoiceDataHandle(int lHandle, Pointer pData, int dwSize) {

        Integer luserId=-1;

        for(String key:voiceMap.keySet()){
            if(Integer.valueOf(voiceMap.get(key)).intValue()==lHandle){
                luserId=Integer.valueOf(key);
            }
        }
        if(luserId!=-1){
            try{
                //其他数据处理，语音数据转发、解码播放需要自行实现
                long offset = 0;
                ByteBuffer buffers = pData.getByteBuffer(offset, dwSize);
                byte [] bytes = new byte[dwSize];
                buffers.rewind();
                buffers.get(bytes);
                byte[] converterData=new byte[bytes.length*2];
                G711Converter.decode(bytes,0,bytes.length,converterData);
//                WebSocketSendVoiceThreadUtil webSocketSendVoiceThreadUtil=VoiceTalkWithServer.webSocketSendVoiceThreadUtilHashMap.get(luserId);
//                if(webSocketSendVoiceThreadUtil!=null){
//                    webSocketSendVoiceThreadUtil.onStart(ByteBuffer.wrap(converterData));
//                }
                WebSocketUsers.sendMessageToUserByByteBuffer(luserId.longValue(),ByteBuffer.wrap(converterData));

            }catch (IOException e){
                e.printStackTrace();
            }
        }



        return true;
    }

    //通过回调函数发送语音数据给指定设备
    public int sendVoiceData(byte[] message,Long voiceHandle){

        logger.info("输出语音给设备");
        int dataLength = 0;
        dataLength = message.length;

        if (dataLength < 0) {
            logger.info("sendVoiceData() dataSize < 0");
            return -1;
        }

        ISUPSSByJNA.BYTE_ARRAY ptrVoiceByte = new ISUPSSByJNA.BYTE_ARRAY(dataLength);
        ptrVoiceByte.byValue=message;
        ptrVoiceByte.write();

        int iEncodeSize = 0;

        int iSendLen = 160;//G722音频编码类型时，每次发送的数据为80字节；G711音频编码类型时，每次发送的数据为160字节
        int iSend = -1;
        while ((dataLength - iEncodeSize) > iSendLen) {
            ISUPSSByJNA.BYTE_ARRAY ptrSendData = new ISUPSSByJNA.BYTE_ARRAY(iSendLen);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrSendData.byValue, 0, iSendLen);
            ptrSendData.write();

            ISUPStreamByJNA.NET_EHOME_VOICETALK_DATA struVoiceData = new ISUPStreamByJNA.NET_EHOME_VOICETALK_DATA();
            struVoiceData.dwDataLen = iSendLen;
            struVoiceData.pSendBuf = ptrSendData.getPointer();
            struVoiceData.write();
            iSend = m_StreamInstance.NET_ESTREAM_SendVoiceTalkData(voiceHandle.intValue(), struVoiceData);
            if (iSend < 0) {
                logger.error("NET_DVR_VoiceComSendData failed, error code:" + m_StreamInstance.NET_ESTREAM_GetLastError());
                break;
            }

            iEncodeSize += iSendLen;
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return iSend;
    }

    //预览回调函数，用于接收预览请求的响应报文。
    public class FPreviewNewLinkCallBack implements PREVIEW_NEWLINK_CB{
        @Override
        public boolean invoke(int lLinkHandle, NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData){

            pNewLinkCBMsg.read();
            m_lPreviewNewLinkHandle = lLinkHandle;
            logger.info("Callback of preview listening, DeviceID:" + CommonMethod.byteToString(pNewLinkCBMsg.szDeviceID) +
                    "Channel:" + pNewLinkCBMsg.dwChannelNo);
            if(fnPreviewDateCallBack == null)
            {
                fnPreviewDateCallBack = new FPreviewDataCallback();
            }
            NET_EHOME_PREVIEW_DATA_CB_PARAM struPreviewDataCB = new NET_EHOME_PREVIEW_DATA_CB_PARAM();
            struPreviewDataCB.fnPreviewDataCB = fnPreviewDateCallBack;
            struPreviewDataCB.byStreamFormat = 0;      //PS
            if(!EStream.m_StreamInstance.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struPreviewDataCB))
            {
                logger.error("NET_ESTREAM_SetPreviewDataCB failed, errCode is:" + EStream.m_StreamInstance.NET_ESTREAM_GetLastError());
                return false;
            }
            logger.info("NET_ESTREAM_SetPreviewDataCB Success!");

            System.out.println("EStream:"+pNewLinkCBMsg.iSessionID);
            String deviceId=redisCache.getCacheObject(pNewLinkCBMsg.iSessionID+"").toString();
            WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByDeviceId(deviceId);

            if(wvpDevice!=null){
                HaiKangSdkAccess haiKangSdkAccess=new HaiKangSdkAccess(wvpDevice.getDeviceId(),wvpDevice.getLuserId(),lLinkHandle,rtmpUrl,redisCache);
                haiKangSdkAccessHashMap.put(lLinkHandle,haiKangSdkAccess);
                wvpDevice.setPushState(1);
                wvpDevice.setVideoHlsUrl(hlsUrl+wvpDevice.getDeviceId()+".m3u8");
                wvpDevice.setLLinkHandle((long)lLinkHandle);
                wvpDeviceService.updateWvpDevice(wvpDevice);

                System.out.println("开始推流设备ID："+wvpDevice.getDeviceId());
            }else{
                Preview.StopPreview(wvpDevice.getDeviceId());
            }
            return true;
        }
    }

    //预览回调数据
    public class FPreviewDataCallback implements PREVIEW_DATA_CB {

        @Override
        public boolean invoke(int iPreviewHandle, Pointer pPreviewCBMsg, Pointer pUserData) {
            String sClassName = NET_EHOME_PREVIEW_CB_MSG.class.getName();
            NET_EHOME_PREVIEW_CB_MSG strPreviewData=null;
            try{
                strPreviewData = (NET_EHOME_PREVIEW_CB_MSG) CommonMethod.WritePointerDataToClass(pPreviewCBMsg, sClassName);
            }catch (Exception e){
                e.printStackTrace();
            }


            if(strPreviewData.pRecvdata == null || strPreviewData.dwDataLen <= 0)
            {
                return false;
            }
            switch (strPreviewData.byDataType)
            {
                case ISUPStreamByJNA.NET_DVR_SYSHEAD: {
                    logger.info("HIK Stream Header len:" + strPreviewData.dwDataLen);
                    break;
                }
                case ISUPStreamByJNA.NET_DVR_STREAMDATA:
                {

                    try{
                        haiKangSdkAccessHashMap.get(iPreviewHandle).onMediaStream(strPreviewData.pRecvdata.getByteArray(0,strPreviewData.dwDataLen),false);
                    }catch (Exception e){
                        e.printStackTrace();
                        WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByIPreviewHandle((long)iPreviewHandle);
                        if(wvpDevice!=null){
                            Preview.StopPreview(wvpDevice.getDeviceId());
                            haiKangSdkAccessHashMap.remove(iPreviewHandle);
                        }
                    }

                }
            }
            return true;
        }
    }

    //回放回调函数，用于接收回放请求的响应报文。
    public class FPlaybackNewLinkCallBack implements PLAYBACK_NEWLINK_CB {
        @Override
        public boolean invoke(int lLinkHandle, NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBMsg, Pointer pUserData) {
            m_lPlaybackNewLinkHandle = lLinkHandle;

            logger.info("Callback of playback listening, DeviceID:" + CommonMethod.byteToString(pNewLinkCBMsg.szDeviceID) +
                    "Channel:" + pNewLinkCBMsg.dwChannelNo);
            if(m_fnPlaybackDataCallBack == null)
            {
                m_fnPlaybackDataCallBack = new FPlaybackDataCallback();
            }

            NET_EHOME_PLAYBACK_DATA_CB_PARAM struPlaybackCBParam = new NET_EHOME_PLAYBACK_DATA_CB_PARAM();
            struPlaybackCBParam.fnPlayBackDataCB = m_fnPlaybackDataCallBack;
            struPlaybackCBParam.byStreamFormat = 0;      //PS

            if(!EStream.m_StreamInstance.NET_ESTREAM_SetPlayBackDataCB(m_lPlaybackNewLinkHandle, struPlaybackCBParam))
            {
                logger.error("NET_ESTREAM_SetPlayBackDataCB failed, errCode is:" + EStream.m_StreamInstance.NET_ESTREAM_GetLastError());
                return false;
            }
            logger.info("NET_ESTREAM_SetPlayBackDataCB Success!");
            return true;
        }
    }

    //回放回调数据
    public class FPlaybackDataCallback implements PLAYBACK_DATA_CB {
        @Override
        public boolean invoke(int iPlaybackHandle, Pointer pPlaybackCBMsg, Pointer pUserData) {
            try{
                String sClassName = NET_EHOME_PLAYBACK_DATA_CB_INFO.class.getName();
                NET_EHOME_PLAYBACK_DATA_CB_INFO struPlayBackCbInfo = (NET_EHOME_PLAYBACK_DATA_CB_INFO) CommonMethod.WritePointerDataToClass(pPlaybackCBMsg, sClassName);
                if(struPlayBackCbInfo.pData == null || struPlayBackCbInfo.dwDataLen <= 0)
                {
                    return false;
                }

                switch (struPlayBackCbInfo.byType)
                {
                    case ISUPStreamByJNA.NET_DVR_SYSHEAD: {

                        break;
                    }
                    case ISUPStreamByJNA.NET_DVR_STREAMDATA:
                    {
                        System.out.println("Stream data len" + struPlayBackCbInfo.dwDataLen);
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return true;
        }
    }

    //加载库文件
    private static boolean CreateStreamInstance() {
        if(m_StreamInstance == null)
        {
            synchronized (ISUPStreamByJNA.class)
            {
                String strDllPath = "";
                try
                {
                    if(CommonMethod.isWindows()) {
                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCISUPStream";
                    }else if(CommonMethod.isLinux()) {
                        strDllPath = "/home/hik/LinuxSDK/libHCISUPStream.so";
                    }
                    logger.info("[EStream]->"+strDllPath);
                    m_StreamInstance = (ISUPStreamByJNA) Native.loadLibrary(strDllPath, ISUPStreamByJNA.class);
                }catch (Exception ex) {
                    logger.error("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public static ISUPStreamByJNA GetStreamInstance() {
        return m_StreamInstance;
    }

    public boolean IsRunning(){return m_bIsRunning;}


}
