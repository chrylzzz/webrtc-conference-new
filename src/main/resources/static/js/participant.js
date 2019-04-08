
/**
 * Creates a video element for a new participant
 *
 * @param {String} name - the name of the new participant, to be used as tag
 *                        name of the video element.
 *                        The tag of the new element will be 'video<name>'
 * @return
 */
function Participant(name) {
	this.name = name;
	 
	var rtcPeer;

	
	var videoId=name+'-video';
	var first=false;
	
	if(name==ILiveSDK.loginInfo.identifier){
		videoId='local';
	}
	
	 if (app.video_list.length == 0) {
		 first=true;
	 }
	
	app.video_list.push({
        videoId:videoId ,
        openId: videoId,
        first: first
      });
	


	this.getElement = function() {
		//视频ID
		return container;
	}

	this.getVideoElement = function() {
		var videoObj=document.getElementById(videoId)
		if(videoId=='local'){
			videoObj.muted = true
		}
		
		return videoObj;
	}



	this.offerToReceiveVideo = function(error, offerSdp, wp){
		if (error) return console.error ("sdp offer error")
		console.log('Invoking SDP offer callback function');
		var msg =  { id : "receiveVideoFrom",
				sender : name,
				sdpOffer : offerSdp
			};
		sendMessage(msg);
	}


	this.onIceCandidate = function (candidate, wp) {
		  console.log("Local candidate" + JSON.stringify(candidate));

		  var message = {
		    id: 'onIceCandidateRoom',
		    candidate: candidate,
		    name: name
		  };
		  sendMessage(message);
	}

	Object.defineProperty(this, 'rtcPeer', { writable: true});

	this.dispose = function() {
		console.log('Disposing participant ' + this.name);
		this.rtcPeer.dispose();
//		container.parentNode.removeChild(container);
		app.onRemoteStreamRemove(this.name);
	};
}
