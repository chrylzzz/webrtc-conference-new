package com.igu.webrtc.conference.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igu.webrtc.conference.mapper.AccountMapper;
import com.igu.webrtc.conference.model.Account;
import com.igu.webrtc.conference.utils.PageUtils;

/**
 * 
 *@Description: 账号dao层
 *@author gu
 *@date 2018年11月8日  
 *
 */
@Service
public class AccountService {

	@Autowired
	private AccountMapper accountMapper;

	public Account selectOne(String username) {
		 
		return accountMapper.selectOne(username);
	}

	public Integer insertOne(Account account) {
		
		return accountMapper.insertOne(account);
	}

	public Integer updateOne(Account account) {
		
		return accountMapper.updateOne(account);
	}

	public Integer deleteOne(Long id) {
		return accountMapper.deleteOne(id);
	}

	public Long getCount(String username) {
		return accountMapper.getCount(username);
	}

	public List<Account> getList(Integer page, Integer limit, String username) {
		
		// 计算开始索引
		int startIndex = PageUtils.getStartIndex(page, limit);
		return accountMapper.getList(startIndex, limit,username);
	}


}
