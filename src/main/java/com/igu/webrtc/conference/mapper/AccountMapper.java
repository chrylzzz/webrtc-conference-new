package com.igu.webrtc.conference.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.igu.webrtc.conference.model.Account;

/**
 * 
 *@Description: TODO  
 *@date 2018年11月8日  
 *
 */
@Mapper
public interface AccountMapper {

    /**
     * 查询 
     * @param  
     * @return
     */
	Account selectOne(@Param("username")String username);
	
	Integer insertOne(Account account);

	Integer updateOne(Account account);

	Integer deleteOne(@Param("id") Long id);

	Long getCount(@Param("username")String username);

	List<Account> getList(@Param("startIndex") Integer startIndex, @Param("skip")Integer skip, @Param("username")String username);
	 
}
