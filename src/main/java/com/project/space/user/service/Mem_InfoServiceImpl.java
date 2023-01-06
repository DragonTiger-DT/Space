package com.project.space.user.service;


import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.space.domain.Mem_InfoVO;
import com.project.space.domain.NotUserException;
import com.project.space.domain.PagingVO;
import com.project.space.user.mapper.Mem_InfoMapper;

import lombok.extern.log4j.Log4j;


@Service
@Log4j
public class Mem_InfoServiceImpl implements Mem_InfoService {
	
	@Autowired
	private PasswordEncoder pwencoder; //암호화 객체
	
	@Inject
	private Mem_InfoMapper memberMapper;
	
	@Override
	public int createUser(Mem_InfoVO memvo) {
		String beforeEncording = memvo.getMpwd();
		log.info("암호화하기 전 비밀번호 확인==>"+memvo.getMpwd());
		String afterEncording = pwencoder.encode(beforeEncording);
		memvo.setMpwd(afterEncording);
		log.info("암호화된지 비밀번호 확인==>"+memvo.getMpwd());
		return memberMapper.createUser(memvo);
	}

	@Override
	public int getUserCount(PagingVO pvo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Mem_InfoVO> listUser(PagingVO pvo) {
		return memberMapper.listUser(pvo);
	}

	@Override
	public int idCheck(String userid) {
		// TODO Auto-generated method stub
		return memberMapper.idCheck(userid);
	}

	@Override
	public int deleteUser(Mem_InfoVO vo) {
		return memberMapper.deleteUser(vo);
	}

	@Override
	public int updateUser(Mem_InfoVO user) {
		String afterEncording = pwencoder.encode(user.getMpwd());
		user.setMpwd(afterEncording);
		return memberMapper.updateUser(user);
	}

	@Override
	public Mem_InfoVO getUser(String userid) {
		// TODO Auto-generated method stub
		return memberMapper.getUser(userid);
	}

	@Override
	public Mem_InfoVO findUser(Mem_InfoVO findUser) throws NotUserException {
		Mem_InfoVO user=memberMapper.findUser(findUser);
		if(user==null) {
			throw new NotUserException("존재하지 않는 아이디입니다");
		}
		return user;
	}

	@Override
	public Mem_InfoVO loginCheck(String userid, String mpwd) throws NotUserException {
		Mem_InfoVO user=this.getUser(userid);
		log.info("mem_infoservice logincheck user==>"+user);
		if(user==null) {
			throw new NotUserException("존재하지 않는 아이디입니다");
		}
		if(!pwencoder.matches(mpwd,user.getMpwd() )) {
		//if(!user.getMpwd().equals(mpwd)) { 
			throw new NotUserException("비밀번호가 일치하지 않습니다");
		}
		
		return user;
	}

}