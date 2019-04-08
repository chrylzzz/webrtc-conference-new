// 全局定义场所类型变量
var place = '';
var url = baseUrl+'/api/record/list';

// 初始化表格
$(function() {
    initTable(url);

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
                	{ field: 'fileName', unresize: true, align: 'center', minWidth: 150, title: '文件名' },
                    { field: 'fileSize', unresize: true, align: 'center', minWidth: 150, title: '大小' },
                    { field: 'lastModified', unresize: true, align: 'center', minWidth: 115, title: '最后修改时间' },
                    { fixed: '', width: 120, align: 'center', title: '操作', toolbar: '#barDemo' }
                ]
            ],
            done: function(res, curr, count){
                layer.closeAll();
            }
        });
        //监听工具条
        table.on('tool(equipmentTable)', function(obj) {
        	var layEvent = obj.event;
            var data = obj.data;
            var fileName = data.fileName;
            console.log(obj);
            
            
            layer.open({
                title:'播放',
                type: 1,
                area: ['380px', '380px'],
                fixed: false, //不固定
                maxmin: true,
                shadeClose : true,
                content: '<video src="/record/'+fileName+'" controls="controls" autoplay="autoplay" width="100%" height="100%"></video>' 
            });
            
         
        });
    });
}
 