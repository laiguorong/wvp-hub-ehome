package com.wvp.hk.module.service.storage;

import com.wvp.hk.module.common.CommonClass.IntPointer;
import com.wvp.hk.module.common.CommonClass.StringPointer;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.alarm.ISUPAMSByJNA;
import com.wvp.hk.module.service.storage.ISUPSSByJNA.*;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ESS {
    private static ISUPSSByJNA m_SSInstance = null;

    private static final String SS_PATH         = "./StorageServer";
    private static final String SS_STORAGE_PATH = "./StorageServer/Storage";
    private static final String SS_MESSAGE_PATH = "./StorageServer/Message";
    private static EHomeSSStorageCallBack m_fEHomeSStorageCallBack = null;
    private static EHomeSSMsgCallBack m_fEHomeSSMsgCallBack = null;
    private static EHomeSSRWCallBack m_fEHomeSSRWCallBack = null;
    private static ISUPSSByJNA.EHomeSSRWCallBackEx m_fEHomeSSRWCallBackEx = null;

    private static Map<String, String> m_mapPictureStorageList = new ConcurrentHashMap<String, String>();

    private int m_lSSListenHandle = -1;
    private static NET_EHOME_SS_LISTEN_PARAM m_struSSListenParam = new NET_EHOME_SS_LISTEN_PARAM();
    private boolean m_bIsRunning = false;


    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(ESS.class);

    @Value("${ehome.in-ip}")
    private String ehomeInIp;

    @Value("${ehome.ess-prot}")
    private short ehomeEssProt;

    @Value("${ehome.ess-accesskey}")
    private String ehomeEssAccessKey;

    @Value("${ehome.ess-secretkey}")
    private String ehomeEssSecretKey;

    @Value("${ehome.ess-kmsUserName}")
    private String ehomeEssKmsUserName;

    @Value("${ehome.ess-kmsPassword}")
    private String ehomeEssKmsPassWord;

    //初始化ESS存储服务

    public void ESSInit() {
        if(m_SSInstance == null) {
            if(!CreateSSInstance())
            {
                System.out.println("Load SS fail");
                return;
            }
        }

        //config sdk path
        String sSDKPath = null;
        String strPathSq = null;

        if(CommonMethod.isLinux()){
            sSDKPath="/home/hik/LinuxSDK/HCAapSDKCom/";
            strPathSq = "/home/hik/LinuxSDK/libsqlite3.so";
        }else{
            sSDKPath=System.getProperty("user.dir")+"\\lib\\HCAapSDKCom\\";
            strPathSq =  System.getProperty("user.dir")+"\\lib\\sqlite3.dll";//Linux版本是libssl.so库文件的路径
        }
        StringPointer strPointer = new StringPointer(sSDKPath);
        strPointer.write();
        if (m_SSInstance.NET_ESS_SetSDKInitCfg(ISUPSSByJNA.NET_EHOME_SS_INIT_CFG_SDK_PATH, strPointer.getPointer())) {
            System.out.println("SDKPath:" + sSDKPath);
        }else {
            System.out.println("SDKPath:" + sSDKPath);
        }
        strPointer.read();

        //设置sqlite3.dll所在路径
        ISUPSSByJNA.BYTE_ARRAY ptrByteArraySq = new ISUPSSByJNA.BYTE_ARRAY(256);


        //ESS服务器相关配置
        {
            CommonMethod.GetServerInfo().struPictureSever.szIP=ehomeInIp.getBytes();
            CommonMethod.GetServerInfo().struPictureSever.wPort=ehomeEssProt;
        }

        System.out.println("sqlite3 Path:" + strPathSq);
        System.arraycopy(strPathSq.getBytes(), 0, ptrByteArraySq.byValue, 0, strPathSq.length());
        ptrByteArraySq.write();
        m_SSInstance.NET_ESS_SetSDKInitCfg(6, ptrByteArraySq.getPointer());

        //Set the address of the public network (only valid when the private network is mapped to the public network).
        CommonMethod.GetServerInfo().struPictureSever.write();
        if(m_SSInstance.NET_ESS_SetSDKInitCfg(ISUPSSByJNA.NET_EHOME_SS_INIT_CFG_PUBLIC_IP_PORT,
                CommonMethod.GetServerInfo().struPictureSever.getPointer())){
            logger.info("NET_EHOME_SS_INIT_CFG_PUBLIC_IP_PORT Success, IP:" +
                    CommonMethod.byteToString(CommonMethod.GetServerInfo().struPictureSever.szIP) + "Port:" +
                    CommonMethod.GetServerInfo().struPictureSever.wPort);
        }else{
            logger.error("NET_EHOME_SS_INIT_CFG_PUBLIC_IP_PORT Fail");
        }

        //Set openssl dll path
        String sLibCryptoPath = null;
        String sLibSslPath = null;

        if(CommonMethod.isLinux()){
            sLibCryptoPath = "/home/hik/LinuxSDK/libcrypto.so";
            sLibSslPath = "/home/hik/LinuxSDK/libssl.so";
        }else{
            sLibCryptoPath = System.getProperty("user.dir")+"\\lib\\libeay32.dll";
            sLibSslPath = System.getProperty("user.dir")+"\\lib\\ssleay32.dll";
        }
        StringPointer pLibCryptoPath = new StringPointer(sLibCryptoPath);
        StringPointer pLibSslPath = new StringPointer(sLibSslPath);
        pLibCryptoPath.write();
        pLibSslPath.write();

//        if(m_SSInstance.NET_ESS_SetSDKInitCfg(ISUPSSByJNA.NET_EHOME_SS_INIT_CFG_LIBEAY_PATH,
//                pLibCryptoPath.getPointer())){
//            CommonMethod.logRecord("INFO", "LIBEAY load success: " + sLibCryptoPath);
//        }else{
//            CommonMethod.logRecord("ERROR","LIBEAY load fail" + sLibCryptoPath);
//        }
//
//        if(m_SSInstance.NET_ESS_SetSDKInitCfg(ISUPSSByJNA.NET_EHOME_SS_INIT_CFG_SSLEAY_PATH,
//                pLibSslPath.getPointer())){
//            CommonMethod.logRecord("INFO", "SSLEAY load success: " + sLibSslPath);
//        }else{
//            CommonMethod.logRecord("ERROR","SSLEAY load fail" + sLibSslPath);
//        }

        //设置libcrypto.so或libeay32.dll所在路径
        ISUPSSByJNA.BYTE_ARRAY ptrByteArrayCrypto = new ISUPSSByJNA.BYTE_ARRAY(256);
        String strPathCrypto = null; //Linux版本是libcrypto.so库文件的路径
        if(CommonMethod.isLinux()){
            strPathCrypto = "/home/hik/LinuxSDK/libcrypto.so";
        }else{
            strPathCrypto = System.getProperty("user.dir")+"\\lib\\libeay32.dll";
        }

        System.out.println("libeay32 Path:" + strPathCrypto);
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        m_SSInstance.NET_ESS_SetSDKInitCfg(4, ptrByteArrayCrypto.getPointer());
        System.out.println("初始化库文件路径：" + strPathCrypto);

        //设置libssl.so或ssleay32.dll所在路径
        ISUPSSByJNA.BYTE_ARRAY ptrByteArraySsl = new ISUPSSByJNA.BYTE_ARRAY(256);
        String strPathSsl = null;//Linux版本是libssl.so库文件的路径

        if(CommonMethod.isLinux()){
            strPathSsl = "/home/hik/LinuxSDK/libssl.so";
        }else{
            strPathSsl = System.getProperty("user.dir")+"\\lib\\ssleay32.dll";
        }
        System.out.println("ssleay32 Path:" + strPathSsl);
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        m_SSInstance.NET_ESS_SetSDKInitCfg(5, ptrByteArraySsl.getPointer());
        System.out.println("初始化库文件路径：" + strPathSsl);

        if(m_SSInstance.NET_ESS_Init()) {
            logger.info("[NET_ESS_Init]->ESS initialize successfully");
            // open ESS sdk log
            m_SSInstance.NET_ESS_SetLogToFile(3, "./EHomeSdkLog", true);

            // register ESS registetr message callback function
            if (CommonMethod.GetListenInfo().byCallbackType == 0) {
                m_fEHomeSStorageCallBack = new FEHomeSSStorageCallBack();
                m_struSSListenParam.fnSStorageCb = m_fEHomeSStorageCallBack;
            }
            else if(CommonMethod.GetListenInfo().byCallbackType == 1)
            {
                if (m_fEHomeSSRWCallBack == null) {
                    m_fEHomeSSRWCallBack = new FEHomeSSRWCallBack();
                    m_struSSListenParam.fnSSRWCb = m_fEHomeSSRWCallBack;
                }
            }
            else if(CommonMethod.GetListenInfo().byCallbackType == 2)
            {
                if (m_fEHomeSSRWCallBackEx == null) {
                    m_fEHomeSSRWCallBackEx = new FEHomeSSRWCallBackEX();
                    m_struSSListenParam.fnSSRWCbEx = m_fEHomeSSRWCallBackEx;
                }
            }

            if (m_fEHomeSSMsgCallBack == null) {
                m_fEHomeSSMsgCallBack = new FEHomeSSMsgCallBack();
                m_struSSListenParam.fnSSMsgCb = m_fEHomeSSMsgCallBack;
            }

            CommonMethod.GetListenInfo().struSSListenParam.struAddress.szIP=ehomeInIp.getBytes();
            CommonMethod.GetListenInfo().struSSListenParam.struAddress.wPort=ehomeEssProt;
            CommonMethod.GetListenInfo().struSSListenParam.szAccessKey=ehomeEssAccessKey.getBytes();
            CommonMethod.GetListenInfo().struSSListenParam.szSecretKey=ehomeEssSecretKey.getBytes();
            CommonMethod.GetListenInfo().struSSListenParam.szKMS_UserName=ehomeEssKmsUserName.getBytes();
            CommonMethod.GetListenInfo().struSSListenParam.szKMS_Password=ehomeEssKmsPassWord.getBytes();
            CommonMethod.GetServerInfo().byClouldHttps=0;

            CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struSSListenParam.struAddress.szIP, m_struSSListenParam.struAddress.szIP);
            m_struSSListenParam.struAddress.wPort = CommonMethod.GetListenInfo().struSSListenParam.struAddress.wPort;
            CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struSSListenParam.szAccessKey, m_struSSListenParam.szAccessKey);
            CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struSSListenParam.szSecretKey, m_struSSListenParam.szSecretKey);
            CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struSSListenParam.szKMS_UserName, m_struSSListenParam.szKMS_UserName);
            CommonMethod.ByteCopy(CommonMethod.GetListenInfo().struSSListenParam.szKMS_Password, m_struSSListenParam.szKMS_Password);
            m_struSSListenParam.byHttps = CommonMethod.GetServerInfo().byClouldHttps;
