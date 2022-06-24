package com.wvp.hk.module.service.stream;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.wvp.hk.module.common.CommonClass;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import org.springframework.stereotype.Component;

@Component
public class VoiceTalk {

    private static FVoiceDataCallBack m_fVoiceDataCallBack = null;
    private static int m_lVoiceTalkHandle = -1;
    private static FileOutputStream m_fVoiceStreamData = null;
    private static Thread m_thSendVoice = null;

    public static boolean StartTalk(int lUserID, int dwVoiceChan, boolean bNeedCBNoEncData, byte byVoiceTalk){

        NET_EHOME_VOICETALK_PARA struVoiceTalkPara = new NET_EHOME_VOICETALK_PARA();
        struVoiceTalkPara.bNeedCBNoEncData = bNeedCBNoEncData;

        if (m_fVoiceDataCallBack == null) {
            m_fVoiceDataCallBack = new FVoiceDataCallBack();
            struVoiceTalkPara.fVoiceDataCallBack = m_fVoiceDataCallBack;
        }

        //0-G.722，1-G.711U，2-G.711A，3-G.726，4-AAC，5-MP2L2，6-PCM。
        struVoiceTalkPara.dwEncodeType = 0;
        //0-语音对讲，1-语音转发
        struVoiceTalkPara.byVoiceTalk = byVoiceTalk;
        struVoiceTalkPara.byDevAudioEnc = 0;

        m_lVoiceTalkHandle = ECMS.GetCMSInstance().NET_ECMS_StartVoiceTalk(lUserID, dwVoiceChan, struVoiceTalkPara);
        if(m_lVoiceTalkHandle < 0)
        {
            System.out.println("NET_ECMS_StartVoiceTalk voiceChann["+ dwVoiceChan +
                    "] Error! ErrCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
//            CommonMethod.logRecord("ERROR", "NET_ECMS_StartVoiceTalk voiceChann["+ dwVoiceChan +
//                    "] Error! ErrCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return false;
        }else{
            CommonMethod.logRecord("INFO", "NET_ECMS_StartVoiceTalk voiceChann["+ dwVoiceChan +
                    "] Success!");
            //not found example voice data
            if(m_thSendVoice == null){
                m_thSendVoice = new Thread(new SendVoiceThread());
                m_thSendVoice.start();
            }
            try{
                m_fVoiceStreamData = new FileOutputStream("D:\\testVoiceData.g722");
            }catch(FileNotFoundException ex){
                ex.printStackTrace();
            }
            return true;
        }
    }

    public static boolean StopTalk(){
        if(!ECMS.GetCMSInstance().NET_ECMS_StopVoiceTalk(m_lVoiceTalkHandle))
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_StopVoiceTalk handle["+ m_lVoiceTalkHandle +
                    "] Error! ErrCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return false;
        }else{
            m_fVoiceDataCallBack = null;
            try{
                m_fVoiceStreamData.close();
                m_thSendVoice.stop();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            CommonMethod.logRecord("INFO", "NET_ECMS_StopVoiceTalk handle["+ m_lVoiceTalkHandle +
                    "] Success!");
            return true;
        }
    }

    private static class SendVoiceThread implements Runnable{
        @Override
        public void run(){
            try {
                FileInputStream fis = new FileInputStream("D:\\receiveVoiceData.g722");
                byte[] byVoiceData = new byte[fis.available()];
                fis.read(byVoiceData, 0, byVoiceData.length);
                int nEachLen = byVoiceData.length / 80;

                for (int i = 0; i < nEachLen; i++){
                    byte[] byVoiceOfSplite = new byte[80];
                    System.arraycopy(byVoiceData, i * 80, byVoiceOfSplite, 0, 80);
                    CommonClass.StringPointer pVoiceBuf = new CommonClass.StringPointer(byVoiceOfSplite);
                    pVoiceBuf.write();
                    boolean bSendVoiceIsOk = ECMS.GetCMSInstance().NET_ECMS_SendVoiceTransData(m_lVoiceTalkHandle,
                            pVoiceBuf.getPointer(), 80);
                    if(!bSendVoiceIsOk) {
                        System.out.println("NET_ECMS_SendVoiceTransData send fail, errCode:" +
                                ECMS.GetCMSInstance().NET_ECMS_GetLastError());
                    }
                }
                Thread.sleep(40);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private static class FVoiceDataCallBack implements fVoiceDataCallBack{
        @Override
        public void invoke(int iVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, int dwEncodeType,
                           byte byAudioFlag, Pointer pUser){
            try{
                System.out.println("Enter FVoiceDataCallBack, flag: " + byAudioFlag + ",encodeType:" + dwEncodeType + " size: " + dwBufSize);
                m_fVoiceStreamData.write(pRecvDataBuffer.getByteArray(0, dwBufSize));
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

}
