package com.igu.webrtc.conference.service.webrtc.room;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.igu.webrtc.conference.pojo.Response.SxbBaseRsp;
import com.igu.webrtc.conference.utils.SessionUtils;

/**
 * 
 * 房间 
 * 
 * @since 4.3.1
 */
public class Room implements Closeable {
	private final Logger log = LoggerFactory.getLogger(Room.class);

	//视频者集合
	private final ConcurrentMap<String, UserSession> participants = new ConcurrentHashMap<>();
	//观看者集合
	private final ConcurrentMap<Object, WebSocketSession> watchUser = new ConcurrentHashMap<Object, WebSocketSession>();
	private final MediaPipeline pipeline;
	//房间名称
	private final String name;
	//创建房间用户名
	private final String creator;

	public String getName() {
		return name;
	}

	public Room(String roomName, MediaPipeline pipeline, String creator) {
		this.name = roomName;
		this.pipeline = pipeline;
		this.creator = creator;
		log.info("ROOM {} has been created", roomName);
	}

	@PreDestroy
	private void shutdown() {
		this.close();
	}

	public UserSession join(String username, WebSocketSession session) throws IOException {
		log.info("ROOM {}: adding participant {}", username, username);
		final UserSession participant = new UserSession(username, this.name, session, this.pipeline);
		joinRoom(participant);
		participants.put(participant.getName(), participant);
		sendParticipantNames(participant);
		
		JsonObject message=new JsonObject();
		message.addProperty("content", "["+username+"] 新增/更新了视频");
		message.addProperty("isSystem",true);
		sendMessageInRoom(message);
		
		return participant;
	}

	/**
	 * 视频者 离开房间
	 * @param user
	 * @throws IOException
	 */
	public void leave(WebSocketSession session) throws IOException {
		String username=SessionUtils.getUsernameFromSession(session);
		log.debug("PARTICIPANT {}: Leaving room {}", username, this.name);
		
		UserSession user=participants.get(username);
		
		this.removeParticipant(username);
		
		if(user!=null) {
			user.close();
		}
		
		JsonObject message=new JsonObject();
		message.addProperty("content", "["+username+"] 断开了视频连接");
		message.addProperty("isSystem",true);
		sendMessageInRoom(message);
	}

	/**
	 * 视频者 进入房间
	 * 
	 * @param newParticipant
	 * @return
	 * @throws IOException
	 */
	private Collection<String> joinRoom(UserSession newParticipant) throws IOException {
		final JsonObject newParticipantMsg = new JsonObject();
		newParticipantMsg.addProperty("id", "newParticipantArrived");
		newParticipantMsg.addProperty("name", newParticipant.getName());

		final List<String> participantsList = new ArrayList<>(participants.values().size());
		log.debug("ROOM {}: notifying other participants of new participant {}", name, newParticipant.getName());

		for (final UserSession participant : participants.values()) {
				boolean sendResult=participant.sendMessage(newParticipantMsg);
				if(!sendResult) {
					log.debug("ROOM {}: participant {} could not be notified", name, participant.getName());
				}
			 
			participantsList.add(participant.getName());
		}

		return participantsList;
	}

	private void removeParticipant(String name) throws IOException {
		participants.remove(name);

		log.debug("ROOM {}: notifying all users that {} is leaving the room", this.name, name);

		final List<String> unnotifiedParticipants = new ArrayList<>();
		final JsonObject participantLeftJson = new JsonObject();
		participantLeftJson.addProperty("id", "participantLeft");
		participantLeftJson.addProperty("name", name);
		for (final UserSession participant : participants.values()) {
			participant.cancelVideoFrom(name);
			boolean sendResult=participant.sendMessage(participantLeftJson);
			if(!sendResult) {
				unnotifiedParticipants.add(participant.getName());
			}
		}

		if (!unnotifiedParticipants.isEmpty()) {
			log.debug("ROOM {}: The users {} could not be notified that {} left the room", this.name,
					unnotifiedParticipants, name);
		}

	}

	public void sendParticipantNames(UserSession user) throws IOException {

		final JsonArray participantsArray = new JsonArray();
		for (final UserSession participant : this.getParticipants()) {
			if (!participant.equals(user)) {
				final JsonElement participantName = new JsonPrimitive(participant.getName());
				participantsArray.add(participantName);
			}
		}

		final JsonObject existingParticipantsMsg = new JsonObject();
		existingParticipantsMsg.addProperty("id", "existingParticipants");
		existingParticipantsMsg.add("data", participantsArray);
		log.debug("PARTICIPANT {}: sending a list of {} participants", user.getName(), participantsArray.size());
		user.sendMessage(existingParticipantsMsg);
	}

	public Collection<UserSession> getParticipants() {
		return participants.values();
	}

	public Collection<WebSocketSession> getWatchUsers() {
		return watchUser.values();
	}

	public UserSession getParticipant(String name) {
		return participants.get(name);
	}

	public String getCreator() {
		return creator;
	}

	@Override
	public void close() {
		for (final UserSession user : participants.values()) {
			try {
				user.close();
			} catch (IOException e) {
				log.debug("ROOM {}: Could not invoke close on participant {}", this.name, user.getName(), e);
			}
		}

		participants.clear();

		pipeline.release(new Continuation<Void>() {

			@Override
			public void onSuccess(Void result) throws Exception {
				log.trace("ROOM {}: Released Pipeline", Room.this.name);
			}

			@Override
			public void onError(Throwable cause) throws Exception {
				log.warn("PARTICIPANT {}: Could not release Pipeline", Room.this.name);
			}
		});

		log.debug("Room {} closed", this.name);
	}

	


	/**
	 *  房间内发送消息
	 *  
	 * @param params
	 */
	public void sendMessageInRoom(JsonObject params) {


		SxbBaseRsp sxbRsp = new SxbBaseRsp();
		sxbRsp.setErrorCode(0);
		sxbRsp.setData(params);
		sxbRsp.setId("chatRsp");
		String chatMsg = JsonUtils.toJson(sxbRsp);

		Collection<UserSession> videoUser = getParticipants();
		for (UserSession userSession : videoUser) {
			sendMessage(chatMsg, userSession.getSession());
		}

		Collection<WebSocketSession> watchUser = getWatchUsers();
		for (WebSocketSession userSession : watchUser) {
			sendMessage(chatMsg, userSession);
		}

	}
	

	private void sendMessage(String message, WebSocketSession session) {
		if (!session.isOpen()) {
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
	 * 进入观看模式
	 * 
	 * @param session
	 */
	public void watchRoom(WebSocketSession session) {
		SessionUtils.setRoomNameToSession(session, name);
		String username=SessionUtils.getUsernameFromSession(session);
		watchUser.put(username, session);
		
		JsonObject message=new JsonObject();
		message.addProperty("content", "["+username+"] 进入");
		message.addProperty("isSystem",true);
		sendMessageInRoom(message);
	}
	
	/**
	 * 离开观看模式
	 * 
	 * @param session
	 */
	public void leaveWatch(WebSocketSession session) {
		SessionUtils.cleanRoomNameInSession(session);
		String username=SessionUtils.getUsernameFromSession(session);
		watchUser.remove(username);

		JsonObject message = new JsonObject();
		message.addProperty("content", "[" + username + "] 离开");
		message.addProperty("isSystem", true);
		sendMessageInRoom(message);
	}

}
