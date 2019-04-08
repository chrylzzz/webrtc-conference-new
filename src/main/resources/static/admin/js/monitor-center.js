// 初始化数据
var beginTime = ''; // 开始日期
var endTime = ''; // 结束日期
var placeType = ''; // 使用场所
var quota = 1; // 指标

$(function() {

    // 获取使用概况数据
    $.ajax({
        url: "api/monitor/getsum",
        dataType: "json",
        type: "post",
        success: function(res) {
            if (res.code == 0) { //成功

                var steelyard_num = res.data.steelyard_num;
                var normal_steelyard_num = res.data.normal_steelyard_num;
                var yusterday_use_num = res.data.yusterday_use_num;
                var yusterday_new_follow_num = res.data.yusterday_new_follow_num;
                var yusterday_un_follow_num = res.data.yusterday_un_follow_num;
                var yusterday_un_follow_rate = res.data.yusterday_un_follow_rate;
                var yesterday_profit = res.data.yesterday_profit;
                var this_month_follow_num = res.data.this_month_follow_num;
                var this_month_profit = res.data.this_month_profit;
                var all_profit = res.data.all_profit;

                // 添加数据
                $("#steelyard_num").html(steelyard_num);
                $("#normal_steelyard_num").html(normal_steelyard_num);
                $("#yusterday_use_num").html(yusterday_use_num);
                $("#yusterday_new_follow_num").html(yusterday_new_follow_num);
                $("#yusterday_un_follow_num").html(yusterday_un_follow_num);
                $("#yusterday_un_follow_rate").html(yusterday_un_follow_rate);
                $("#yesterday_profit").html(yesterday_profit);
                $("#this_month_follow_num").html(this_month_follow_num);
                $("#this_month_profit").html(this_month_profit);
                $("#all_profit").html(all_profit);
            } else { // 失败
                alert(res.msg);
            }
        }
    });

    //设置日期，当前日期的前七天
    var myDate = new Date(); //获取今天日期
    myDate.setDate(myDate.getDate() - 7);
    // 创建日期数组
    var dateArray = [];
    var dateTemp;
    var flag = 1;
    for (var i = 0; i < 7; i++) {
        dateTemp = myDate.getFullYear() + "-" + (myDate.getMonth() + 1) + "-" + myDate.getDate();
        dateArray.push(dateTemp);
        myDate.setDate(myDate.getDate() + flag);
    }
    // 获取最近七天开始日期和结束日期
    endTime = dateArray[6];
    beginTime = dateArray[0];

    // 往右边input添加日期
    var dateVal = beginTime + ' - ' + endTime;
    $('#dateRange').val(dateVal);

    // 加载历史概况图
    historyChart();
    // 加载历史概况数据表格
    historyDataTable()

    // 历史概况布局手机模式兼容
    if ($(window).width() < 768) {
        $('.layui-inline').css('margin-top', '5px');
    }
});


