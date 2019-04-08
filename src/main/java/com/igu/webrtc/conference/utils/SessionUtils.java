package com.igu.webrtc.conference.utils;

import org.springframework.web.socket.WebSocketSession;

public class SessionUtils {

	private static String USERNAME_KEY = "username";

	private static String ROOM_NAME_KEY = "roomName";

	/**
	 * 在会话中，获取用户名
	 * @param userSession
	 * @return
	 */
	public static String getUsernameFromSession(WebSocketSession session) {
		return session.getAttributes().get(USERNAME_KEY).toString();
	}

	public static String getRoomNameFromSession(WebSocketSession session) {
		Object roomName = session.getAttributes().get(ROOM_NAME_KEY);
		if (roomName == null) {
			return null;
		}
		return roomName.toString();
	}

	public static void setUsernameToSession(WebSocketSession session, String username) {
		session.getAttributes().put(USERNAME_KEY, username);
	}

	public static void setRoomNameToSession(WebSocketSession session, String roomName) {
		session.getAttributes().put(ROOM_NAME_KEY, roomName);
	}

	public static void cleanRoomNameInSession(WebSocketSession session) {
		session.getAttributes().remove(ROOM_NAME_KEY);
	}


}
