package com.wvp.hk.module.service.stream;


import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import com.wvp.hk.module.service.cms.OnlineDevManager;

public class Playback {
    public static boolean StartPlayback(int lUserID, NET_EHOME_PLAYBACK_INFO_IN struPlaybackIn){
        if(OnlineDevManager.GetDevObj(lUserID).dwVersion == 5 && struPlaybackIn.byPlayBackMode == 0)
        {
            CommonMethod.logRecord("ERROR", "ISUP5.0 Not Support PlayBack By FileName");
            return false;
        }
        NET_EHOME_PLAYBACK_INFO_OUT struPlayBackOut = new NET_EHOME_PLAYBACK_INFO_OUT();
        struPlayBackOut.write();
        boolean bRet = ECMS.GetCMSInstance().NET_ECMS_StartPlayBack(lUserID, struPlaybackIn, struPlayBackOut.getPointer());
        if(bRet)
        {
            struPlayBackOut.read();
            EStream.m_iSessionIDPlayBack = struPlayBackOut.lSessionID;
            CommonMethod.logRecord("INFO", "NET_ECMS_StartPlayBack Success");

            if(OnlineDevManager.GetDevObj(lUserID).dwVersion >= 4) {
                //Call NET_ECMS_XMLConfig and command GetDevAbility to obtain device capabilities and determine whether to support preview.
                //The device capability set is returned by pOutBuf.
                //If it supports, it will return to the node <NewInviteStream>, and continue with the following steps.
//                String sXMLConfig =
//                        "<Params>\r\n" +
//                                "<ConfigCmd>GetDevAbility</ConfigCmd>\r\n" +
//                                "<ConfigParam1>1</ConfigParam1>\r\n" +
//                                "<ConfigParam2>0</ConfigParam2>\r\n" +
//                                "<ConfigParam3>0</ConfigParam3>\r\n" +
//                                "<ConfigParam4>0</ConfigParam4>\r\n" +
//                                "</Params>";
//                StringBuffer spConfigOutBuf = new StringBuffer();
//                if (XMLConfig.CommonXMLConfig(lUserID, "GETDEVICECONFIG", sXMLConfig, spConfigOutBuf)) {
//                    String sXmlOutBuf = spConfigOutBuf.toString();
//                    List<String> listResLabel = CommonMethod.getFieldListByRegex(sXmlOutBuf, "NewPlayBack");
//                    if (listResLabel.get(0).equals("TRUE")) {
//                        //support 4.0 new GetStream Protocol
//                        ISUPCMSByJNA.NET_EHOME_PUSHPLAYBACK_IN struPushPlayBackIn = new ISUPCMSByJNA.NET_EHOME_PUSHPLAYBACK_IN();
//                        CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struPlaybackListenParam.struIPAddress.szIP, struPlaybackIn.struStreamSever.szIP);
//                        struPushPlayBackIn.lSessionID = EStream.m_iSessionIDPlayBack;
//                        struPushPlayBackIn.dwSize = struPushPlayBackIn.size();
//                        ISUPCMSByJNA.NET_EHOME_PUSHPLAYBACK_OUT struPushPlayBackOut = new ISUPCMSByJNA.NET_EHOME_PUSHPLAYBACK_OUT();
//                        struPushPlayBackOut.dwSize = struPlayBackOut.size();
//                        struPushPlayBackOut.write();
//
//                        bRet = ECMS.GetCMSInstance().NET_ECMS_StartPushPlayBack(lUserID, struPushPlayBackIn, struPushPlayBackOut.getPointer());
//                        if(bRet)
//                        {
//                            CommonMethod.logRecord("INFO", "NET_ECMS_StartPushPlayBack Success");
//                        }
//                        else
//                        {
//                            CommonMethod.logRecord("ERROR", "NET_ECMS_StartPushPlayBack failed, errCode:" + EStream.GetStreamInstance().NET_ESTREAM_GetLastError());
//                        }
//                    }
//                } else {
//                    CommonMethod.logRecord("ERROR", "GetDevAbility failed");
//                }
                //support 4.0 new GetStream Protocol
                NET_EHOME_PUSHPLAYBACK_IN struPushPlayBackIn = new NET_EHOME_PUSHPLAYBACK_IN();
                CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struPlaybackListenParam.struIPAddress.szIP, struPlaybackIn.struStreamSever.szIP);
                struPushPlayBackIn.lSessionID = EStream.m_iSessionIDPlayBack;
                struPushPlayBackIn.dwSize = struPushPlayBackIn.size();
                NET_EHOME_PUSHPLAYBACK_OUT struPushPlayBackOut = new NET_EHOME_PUSHPLAYBACK_OUT();
                struPushPlayBackOut.dwSize = struPlayBackOut.size();
                struPushPlayBackOut.write();

                bRet = ECMS.GetCMSInstance().NET_ECMS_StartPushPlayBack(lUserID, struPushPlayBackIn, struPushPlayBackOut.getPointer());
                if(bRet)
                {
                    CommonMethod.logRecord("INFO", "NET_ECMS_StartPushPlayBack Success");
                }
                else
                {
                    CommonMethod.logRecord("ERROR", "NET_ECMS_StartPushPlayBack failed, errCode:" + EStream.GetStreamInstance().NET_ESTREAM_GetLastError());
                }
            }
        }
        else
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_StartPlayBack failed, errCode:" + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
        }
        return bRet;
    }

    public static boolean StopPlayback(int lUserID){
        boolean bRet = true;
        if(!ECMS.GetCMSInstance().NET_ECMS_StopPlayBack(lUserID, EStream.m_iSessionIDPlayBack))
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_StopPlayBack failed, errCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            bRet = false;
        }
        else {
            CommonMethod.logRecord("INFO", "NET_ECMS_StopPlayBack Success!");
        }

        if(!EStream.GetStreamInstance().NET_ESTREAM_StopPlayBack(EStream.m_lPlaybackNewLinkHandle))
        {
            CommonMethod.logRecord("ERROR", "NET_ESTREAM_StopPlayBack failed, errCode:" + EStream.GetStreamInstance().NET_ESTREAM_GetLastError());
            bRet = false;
        }
        else
        {
            CommonMethod.logRecord("INFO", "NET_ESTREAM_StopPreview Success!");
        }

//        if(EStream.m_iRtmpHandle > 0)
//        {
//            if(EStream.GetRtmpInstance().Rtmp_DestroyHandle(EStream.m_iRtmpHandle))
//            {
//                System.out.println("Rtmp_DestroyHandle Success");
//            }
//            else
//            {
//                System.out.println("Rtmp_DestroyHandle Failed" + EStream.GetRtmpInstance().Rtmp_GetLastError());
//            }
//        }

        return bRet;
    }


}
