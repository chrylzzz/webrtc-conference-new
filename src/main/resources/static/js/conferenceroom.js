var participants = {};
var name;

//websocket
var ws =null;

function initWebsocket(){
	
	if(ws!=null&&ws.readyState==1){
		return;
	}
	
	//当前域名或ip（包括ip和端口）
	var url=window.location.host;
	//console.log(url);
	
	//ws还是wss
	var wsProtocol = 'https:' == document.location.protocol ? 'wss' : 'ws';
	
	var pathName = window.document.location.pathname;
	//获取带"/"的项目名，如：/webrtc
	var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
	
	var webSocketUrl=wsProtocol+"://"+url+projectName+"/groupcall?"+ILiveSDK.loginInfo.token;
	
	ws = new WebSocket(webSocketUrl);
	
	
	window.onbeforeunload = function() {
		if(ws.readyState==1){
			ws.close();
		}
	};
	
	ws.onclose = function(){
		app.$root.$refs.toastr.e("连接已中断，请刷新页面！");
		
		app.chatList.push({
	        who: '系统',
	        content: "连接已中断，请刷新页面",
	        isSelfSend: 0,
	        isSystem: 1
	      });
	};
	
	ws.onmessage = function(message) {
		var parsedMessage = JSON.parse(message.data);
		console.info('Received message: ' + message.data);
	
		switch (parsedMessage.id) {
		case 'existingParticipants':
			onExistingParticipants(parsedMessage);
			break;
		case 'newParticipantArrived':
			onNewParticipant(parsedMessage);
			break;
		case 'participantLeft':
			onParticipantLeft(parsedMessage);
			break;
		case 'receiveVideoAnswer':
			receiveVideoResponse(parsedMessage);
			break;
		case 'iceCandidateRoom':
			participants[parsedMessage.name].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
		        if (error) {
			      console.error("Error adding candidate: " + error);
			      return;
		        }
		    });
		    break;
		case 'tipRsp':
			tipRsp(parsedMessage);
			break;
		case 'getRoomListRsp':
			getRoomListRsp(parsedMessage);
			break;
		case 'getRoomUserRsp':
			getRoomUserRsp(parsedMessage);
			break;
		case 'chatRsp':
			chatRsp(parsedMessage);
			break;

		//观看模式
		case 'viewerResponse':
			viewerResponse(parsedMessage);
			break;
		case 'iceCandidate':
			watchWebRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
				if (error)
					return console.error('Error adding candidate: ' + error);
			});
			break;
		case 'stopCommunication':
			dispose();
			break;	
			
		default:
			console.error('Unrecognized message', parsedMessage);
		}
	}

}

function onNewParticipant(request) {
	receiveVideo(request.name);
}

function receiveVideoResponse(result) {
	participants[result.name].rtcPeer.processAnswer (result.sdpAnswer, function (error) {
		if (error) return console.error (error);
	});
}

function callResponse(message) {
	if (message.response != 'accepted') {
		console.info('Call not accepted by peer. Closing call');
		stop();
	} else {
		webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
			if (error) return console.error (error);
		});
	}
}
/**
 * 
 * 开始加载，我进入会议室
 * 
 * @param msg
 * @returns
 */
function onExistingParticipants(msg) {
	
	//获取分辨率设置
	var defaultSetting = {
			width: 320,
			height: 240,
			framerate : 15
	      };
	      
    var resolution = app.resolution;
    if(resolution != "auto"){
        var opts = resolution.split("x");
        defaultSetting.width= parseInt(opts[0]);
        defaultSetting.height= parseInt(opts[1]);
    }

    var frameRate = app.frameRate;
    if(frameRate != "auto"){
        defaultSetting.framerate=  parseInt(frameRate);
    }
	
	//定义视频和音频的质量
	var constraints = {
		audio : true,
		video : defaultSetting
	};
	
	
	var participant = new Participant(name);
	
	participants[name] = participant;
	
	setTimeout(function(){
		
		
		var video = participant.getVideoElement();
		 
	
		
		app.userList.push({
	        id: name,
	        role: 1
	      });
		
		
	var options = {
	      localVideo: video, //本地流应用程序中的视频标记
	      mediaConstraints: constraints,
	      onicecandidate: participant.onIceCandidate.bind(participant)
	    }
	
	participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
		function (error) {
		  if(error) {
			  return console.error(error);
		  }
		  this.generateOffer (participant.offerToReceiveVideo.bind(participant));
	});

	msg.data.forEach(receiveVideo);
	
	
	},5);
}

/**
 * 其它人加入
 * 
 * @param sender
 * @returns
 */
function receiveVideo(sender) {
	var participant = new Participant(sender);
	participants[sender] = participant;
	
	setTimeout(function(){
		
		var video = participant.getVideoElement();
		console.log(video);
	
		var options = {
	      remoteVideo: video,
	      onicecandidate: participant.onIceCandidate.bind(participant)
	    }
	
		participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
				function (error) {
				  if(error) {
					  return console.error(error);
				  }
				  this.generateOffer (participant.offerToReceiveVideo.bind(participant));
		});
		
		app.userList.push({
	        id: sender,
	        role: 1
	      });
		
	
	},5);
	
}

/**
 * 我 离开会议室
 * 
 * @returns
 */
function leaveRoom() {
	sendMessage({
		id : 'leaveRoom'
	});

	for ( var key in participants) {
		participants[key].dispose();
	}

	
	//关闭websocket
	ws.close();
}

/**
 * 其它人 离开会议室
 * 
 * @returns
 */
function onParticipantLeft(request) {
	console.log('Participant ' + request.name + ' left');
	var participant = participants[request.name];
	participant.dispose();
	delete participants[request.name];
}
/**
 * 查询参数
 * @param n
 * @returns
 */
function BomQuery(n){
    var m = window.location.search.match(new RegExp( "(\\?|&)"+n+"=([^&]*)(&|$)"));
    return !m ? "":decodeURIComponent(m[2]);
}

/**
 * 发送消息
 * @param message
 * @returns
 */
function sendMessage(message) {
	var jsonMessage = JSON.stringify(message);
	if(ws==null||ws.readyState!=1){
		return;
	}
	console.log('Senging message: ' + jsonMessage);
	ws.send(jsonMessage);
}
