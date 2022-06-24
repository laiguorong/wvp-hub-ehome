package com.wvp.hk.module.service.stream;


import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.wvp.hk.module.service.cms.ISUPCMSByJNA.*;

public class FileFind {
    private static long m_lFindHandle = -1;

    public static JSONArray FindRecFile(long lUserID, NET_EHOME_REC_FILE_COND struRecFileCond)
    {
        struRecFileCond.write();
        m_lFindHandle = ECMS.GetCMSInstance().NET_ECMS_StartFindFile_V11(lUserID, ENUM_SEARCH_RECORD_FILE, struRecFileCond.getPointer(), struRecFileCond.size());
        if(m_lFindHandle >= 0)
        {
            struRecFileCond.read();
            CommonMethod.logRecord("INFO", "NET_ECMS_StartFindFile_V11 Success");

            NET_EHOME_REC_FILE struFileInfo = new NET_EHOME_REC_FILE();
            struFileInfo.dwSize = struFileInfo.size();
            struFileInfo.write();
            long lRet = -1;
            JSONArray arrJsonInfo = new JSONArray();
            while(true)
            {
                lRet = ECMS.GetCMSInstance().NET_ECMS_FindNextFile_V11(m_lFindHandle, struFileInfo.getPointer(), struFileInfo.dwSize);
                struFileInfo.read();
                String sFileSize = "";
                if(lRet == ENUM_GET_NEXT_STATUS_SUCCESS)
                {
                    if(struFileInfo.dwFileSize / 1024 == 0)
                        sFileSize = struFileInfo.dwFileSize + "B";
                    else if(struFileInfo.dwFileSize / 1024 > 0 && struFileInfo.dwFileSize /(1024*1024) == 0)
                        sFileSize = struFileInfo.dwFileSize / 1024 + "KB";
                    else
                        sFileSize = struFileInfo.dwFileSize / 1024 /1024 + "MB";

                    JSONObject jsonFileInfo = new JSONObject();
                    jsonFileInfo.put("fileSize", sFileSize);
                    jsonFileInfo.put("fileName", CommonMethod.byteToString(struFileInfo.szFileName));
                    jsonFileInfo.put("startTime", CommonMethod.StruTimeToStrTime(struFileInfo.struStartTime));
                    jsonFileInfo.put("stopTime", CommonMethod.StruTimeToStrTime(struFileInfo.struStopTime));
                    arrJsonInfo.put(jsonFileInfo);
                }
                else
                {
                    if(lRet == ENUM_GET_NETX_STATUS_NEED_WAIT)
                    {
                        try {
                            Thread.sleep(5000);
                            continue;
                        }catch (InterruptedException ex){
                            ex.printStackTrace();
                        }
                    }
                    else if(lRet == ENUM_GET_NETX_STATUS_NO_FILE)
                    {
                        System.out.println("No more file!");
                        CommonMethod.logRecord("INFO", "found file number is 0");
                        break;
                    }
                    else if(lRet == ENUM_GET_NEXT_STATUS_FINISH)
                    {
                        System.out.println("Search File Finish");
                        break;
                    }
                    else if(lRet == ENUM_GET_NEXT_STATUS_NOT_SUPPORT)
                    {
                        System.out.println("Device does not support");
                    }
                    else
                    {
                        System.out.println("Failed to find a file, for the server is busy or network failure!");
                        break;
                    }
                }
            }

            StopFindFile();
            return arrJsonInfo;
        }
        else
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_StartFindFile_V11 Failed, errCode:" +
                    ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return null;
        }
    }

    public static JSONArray FindPicFile(long lUserID, NET_EHOME_PIC_FILE_COND struPicFileCond) {
        struPicFileCond.write();
        m_lFindHandle = ECMS.GetCMSInstance().NET_ECMS_StartFindFile_V11(lUserID, ENUM_SEARCH_RECORD_FILE, struPicFileCond.getPointer(), struPicFileCond.size());
        if(m_lFindHandle >= 0)
        {
            struPicFileCond.read();
            CommonMethod.logRecord("INFO", "NET_ECMS_StartFindFile_V11 Success");

            NET_EHOME_PIC_FILE struFileInfo = new NET_EHOME_PIC_FILE();
            struFileInfo.dwSize = struFileInfo.size();
            struFileInfo.write();
            long lRet = -1;
            JSONArray arrJsonInfo = new JSONArray();
            while(true)
            {
                lRet = ECMS.GetCMSInstance().NET_ECMS_FindNextFile_V11(m_lFindHandle, struFileInfo.getPointer(), struFileInfo.dwSize);
                struFileInfo.read();
                String sFileSize = "";
                if(lRet == ENUM_GET_NEXT_STATUS_SUCCESS)
                {
                    if(struFileInfo.dwFileSize / 1024 == 0)
                        sFileSize = struFileInfo.dwFileSize + "Byte";
                    else if(struFileInfo.dwFileSize / 1024 > 0 && struFileInfo.dwFileSize /(1024*1024) == 0)
                        sFileSize = struFileInfo.dwFileSize + "KB";
                    else
                        sFileSize = struFileInfo.dwFileSize + "MB";

                    JSONObject jsonFileInfo = new JSONObject();
                    jsonFileInfo.put("fileSize", sFileSize);
                    jsonFileInfo.put("fileName", struFileInfo.szFileName);
                    jsonFileInfo.put("startTime", CommonMethod.StruTimeToStrTime(struFileInfo.struPicTime));
                    jsonFileInfo.put("stopTime", CommonMethod.StruTimeToStrTime(struFileInfo.struPicTime));
                    jsonFileInfo.put("snapType", struFileInfo.dwFileMainType);
                    arrJsonInfo.put(jsonFileInfo);
                }
                else
                {
                    if(lRet == ENUM_GET_NETX_STATUS_NEED_WAIT)
                    {
                        try {
                            Thread.sleep(5000);
                            continue;
                        }catch (InterruptedException ex){
                            ex.printStackTrace();
                        }
                    }
                    else if((lRet == ENUM_GET_NETX_STATUS_NO_FILE) || (lRet == ENUM_GET_NEXT_STATUS_FINISH))
                    {
                        System.out.println("No more file!");
                        break;
                    }
                    else if(lRet == ENUM_GET_NEXT_STATUS_NOT_SUPPORT)
                    {
                        System.out.println("Device does not support");
                    }
                    else
                    {
                        System.out.println("Failed to find a file, for the server is busy or network failure!");
                        break;
                    }
                }
            }

            StopFindFile();
            return arrJsonInfo;
        }
        else
        {
            CommonMethod.logRecord("ERROR", "NET_ECMS_StartFindFile_V11 Failed, errCode:" +
                    ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            return null;
        }
    }

    private static boolean StopFindFile()
    {
        if(m_lFindHandle >= 0)
        {
            boolean bRet = ECMS.GetCMSInstance().NET_ECMS_StopFindFile(m_lFindHandle);
            if(bRet)
            {
                CommonMethod.logRecord("INFO", "NET_ECMS_StopFindFile Success");
            }
            else
            {
                CommonMethod.logRecord("ERROR", "NET_ECMS_StopFindFile Failed, errCode:" +
                        ECMS.GetCMSInstance().NET_ECMS_GetLastError());
            }
            return bRet;
        }
        return false;
    }
}
