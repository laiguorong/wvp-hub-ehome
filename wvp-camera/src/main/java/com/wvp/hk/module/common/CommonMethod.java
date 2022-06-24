package com.wvp.hk.module.common;


import org.json.JSONException;
import org.json.JSONObject;
import com.jfinal.kit.PathKit;
import com.sun.jna.Pointer;
import com.wvp.common.utils.StringUtils;
import com.wvp.hk.module.communication.WebSocketService;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonMethod{

    public static CommonClass.NET_EHOME_SERVER_INFO_V50 GetServerInfo(){
        return CommonClass.g_struServerInfoV50;
    }

    public static CommonClass.ListenInfo GetListenInfo(){
    	return CommonClass.g_struListenInfo;
	}

	public static CommonClass.StringPointer Base64Decode(String sImgStr){
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] byAfterDecodeBuffer = decoder.decodeBuffer(sImgStr);
            for (int i = 0; i < byAfterDecodeBuffer.length; ++i) {
                //调整异常数据
                if (byAfterDecodeBuffer[i] < 0) {
                    byAfterDecodeBuffer[i] += 256;
                }
            }
            return new CommonClass.StringPointer(byAfterDecodeBuffer);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * BASE64 encrypt
     */
    public static String EncryptBase64(byte[] key) {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

	public static String byteToString(byte[] bytes) {
		if (null == bytes || bytes.length == 0) {
			return "";
		}
		int iLengthOfBytes = 0;
		for(byte st:bytes){
			if(st != 0){
				iLengthOfBytes++;
			}else
				break;
		}
		String strContent = "";
		try {
			strContent = new String(bytes, 0, iLengthOfBytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return strContent;
	}

    /**
     * byteBuffer 转 byte数组
     * @param buffer
     * @return
     */
    public static byte[] Bytebuffer2ByteArray(ByteBuffer buffer, int dwBuffLen) {
        byte [] bytes=new byte[dwBuffLen];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i]=buffer.get();
        }

        return bytes;
    }

	public static void ByteCopy(byte[] src, byte[] dest){
		System.arraycopy(src, 0, dest, 0, src.length);
	}

	public static void ByteCopy(String src, byte[] dest){
		System.arraycopy(src.getBytes(), 0, dest, 0, src.length());
	}

	public static CommonClass.NET_EHOME_TIME StrTimeToStruTime(String strTime) {
		String regex = "(\\d{4})-(\\d+)-(\\d+) (\\d+):(\\d+):(\\d+)";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(strTime);
		CommonClass.NET_EHOME_TIME struTime;
		if (matcher.matches()) {
			int year = Integer.valueOf(matcher.group(1));
			int month = Integer.valueOf(matcher.group(2));
			int day = Integer.valueOf(matcher.group(3));
			int hour = Integer.valueOf(matcher.group(4));
			int minute = Integer.valueOf(matcher.group(5));
			int second = Integer.valueOf(matcher.group(6));
			struTime = new CommonClass.NET_EHOME_TIME((short)year, (byte)month, (byte)day, (byte)hour, (byte)minute, (byte)second);
		} else
			struTime = null;
		return struTime;
	}

	public static String StruTimeToStrTime(CommonClass.NET_EHOME_TIME struTime) {
        String sTime = struTime.wYear + "-";
    	if(struTime.byMonth < 10)
            sTime += "0" + struTime.byMonth + "-";
    	else
            sTime += struTime.byMonth + "-";
        if(struTime.byDay < 10)
            sTime += "0" + struTime.byDay + " ";
        else
            sTime += struTime.byDay + " ";
        if(struTime.byHour < 10)
            sTime += "0" + struTime.byHour + ":";
        else
            sTime += struTime.byHour + ":";
        if(struTime.byMinute < 10)
            sTime += "0" + struTime.byMinute + ":";
        else
            sTime += struTime.byMinute + ":";
        if(struTime.bySecond < 10)
            sTime += "0" + struTime.bySecond;
        else
            sTime += struTime.bySecond;

        return sTime;
	}

	public static void WriteBuffToPointer(byte[] byData, Pointer pInBuffer){
		pInBuffer.write(0, byData, 0, byData.length);
	}

	public static void WriteBuffToPointer(String sData, Pointer pInBuffer){
		pInBuffer.write(0, sData.getBytes(), 0, sData.length());
	}

	public static void PointerCopy(Pointer pSrc, Pointer pDest, int dwSrcLen){
		CommonMethod.WriteBuffToPointer(pSrc.getByteArray(0, dwSrcLen), pDest);
	}

	public static Object WritePointerDataToClass(Pointer pInBuff, String sClassName) throws
	ClassNotFoundException,IllegalArgumentException, SecurityException, IllegalAccessException,
	NoSuchMethodException, InstantiationException, InvocationTargetException{

        Class<?> clazz = Class.forName(sClassName);
        Object obj = clazz.newInstance();

        clazz.getMethod("write").invoke(obj, null);
        Pointer pTemp = (Pointer)clazz.getMethod("getPointer").invoke(obj, null);
        int dwSize = (Integer)clazz.getMethod("size").invoke(obj, null);
        pTemp.write(0, pInBuff.getByteArray(0, dwSize), 0, dwSize);
        clazz.getMethod("read").invoke(obj, null);

        return obj;
	}

	public static byte BooleanToByte(boolean bInput) {
		return (byte) (bInput?1:0);
	}

	public static byte[] Int2ByteArray(int num){
		byte[]bytes=new byte[4];
		bytes[0]=(byte) ((num>>24)&0xff);
		bytes[1]=(byte) ((num>>16)&0xff);
		bytes[2]=(byte) ((num>>8)&0xff);
		bytes[3]=(byte) (num&0xff);
		return bytes;
	}

	public static int Byte2Int(Byte[]bytes) {
		return (bytes[0]&0xff)<<24
			| (bytes[1]&0xff)<<16
			| (bytes[2]&0xff)<<8
			| (bytes[3]&0xff);
	}

	public static String IntToHex(int iDec){
	    return Integer.toHexString(iDec);
    }

    public static byte HexToByte(String inHex){
        return (byte) Integer.parseInt(inHex, 16);
    }

	//judge string is or not a number
	public static boolean isNumber(String string) {
        if (string == null)
            return false;
        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
        return pattern.matcher(string).matches();
    }

	public static String generateString(int length) {
		char[] text = new char[length];
		final String stringSource = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		for (int i = 0; i < length; i++) {
			text[i] = stringSource.charAt(new SecureRandom().nextInt(stringSource.length()));
		}
		return new String(text);
	}

	public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean Is32Bit(){
    	if(isWindows() && System.getProperty("os.arch").toLowerCase().equals("i386")
    		||(isLinux() && "x86".equals(System.getProperty("os.arch").toLowerCase())
    	)){
    		return true;
    	}
    	else {
			return false;
		}
    }

    public static String GetLibPathByArch(){
    	if(isWindows()){
    		if(Is32Bit())
    			return PathKit.getWebRootPath() + "\\lib\\win32\\";
    		else
    			return PathKit.getWebRootPath() + "\\lib\\win64\\";
    	}
    	else if(isLinux()){
    		if(Is32Bit())
    			return PathKit.getWebRootPath() + "/lib/linux32/";
    		else
    			return PathKit.getWebRootPath() + "/lib/linux64/";
    	}
    	else {
    		throw new RuntimeException("System Not Support");
		}
    }

    public static String GetHostAddressOnLinux(){
    	String sHostIP = "";
    	try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                    	sHostIP = ia.getHostAddress();
                    	System.out.println(sHostIP);
                    }
                }
            }
        } catch (SocketException e) {
        	e.printStackTrace();
        }
    	return sHostIP;
    }

	//write Log Interface
	public static void logRecord(String sEventType, String sContent) {
		// Gets the current system time to save the file
		JSONObject logJson = new JSONObject();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String strDateTimeNow = df.format(new Date());
		try {
			logJson.put("sTime", strDateTimeNow);
			logJson.put("sEventType", sEventType);
			logJson.put("sContent", sContent);
		} catch (JSONException e) {
             e.printStackTrace();
		}
		sendString(WebSocketService.MessageType.LogFileRefresh.value(), logJson.toString());
	}

	public static void sendString(int iMessageType, String sContent) {
		JSONObject objMessage = null;
		try{
		if (iMessageType == WebSocketService.MessageType.AlarmInfo.value()) {
			objMessage = new JSONObject();
			objMessage.put("type", iMessageType);
			objMessage.put("content", sContent);
		} else {
			String strJson = "{\"type\": " + iMessageType + ",\"content\":" + sContent + "})";
			objMessage = new JSONObject(strJson);
		}
		}catch(JSONException ex){
			ex.printStackTrace();
		}
		WebSocketService.onMessage(objMessage.toString());
	}

    public static List<String> getFieldListByRegex(String xml, String label) {
        //正则表达式
        String regex = "<" + label + ">(.*?)</" + label + ">";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(xml);
        //匹配的有多个
        List<String> fieldList = new ArrayList<>();
        while (m.find()) {
            if (StringUtils.isNotEmpty(m.group(1).trim())) {
                fieldList.add(m.group(1).trim());
            }
        }
        return fieldList;
    }

	//Parse ajax param to  JSONObject
	public static JSONObject parseUrlData(String sReqData){
		JSONObject jsonUrlParams = new JSONObject();
		String[] params = null;
		if(sReqData.contains("&")){
			params = sReqData.split("&");
		}else{
			params = new String[] {sReqData};
		}
		try {
			for(String p:params){
				if(p.contains("=")) {
					String[] param = p.split("=");
					if(param.length==1){
						jsonUrlParams.put(param[0],"");
					}else{
						String key = param[0];
						String value = param[1];

						jsonUrlParams.put(key, isNumber(value)?Integer.parseInt(value):value);
					}
				}else {
					jsonUrlParams.put("errorParam",p);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonUrlParams;
	}

}
