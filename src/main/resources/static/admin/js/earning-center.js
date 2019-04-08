$(function() {
    // 加载动画
    layer.load(2);

    // 获取收入概况
    $.ajax({
        url: "api/profit/getsum",
        dataType: "json",
        type: "post",
        success: function(res) {
            if (res.code == 0) {
                var yesterday_profit = res.data.yesterday_profit;
                var this_month_profit = res.data.this_month_profit;
                var all_profit = res.data.all_profit;
                var settle_profit = res.data.settle_profit;
                var un_settle_profit = res.data.un_settle_profit;

                $('#yesterday_profit').html(yesterday_profit)
                $('#this_month_profit').html(this_month_profit)
                $('#all_profit').html(all_profit)
                $('#settle_profit').html(settle_profit)
                $('#un_settle_profit').html(un_settle_profit)

                // 关闭加载动画
                layer.closeAll('loading');

            } else {
                // 关闭加载动画
                layer.closeAll('loading');
                layer.alert(res.msg);
            }
        }
    });

    // 提现记录
    layui.use('table', function() {
        var table = layui.table;
        table.render({
            elem: '#kiting',
            url: 'api/profit/getlist',
            page: {
                layout: ['count', 'prev', 'page', 'next', 'skip'],
                groups: 2
            },
            cellMinWidth: 142,
            limit: 10,
            cols: [
                [
                    { field: 'date',   unresize: true, align: 'center', title: '提现日期' },
                    { field: 'money',  unresize: true, align: 'center', title: '提现金额' },
                    {
                        field: 'pay_state',
                        unresize: true,
                       
                        align: 'center',
                        title: '提现状态',
                        // 判断提现状态
                        templet: function(data) {
                            var payState = data.pay_state;
                            if (payState == 0) {
                                return '已到账'
                            } else if (payState == 1) {
                                return '未到账'
                            }
                        }
                    }
                ]
            ],
        });
    });


    // 获取结算账户
    $.ajax({
        url: 'api/user/get',
        type: 'post',
        data: '',
        dataType: 'json',
        success: function(res) {
            if (res.code == 0) {
                var email = res.data.email;
                var card_state = res.data.card_state;
                var card_name = res.data.card_name;
                var card_number = res.data.card_number;

                $('#email').html(email);
                $('#card_name').html(card_name);
                $('#card_number').html(card_number);

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