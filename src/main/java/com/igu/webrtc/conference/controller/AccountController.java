package com.igu.webrtc.conference.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.igu.webrtc.conference.model.Account;
import com.igu.webrtc.conference.pojo.Response.SxbBaseRsp;
import com.igu.webrtc.conference.service.AccountService;
import com.igu.webrtc.conference.utils.TokenUtils;

@RestController
@RequestMapping(value = "/api/account/")
public class AccountController {

	private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	AccountService accountService;


	@RequestMapping(value = "login")
	public SxbBaseRsp login(@RequestParam(required = true) String username, @RequestParam(required = true) String pwd,
			HttpServletRequest request) {
		LOG.info("username:{} try to login ", username);

		// 校验账号
		SxbBaseRsp sxbRsp = new SxbBaseRsp(9000);
		sxbRsp.setId("loginRsp");

		Account account = accountService.selectOne(username);
		if (account == null || !pwd.equals(account.getPwd())) {
			sxbRsp.setErrorCode(401);
			sxbRsp.setErrorInfo("用户名或者密码不正确");
		} else {
			account.setToken(TokenUtils.createJwtToken(username));
			account.setPwd(null);
			accountService.updateOne(account);
			sxbRsp.setErrorCode(0);
			sxbRsp.setData(account);
		}

		return sxbRsp;

	}

	@RequestMapping(value = "register")
	public SxbBaseRsp register(@RequestParam(required = true) String username,
			@RequestParam(required = true) String pwd, HttpServletRequest request) {
		LOG.info("username:{} try to register ", username);

		// 校验账号
		SxbBaseRsp sxbRsp = new SxbBaseRsp(9000);

		Account account = accountService.selectOne(username);
		// 校验账号
		if (account != null) {
			sxbRsp.setErrorCode(401);
			sxbRsp.setErrorInfo("用户已存在。");
			return sxbRsp;
		}

		account = new Account();
		account.setUsername(username);
		account.setPwd(pwd);

		Integer result = accountService.insertOne(account);
		if (result > 0) {
			sxbRsp.setErrorCode(0);
			sxbRsp.setData(account);

		} else {
			sxbRsp.setErrorCode(401);
			sxbRsp.setErrorInfo("注册不成功，请稍后重试。");

		}

		return sxbRsp;

	}


}