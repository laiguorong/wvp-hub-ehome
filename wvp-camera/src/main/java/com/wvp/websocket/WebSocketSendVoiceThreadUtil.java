package com.wvp.websocket;


import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @ClassName WebSocketSendVoiceThreadUtil
 * @Description TODO
 * @Author fs
 * @Date 2022/4/8 9:25
 */
@Slf4j
public class WebSocketSendVoiceThreadUtil{

    private Long luserId;

    sendVoiceThread t=null;

    public WebSocketSendVoiceThreadUtil(Long luserId) {
        this.luserId = luserId;
    }

    public void onStart(ByteBuffer message) throws IOException {
        if(t==null) {
            t = new sendVoiceThread(luserId);
            t.start();
        }else{
            t.push(message);
        }
    }


}


class sendVoiceThread extends Thread{

    ByteBuffer message;
    Long luserId;

    public void push(ByteBuffer message) {
        this.message=message;
    }

    /**
     * 创建用于把字节数组转换为inputstream流的管道流
     * @throws IOException
     */
    public sendVoiceThread(Long luserId) throws IOException {
        this.luserId = luserId;
    }

    @Override
    public void run() {
        try{
            Session session=WebSocketServer.voiceLuserMap.get(luserId.intValue());
            while (session!=null){
                System.out.println("while循环执行"+message);
                session.getBasicRemote().sendBinary(message);
            }
            System.out.println("线程执行结束");
        }catch (Exception e){
            System.out.println("socket连接异常发送语音失败");
        }
    }
}
