var PREFIX="webrtc_conference"
var SVRDOMAIN = "";

var watchWebRtcPeer;

var ILiveSDK = {
  RoomNumber: null,
  Role: null,
  selSess: null,
  loginInfo: {
    'sdkAppId': null,
    'openid': null,
    'identifier': null,
    'userSig': null,
    'identifierNick': null,
    'headurl': null,
    'token': null
  }
};


var Constants = {
  View: {
    Login: 0,
    RoomList: 1,
    RoomDetail: 2
  }
};

var Detector = {
		  Android: function() {
		    return /Android/i.test(navigator.userAgent);
		  },
		  iOS: function() {
		    return /iPhone|iPad|iPod/i.test(navigator.userAgent);
		  },
		  safari: function() {
		    return navigator.userAgent.toLowerCase().indexOf('safari/') > -1 && navigator.userAgent.toLowerCase().indexOf('chrome/') === -1;
		  }
};


//角色
var Role = {
  Guest: 0, //观众
  LiveMaster: 1, //主播
  LiveGuest: 2 //连麦观众
};


var AppSvr = {
  setErrorHandler: function(cb) {
    // Add a response interceptor
    axios.interceptors.response.use(function(response) {
      // Do something with response data
      // console.debug('axios',response);
      if (cb) {
        cb(response);
      }
      return response;
    }, function(error) {
      // Do something with response error
      return Promise.reject(error);
    });
  },
  
  //注册
  register: function(opts) {
	  
	  var params = new URLSearchParams();
	  params.append('username', opts.username);
	  params.append('pwd',  opts.password);

	    axios.post(SVRDOMAIN + 'api/account/register',params
	    ,{headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
	    )
	      .then(function(response) {
	        if (response.status == 200) {
	           var data=response.data;
	        	if (data && data.errorCode == 0) {
	        		app.$root.$refs.toastr.s("注册成功");
	                app.login();
	        	 }
	        } else {
	        	app.$root.$refs.toastr.e(response.data);
	        }
	      }).
	    catch (function(error) {
	    	app.$root.$refs.toastr.e(error);
	    });
	  
	  
    
  },
  //登录
  login: function(opts) {
	  
	  var params = new URLSearchParams();
	  params.append('username', opts.username);
	  params.append('pwd',  opts.password);

	    axios.post(SVRDOMAIN + 'api/account/login',params
	    ,{headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
	    )
	      .then(function(response) {
	        if (response.status == 200) {
	           var data=response.data;
	        	if (data && data.errorCode == 0) {
	        		ILiveSDK.loginInfo.identifier = app.form.username;
	        		ILiveSDK.loginInfo.token = data.data.token;
	        		ILiveSDK.loginInfo.userSig = data.data.userSig;
	        		store.set(PREFIX + "loginInfo", ILiveSDK.loginInfo);
	        		app.loginInfo = ILiveSDK.loginInfo;
	        		app.$root.$refs.toastr.s("登录成功");
	        		app.renderRoomList();
	        	 }
	        } else {
	        	app.$root.$refs.toastr.e(response.data);
	        }
	      }).
	    catch (function(error) {
	    	app.$root.$refs.toastr.e(error);
	    });
	  
	  
  },

  getUserList: function(opts) {
	  
	  sendMessage({
		  "token": ILiveSDK.loginInfo.token,
	      "roomnum": opts.roomnum,
	      "index": 0,
	      "size": 40,
	      id : 'getRoomUser'
	  });
  },


  getRoomList: function(opts, succ, err) {
	  
	  sendMessage({
		  "type": 1,
	      "token": ILiveSDK.loginInfo.token,
	      "index": 0,
	      "size":30,
	      id : 'getRoomList'
	  });
	  
  }
};


Vue.directive('focus', {
  // When the bound element is inserted into the DOM...
  inserted: function(el) {
    // Focus the element
    el.focus();
  }
});


var renderRoomListItv = null,
  renderUserListItv = null;
var app = new Vue({
  el: '#app-main',
  data: {
    form: {
      msg: null,
      username: store.get(PREFIX + "username") || null,
      password: store.get(PREFIX + "password") || null
    },
    role: BomQuery('role'),
    onMic: 0,
    logined: false,
    view: store.get(PREFIX + "roomview") || 0,
    roomList: [],
    userList: [],
    chatList: [],
//    roomUsers: [],
    roomnum: null,
    loginInfo: null,
    entryType: 'join',
    selToID: null,
    joinRoomModal: false,
    createRoomModal: false,
    modalForm: {
      roomname: null,
      roomnum: null
    },
    applying: false,
    mode: store.get(PREFIX + "mode") || 'fixed',
    video_list: [],
    video_map: {},
    apply_list: [],
    resolution: store.get(PREFIX + "resolution") || "auto",
    frameRate: store.get(PREFIX + "frameRate") || "auto",
    configRole: store.get(PREFIX + "configRole") || "ed640",
//    selectWatch:'',
    open: {
      audio: true,
      video: true
    },
    showUserList: 0,
    showChat: 0,
    isSafari: Detector.safari()
  },
  components: {
    'vue-toastr': window.vueToastr
  },


  filters: {
    getSrcTinyId: function(val) {
      return val.split("-")[0]
    },
    getOpenId: function(val) {
      var srctinyid = val.split("-")[0]
//      return WebRTCAPI.getOpenId(srctinyid);
//      return srctinyid;
    }
  },

  watch: {
    view(val) {
      if (val == 1) {
    	  
    	initWebsocket();
    	  
        clearInterval(renderRoomListItv)
        this.getRoomList();
        renderRoomListItv = setInterval(this.getRoomList, 3000);
        
      } else {
        clearInterval(renderRoomListItv)
      }
      if (val == 2||val == 3) {
//        this.renderUserList();
    	//获取房间用户列表
        renderUserListItv = setInterval(this.renderUserList, 3000);
      } else {
        clearInterval(renderUserListItv);
      }
    },
    resolution(val) {
      store.set(PREFIX + "resolution", val);
    },
    frameRate(val) {
      store.set(PREFIX + "frameRate", val);
    },
//    selectWatch(val) {
//    	alert(val);
//    },
    configRole(val) {
      store.set(PREFIX + "configRole", val);
      if (WebRTCAPI.changeSpearRole) {
        WebRTCAPI.changeSpearRole(val)
        this.$root.$refs.toastr.s("切换角色配置 -> " + val)
      }
    }
  },

  mounted() {
    AppSvr.setErrorHandler(function(response) {
      var data = response.data;
      switch (data.errorCode) {
        case 0:
        case 10003:
          break;
        case 10009:
          self.$root.$refs.toastr.w("登录态失效");
          self.logout();
          break;
        default:
          self.$root.$refs.toastr.w(data.errorInfo);
          break;
      }
    });

    var self = this;
    var loginInfo = store.get(PREFIX + "loginInfo");
    if (loginInfo) {
      ILiveSDK.loginInfo = loginInfo;
      self.loginInfo = loginInfo;
      self.renderRoomList();
      return;
    }

  },
  methods: {
    restoreVideo: function(removeVideoId) {
      var self = this;
      setTimeout(function() {
        Array.prototype.forEach.call(self.video_list, function(item, idx) {
        	
        	console.log('removeVideoId:',removeVideoId);
        	console.log('item.videoId:',item.videoId);
        	
          var video = document.getElementById(item.videoId)
          if (!video) return
          
          if(removeVideoId==item.videoId){
        	  self.video_list.splice(idx,1);//清空数组   
        	  return
          }
          
        })
      }, 100);
    },
    
    chanceMode: function(mode) {
      this.mode = mode;
      store.set(PREFIX + "mode", mode);
    },
    login: function(e) {
      var self = this;
      if (this.form.username && this.form.password) {
        store.set(PREFIX + "username", this.form.username);
        store.set(PREFIX + "password", this.form.password);
      }
      
      AppSvr.login(this.form);
      
    },
    register: function(e) {
      var self = this;

      if( !this.form.username || !this.form.password){
          this.$root.$refs.toastr.w("请输入用户名和密码")
      }
      if (this.form.username && this.form.password) {
        store.set(PREFIX + "username", this.form.username);
        store.set(PREFIX + "password", this.form.password);
      }
      
      
      AppSvr.register(this.form);
      
      
    },

    logout: function() {
      store.remove(PREFIX + "loginInfo");
      if(this.view==2){
    	  app.quitRoom();
      }
      this.view = 0;
    },

    unshiftThis: function(event) {
      var videoId = $(event.currentTarget).data("id");
      var video_list = this.video_list;
      _.each(video_list, function(item) {
        if (item.first) {
          item.first = false;
        }
        if (item.videoId == videoId) {
          item.first = true;
        }
      });
      this.video_list = video_list;
    },

    renderRoomList: function(data) {
      this.view = Constants.View.RoomList;
    },

    getRoomList: function() {
      var self = this;
      AppSvr.getRoomList();
    },

    showCreateRoom: function() {
      this.createRoomModal = true;
    },

    hideCreateRoom: function() {
      this.createRoomModal = false;
    },

    showJoinRoom: function() {
      this.joinRoomModal = true;
    },

    hideJoinRoom: function() {
      this.joinRoomModal = false;
    },

    initWebRTC: function() {
     
      name=this.loginInfo.identifier;
      
      var message = {
    			id : 'joinRoom',
    			name : name,
    			room : this.roomnum
    		}
   
      sendMessage(message);
       
      
      
    },
    handleMsgSend: function() {
      var self = this;
      msgContent = _.trim(this.form.msg);

      if (!msgContent) {
        return;
      }
      this.form.msg = '';
      
      // 通过websocket发布消息
      sendMessage({
    	  id : 'chat',
    	  type : 'groupChat',
    	  roomnum: this.roomnum,
    	  fromUser : this.loginInfo.identifier,
    	  content : msgContent
  		});
      
    },

    renderUserList: function() {
      var self = this;
      AppSvr.getUserList({
        roomnum: this.roomnum
      });
    },
    //初始化会议界面
    renderRoom: function() {
      this.view = 2;
      this.chatList = [];
      this.createRoomModal = false;
      this.joinRoomModal = false;

    },

    joinSpecificRoom: function() {
      if (!this.role) {
        // this.role = 'Guest';
        this.role = 'LiveGuest';
      }
      if (!this.modalForm.roomnum) {
        this.$root.$refs.toastr.e("请输入会议室id");
        return;
      }
      this.entryType = 'join';
      this.roomnum = String(this.modalForm.roomnum);
      this.renderRoom();
      this.initWebRTC();
    },
    //进入观看直播
    watchRoom: function(e) {
    	var roomName=String(e.currentTarget.getAttribute("data-roomnum"));
    	this.view = 3;
    	//参加会议者列表
//    	this.roomUsers=[
//    		{name:13},
//    		{name:12}
//    	]
    	
    	this.roomnum =roomName;
    	
    	this.chatList = [];
        this.createRoomModal = false;
        this.joinRoomModal = false;
    	
       
    	
        sendMessage({
        	id : 'watchRoom',
        	roomName : roomName
        });
    	
    	 
    },
    changeWatchUser: function(username,roomnum) {
    	
    	console.log(username,roomnum);
    	
    	 app.video_list =[
      		{
      			videoId :'watchVideo'
      		   ,username :username
      		} ];
    	 
    	startWatch(username, roomnum);
    	
    	
    	 
    	
    },
    joinRoom: function(e) {
      if (!this.role) {
        // this.role = 'Guest';
        this.role = 'LiveGuest';
      }
      
      this.entryType = 'join';
      this.roomnum = String(e.currentTarget.getAttribute("data-roomnum"));
      
      this.renderRoom();
      this.initWebRTC();
      
    },

    createRoom: function() {
      var self = this;
      this.entryType = 'create';
      this.role = 'LiveMaster';
      
      self.roomnum =app.modalForm.roomname;
      self.renderRoom();
      self.initWebRTC();
      
      
     
    },
    
    //退出观看会议
    quitWatchRoom: function() {
        var self = this;
        
//        sendMessage({
//      	  id : 'leaveRoom'
//        });
  	  	
        for ( var key in participants) {
      	  participants[key].dispose();
        }

        
        this.view = 1;
        this.onMic = 0;
        this.video_list = [];
        this.apply_list = [];
        this.role = BomQuery('role');
        this.applying = 0;
        
        
        sendMessage({
      	  id : 'leaveWatchRoom'
        });
        
        //清除
        if (watchWebRtcPeer) {
    		watchWebRtcPeer.dispose();
    		watchWebRtcPeer = null;
    	}
        
        
     },
    
    quitRoom: function() {
      var self = this;
      
      sendMessage({
    	  id : 'leaveRoom'
      });
	  	
      for ( var key in participants) {
    	  participants[key].dispose();
      }

      
      this.view = 1;
      this.onMic = 0;
      this.video_list = [];
      this.apply_list = [];
      this.role = BomQuery('role');
      this.applying = 0;
      
    },


    onKickout: function() {
      console.log("on kick out!");
      var self = this;
      self.$root.$refs.toastr.e("其他地方登录，被T下线");
      self.quitRoom();
      self.logout();
    },

   

    onRemoteStreamRemove: function(videoId) {
      // _.remove(this.video_list, function(o) {
      //   return o.videoId == videoId
      // })
      videoId=videoId+'-video';
      console.debug('onRemoteStreamRemove', videoId)
      
      //重置视频列表
      var newArr = [];
      var needResetFirst = false;
      _.each(this.video_list, function(o) {
        if (o.videoId != videoId) {
          newArr.push(o);
        } else if (o.first) {
          needResetFirst = true;
        }
      });
      console.debug(needResetFirst);
      if (needResetFirst && newArr[0]) {
        newArr[0].first = true;
      }
      console.debug('newArr', newArr);
      this.video_list = newArr;
//      this.restoreVideo(videoId);
      
      var openid = videoId.split('-')[0];
      
      for (var i = 0; i < this.userList.length; i++) {
		if(this.userList[i].id==openid){
			this.userList.splice(i,1);
			break;
		}
      }
      
      //插入聊天信息
//      this.chatList.push({
//        who: openid,
//        content: openid + "断开了视频连接",
//        isSelfSend: 0,
//        isSystem: 1
//      });
      
      
    },


    onWebSocketClose: function() {
      var self = this;
      self.quitRoom();
    },
    toggleChat: function() {
        this.showChat = !this.showChat
    },
    //关闭或者开启麦克风
    toggleMic: function(videoId) {
    	
    	var video = document.getElementById('local');
    	 
    	 video.srcObject.getAudioTracks().forEach(t => t.enabled = !t.enabled);
     

      this.open.audio = !this.open.audio
    },
    //关闭或者开启视频
    toggleCamera: function(videoId) {
    	
      var video = document.getElementById('local');
    	
      video.srcObject.getVideoTracks().forEach(t => t.enabled = !t.enabled);
      
      this.open.video = !this.open.video
    },
    toggleFullScreen: function(videoId) {
    	
    	var isFullscreen=document.fullScreen||document.mozFullScreen||document.webkitIsFullScreen;
    	if (isFullscreen) {
    		
    		//退出全屏,三目运算符
    		document.exitFullscreen?document.exitFullscreen():
    		document.mozCancelFullScreen?document.mozCancelFullScreen():
    		document.webkitExitFullscreen?document.webkitExitFullscreen():'';
    		 
    	} else {
//    			var el = document.getElementById("local").parentNode;   
    			var el = document.getElementById(videoId);   
    			//进入全屏,多重短路表达式
    			(el.requestFullscreen&&el.requestFullscreen())||
    			(el.mozRequestFullScreen&&el.mozRequestFullScreen())||
    			(el.webkitRequestFullscreen&&el.webkitRequestFullscreen())||(el.msRequestFullscreen&&el.msRequestFullscreen());
    		  
    	}
    }
  }
})
