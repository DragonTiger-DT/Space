package com.project.space;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.space.domain.Mem_InfoVO;
import com.project.space.domain.PointVO;
import com.project.space.domain.ReservationVO;
import com.project.space.payment.service.PaymentService;
import com.project.space.point.service.PointService;
import com.project.space.reservation.service.ReservationService;
import com.project.space.user.service.Mem_InfoService;

import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class PointController {
	
	@Inject
	private Mem_InfoService meminfoService;
	
	@Inject
	private PointService pointService;
	
	@Inject
	private ReservationService reservationService;

	@Inject
	private PaymentService paymentService;

	@GetMapping(value="/user/pointAdd")
	public String pointAdd(Model m, HttpSession ses) {
		Mem_InfoVO mvo=(Mem_InfoVO)ses.getAttribute("loginUser"); //세션에 저장된 로그인 정보
		String mvoId=mvo.getUserid();  //로그인된 유저 꺼내오기
		log.info("들어온 회원 아이디: "+mvoId);
		
		Mem_InfoVO mivo=this.meminfoService.getUser(mvoId); //회원정보
		List<PointVO> pvoArr=this.pointService.getPointUser(mvoId); //포인트 충전내역
		List<ReservationVO> resArr=this.reservationService.userBookingInfo(mvoId); //예약한 공간정보(포인트 사용처)
		String resPrice=this.reservationService.userBookingTotalprice(mvoId); //총 예약 포인트 합계
		//log.info("resPrice--->"+resPrice);
		
		m.addAttribute("mivo", mivo);
		m.addAttribute("pvoArr", pvoArr);
		m.addAttribute("resArr", resArr);
		m.addAttribute("resPrice", resPrice);


		String userid = ((Mem_InfoVO)ses.getAttribute("loginUser")).getUserid();
      
		int num;
		if(paymentService.getpaymentcount() == 0){
			num = 1;
		}else{
			num = paymentService.getPaynum();
		}

		String numStr = String.format("%07d", num);     
		log.info("numStr====>"+numStr);
		
		//랜덤한 3자리 문자를 만든다
        String randomStr = "";
        for(int i=0; i<3; i++){
            int random = (int)(Math.random()*26+97);
            randomStr += (char)random;
        }

        String merchant_uid = randomStr +  new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" +  numStr;

		
		//
		
		Mem_InfoVO mem = meminfoService.getUser(userid);


		m.addAttribute("mname", mem.getMname());
		m.addAttribute("hp", mem.getHp());
		m.addAttribute("merchant_uid", merchant_uid);


		return "ajax/Reservation/pointAdd";
	}
	
	
}
