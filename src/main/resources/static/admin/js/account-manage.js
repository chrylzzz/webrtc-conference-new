// 全局定义场所类型变量
var place = '';
var url = baseUrl+'/api/user/account/get';

// 初始化表格
$(function() {
    initTable(url);
    // 点击查询事件
    $('#checkbtn').click(function() {
        // 获取名称
        var username = $('#username').val();
        var url = baseUrl+'/api/user/account/get?username=' + username;
        // 获取信息
        initTable(url);
    })

})

 
// 获取信息
function initTable(url) {
    layui.use('table', function() {
        // 加载动画
        layer.msg('加载中', {
            icon: 16,
            shade: 0.3
        });

        var table = layui.table;
        table.render({
            elem: '#equipmentTable',
            url: url,
            page: {
                layout: ['count', 'prev', 'page', 'next', 'skip'],
                groups: 2
            },
            cellMinWidth: 115,
            cols: [
                [
                	{ field: 'id', unresize: true, align: 'center', minWidth: 150, title: 'id' },
                    { field: 'username', unresize: true, align: 'center', minWidth: 150, title: '用户名' },
                    { field: 'pwd', unresize: true, align: 'center', minWidth: 115, title: '密码' },
                    { field: 'registerTime', unresize: true, align: 'center', minWidth: 115, title: '注册时间',templet: function(d){
                        return getFormatDate(d.registerTime)
                    } },
                    { field: 'deleted', unresize: true, align: 'center', minWidth: 115, title: '是否删除',templet: function(d){
                    	if(d.deleted==1){
                    		return '已删除'
                    	}
                        return '否'
                    } },
                    { fixed: '', width: 120, align: 'center', title: '操作', toolbar: '#barDemo' }
                ]
            ],
            done: function(res, curr, count){
                layer.closeAll();
            }
        });
        //监听工具条
        table.on('tool(equipmentTable)', function(obj) {
            var data = obj.data;
            var id = data.id;
            var pwd = data.pwd;
            var username = data.username;
            var deleted = data.deleted;
            var layEvent = obj.event;
            
            console.log(obj);
            
            if(layEvent=='delete'){
            	
            	if(deleted==1){
            		layer.msg('已删除');
            		return;
            	}
            	
            	 layer.confirm('确认要删除吗？',function(index){
                     
                     $.ajax({
                         url: baseUrl+'/api/user/account/delete',
                         type: 'post',
                         dataType: 'json',
                         data: {
                             id: id
                         },
                         success: function(res) {
                             if (res.code == "0") {
                                 layer.alert("删除成功", function() {
                                     layer.closeAll();
                                     var name = $('#name').val();
                                     var url = baseUrl+'/api/user/account/get?name=' + name ;
                                     initTable(url);
                                 });
                             } else if (!isNullOrEmpty(res.msg)) {
                                 layer.alert(res.msg);
                             }
                         }
                     })
                     
                     
                     
                 });
            	
            	
            	
            }else{
            
            layer.open({
                title:'修改',
                type: 1,
                area: ['380px', '280px'],
                fixed: false, //不固定
                maxmin: false,
                content: `<div class="x-body">
                      <div class="layui-form-item">
                          <label for="username" class="layui-form-label">
                              <span class="x-red">*</span>用户名
                          </label>
                          <div class="layui-input-inline">
                           <input type="hidden" id="id"  value=${id}>
                              <input type="text"    required="" lay-verify="required" disabled="disabled"
                               class="layui-input" value=${username}>
                          </div>
                      </div>
                      <div class="layui-form-item">
                          <label for="phone" class="layui-form-label">
                              <span class="x-red">*</span>密码
                          </label>
                          <div class="layui-input-inline">
                              <input type="text" id="pwd"   required=""  
                               class="layui-input dutyPersonPhone"  value=${pwd}>
                          </div>
                      </div>
                	 
                      <div class="layui-form-item">
                          <label class="layui-form-label"></label>
                          <button class="layui-btn" onclick="compileData(this)" lg='${data.id}'>
                			修改
                          </button>
                      </div>
                </div>`
            });
            
        }
        });
    });
}
// 修改信息
function compileData(thisObj) {
	var id = $('#id').val();
    var pwd = $('#pwd').val();

    if(isNullOrEmpty(id)){
        layer.msg('用户名不能为空');
        return;
    }
    if(isNullOrEmpty(pwd)){
        layer.msg('密码不能为空');
        return;
    }
    $.ajax({
        url: baseUrl+'/api/user/account/update',
        type: 'post',
        dataType: 'json',
        data: {
            id: id,
            pwd: pwd
        },
        success: function(res) {
            if (res.code == "0") {
                layer.alert("修改成功", function() {
                    layer.closeAll();
                    var name = $('#name').val();
                    var url = baseUrl+'/api/user/account/get?name=' + name ;
                    initTable(url);
                });
            } else if (!isNullOrEmpty(res.msg)) {
                layer.alert(res.msg);
            }
        }
    })

}