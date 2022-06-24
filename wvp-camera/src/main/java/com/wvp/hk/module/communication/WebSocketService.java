package com.wvp.hk.module.communication;

import com.wvp.hk.module.common.CommonMethod;


import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
public class WebSocketService {
	private Session m_session;
	private static Map<String, WebSocketService> m_clients = new ConcurrentHashMap<String, WebSocketService>();
	private String m_userID;

	public static enum MessageType{
		DevAdd(0),
		DevAdd5(1),
		DevLogout(2),
		LogFileRefresh(3),
		AlarmInfo(4),
		EhomeKeyError(5),
		StreamData(6);

		private int value = 0;

		private MessageType(int value){
			this.value = value;
		}

		public int value() {
	        return this.value;
	    }
	}

	@OnOpen
    public void onOpen(Session session) throws IOException {
	    String sRandomStr = CommonMethod.generateString(15);
        this.m_userID = sRandomStr;
        this.m_session = session;

        m_clients.put(sRandomStr, this);
	}

	@OnClose
    public void onClose(Session session) {
	    m_clients.remove(m_userID);
    }

	@OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

	@OnMessage
    public static synchronized void onMessage(String message) {
        // Determine whether the client is online, and send a message if it is online (like "broadcast")
        for (WebSocketService item : m_clients.values())
        {
            //Currently, userID judgment is not performed temporarily. If necessary,
			// userID can be added to the sent message and then judged here
            /* if (item.m_userID.equals(user)) {}  */
            if(item.m_session.isOpen())
            {
                item.m_session.getAsyncRemote().sendText(message);
            }
            else
            {
                m_clients.remove(item.m_userID);
            }
        }
    }
}