//            m_struSSListenParam.bySecurityMode = CommonMethod.GetListenInfo().struSSListenParam.bySecurityMode;

            m_lSSListenHandle = m_SSInstance.NET_ESS_StartListen(m_struSSListenParam);
            if (m_lSSListenHandle < 0) {
                logger.error("[NET_ESS_StartListen]->SS start listen failed, errorCode is " + m_SSInstance.NET_ESS_GetLastError());
            } else {
                m_bIsRunning = true;
                logger.info("[NET_ESS_StartListen]->SS start listen successfully; port:" +
                        m_struSSListenParam.struAddress.wPort);
            }
        }else{
            logger.error("[NET_ESS_Init]->ESS initialize Failed, errorCode:" + m_SSInstance.NET_ESS_GetLastError());
        }
    }

    public void StopESS() {
        boolean bIsStopSSListen = m_SSInstance.NET_ESS_StopListen(m_lSSListenHandle);
        if (bIsStopSSListen) {
            m_bIsRunning = false;
            logger.info("[NET_ESS_StopListen]->SS stop listen successfully");
            m_lSSListenHandle=-1;
        } else {
            logger.error("[NET_ESS_StopListen]->AMS stop listen failed, errorCode is:" + m_SSInstance.NET_ESS_GetLastError());
        }
        // SDK cleanUp
        m_SSInstance.NET_ESS_Fini();
        logger.info("[NET_ESS_Fini]->EHomeSS release resource successfully");
    }

    //RW callback, the user needs to maintain the correspondence between the image file path and the image url by themselves
    //If the user defines the storage callback and RW callback at the same time, the Storage callback will not take effect
    private class FEHomeSSRWCallBack implements EHomeSSRWCallBack{
        @Override
        public boolean invoke(int iHandle, byte byAct, String pFileName, Pointer pFileBuf, IntPointer dwFileLen, Pointer pFileUrl, Pointer pUser) {
            //write file(upload file to local storage server)
            if(byAct == 0)
            {
                String strFilePath = "";
                if(pFileName != null)
                {
                    strFilePath = SS_STORAGE_PATH + "/" + pFileName;
                }

                byte[] byFileURL = new byte[256];
                if(pFileUrl != null)
                {
                    CommonMethod.ByteCopy(pFileUrl.getByteArray(0, 256), byFileURL);
                }

                File myPath = new File(SS_STORAGE_PATH);
                if ( !myPath.exists()){
                    myPath.mkdir();
                    System.out.println("mkdir path："+ SS_STORAGE_PATH);
                }

                if (dwFileLen.GetData() > 0 && pFileBuf != null)
                {
                    FileOutputStream fout;
                    try {
                        fout = new FileOutputStream(strFilePath);
                        //将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = pFileBuf.getByteBuffer(offset, dwFileLen.GetData());
                        byte [] bytes = new byte[dwFileLen.GetData()];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                String sPictureUrl = CommonMethod.byteToString(byFileURL);
                System.out.println("Picture url: " + sPictureUrl);
                //Here as an example, we use hashmap to save the relationship between image url and file path.
                //production scene, it is recommended to use a key value similar to redis to save the database
                m_mapPictureStorageList.put(sPictureUrl, strFilePath);
            }

            //read file(download file from local storage server)
            if(byAct == 1)
            {
                String strFilePath = "";
                if(pFileUrl != null)
                {
                    byte[] byFileURL = new byte[256];
                    CommonMethod.ByteCopy(pFileUrl.getByteArray(0, 256), byFileURL);
                    String sPicUrl = CommonMethod.byteToString(byFileURL);
                    //remove [token words] of the kms url
                    if(sPicUrl.contains("kms"))
                    {
                        sPicUrl = sPicUrl.split("&token=")[0];
                    }
                    System.out.println("download file url: " + sPicUrl);
                    if(m_mapPictureStorageList.containsKey(sPicUrl))
                    {
                        strFilePath = m_mapPictureStorageList.get(sPicUrl);
                    }
                    System.out.println("Picture Path:" + strFilePath);
                }
                //according sql to get the image file path
                try {
                    FileInputStream picfile = null;
                    int picdataLength = 0;

                    picfile = new FileInputStream(new File(strFilePath));
                    picdataLength = picfile.available();

                    if(picdataLength < 0)
                    {
                        System.out.println("input file dataSize < 0");
                        return false;
                    }

                    //There will be two callbacks here
                    if (pFileBuf == null) //if(pFileBuf == null) => 1st callback
                    {
                        dwFileLen.SetData(picdataLength);  //only return file size
                    }

                    if(pFileBuf != null)    //if(pFileBuf != null) => 2nd callback
                    {
                        //return file content
                        StringPointer ptrpicByte = new StringPointer(picdataLength);
                        int dwReadCount = 0;
                        while(dwReadCount < picdataLength)
                        {
                            dwReadCount += picfile.read(ptrpicByte.sData, dwReadCount, picdataLength - dwReadCount);
                        }
                        CommonMethod.WriteBuffToPointer(ptrpicByte.sData, pFileBuf);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
            //byAct == 2（delete file）No example
            return true;
        }
    }

    //Storage callback: The SDK will save the relationship between the image url and the file path through the built-in
    // Sqlite database, without the user's own maintenance, the user only needs to save the file.
    //But due to the performance limitations of sqlite, its read and write efficiency is not high
    private class FEHomeSSStorageCallBack implements EHomeSSStorageCallBack {
        @Override
        public boolean invoke(int iHandle, String pFileName, Pointer pFileBuf, int dwFileLen, Pointer pFilePath,
                              Pointer pUser) {
            if (pFileName == null || pFileBuf == null || dwFileLen == 0) {
                return false;
            }

            File mySSPath = new File(SS_PATH);
            if ( !mySSPath.exists()){
                mySSPath.mkdir();
                System.out.println("Creat dictionary:"+ SS_PATH);
            }

            File mySSStoragePath = new File(SS_STORAGE_PATH);
            if ( !mySSStoragePath.exists()){
                mySSStoragePath.mkdir();
                System.out.println("Creat dictionary:"+ SS_STORAGE_PATH);
            }

            String sFileName = SS_STORAGE_PATH + "\\" + pFileName.replace(":", "_");
            try {
                File fw=new File(sFileName);
                InputStream bis = new ByteArrayInputStream(pFileBuf.getByteArray(0, dwFileLen));
                OutputStream out = new FileOutputStream(fw);
                //fw.write(CommonMethod.byteToString(pFileBuf.getByteArray(0, dwFileLen)));
                int count = 0;
                byte[] buf = new byte[8 * 1024];
                while( (count=bis.read(buf)) != -1 ) {
                    out.write(buf, 0, count);
                }
                out.close();

                StringPointer spFilePath = new StringPointer(sFileName);
                spFilePath.write();
                CommonMethod.PointerCopy(spFilePath.getPointer(), pFilePath, sFileName.length());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("StorageCallback Exception, filePath: " + sFileName);
                return false;
            }

            return true;
        }
    }

    //Message callback is used when the picture server needs to upload to the user, and the user needs to confirm certain parameters
    // tomcat -> return picture url
    // vrb -> file code
    // KMS -> kmsUserName & kmsUserPasswd
    // CloudStorage -> AK & SK
    private class FEHomeSSMsgCallBack implements EHomeSSMsgCallBack {
        @Override
        public boolean invoke(int iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
                              int dwInLen, Pointer pUser) {
            switch (enumType) {
                case ISUPSSByJNA.NET_EHOME_SS_MSG_TOMCAT:
                {
                    NET_EHOME_SS_TOMCAT_MSG struTomcatMsg = new NET_EHOME_SS_TOMCAT_MSG();
                    try{
                        String sClassName = NET_EHOME_SS_TOMCAT_MSG.class.getName();
                        struTomcatMsg = (NET_EHOME_SS_TOMCAT_MSG) CommonMethod.WritePointerDataToClass(pOutBuffer, sClassName);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }

                    int dwPicNum = struTomcatMsg.dwPicNum;
                    byte[] byPicUri = new byte[128 * 4];
//				for(int i = 0; i < dwPicNum; i++)
//				{
//					System.arraycopy(struTomcatMsg.pPicURLs, i * ISUPSSByJNA.MAX_URL_LEN_SS, byPicUri, i * ISUPSSByJNA.PIC_URI_LEN, ISUPSSByJNA.PIC_URI_LEN);
//
//				}
                    CommonMethod.ByteCopy(struTomcatMsg.pPicURLs, byPicUri);

                    String szUrlHead = CommonMethod.byteToString(struTomcatMsg.szDevUri);
                    String szPicUrl = CommonMethod.byteToString(byPicUri);
                    String szMsg = "tomcat:url" + szUrlHead + ", picNum: " + dwPicNum + ", picInfo:" + szPicUrl;

                    logger.info("[NET_EHOME_SS_MSG_TOMCAT]->" + szMsg);
                    File mySSPath = new File(SS_PATH);
                    if ( !mySSPath.exists()){
                        mySSPath.mkdir();
                        System.out.println("Creat dictionary:"+ SS_PATH);
                    }

                    File mySSMessagePath = new File(SS_MESSAGE_PATH);
                    if ( !mySSMessagePath.exists()){
                        mySSMessagePath.mkdir();
                        System.out.println("Creat dictionary:"+ SS_MESSAGE_PATH);
                    }
                    String sFileName = SS_MESSAGE_PATH + "\\tomcatOutput.txt";

                    try {
                        FileWriter fw = new FileWriter(sFileName, true);

//					for(int i = 0; i < dwPicNum; i++)
//					{
//						fw.write(szPicUrl, szPicUrl.length() + i * ISUPSSByJNA.PIC_URI_LEN, ISUPSSByJNA.PIC_URI_LEN);
//						fw.write("\n");
//					}
                        fw.write(szPicUrl, 0, szPicUrl.length());
                        fw.write("\n");
                        fw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case ISUPSSByJNA.NET_EHOME_SS_MSG_KMS_USER_PWD:
                {
                    StringPointer spInBuff = new StringPointer("1");
                    spInBuff.write();
                    pInBuffer = spInBuff.getPointer();
                }
                case ISUPSSByJNA.NET_EHOME_SS_MSG_CLOUD_AK:
                {
                    byte[] byTemp = new byte[128];
                    System.arraycopy(pOutBuffer.getByteArray(0, dwOutLen), 0, byTemp, 0, dwOutLen);
                    String sSecKey = CommonMethod.byteToString(CommonMethod.GetServerInfo().byClouldSecretKey);
                    StringPointer spInBuff = new StringPointer(sSecKey);
                    spInBuff.write();
                    CommonMethod.PointerCopy(spInBuff.getPointer(), pInBuffer, sSecKey.length());
                    break;
                }
                default:
                    break;
            }
            return true;
        }
    }

    //RW callback, the user needs to maintain the correspondence between the image file path and the image url by themselves
    //If the user defines the storage callback and RW callback at the same time, the Storage callback will not take effect
    private class FEHomeSSRWCallBackEX implements ISUPSSByJNA.EHomeSSRWCallBackEx {
        @Override
        public boolean invoke(int iHandle, ISUPSSByJNA.NET_EHOME_SS_RW_PARAM pRwParam, ISUPSSByJNA.NET_EHOME_SS_EX_PARAM pExStruct) {
            //write file(upload file to local storage server)
            if(pRwParam.byAct == 0)
            {
                String strFilePath = "";
                if(pRwParam.pFileName != null)
                {
                    strFilePath = SS_STORAGE_PATH + "/" + pRwParam.pFileName;
                }

                byte[] byFileURL = new byte[256];
                if(pRwParam.pFileUrl != null)
                {
                    CommonMethod.ByteCopy(pRwParam.pFileUrl.getByteArray(0, 256), byFileURL);
                }

                File myPath = new File(SS_STORAGE_PATH);
                if ( !myPath.exists()){
                    myPath.mkdir();
                    System.out.println("mkdir path："+ SS_STORAGE_PATH);
                }

                if (pRwParam.dwFileLen.getValue() > 0 && pRwParam.pFileBuf != null)
                {
                    FileOutputStream fout;
                    try {
                        fout = new FileOutputStream(strFilePath);
                        //将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = pRwParam.pFileBuf.getByteBuffer(offset, pRwParam.dwFileLen.getValue());
                        byte [] bytes = new byte[pRwParam.dwFileLen.getValue()];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                String sPictureUrl = CommonMethod.byteToString(byFileURL);
                System.out.println("Picture url: " + sPictureUrl);
                //Here as an example, we use hashmap to save the relationship between image url and file path.
                //production scene, it is recommended to use a key value similar to redis to save the database
                m_mapPictureStorageList.put(sPictureUrl, strFilePath);
            }

            //read file(download file from local storage server)
            if(pRwParam.byAct == 1)
            {
                String strFilePath = "";
                if(pRwParam.pFileUrl != null)
                {
                    byte[] byFileURL = new byte[256];
                    CommonMethod.ByteCopy(pRwParam.pFileUrl.getByteArray(0, 256), byFileURL);
                    String sPicUrl = CommonMethod.byteToString(byFileURL);
                    //remove [token words] of the kms url
                    if(sPicUrl.contains("kms"))
                    {
                        sPicUrl = sPicUrl.split("&token=")[0];
                    }
                    System.out.println("download file url: " + sPicUrl);
                    if(m_mapPictureStorageList.containsKey(sPicUrl))
                    {
                        strFilePath = m_mapPictureStorageList.get(sPicUrl);
                    }
                    System.out.println("Picture Path:" + strFilePath);
                }
                //according sql to get the image file path
                try {
                    FileInputStream picfile = null;
                    int picdataLength = 0;

                    picfile = new FileInputStream(new File(strFilePath));
                    picdataLength = picfile.available();

                    if(picdataLength < 0)
                    {
                        System.out.println("input file dataSize < 0");
                        return false;
                    }

                    //There will be two callbacks here
                    if (pRwParam.pFileBuf == null) //if(pFileBuf == null) => 1st callback
                    {
                        pRwParam.dwFileLen.setValue(picdataLength);  //only return file size
                    }

                    if(pRwParam.pFileBuf != null)    //if(pFileBuf != null) => 2nd callback
                    {
                        //return file content
                        StringPointer ptrpicByte = new StringPointer(picdataLength);
                        int dwReadCount = 0;
                        while(dwReadCount < picdataLength)
                        {
                            dwReadCount += picfile.read(ptrpicByte.sData, dwReadCount, picdataLength - dwReadCount);
                        }
                        CommonMethod.WriteBuffToPointer(ptrpicByte.sData, pRwParam.pFileBuf);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
            //byAct == 2（delete file）No example
            return true;
        }
    }

    private boolean CreateSSInstance()
    {
        if(m_SSInstance == null)
        {
            synchronized (ISUPAMSByJNA.class)
            {
                String strDllPath = "";
                try
                {
                    //System.setProperty("jna.debug_load", "true");
                    if(CommonMethod.isWindows()) {
                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCISUPSS";
                    }else if(CommonMethod.isLinux()) {
                        strDllPath = "/home/hik/LinuxSDK/libHCISUPSS.so";
                    }
                    logger.info("[ESS]->"+strDllPath);
                    m_SSInstance = (ISUPSSByJNA) Native.loadLibrary(strDllPath, ISUPSSByJNA.class);
                }catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public static ISUPSSByJNA GetSSInstance()
    {
        return m_SSInstance;
    }

    public boolean IsRunning(){return m_bIsRunning;}
}
