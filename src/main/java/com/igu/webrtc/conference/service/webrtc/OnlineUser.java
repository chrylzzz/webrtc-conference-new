package com.igu.webrtc.conference.service.webrtc;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

/**
 * 
 *@Description: 在线用户列表  
 *@author lyn
 *@date 2018年12月24日  
 *
 */
public class OnlineUser {

	private final ConcurrentHashMap<String, WebSocketSession> usersByName = new ConcurrentHashMap<>();

	public void register(String name, WebSocketSession session) {
		session.getAttributes().put("name", name);
		usersByName.put(name, session);
	}

	public WebSocketSession getByName(String name) {
		return usersByName.get(name);
	}

	public boolean exists(String name) {
		return usersByName.keySet().contains(name);
	}
	
	public Object getUsers() {
		return usersByName.keySet();
	}

	public void remove(WebSocketSession session) {
		if (session.getAttributes().get("name") == null) {
			return;
		}
		String name = session.getAttributes().get("name").toString();
		if (exists(name)) {
			usersByName.remove(name);
		}

	}

}
