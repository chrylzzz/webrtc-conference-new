package com.igu.webrtc.conference.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.igu.webrtc.conference.service.webrtc.CallHandler;
import com.igu.webrtc.conference.utils.Byteutils;

/**
 * 
 *@Description: 录像文件 服务层
 *@author gu
 *@date 2018年11月8日  
 *
 */
@Service
public class RecordService {

	 private static final Logger LOG = LoggerFactory.getLogger(CallHandler.class);
	
	@Value("${recorder.file.path}")
	private String recorderFilePath;
	

	public List<Map<String,Object>> getList(Integer page, Integer limit) {
		
		LOG.info("recorderFilePath:{}",recorderFilePath);
		
		File logDir = new File(recorderFilePath.replace("file://", ""));
		File[] files = logDir.listFiles();
		List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
		if(files==null||files.length==0) {
			return list;
		}
		for (File f : files) {
			Map<String,Object> lf = new HashMap<String,Object>();
			lf.put("fileName",f.getName());
//			lf.put("filePath",f.getAbsolutePath().replaceAll("\\\\", "/"));
			
			lf.put("fileSize",Byteutils.formatByte(f.length(), 2));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			lf.put("lastModified",sdf.format(new Date(f.lastModified())));
			 
			list.add(lf);
			
		}
	 
		return list;
	}


}
