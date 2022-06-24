package com.wvp.hk.module.service.stream;

import com.sun.jna.NativeLong;
import com.wvp.common.core.redis.RedisCache;
import com.wvp.domain.WvpDevice;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import com.wvp.hk.module.RemoteManage.XMLConfig;
import com.wvp.hk.module.service.util.HaiKangSdkAccess;
import com.wvp.service.IWvpDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class Preview
{
    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(EStream.class);

    private static IWvpDeviceService wvpDeviceService;

    public static long m_lPreviewSessionID = -1;

    public static HashMap<Integer, Long> previewMap = new HashMap<>();

    @Autowired
    private RedisCache redisCache;

    public static RedisCache staticRedisCache;

    @PostConstruct
    public void init(){
        staticRedisCache=this.redisCache;
    }

    @Autowired
    public Preview(IWvpDeviceService iWvpDeviceService){
        Preview.wvpDeviceService=iWvpDeviceService;
    }

    public static boolean StartPreview(int lUserID, NET_EHOME_PREVIEWINFO_IN_V11 struPreviewIn)
    {
        WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByLuserId((long)lUserID);
        NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new NET_EHOME_PREVIEWINFO_OUT();
        struPreviewOut.write();
        logger.info("NET_ECMS_StartGetRealStreamV11"+CommonMethod.byteToString(struPreviewIn.struStreamSever.szIP)+",端口："+struPreviewIn.struStreamSever.wPort);

        System.out.println("NET_ECMS_StartGetRealStreamV11"+CommonMethod.byteToString(struPreviewIn.struStreamSever.szIP)+",端口："+struPreviewIn.struStreamSever.wPort);

        if(!ECMS.GetCMSInstance().NET_ECMS_StartGetRealStreamV11(lUserID, struPreviewIn, struPreviewOut.getPointer()))
        {

            logger.info("NET_ECMS_StartGetRealStreamV11 failed, errCode is " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            System.out.println("NET_ECMS_StartGetRealStreamV11 failed, errCode is " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            CommonMethod.logRecord("ERROR", "NET_ECMS_StartGetRealStreamV11 failed, errCode is " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return false;
        }
        else
        {
            CommonMethod.logRecord("INFO", "NET_ECMS_StartGetRealStreamV11 Success");

            struPreviewOut.read();
            logger.info("Preview设备序列号：" + wvpDevice.getDeviceId() + ",sessionId:" + struPreviewOut.lSessionID + ",lHandle:" + struPreviewOut.lHandle);
            System.out.println("Preview设备序列号：" + wvpDevice.getDeviceId() + ",sessionId:" + struPreviewOut.lSessionID + ",lHandle:" + struPreviewOut.lHandle);
            staticRedisCache.setCacheObject(struPreviewOut.lSessionID+"", wvpDevice.getDeviceId(),60, TimeUnit.SECONDS);


            if(wvpDevice!=null&&Integer.valueOf(wvpDevice.getDeviceVersion()) >= 4)
            {
                //调用NET_ECMS_XMLConfig并命令GetDevAbility以获取设备功能，并确定是否支持预览。
                //设备功能集由pOutBuf返回。
                //如果支持，它将返回节点<NewInviteStream>，并继续执行以下步骤。
                String sXMLConfig =
                        "<Params>" +
                                "<ConfigCmd>GetDevAbility</ConfigCmd>" +
                                "<ConfigParam1>1</ConfigParam1>" +
                                "<ConfigParam2>0</ConfigParam2>" +
                                "<ConfigParam3>0</ConfigParam3>" +
                                "<ConfigParam4>0</ConfigParam4>" +
                                "</Params>";
                StringBuffer spConfigOutBuf = new StringBuffer();
                boolean bSupportGetStreamV4 = false;
                if(XMLConfig.CommonXMLConfig(lUserID, "GETDEVICECONFIG", sXMLConfig, spConfigOutBuf))
                {
                    String sXmlOutBuf = spConfigOutBuf.toString();
                    List<String> listResLabel = CommonMethod.getFieldListByRegex(sXmlOutBuf, "HRUDP");
                    if(listResLabel.size()>0&&listResLabel.get(0).equals("TRUE"))
                        //support 4.0 new GetStream Protocol
                        bSupportGetStreamV4 = true;
                }
                else
                {
                    logger.info("GetDevAbility failed");
                    CommonMethod.logRecord("ERROR", "GetDevAbility failed");
//                    return false;
                }


                NET_EHOME_PUSHSTREAM_IN struPushStreamIn = new NET_EHOME_PUSHSTREAM_IN();
                struPushStreamIn.read();
                struPushStreamIn.dwSize = struPushStreamIn.size();
                struPushStreamIn.lSessionID = struPreviewOut.lSessionID;
                struPushStreamIn.write();

                NET_EHOME_PUSHSTREAM_OUT struPushStreamOut = new NET_EHOME_PUSHSTREAM_OUT();
                struPushStreamOut.read();
                struPushStreamOut.dwSize = struPushStreamOut.size();
                struPushStreamOut.write();
//                struPushStreamOut.write();
//                struPushStreamOut.dwSize = struPreviewOut.size();

//                struPushStreamOut.dwSize = struPushStreamOut.size();

                logger.info("[preview]lUserID:"+lUserID+",struPushStreamIn:"+struPushStreamIn.lSessionID);

                if(!ECMS.GetCMSInstance().NET_ECMS_StartPushRealStream(lUserID, struPushStreamIn, struPushStreamOut))
                {
                    logger.info("NET_ECMS_StartPushRealStream failed, errCode:" + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
                    CommonMethod.logRecord("ERROR", "NET_ECMS_StartPushRealStream failed, errCode:" + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
                    return false;
                }
                else
                {
                    previewMap.put(lUserID,(long)struPreviewOut.lSessionID);
                    logger.info("NET_ECMS_StartPushRealStream Success!");
                    CommonMethod.logRecord("INFO", "NET_ECMS_StartPushRealStream Success!");
                    wvpDevice.setSessionId((long) struPushStreamIn.lSessionID);
                    wvpDevice.setPushState(1);
                    wvpDeviceService.updateWvpDevice(wvpDevice);
                    return true;
                }
            }
        }


        return true;
    }

    public static boolean StopPreview(String deviceId){
        logger.info("终止推流："+deviceId);
        WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByDeviceId(deviceId);
        int lUserID=wvpDevice.getLuserId().intValue();
        boolean bRet = true;
        if(!ECMS.GetCMSInstance().NET_ECMS_StopGetRealStream(lUserID, previewMap.get(lUserID).intValue()))
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_StopGetRealStream failed, errCode: " + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            bRet = false;
        }
        else {
            CommonMethod.logRecord("INFO", "NET_ECMS_StopGetRealStream Success!");
            if(wvpDevice!=null){
                wvpDevice.setPushState(0);
                wvpDevice.setLLinkHandle(-1L);
                wvpDeviceService.updateWvpDevice(wvpDevice);
            }
//            if(!EStream.GetStreamInstance().NET_ESTREAM_StopPreview(EStream.m_lPreviewNewLinkHandle))
//            {
//                System.out.println("停止拉流错误码："+EStream.GetStreamInstance().NET_ESTREAM_GetLastError());
//                CommonMethod.logRecord("ERROR", "NET_ESTREAM_StopPreview failed, errCode:" + EStream.GetStreamInstance().NET_ESTREAM_GetLastError());
//                bRet = false;
//            }
//            else
//            {
//                if(wvpDevice!=null){
//                    wvpDevice.setPushState(0);
//                    wvpDevice.setLLinkHandle(-1L);
//                    wvpDeviceService.updateWvpDevice(wvpDevice);
//                }
//                CommonMethod.logRecord("INFO", "NET_ESTREAM_StopPreview Success!");
//            }

//            if(EStream.m_iRtmpHandle > 0)
//            {
//                if(EStream.GetRtmpInstance().Rtmp_DestroyHandle(EStream.m_iRtmpHandle))
//                {
//                    System.out.println("Rtmp_DestroyHandle Success");
//                }
//                else
//                {
//                    System.out.println("Rtmp_DestroyHandle Failed" + EStream.GetRtmpInstance().Rtmp_GetLastError());
//                }
//            }
        }
        return bRet;
    }
}
