package com.wvp.hk.module.service.stream;

import com.wvp.domain.WvpDevice;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import com.wvp.service.IWvpDeviceService;
import com.wvp.websocket.WebSocketSendVoiceThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;

/**
 * 流媒体语音对讲
 *
 * @author fs
 */
public class VoiceTalkWithServer {


    private static Thread m_thSendVoice = null;
    private static int m_lUserID = -1;
    public static ISUPStreamByJNA m_StreamInstance = null;


    private static IWvpDeviceService wvpDeviceService;

    //<LuserId,sessionId>
    public static HashMap<Integer,Integer> voiceSessionMap=new HashMap<>();

    public static HashMap<Integer, WebSocketSendVoiceThreadUtil> webSocketSendVoiceThreadUtilHashMap=new HashMap<>();

    //日志文件
    protected final static Logger logger = LoggerFactory.getLogger(VoiceTalkWithServer.class);

    public static Integer currentLuserId = -1;



    private static String ehomePuIp;


    private static short ehomeSmsVoiceTalkProt;

    @Value("${ehome.pu-ip}")
    public void setEhomePuIp(String ehomePuIp) {
        VoiceTalkWithServer.ehomePuIp = ehomePuIp;
    }

    @Value("${ehome.sms-voicetalk-prot}")
    public void setEhomeSmsVoiceTalkProt(short ehomeSmsVoiceTalkProt) {
        VoiceTalkWithServer.ehomeSmsVoiceTalkProt = ehomeSmsVoiceTalkProt;
    }

    @Autowired
    public void setEhomeDeviceService(IWvpDeviceService wvpDeviceService) {
        VoiceTalkWithServer.wvpDeviceService = wvpDeviceService;
    }

    public static boolean StartVoiceTalk(int lUserID)
    {
        currentLuserId=lUserID;
        //下发CMS服务器语音对讲请求
        NET_EHOME_VOICE_TALK_IN struVoiceReqIn = new NET_EHOME_VOICE_TALK_IN();
        //CMS 服务IP 外网Ip
        byte[] byIp=ehomePuIp.getBytes();
        System.arraycopy(byIp, 0, struVoiceReqIn.struStreamSever.szIP, 0, byIp.length);
        //语音对讲通道号
        struVoiceReqIn.dwVoiceChan = 1;
        //CMS对讲监听端口
        struVoiceReqIn.struStreamSever.wPort = ehomeSmsVoiceTalkProt;
        //0、编码类型开始
        //1、G722_1
        //2、G711_MU
        //3、G711_A
        //4、G723
        //5、MP1L2
        //6、MP2L2
        //7、G726
        //8、AAC
        //100、RAW
        struVoiceReqIn.byEncodingType[0]=3;
        struVoiceReqIn.write();

        NET_EHOME_VOICE_TALK_OUT struVoiceTalkOut = new NET_EHOME_VOICE_TALK_OUT();
        struVoiceTalkOut.write();

        //将语音对讲开启请求从 CMS 发送给设备。
        if(ECMS.GetCMSInstance().NET_ECMS_StartVoiceWithStmServer(lUserID, struVoiceReqIn.getPointer(), struVoiceTalkOut.getPointer()))
        {


            logger.info("NET_ECMS_StartVoiceWithStmServer LuserId:"+lUserID+",sessionID:"+struVoiceTalkOut.lSessionID);
            struVoiceTalkOut.read();
            voiceSessionMap.put(lUserID,struVoiceTalkOut.lSessionID);
            logger.info("NET_ECMS_StartVoiceWithStmServer voiceChann["+ struVoiceReqIn.dwVoiceChan +
                    "] Success!");
            NET_EHOME_PUSHVOICE_IN struPushVoiceIn = new NET_EHOME_PUSHVOICE_IN();
            struPushVoiceIn.dwSize = struPushVoiceIn.size();
            struPushVoiceIn.lSessionID = struVoiceTalkOut.lSessionID;
            //自定义Token字符串
            String szToken = "aaaa1111344";
            System.arraycopy(szToken.getBytes(), 0, struVoiceReqIn.struStreamSever.szIP, 0, szToken.length());
            struPushVoiceIn.write();


            NET_EHOME_PUSHVOICE_OUT struPushVoiceOut = new NET_EHOME_PUSHVOICE_OUT();
            struPushVoiceOut.dwSize = struPushVoiceOut.size();
            struPushVoiceOut.write();
            //将语音传输请求从 CMS 发送给设备



            logger.info("NET_ECMS_StartPushVoiceStream LuserId:"+lUserID+",sessionID:"+struPushVoiceIn.lSessionID+",dwsize:"+struPushVoiceIn.dwSize+",lsessionid:"+struPushVoiceIn.lSessionID);

            if(ECMS.GetCMSInstance().NET_ECMS_StartPushVoiceStream(lUserID, struPushVoiceIn.getPointer(), struPushVoiceOut.getPointer()))
            {

//                WebSocketSendVoiceThreadUtil webSocketSendVoiceThreadUtil=new WebSocketSendVoiceThreadUtil(new Integer(lUserID).longValue());
//                webSocketSendVoiceThreadUtilHashMap.put(lUserID,webSocketSendVoiceThreadUtil);
                struPushVoiceOut.read();
                logger.info("NET_ECMS_StartPushVoiceStream sessionID["+ struPushVoiceIn.lSessionID +
                        "] Success!");
                m_lUserID = lUserID;

                return true;
            }
            else
            {
                logger.error("NET_ECMS_StartPushVoiceStream sessionID["+ struPushVoiceIn.lSessionID +
                        "] Error! ErrCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
                return false;
            }
        }
        else
        {
            logger.error("NET_ECMS_StartVoiceWithStmServer sessionID["+ struVoiceReqIn.dwVoiceChan +
                    "] Error! ErrCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return false;
        }
    }

    public static boolean StopVoiceTalk(long lUserID,Integer voiceHandle)
    {

        if(EStream.m_StreamInstance.NET_ESTREAM_StopVoiceTalk(voiceHandle))
        {
            logger.info("NET_ESTREAM_StopVoiceTalk Success!");
            Integer sessionId=voiceSessionMap.get((int)lUserID);
            if(ECMS.GetCMSInstance().NET_ECMS_StopVoiceTalkWithStmServer(lUserID, sessionId))
            {
                logger.info("NET_ECMS_StopVoiceTalkWithStmServer Success!");
                m_lUserID = -1;
                voiceSessionMap.remove((int)lUserID);
//                webSocketSendVoiceThreadUtilHashMap.remove(lUserID);
//                try{
//                    m_thSendVoice.stop();
//                }catch(Exception ex){
//                    ex.printStackTrace();
//                }
                EStream.voiceMap.remove((int)lUserID);
                WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByLuserId(lUserID);
                if(wvpDevice!=null){
                    wvpDevice.setVoiceHandle(-1L);
                    wvpDeviceService.updateWvpDevice(wvpDevice);
                }
                return true;
            }
            else
            {
                logger.info("NET_ECMS_StopVoiceTalkWithStmServer failed, errCode:" + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
                return false;
            }
        }
        else
        {
            logger.info("NET_ESTREAM_StopVoiceTalk failed, errCode:" + EStream.m_StreamInstance.NET_ESTREAM_GetLastError());
            return false;
        }
    }


}
