
package com.igu.webrtc.conference.service.webrtc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.igu.webrtc.conference.pojo.Response.SxbBaseRsp;
import com.igu.webrtc.conference.service.webrtc.room.Room;
import com.igu.webrtc.conference.service.webrtc.room.RoomManager;
import com.igu.webrtc.conference.service.webrtc.room.UserSession;
import com.igu.webrtc.conference.utils.SessionUtils;
import com.igu.webrtc.conference.utils.TokenUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

/**
 * 
 * websocket 处理
 * 
 * @author gu
 * @since 4.3.1
 */
public class CallHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(CallHandler.class);

	private static final Gson gson = new GsonBuilder().create();


	@Autowired
	private RoomManager roomManager;

	/**
	 * 在线用户
	 */
	@Autowired
	private OnlineUser onlineUser;


	/**
	 * websock连接建立
	 * 1.根据token鉴权
	 * 2.判断用户是否重复登录
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		String token = session.getUri().getQuery();

		// 判断是否为调试
		if ("gutest".equals(token)) {
			SessionUtils.setUsernameToSession(session, System.currentTimeMillis()+"");
			super.afterConnectionEstablished(session);
			return;
		}

		
		if (token == null) {
			session.close();
			return;
		}

		Claims claims = null;
		try {
			claims = TokenUtils.parseJWT(token);
		} catch (ExpiredJwtException e) {
			log.error("token失效，请重新登录");
		} catch (SignatureException se) {
			log.error("token令牌错误");
		}

		if (claims == null) {
			SxbBaseRsp sxbRsp = new SxbBaseRsp(401);
			sxbRsp.setId("tipRsp");
			sxbRsp.setErrorInfo("授权失效，请重新登录!");
			sendMessage(JsonUtils.toJson(sxbRsp), session);
			session.close();
			return;
		} else {
			String username = claims.getId();
			if (onlineUser.exists(username)) {
				SxbBaseRsp sxbRsp = new SxbBaseRsp(401);
				sxbRsp.setId("tipRsp");
				sxbRsp.setErrorInfo("已登录，请确认!");
				sendMessage(JsonUtils.toJson(sxbRsp), session);
				session.close();
				return;
			}
			
			//把用户名放到session中，以便后续获取
			SessionUtils.setUsernameToSession(session,username);
			onlineUser.register(username, session);
			super.afterConnectionEstablished(session);
		}


	}


	/**
	 * 上行消息处理
	 * 
	 */
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

		

		switch (jsonMessage.get("id").getAsString()) {
			case "chat":
				chatInRoom(jsonMessage, session);
				break;
			case "getRoomList":
				getRoomList(jsonMessage, session);
				break;
			case "joinRoom":
				joinRoom(jsonMessage, session);
				break;
			case "receiveVideoFrom":
				receiveVideoFrom(jsonMessage, session);
				break;
			case "leaveRoom":
				leaveRoom(session);
				break;
			case "getRoomUser":
				getRoomUser(session);
				break;
			case "onIceCandidateRoom":
				onIceCandidateRoom(jsonMessage, session);
				break;

			// 观看
			case "watchRoom":
				watchRoom(session, jsonMessage);
				break;
			case "viewerRoom":
				viewerRoom(session, jsonMessage);
				break;
			case "leaveWatchRoom":
				leaveWatchRoom(session, jsonMessage);

				break;

			default:
				break;
		}
	}
	
	private synchronized void receiveVideoFrom(JsonObject jsonMessage, final WebSocketSession session) {
		Room room = roomManager.getRoom(SessionUtils.getRoomNameFromSession(session));
		if (room == null) {
			return;
		}

		final String senderName = jsonMessage.get("sender").getAsString();
		final UserSession sender = room.getParticipant(senderName);
		final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

		final UserSession user = room.getParticipant(SessionUtils.getUsernameFromSession(session));
		if(user!=null) {
			user.receiveVideoFrom(sender, sdpOffer);
		}

	}
	
	/**
	 * 进入观看模式
	 * 
	 * @param session
	 * @param jsonMessage
	 */
	private synchronized void watchRoom(final WebSocketSession session, JsonObject jsonMessage) {
	
		// 房间名称
		String roomName = jsonMessage.get("roomName").getAsString();
		final Room room = roomManager.getRoom(roomName);
		if (room != null) {
			room.watchRoom(session);
			//下发用户列表
			getRoomUser(session);
		}
		
		
	}
	
	/**
	 * 离开观看模式
	 * @param session
	 * @param jsonMessage
	 */
	private synchronized void leaveWatchRoom(final WebSocketSession session, JsonObject jsonMessage) {
		// 观看的房间
		final Room room = roomManager.getRoom(SessionUtils.getRoomNameFromSession(session));
		if (room != null) {
			room.leaveWatch(session);
		}
	}

	/**
	 * 开始观看
	 * 
	 * @param session
	 * @param jsonMessage
	 * @throws IOException
	 */
	private synchronized void viewerRoom(final WebSocketSession session, JsonObject jsonMessage) {

		// 房间
		final Room room = roomManager.getRoom(SessionUtils.getRoomNameFromSession(session));
		if (room == null) {
			JsonObject response = new JsonObject();
			response.addProperty("id", "viewerResponse");
			response.addProperty("response", "rejected");
			response.addProperty("message", "No active sender now. Become sender or . Try again later ...");
			sendMessage(response.toString(), session);
		}

		// 主播名称
		String userName = jsonMessage.get("username").getAsString();
		UserSession presenterUserSession = room.getParticipant(userName);
		if (presenterUserSession == null || presenterUserSession.getOutgoingWebRtcPeer() == null) {
			JsonObject response = new JsonObject();
			response.addProperty("id", "viewerResponse");
			response.addProperty("response", "rejected");
			response.addProperty("message", "No active sender now. Become sender or . Try again later ...");
			sendMessage(response.toString(), session);

		} else {

			WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(presenterUserSession.getPipeline()).build();

			nextWebRtc.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

				@Override
				public void onEvent(IceCandidateFoundEvent event) {
					JsonObject response = new JsonObject();
					response.addProperty("id", "iceCandidate");
					response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
					try {
						synchronized (session) {
							session.sendMessage(new TextMessage(response.toString()));
						}
					} catch (IOException e) {
						log.debug(e.getMessage());
					}
				}
			});

			presenterUserSession.getOutgoingWebRtcPeer().connect(nextWebRtc);
			String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
			String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

			JsonObject response = new JsonObject();
			response.addProperty("id", "viewerResponse");
			response.addProperty("response", "accepted");
			response.addProperty("sdpAnswer", sdpAnswer);

			sendMessage(response.toString(), session);
			 
			nextWebRtc.gatherCandidates();
		}

	}

	/**
	 * websocket 关闭了
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		onlineUser.remove(session);
		
		Room room = roomManager.getRoom(SessionUtils.getRoomNameFromSession(session));
		if (room == null) {
			return;
		}
		final UserSession user = room.getParticipant(SessionUtils.getUsernameFromSession(session));
		
		if (user != null) {
			leaveRoom(session);
		} else {
		   room.leaveWatch(session);
		}
	}

	/**
	 * 通过websocket 下发消息
	 * 
	 * @param message
	 * @param session
	 * @throws IOException
	 */
	private void sendMessage(String message, WebSocketSession session){
		if(!session.isOpen()) {
			return;
		}
		
		log.debug("Sending message {}", message);
		try {
			synchronized (session) {
				session.sendMessage(new TextMessage(message));
			}
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
	}

	/**
	 * 房间 网络候选 处理
	 * @param jsonMessage
	 * @param user
	 * @throws IOException
	 */
	private void onIceCandidateRoom(JsonObject jsonMessage, WebSocketSession session) throws IOException {
		Room room = roomManager.getRoom(SessionUtils.getRoomNameFromSession(session));
		if (room == null) {
			return;
		}
		final UserSession user = room.getParticipant(SessionUtils.getUsernameFromSession(session));
		if (user != null) {
			JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
			IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
					candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
			user.addCandidate(cand, jsonMessage.get("name").getAsString());
		}

	}

	/**
	 * 获取房间在线用户
	 * 
	 * @param jsonData
	 * @param session
	 * @throws IOException
	 */
	private void getRoomUser(WebSocketSession session){

		SxbBaseRsp sxbRsp = new SxbBaseRsp(9000);
		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String, Object>> idlist = new ArrayList<Map<String, Object>>();

		final Room room = roomManager.getRoom(SessionUtils.getRoomNameFromSession(session));
		if (room != null) {
			Collection<UserSession> user = room.getParticipants();

			for (UserSession userSession : user) {

				Map<String, Object> oneMember = new HashMap<String, Object>();
				oneMember.put("id", userSession.getName());
				oneMember.put("role", 1);// 1为连麦中
				
				idlist.add(oneMember);
			}
			
			Collection<WebSocketSession>  watchUser = room.getWatchUsers();
			for (WebSocketSession userSession : watchUser) {
				Map<String, Object> oneMember = new HashMap<String, Object>();
				oneMember.put("id", userSession.getAttributes().get("username"));

				oneMember.put("role", 2);// 2为观看者

				idlist.add(oneMember);
				
			}
		}
		
		data.put("idlist", idlist);
		data.put("total", idlist.size());

		sxbRsp.setData(data);
		sxbRsp.setErrorCode(0);
		sxbRsp.setId("getRoomUserRsp");
		sendMessage(JsonUtils.toJson(sxbRsp), session);
	}


	/**
	 * 
	 * 获取会议中房间
	 * 
	 * @param jsonData
	 * @param session
	 * @throws IOException
	 */
	private void getRoomList(JsonObject jsonData, WebSocketSession session) throws IOException {


		SxbBaseRsp sxbRsp = new SxbBaseRsp(9000);


		Map<String, Object> data = new HashMap<String, Object>();
		ConcurrentMap<String, Room> allRoom = roomManager.getRooms();
		List<Map<String, Object>> rooms = new ArrayList<Map<String, Object>>();

		data.put("total", allRoom.size());

		
		Set<String> roomKey = allRoom.keySet();
		for (String roomName : roomKey) {

			Map<String, Object> oneRoom = new HashMap<String, Object>();
			Map<String, Object> oneRoomInfo = new HashMap<String, Object>();


			Room room = allRoom.get(roomName);
			Integer memsize = room.getParticipants().size();

			String roomId = roomName;

			oneRoomInfo.put("cover", "");
			oneRoomInfo.put("groupid", roomId);
			oneRoomInfo.put("memsize", memsize);
			oneRoomInfo.put("roomnum", roomId);
			// 名称
			oneRoomInfo.put("title", roomName);
			oneRoomInfo.put("type", "live");

			oneRoom.put("info", oneRoomInfo);
			oneRoom.put("uid", room.getCreator());

			rooms.add(oneRoom);


		}


		data.put("rooms", rooms);
		data.put("onlineUser", onlineUser.getUsers());

		sxbRsp.setData(data);
		sxbRsp.setErrorCode(0);
		sxbRsp.setId("getRoomListRsp");

		sendMessage(JsonUtils.toJson(sxbRsp), session);


	}

	/**
	 * 房间内文字对话
	 * 
	 * @param params
	 * @param session
	 * @param user
	 * @throws IOException
	 */
	private void chatInRoom(JsonObject params, WebSocketSession session) throws IOException {

		String roomName = params.get("roomnum").getAsString();
		final Room room = roomManager.getRoom(roomName);
		if (room != null) {
			room.sendMessageInRoom(params);		 
		}

	}

	/**
	 * 
	 * 加入房间
	 * 
	 * 
	 * @param params
	 * @param session
	 * @throws IOException
	 */
	private void joinRoom(JsonObject params, WebSocketSession session) throws IOException {
		final String roomName = params.get("room").getAsString();
		
		String username = SessionUtils.getUsernameFromSession(session);
		SessionUtils.setRoomNameToSession(session, roomName);
		
		log.info("PARTICIPANT {}: trying to join room {}", username, roomName);
		
		Room room = roomManager.joinRoom(roomName, username);
		room.join(username, session);
		
		
	}

	/**
	 * 离开房间
	 * @param user
	 * @throws IOException
	 */
	private void leaveRoom(WebSocketSession session) throws IOException {
		String roomName = SessionUtils.getRoomNameFromSession(session);
		final Room room = roomManager.getRoom(roomName);
		if (room != null) {
			room.leave(session);
			if (room.getParticipants().isEmpty()) {
				roomManager.removeRoom(room);
			}
			
		}
	}
}