// 历史概况图
function historyChart() {
    // 初始化历史概况图表
    var map = document.getElementById("container");
    var historyMap = echarts.init(map);
    option = null;
    // 显示标题，图例和空的坐标轴
    option = {
        title: {
            text: '总收益'
        },
        color: ['#3398DB'],
        tooltip: {
            trigger: 'axis',
            axisPointer: { // 坐标轴指示器，坐标轴触发有效
                type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [{
            type: 'category',
            data: [''],
            axisTick: {
                alignWithLabel: true
            }
        }],
        yAxis: [{
            type: 'value'
        }],
        series: [{
            type: 'bar',
            barWidth: '60%',
            data: []
        }]
    };

    var url = 'api/monitor/gethistory4chart?beginTime=' + beginTime + '&endTime=' + endTime + '';
    // 异步加载数据
    $.post(url, {
        placeType: placeType,
        quota: quota
    }).done(function(res) {
        var date = res.data.date;
        var value = res.data.value;
        // 填入数据
        historyMap.setOption({
            xAxis: {
                data: date
            },
            series: [{
                // 根据名字对应到相应的系列
                data: value
            }]
        });
    });
    // 渲染图表
    if (option && typeof option === "object") {
        historyMap.setOption(option, true);

        /*echarts图表窗口自适应*/
        setTimeout(function() {
            window.onresize = function() {
                historyMap.resize();
            }
        }, 100);
    }
}

// 历史概况数据表
function historyDataTable() {
    // 历史概况数据表格
    layui.use('table', function() {
        var table = layui.table;
        table.render({
            elem: '#dataTable',
            url: 'api/monitor/gethistory4list',
            where: {
                beginTime: beginTime,
                endTime: endTime,
                placeType: placeType,
                quota: quota
            },
            cellMinWidth: 115,
            cols: [
                [
                    { field: 'date', unresize: true, align: 'center', title: '日期' },
                    { field: 'no', unresize: true, align: 'center', title: '设备编号' },
                    { field: 'version', unresize: true, align: 'center', title: '设备型号' },
                    { field: 'address', unresize: true, align: 'center', title: '投放地点' },
                    { field: 'point_name', unresize: true, align: 'center', title: '投放点位' },
                    { field: 'shop_type', unresize: true, align: 'center', title: '投放类型' },
                    { field: 'state', unresize: true, align: 'center', title: '设备状态' },
                    { field: 'use_num', unresize: true, align: 'center', title: '使用人数' },
                    { field: 'follower', unresize: true, align: 'center', title: '新关注人数' },
                    { field: 'un_follower', unresize: true, align: 'center', title: '取关人数' },
                    { field: 'rate', unresize: true, align: 'center', title: '取关率' },
                    { field: 'commission', unresize: true, align: 'center', title: '收益' }
                ]
            ],
            page: {
                layout: ['count', 'prev', 'page', 'next', 'skip'],
                groups: 2
            },
            limit: 10
        });
    });
}

// 日期范围选择
layui.use('laydate', function() {
    var laydate = layui.laydate;

    //执行一个laydate实例
    laydate.render({
        elem: '#dateRange',
        range: true,
        change: function(value, date) { //监听日期被切换
            var time = value.split(' - ');
            beginTime = time[0];
            endTime = time[1];
            // 加载历史概况图
            historyChart();
            // 加载历史概况数据表格
            historyDataTable();
        }
    });
});

// 点击最近7天
function getSevenDays(thisObj) {
    var str = $(thisObj).text();
    $(thisObj).parent().parent().siblings('a').text(str);
    //设置日期，当前日期的前七天
    var myDate = new Date(); //获取今天日期
    myDate.setDate(myDate.getDate() - 7);

    var dateArray = [];
    var dateTemp;
    var flag = 1;
    for (var i = 0; i < 7; i++) {
        dateTemp = myDate.getFullYear() + "-" + (myDate.getMonth() + 1) + "-" + myDate.getDate();
        dateArray.push(dateTemp);
        myDate.setDate(myDate.getDate() + flag);
    }
    beginTime = dateArray[0];
    endTime = dateArray[6];

    // 往右边input添加日期
    var dateVal = beginTime + ' - ' + endTime;
    $('#dateRange').val(dateVal);
    // 加载历史概况图
    historyChart();
    // 加载历史概况数据表格
    historyDataTable();

}

// 点击最近30天
function getThirtyDays(thisObj) {

    var str = $(thisObj).text();
    $(thisObj).parent().parent().siblings('a').text(str);
    //设置日期，当前日期的前30天
    var myDate = new Date(); //获取今天日期
    myDate.setDate(myDate.getDate() - 30);

    var dateArray = [];
    var dateTemp;
    var flag = 1;
    for (var i = 0; i < 30; i++) {
        dateTemp = myDate.getFullYear() + "-" + (myDate.getMonth() + 1) + "-" + myDate.getDate();
        dateArray.push(dateTemp);
        myDate.setDate(myDate.getDate() + flag);
    }
    beginTime = dateArray[0];
    endTime = dateArray[29];

    // 往右边input添加日期
    var dateVal = beginTime + ' - ' + endTime;
    $('#dateRange').val(dateVal);

    // 加载历史概况图
    historyChart();
    // 加载历史概况数据表格
    historyDataTable();

}

// 使用场所
function getStrInfo(thisObj) {
    var str = $(thisObj).text();
    $(thisObj).parent().parent().siblings('a').text(str);
    placeType = (str == '全部') ? '' : str;
    // 加载历史概况图
    historyChart();
    // 加载历史概况数据表格
    historyDataTable();
}
// 选择指标
function getIndexInfo(thisObj) {
    var str = $(thisObj).text();
    $(thisObj).addClass('layui-btn-normal').siblings().removeClass('layui-btn-normal');
    switch (str) {
        case '使用人数':
            quota = 1;
            break;
        case '新关注人数':
            quota = 2;
            break;
        case '取关人数':
            quota = 3;
            break;
        case '取关率':
            quota = 4;
            break;
        case '收益':
            quota = 5;
            break;
        default:
            quota = 1;
    }
    // 加载历史概况图
    historyChart();
    // 加载历史概况数据表格
    historyDataTable();
}

// 数据导出
function dataExport() {
    var url = 'api/monitor/gethistory4export?beginTime=' + beginTime + '&endTime=' + endTime + '&placeType=' + placeType + '&quota=' + quota + '';
    $.ajax({
        url: url,
        type: 'get',
        dataType: 'json',
        success: function(res) {
            if (res.code == 0) {
                window.location.href = res.data;
            } else {
                layer.alert(res.msg);
            }
        }
    })
}