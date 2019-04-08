package com.igu.webrtc.conference.service.webrtc.room;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gu
 * @since 4.3.1
 */
public class RoomManager {

	private final Logger log = LoggerFactory.getLogger(RoomManager.class);

	@Autowired
	private KurentoClient kurento;

	//房间集合
	private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<String, Room>();

	/**
	 * Looks for a room in the active room list.
	 *
	 * @param roomName
	 *          the name of the room
	 * @return the room if it was already created, or a new one if it is the first time this room is
	 *         accessed
	 */
	public Room getRoom(String roomName) {
		log.debug("Searching for room {}", roomName);
		if(roomName==null) {
			return null;
		}
		Room room = rooms.get(roomName);

		if (room == null) {
			log.debug("Room {} not existent.", roomName);
			return null;
		}
		log.debug("Room {} found!", roomName);
		return room;
	}

	/**
	 *  进入房间，如果房间不存在，则创建房间
	 *  
	 * @param roomName
	 * @param creator
	 * @return
	 */
	public Room joinRoom(String roomName, String creator) {
		log.debug("Searching for room {}", roomName);
		Room room = rooms.get(roomName);

		if (room == null) {
			log.debug("Room {} not existent. Will create now!", roomName);
			room = new Room(roomName, kurento.createMediaPipeline(), creator);
			rooms.put(roomName, room);
		}
		log.debug("Room {} found!", roomName);
		return room;
	}

	/**
	 * Removes a room from the list of available rooms.
	 *
	 * @param room
	 *          the room to be removed
	 */
	public void removeRoom(Room room) {
		this.rooms.remove(room.getName());
		room.close();
		log.info("Room {} removed and closed", room.getName());
	}

	/**
	 * 获取当前所有房间
	 * @return
	 */
	public ConcurrentMap<String, Room> getRooms() {
		return rooms;
	}


}
