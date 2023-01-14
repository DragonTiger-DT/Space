package com.project.space.admin.mapper;

import java.util.List;
import java.util.Map;

import com.project.space.admin.service.AdminMemberHistoryVO;
import com.project.space.admin.service.AdminMemberInquiryVO;
import com.project.space.admin.service.AdminSpaceInquiryVO;
import com.project.space.domain.HashtagVO;
import com.project.space.domain.Mem_InfoVO;
import com.project.space.domain.PagingVO;

public interface AdminMapper {
	public List<AdminMemberInquiryVO> searchUserByFilter(Map<String,String> filter);
	
	public List<HashtagVO> getHashTagAll();
	
	public List<AdminMemberInquiryVO> listUser(PagingVO pvo); 
	
	public List<AdminSpaceInquiryVO> listSpace(PagingVO pvo);
	
	public List<String> getSpaceAddr();
	
	public List<AdminSpaceInquiryVO> searchSpaceByFilter(Map<String, String> filter);
	
	public List<AdminMemberHistoryVO> getUserHistory(String userid);
	
	public List<AdminMemberInquiryVO> todayJoinMember(String string);
	
	public List<AdminSpaceInquiryVO> todayInsertSpace(String string);
	
	public List<AdminSpaceInquiryVO> todayPopSpace(String string);
}