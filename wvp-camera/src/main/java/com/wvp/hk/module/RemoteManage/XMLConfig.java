package com.wvp.hk.module.RemoteManage;


import com.wvp.hk.module.common.CommonClass.StringPointer;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import com.wvp.hk.module.service.stream.EStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLConfig
{
    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(EStream.class);
    public static boolean CommonXMLConfig(int lUserID, String sCMDType, String sXMLCfg, StringBuffer spOutBuf)
    {
        NET_EHOME_XML_CFG struXMLCfg = new NET_EHOME_XML_CFG();
        StringPointer pCMDBuf = new StringPointer(sCMDType);
        pCMDBuf.write();
        struXMLCfg.pCmdBuf = pCMDBuf.getPointer();
        struXMLCfg.dwCmdLen = sCMDType.length();

        StringPointer pInBuf = new StringPointer(sXMLCfg);
        pInBuf.write();
        struXMLCfg.pInBuf = pInBuf.getPointer();
        struXMLCfg.dwInSize = sXMLCfg.length();

        StringPointer pOutBuf = new StringPointer(1024 * 20);
        pOutBuf.write();
        struXMLCfg.pOutBuf = pOutBuf.getPointer();
        struXMLCfg.dwOutSize = 1024 * 20;

        struXMLCfg.dwSendTimeOut = 6000;
        struXMLCfg.dwRecvTimeOut = 6000;

        struXMLCfg.write();
        if(!ECMS.GetCMSInstance().NET_ECMS_XMLConfig(lUserID, struXMLCfg.getPointer(), struXMLCfg.size()))
        {
            logger.info("NET_ECMS_XMLConfig cmd: "+ sCMDType + ", failed, errCode:  "+ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            System.out.println("NET_ECMS_XMLConfig cmd: "+ sCMDType + ", failed, errCode:  "+ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            CommonMethod.logRecord("ERROR", "NET_ECMS_XMLConfig cmd:" + sCMDType + ", failed, errCode: " +
                    ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return false;
        }
        else
        {
            logger.info("NET_ECMS_XMLConfig cmd:" + sCMDType + " Success!");
            CommonMethod.logRecord("ERROR", "NET_ECMS_XMLConfig cmd:" + sCMDType + " Success!");
            struXMLCfg.read();
            pOutBuf.read();
            spOutBuf.append(pOutBuf.toString());
            return true;
        }
    }

    public static boolean XMLRemoteControl(long lUserID, String sReqStr)
    {
        NET_EHOME_XML_REMOTE_CTRL_PARAM struXMLCfg = new NET_EHOME_XML_REMOTE_CTRL_PARAM();
        StringPointer pInbound = new StringPointer(sReqStr);
        StringPointer pOutBuff = new StringPointer(20 * 1024);
        pOutBuff.write();
        pInbound.write();
        struXMLCfg.lpInbuffer = pInbound.getPointer();
        struXMLCfg.dwInBufferSize = sReqStr.length();
        // struXMLCfg.dwSendTimeOut = 5 * 1000;
        // struXMLCfg.dwRecvTimeOut = 5 * 1000;
        struXMLCfg.dwOutBufferSize = 20 * 1024;
        struXMLCfg.lpOutBuffer = pOutBuff.getPointer();
        struXMLCfg.dwSize = struXMLCfg.size();

        struXMLCfg.write();
        if(ECMS.GetCMSInstance().NET_ECMS_XMLRemoteControl(lUserID, struXMLCfg.getPointer(), struXMLCfg.dwSize))
        {
            struXMLCfg.read();
            pOutBuff.read();
            String sOutBuffer = struXMLCfg.lpOutBuffer.getString(0, "utf-8");
            if(sOutBuffer.contains("200"))
            {
                CommonMethod.logRecord("INFO", "NET_ECMS_XMLRemoteControl Success");
                return true;
            }
            else
            {
                CommonMethod.logRecord("ERROR", "NET_ECMS_XMLRemoteControl Failed");
                return false;
            }
        }
        else
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_XMLRemoteControl Failed, errCode:" + ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return false;
        }
    }
}
