package com.wvp.hk.module.service.stream;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Arrays;
import java.util.List;

public interface PlayCtrlByJNA extends StdCallLibrary {

	final public int STREAME_REALTIME = 0;
    final public int STREAME_FILE = 1;
    final public int T_UYVY = 1;
    final public int T_YV12 = 3;
    final public int T_RGB32 = 7;

    public boolean PlayM4_GetPort(NativeLongByReference nPort);

    public boolean PlayM4_OpenStream(int nPort, Pointer pFileHeadBuf, int nSize, int nBufPoolSize);

    public boolean PlayM4_InputData(int nPort, Pointer pBuf, int nSize);

    public boolean PlayM4_CloseStream(int nPort);

    public boolean PlayM4_SetStreamOpenMode(int nPort, int nMode);

    public boolean PlayM4_Play(int nPort, Pointer hWnd);

    public boolean PlayM4_PlaySound(int nPort);

    public boolean PlayM4_Stop(int nPort);

    public boolean PlayM4_FreePort(int nPort);

    public boolean PlayM4_SetSecretKey(int nPort, int lKeyType, String pSecretKey, int lKeyLen);

    public boolean PlayM4_GetJPEG(int nPort, byte[] pJpeg, int nBufSize, IntByReference pJpegSize);

    public boolean PlayM4_SetDecCallBack(int nPort, DecCallBack decCBFun);

    public boolean PlayM4_SetDecCallBackMend(int nPort, DecCallBack decCBFun, long nUser);

    public boolean PlayM4_SetDecCallBackExMend(int nPort, DecCallBack decCBFun, Pointer pDest, long nDestSize,
                                               long nUser);

    public int PlayM4_GetLastError(int nPort);

    interface DecCallBack extends StdCallCallback {
        void invoke(int nPort, Pointer pBuf, int nSize, FRAME_INFO pFrameInfo, int nReserved1, int nReserved2);
    }


    public class FRAME_INFO extends Structure
    {
        public int nWidth;
        public int nHeight;
        public int nStamp;
        public int nType;
        public int nFrameRate;
        public int dwFrameNum;
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("nWidth","nHeight","nStamp","nType", "nFrameRate", "dwFrameNum");
		}
    }
}
