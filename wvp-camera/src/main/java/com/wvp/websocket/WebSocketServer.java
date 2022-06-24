package com.wvp.websocket;

import com.alibaba.fastjson.JSONObject;
import com.wvp.common.config.AppclicationContextConfig;
import com.wvp.common.core.redis.RedisCache;
import com.wvp.domain.WvpDevice;
import com.wvp.hk.module.service.stream.EStream;
import com.wvp.hk.module.service.stream.VoiceTalkWithServer;
import com.wvp.service.IWvpDeviceService;
import com.wvp.utils.G711Code;
import com.wvp.utils.G711Converter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.NativeWebSocketSession;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * websocket 消息处理
 *
 * @author ruoyi
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/message")
public class WebSocketServer
{

    private IWvpDeviceService wvpDeviceService;


    /**
     * 默认最多允许同时在线人数100
     */
    public static int socketMaxOnlineCount = 100;

    private static Semaphore socketSemaphore = new Semaphore(socketMaxOnlineCount);

    @Autowired
    EStream eStream=new EStream();

    // Session->voiceHandle
    public static HashMap<String,String> voiceMap=new HashMap<String, String>();

    //luserId->Session
    public static HashMap<Integer,Session> voiceLuserMap=new HashMap<>();


    /**
     *     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) throws IOException {
        this.afterConnectionEstablished(session);
        log.info("socket连接成功："+session.getId());
        wvpDeviceService = (IWvpDeviceService) AppclicationContextConfig.getBean(IWvpDeviceService.class);
        boolean semaphoreFlag = false;
        // 尝试获取信号量
        semaphoreFlag = SemaphoreUtils.tryAcquire(socketSemaphore);

        if (!semaphoreFlag)
        {
            // 未获取到信号量
            log.error("\n 当前在线人数超过限制数- {}", socketMaxOnlineCount);
            WebSocketUsers.sendMessageToUserByText(session, "当前在线人数超过限制数：" + socketMaxOnlineCount);
            session.close();
        }
        else
        {
            // 添加用户
            WebSocketUsers.put(session.getId(), session);
            log.info("\n 建立连接 - {}", session);
            log.info("\n 当前人数 - {}", WebSocketUsers.getUsers().size());
        }
    }


    public void afterConnectionEstablished(Session session) {
        if (session instanceof NativeWebSocketSession) {
            final Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
            if (nativeSession != null) {
                nativeSession.getUserProperties()
                        .put("org.apache.tomcat.websocket.BLOCKING_SEND_TIMEOUT", 1000L);
            }
        }
    }

    /**
     *     * 连接关闭调用的方法
     *
     */
    @OnClose
    public void onClose(Session session) {
        for(Integer key:voiceLuserMap.keySet()){
            if(voiceLuserMap.get(key).equals(session)){
                VoiceTalkWithServer.StopVoiceTalk(key,Integer.valueOf(voiceMap.get(session.getId())));
                voiceLuserMap.remove(key);
            }
        }

        // 移除用户
        WebSocketUsers.remove(session.getId());
        voiceMap.remove(session.getId());
        log.info("onClose执行，session:"+session);
        // 获取到信号量则需释放
        SemaphoreUtils.release(socketSemaphore);
        System.out.println("有一连接关闭！当前在线人数为" + WebSocketUsers.getUsers().size());
    }

    /**
     *     *
     *     * @param session
     *     * @param error
     *
     */
    @OnError
    public void onError(Session session, Throwable exception) throws IOException {

        for(Integer key:voiceLuserMap.keySet()){
            if(voiceLuserMap.get(key).equals(session)){
                VoiceTalkWithServer.StopVoiceTalk(key,Integer.valueOf(voiceMap.get(session.getId())));
                voiceLuserMap.remove(key);
            }
        }
        voiceMap.remove(session.getId());
        log.info("onError执行，session:"+session);
        if (session.isOpen())
        {
            // 关闭连接
            session.close();
        }
        String sessionId = session.getId();
        // 移出用户
        WebSocketUsers.remove(sessionId);

        log.info("\n 连接异常 - {}", sessionId);
        log.info("\n 异常信息 - {}", exception);
        // 移出用户
        WebSocketUsers.remove(sessionId);
        // 获取到信号量则需释放
        SemaphoreUtils.release(socketSemaphore);
    }

    /**
     *     * 收到客户端消息后调用的方法
     *     *
     *     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws InterruptedException {
        if(message.indexOf("=")!=-1){
            String[] mes=message.split("=");

            if("startIntercom".equals(mes[0])){
                if(!"-1".equals(mes[1])){
                    WvpDevice wvpDevice=wvpDeviceService.selectWvpDeviceByLuserId(Long.valueOf(mes[1]));
                    if(wvpDevice.getVoiceHandle()>=0){
                        WebSocketUsers.sendMessageToUserByText(session,"101503");
                        log.info("开启对讲失败，当前设备已被其他用户开启对讲");
                    }else{
                        VoiceTalkWithServer.StartVoiceTalk(Integer.valueOf(mes[1]));
                        Thread.sleep(1000);
                        voiceMap.put(session.getId(),EStream.voiceMap.get(String.valueOf(mes[1])));
                        voiceLuserMap.put(Integer.valueOf(mes[1]),session);
                        WebSocketUsers.sendMessageToUserByText(session,"101501");
                        log.info("开启对讲成功");
                    }
                }else{
                    WebSocketUsers.sendMessageToUserByText(session,"101502");
                    log.info("开启对讲失败，参数无效");
                }

            }
            if("stopIntercom".equals(mes[0])){
                if(voiceMap.get(session.getId())!=null){
                    VoiceTalkWithServer.StopVoiceTalk(Integer.valueOf(mes[1]),Integer.valueOf(voiceMap.get(session.getId())));
                    voiceMap.remove(session.getId());
                    for(Integer key:voiceLuserMap.keySet()){
                        if(voiceLuserMap.get(key).equals(session)){
                            voiceLuserMap.remove(key);
                            log.info("关闭对讲成功");
                            this.onClose(session);
                            WebSocketUsers.sendMessageToUserByText(session,"101504");
                        }
                    }
                }
            }
        }
    }

    @OnMessage
    public void onMessage(byte[] message, Session session) throws IOException {


        if(message.length>0){
            short[] shorts=bytesToShort(message);
            byte[] converterData=new byte[message.length/2];
            G711Code.G711aEncoder(shorts,converterData,shorts.length);
            if(voiceMap.get(session.getId())!=null) {
                eStream.sendVoiceData(converterData, Long.valueOf(voiceMap.get(session.getId())));
            }

        }


    }


    public static short[] bytesToShort(byte[] bytes) {
        if(bytes==null){
            return null;
        }
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
}
