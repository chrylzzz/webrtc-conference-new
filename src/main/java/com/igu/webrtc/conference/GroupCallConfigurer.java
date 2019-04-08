package com.igu.webrtc.conference;

import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.igu.webrtc.conference.service.webrtc.CallHandler;
import com.igu.webrtc.conference.service.webrtc.OnlineUser;
import com.igu.webrtc.conference.service.webrtc.room.RoomManager;

/**
 *
 * @since 4.3.1
 */
@SpringBootApplication
@EnableWebSocket
public class GroupCallConfigurer implements WebSocketConfigurer {

	@Value("${kms.url}")
	private String kmsUrl;
	
	@Value("${open.recorder}")
	private String openRecorder;
	
	@Value("${recorder.file.path}")
	private String recorderFilePath;


	@Bean
	public RoomManager roomManager() {
		return new RoomManager();
	}

	@Bean
	public OnlineUser onlineUser() {
		return new OnlineUser();
	}

	@Bean
	public CallHandler groupCallHandler() {
		return new CallHandler();
	}

	@Bean
	public KurentoClient kurentoClient() {
		return KurentoClient.create();
	}


	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		System.setProperty("kms.url", kmsUrl);
		System.setProperty("open.recorder", openRecorder);
		System.setProperty("recorder.file.path", recorderFilePath);
		registry.addHandler(groupCallHandler(), "/groupcall").setAllowedOrigins("*");
	}
}
