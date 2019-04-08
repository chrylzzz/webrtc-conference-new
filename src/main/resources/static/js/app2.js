/**
 * 提示信息
 * 
 * @param data
 * @returns
 */
function tipRsp(data) {
	if (data && data.errorCode === 0) {
		app.$root.$refs.toastr.s(data.errorInfo);
	} else {
		app.$root.$refs.toastr.e(data.errorInfo);
		console.error(data)
		if(data.errorCode === 401){
			app.logout();
		}
		 
	}
}

/**
 * 房间列表信息
 * 
 * @param data
 * @returns
 */
function getRoomListRsp(data) {

    app.roomList = _.filter(data.data.rooms || [], function(item) {
      // return item.info && item.info.title.indexOf("极速模式") != -1;
      return item.info;
    }) || [];
  
}


function getRoomUserRsp(data) {
	app.userList = data.data.idlist;
}
 

/**
 * 文字消息
 * 
 * @param data
 * @returns
 */
function chatRsp(data) {
	var msgData=data.data;
	console.log(msgData);
	
	
	app.chatList.push({
        who: msgData.fromUser==app.loginInfo.identifier?'我':msgData.fromUser,
        content: msgData.content,
        isSelfSend: msgData.fromUser==app.loginInfo.identifier?1:0,
        isSystem: msgData.isSystem!=null
      });
	
    
    
	$(".chatting-area").scrollTop(100000);
    
	
}




/**
 * 观看
 * 
 * @param error
 * @param offerSdp
 * @returns
 */
function startWatch(username, roomnum) {
	
	 //清除
    if (watchWebRtcPeer) {
		watchWebRtcPeer.dispose();
		watchWebRtcPeer = null;
	}
    
	
	setTimeout(function() {
	    
	 	
	 	var options = {
	 			remoteVideo : document.getElementById('watchVideo')
	 		}
	 		
			watchWebRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
					function(error) {
						if (error) {
							return console.error(error);
						}
						//回调
						this.generateOffer(function(error, offerSdp){
							
							if (error){
								return console.error('Error generating the offer');
							}
							
							console.info('Invoking SDP offer callback function ' + location.host);
							var message = {
								id : 'viewerRoom',
								roomName : roomnum,
								username : username,
								sdpOffer : offerSdp
							}
							
							sendMessage(message);
							
							
						});
					});
	 	
	 	
	 },10);
	
}


function viewerResponse(message) {
	if (message.response != 'accepted') {
		var errorMsg = message.message ? message.message : 'Unknow error';
		console.info('Call not accepted for the following reason: ' + errorMsg);
	} else {
		watchWebRtcPeer.processAnswer(message.sdpAnswer, function(error) {
			if (error){
				return console.error(error);
			}
		});
	}
}
