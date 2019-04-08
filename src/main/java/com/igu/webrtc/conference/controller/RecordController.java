package com.igu.webrtc.conference.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igu.webrtc.conference.common.Const;
import com.igu.webrtc.conference.pojo.Response.BaseDataResp;
import com.igu.webrtc.conference.pojo.Response.BaseStatusCode;
import com.igu.webrtc.conference.pojo.Response.PageDataResp;
import com.igu.webrtc.conference.service.RecordService;

@RestController
@RequestMapping(value = "/api/record/")
public class RecordController {
	
	
    @Autowired
    RecordService recordService;

    
    @RequestMapping(value = "list")
    public BaseDataResp getAccountList(HttpServletRequest request,
    		@RequestParam(required = false,defaultValue="1") Integer page,
    		@RequestParam(required = false,defaultValue="10") Integer limit) {
        
        PageDataResp resp = new PageDataResp();
        
        Object user=request.getSession().getAttribute(Const.USER_KEY);
        if (user != null) {
        	
        	
        	List<Map<String,Object>> list=recordService.getList(page,limit);
            
            Long count=new Long(list.size());
            
            resp.setData(list);
            
            resp.setCount(count);
            
            resp.setCode(BaseStatusCode.SUCCESS);
            
        } else {
            resp.setCode(BaseStatusCode.FAIL);
            resp.setMsg("登录超时,请重新登录。");
        }
        

        return resp;
    }

}