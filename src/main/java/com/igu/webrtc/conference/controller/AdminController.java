package com.igu.webrtc.conference.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igu.webrtc.conference.common.Const;
import com.igu.webrtc.conference.model.Account;
import com.igu.webrtc.conference.pojo.Response.BaseDataResp;
import com.igu.webrtc.conference.pojo.Response.BaseStatusCode;
import com.igu.webrtc.conference.pojo.Response.PageDataResp;
import com.igu.webrtc.conference.service.AccountService;

@RestController
@RequestMapping(value = "/api/user/")
public class AdminController {
	
	private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);
	
	@Value("${admin.password}")
	private String adminPassword;
	
    @Autowired
    AccountService accountService;


	@RequestMapping(value = "login")
	public BaseDataResp login(@RequestParam(required = true) String username,
			@RequestParam(required = true) String password, HttpServletRequest request) {

		BaseDataResp resp = new BaseDataResp();
		
		LOG.info("username:{} try to login ",username);
		

		if (username.equals("admin") && password.equals(adminPassword)) {

			request.getSession().setAttribute(Const.USER_KEY, username);
			resp.setCode(BaseStatusCode.SUCCESS);
			resp.setMsg("登录成功。");
		} else {
			resp.setCode(BaseStatusCode.FAIL);
			resp.setMsg("账号或密码不正确。");
		}

		return resp;
	}

	@RequestMapping(value = "logout", method = RequestMethod.POST)
	public BaseDataResp logout(HttpServletRequest request) {

		BaseDataResp resp = new BaseDataResp();

		request.getSession().removeAttribute(Const.USER_KEY);

		resp.setCode(BaseStatusCode.SUCCESS);
		resp.setMsg("退出成功");

		return resp;
	}
	
	  /**
     * 获取账号信息
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "get")
    public BaseDataResp get(HttpServletRequest request) {

        BaseDataResp resp = new BaseDataResp();
        Object user=request.getSession().getAttribute(Const.USER_KEY);
        if (user != null) {
            resp.setCode(BaseStatusCode.SUCCESS);
            resp.setData(user);
        } else {
            resp.setCode(BaseStatusCode.FAIL);
            resp.setMsg("登录超时,请重新登录。");
        }

        return resp;
    }
    
    
    @RequestMapping(value = "account/get")
    public BaseDataResp getAccountList(HttpServletRequest request,
    		@RequestParam(required = false,defaultValue="1") Integer page,
    		@RequestParam(required = false,defaultValue="10") Integer limit,
    		@RequestParam(required = false,defaultValue="") String username) {
        
        PageDataResp resp = new PageDataResp();
        
        Object user=request.getSession().getAttribute(Const.USER_KEY);
        if (user != null) {
            
            Long count=accountService.getCount(username);
            
            resp.setData(accountService.getList(page,limit,username));
            
            resp.setCount(count);
            
            resp.setCode(BaseStatusCode.SUCCESS);
            
        } else {
            resp.setCode(BaseStatusCode.FAIL);
            resp.setMsg("登录超时,请重新登录。");
        }
        

        return resp;
    }

    
    @RequestMapping(value = "account/update")
    public BaseDataResp updateAccount(HttpServletRequest request,
    		@RequestParam(required = true) Long id,
    		@RequestParam(required = true) String pwd) {
    	
    	BaseDataResp resp = new BaseDataResp();
    	
    	Object user=request.getSession().getAttribute(Const.USER_KEY);
    	if (user != null) {
    		
    		Account account=new Account();
    		account.setId(id);
    		account.setPwd(pwd);
    		
    		accountService.updateOne(account);
    		
    		resp.setCode(BaseStatusCode.SUCCESS);
    		
    	} else {
    		resp.setCode(BaseStatusCode.FAIL);
    		resp.setMsg("登录超时,请重新登录。");
    	}
    	
    	return resp;
    }

    @RequestMapping(value = "account/delete")
    public BaseDataResp deleteAccount(HttpServletRequest request,
    		@RequestParam(required = true) Long id) {
    	
    	BaseDataResp resp = new BaseDataResp();
    	
    	Object user=request.getSession().getAttribute(Const.USER_KEY);
    	if (user != null) {
    		
    		accountService.deleteOne(id);
    		
    		resp.setCode(BaseStatusCode.SUCCESS);
    		
    	} else {
    		resp.setCode(BaseStatusCode.FAIL);
    		resp.setMsg("登录超时,请重新登录。");
    	}
    	
    	return resp;
    }


}