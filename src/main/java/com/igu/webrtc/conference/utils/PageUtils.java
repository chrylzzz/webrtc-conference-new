package com.igu.webrtc.conference.utils;

/**
 * @author wuyunxing
 * @date 2018/3/23
 */
public class PageUtils {
    public static int getStartIndex(int currentPage, int pageSize) {
        if(pageSize >= 1 && pageSize <= 2147483647) {
            if(currentPage < 0) {
                throw new RuntimeException("页码超出范围");
            } else if((currentPage - 1) * pageSize > 2147483647) {
                throw new RuntimeException("非法的分页参数值");
            } else {
                return (currentPage - 1 < 0?0:currentPage - 1) * pageSize;
            }
        } else {
            throw new RuntimeException("每页显示数目超出范围");
        }
    }
}
