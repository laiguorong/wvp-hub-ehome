package com.wvp.hk.module.service.storage;


import com.wvp.hk.module.common.CommonClass.StringPointer;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.storage.ISUPSSByJNA.*;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;

import static com.wvp.hk.module.service.storage.ISUPSSByJNA.*;

public class StorageClient {
    //For convenience, the upload interface unifies the client parameters of each image server into one interface.
    // In production scenarios, users only need to enter parameters for a single image server type supported by the device
	public static boolean UploadPicFile(String sFilePath, StringPointer sFileBuff, NET_EHOME_SS_CLIENT_PARAM struClientParam, String sKMSUserName, String sKMSPwd, String sCloudAK,
										String sCloudSK, String sVRBCode, StringBuffer strResUrl)
	{
		boolean bRet = false;
		int lPssClientHandle = ESS.GetSSInstance().NET_ESS_CreateClient(struClientParam);
	    if (lPssClientHandle < 0)
	    {
			CommonMethod.logRecord("ERROR", "[NET_ESS_CreateClient] Call Error, ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
	        return bRet;
	    }
	    else
	    {
	    	CommonMethod.logRecord("INFO", "[NET_ESS_CreateClient] Call Success");
	    	ESS.GetSSInstance().NET_ESS_ClientSetTimeout(lPssClientHandle, 60 * 1000, 60 * 1000);
	    	switch (struClientParam.enumType) {
			case NET_EHOME_SS_CLIENT_TYPE_TOMCAT:
				break;
			case NET_EHOME_SS_CLIENT_TYPE_VRB:
				ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "Filename-Code", sVRBCode);
				break;
			case NET_EHOME_SS_CLIENT_TYPE_KMS:
				ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "KMS-Username", sKMSUserName);
				ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "KMS-Password", sKMSPwd);
				break;
			case NET_EHOME_SS_CLIENT_TYPE_CLOUD:
				ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "Access-Key", sCloudAK);
				ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "Secret-Key", sCloudSK);
				break;
			default:
				break;
			}

            StringPointer sUrlPointer = new StringPointer(MAX_URL_LEN_SS);
            sUrlPointer.write();

            if(sFilePath == null || sFilePath.equals(""))
            {
                sFileBuff.write();
                if(!ESS.GetSSInstance().NET_ESS_ClientDoUploadBuffer(lPssClientHandle, sUrlPointer.getPointer(),
                        MAX_URL_LEN_SS, sFileBuff.getPointer(), sFileBuff.sData.length)) {
                    CommonMethod.logRecord("ERROR", "[NET_ESS_ClientDoUploadBuffer] Call Error," +
                            " ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
                }else{
                    sUrlPointer.read();
                    String sResponse = CommonMethod.byteToString(sUrlPointer.sData);
                    CommonMethod.logRecord("INFO", "[NET_ESS_ClientDoUploadBuffer] Call Success, url:" + sResponse);
                    strResUrl.append(sResponse);
                    bRet = true;
                }
            }else{
                ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "File-Path", sFilePath);

                if(!ESS.GetSSInstance().NET_ESS_ClientDoUpload(lPssClientHandle, sUrlPointer.getPointer(), MAX_URL_LEN_SS))
                {
                    CommonMethod.logRecord("ERROR", "[NET_ESS_ClientDoUpload] Call Error, ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
                }
                else
                {
                    sUrlPointer.read();
                    String sResponse = CommonMethod.byteToString(sUrlPointer.sData);
                    CommonMethod.logRecord("INFO", "[NET_ESS_ClientDoUpload] Call Success, url:" + sResponse);
                    strResUrl.append(sResponse);
                    bRet = true;
                }
            }

			if(!ESS.GetSSInstance().NET_ESS_DestroyClient(lPssClientHandle))
			{
	    		CommonMethod.logRecord("ERROR", "[NET_ESS_DestroyClient] Call Error, ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
			}
			else
			{
		    	CommonMethod.logRecord("INFO", "[NET_ESS_DestroyClient] Call Success");
			}
	    }
		return bRet;
	}

	//For convenience, the download interface unifies the client parameters of each image server into one interface.
    //In production scenarios, users only need to enter parameters for a single image server type supported by the device
	public static boolean DownloadPicFile(String sFilePath, NET_EHOME_SS_CLIENT_PARAM struClientParam, String sKMSUserName, String sKMSPwd, String sCloudAK,
                                          String sCloudSK, String sVRBCode, StringBuffer sPicBuffer)
	{
        boolean bRet = false;
        int lPssClientHandle = ESS.GetSSInstance().NET_ESS_CreateClient(struClientParam);
        if (lPssClientHandle < 0)
        {
            CommonMethod.logRecord("ERROR", "[NET_ESS_CreateClient] Call Error, ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
            return bRet;
        }
        else
        {
            CommonMethod.logRecord("INFO", "[NET_ESS_CreateClient] Call Success");
            ESS.GetSSInstance().NET_ESS_ClientSetTimeout(lPssClientHandle, 60 * 1000, 60 * 1000);
            switch (struClientParam.enumType) {
                case NET_EHOME_SS_CLIENT_TYPE_TOMCAT:
                    break;
                case NET_EHOME_SS_CLIENT_TYPE_VRB:
                    ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "Filename-Code", sVRBCode);
                    break;
                case NET_EHOME_SS_CLIENT_TYPE_KMS:
                    ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "KMS-Username", sKMSUserName);
                    ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "KMS-Password", sKMSPwd);
                    break;
                case NET_EHOME_SS_CLIENT_TYPE_CLOUD:
                    ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "Access-Key", sCloudAK);
                    ESS.GetSSInstance().NET_ESS_ClientSetParam(lPssClientHandle, "Secret-Key", sCloudSK);
                    break;
                default:
                    break;
            }

            PointerByReference sPicDataBuff = new PointerByReference();
            IntByReference dwFileLength = new IntByReference(0);
            if(!ESS.GetSSInstance().NET_ESS_ClientDoDownload(lPssClientHandle, sFilePath.getBytes(), sPicDataBuff, dwFileLength))
            {
                CommonMethod.logRecord("ERROR", "[NET_ESS_ClientDoDownload] Call Error, ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
            }
            else
            {
                int dwPicLen = dwFileLength.getValue();
                Pointer pPicData = sPicDataBuff.getValue();
                ByteBuffer byPicBuffer = pPicData.getByteBuffer(0, dwPicLen);
                int dwPicBuffLen = byPicBuffer.limit() - byPicBuffer.position();
                byte[] byPicData = new byte[dwPicBuffLen];

                CommonMethod.ByteCopy(CommonMethod.Bytebuffer2ByteArray(byPicBuffer, dwPicBuffLen), byPicData);
                sPicBuffer.append("data:image/jpeg;base64," + CommonMethod.EncryptBase64(byPicData));
                CommonMethod.logRecord("INFO", "[NET_ESS_ClientDoDownload] Call Success. piclen: " + dwPicLen);
                bRet = true;
            }

            if(!ESS.GetSSInstance().NET_ESS_DestroyClient(lPssClientHandle))
            {
                CommonMethod.logRecord("ERROR", "[NET_ESS_DestroyClient] Call Error, ErrorCode is: " + ESS.GetSSInstance().NET_ESS_GetLastError());
            }
            else
            {
                CommonMethod.logRecord("INFO", "[NET_ESS_DestroyClient] Call Success");
            }
        }
        return true;
	}
}
