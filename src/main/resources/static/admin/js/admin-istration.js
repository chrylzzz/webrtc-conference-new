$(function() {
    // 获取账户信息
    $.ajax({
        url: 'api/user/get',
        dataType: 'json',
        type: 'post',
        success: function(res) {
            if (res.code == 0) {
                var id = res.data.id;
                var time = res.data.time;
                var username = res.data.username;
                var password = res.data.password;
                var phone = res.data.phone;
                var email = res.data.email;
                var card_state = res.data.card_state;
                var card_name = res.data.card_name;
                var card_number = res.data.card_number;
                var wechat_openid = res.data.wechat_openid;
                var type = res.data.type;
                var state = res.data.state;

                $('#username').html(username);
                $('#password').html(password);
                $('#phone').html(phone);
                $('#email').html(email);
                $('#card_name').html(card_name);
                $('#card_number').html(card_number);
                if(wechat_openid==null||wechat_openid=='-1'){
                	$('#wechat').html('未绑定微信');
                	
                	//获取二维码信息
                	 $.ajax({
                	        url: 'api/user/getqrcode',
                	        dataType: 'json',
                	        type: 'post',
                	        success: function(res) {
                	            if (res.code == 0) {
                	            	var qrCodeHtml='2.请用微信扫描以下二维码进行绑定操作。绑定微信后，每天可接收数据日报。 <br>'+
                	            	'<img src="data:image/jpeg;base64,'+res.data+'" alt="二维码图片" width="30%"/>';
                	            	
                	            	$('#qrCodeId').html(qrCodeHtml);
                	            }
                	        }
                	 });        
                	
                	
                }else{
                	$('#wechat').html('已绑定微信');
                }

                // 判断结算账户状态
                if (card_state = 1) {
                    $('#card_state').html('审核中');
                } else if (card_state = 2) {
                    $('#card_state').html('审核通过');
                } else {
                    $('#card_state').html('审核不通过');
                }
            } else {
               layer.alert(res.msg);
            }
        }
    })
})