package com.project.space.user.mapper;

import java.util.List;
import java.util.Map;

import com.project.space.domain.Mem_InfoVO;
import com.project.space.domain.NotUserException;
import com.project.space.domain.PagingVO;

public interface Mem_InfoMapper {
	int createUser(Mem_InfoVO memvo);  //유저등록

	int getUserCount(PagingVO pvo);  //페이지

	List<Mem_InfoVO> listUser(PagingVO pvo);  //페이지 유저 리스트

	int idCheck(String userid);

	int deleteUser(Mem_InfoVO mpwd);//회원탈퇴

	int updateUser(Mem_InfoVO user);//회원수정

	Mem_InfoVO getUser(String userid);

	Mem_InfoVO findUser(Mem_InfoVO findUser) throws NotUserException;

	Mem_InfoVO loginCheck(String userid, String mpwd) throws NotUserException;

	int getStatusByUserid(String userid);
	
	Mem_InfoVO pwCheck(String userid, String mpwd)throws NotUserException;
	
	List<Mem_InfoVO> listBankcode();
	
	List<Mem_InfoVO> searchUserByFilter(Map<String,String> filter);

    int updateUserPoint(Map<String, Object> map);
    
}
