package com.project.space;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.space.domain.FeedbackVO;
import com.project.space.domain.ReservationVO;
import com.project.space.domain.Space_InfoVO;
import com.project.space.reservation.Schedule;
import com.project.space.reservation.service.ReservationService;
import com.project.space.spaceinfo.service.SpaceInfoService;

import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class Res_StatusController {
	
	@Inject
	private SpaceInfoService spaceinfoService;
	
	@Inject
	private ReservationService reservationService;

	@GetMapping(value="/ReservationStatus")
	public String ReservationStatus(Model m, @RequestParam(defaultValue="0") int snum, HttpSession ses) {
		log.info("snum: "+snum);
		
		ses.setAttribute("snum", snum);  //세션에 snum 저장
		
		Space_InfoVO svo=this.spaceinfoService.selectBySnum(snum);
		
		Date nowTime=new Date();
		SimpleDateFormat sf=new SimpleDateFormat("yyyyMMdd");
		String now=sf.format(nowTime);
		log.info("now: "+now);
		m.addAttribute("now", now);
		
		ReservationVO rvo=new ReservationVO(); //예약vo 생성
		rvo.setSnum(snum); //공간 번호 넣어주고
		rvo.setRtstartdate(String.valueOf(now)); //오늘 날짜 넣어주고
		List<ReservationVO> rsvo=this.reservationService.ReservationDateInfo(rvo);
		
		log.info(svo+"<<<<공간번호");
		m.addAttribute("svo",svo);
		m.addAttribute("rsvo",rsvo);
		ses.setAttribute("svo", svo);
		return "ajax/Reservation/ReservationStatus";
	}
	
	@RequestMapping(value = "/ReservationStatusAjax", method=RequestMethod.GET)
	public String ReservationStatusAjax(Model m, HttpServletRequest req, Schedule sch) {
		Calendar cal=Calendar.getInstance();
		Schedule schedule;
		
		if(sch.getDate().equals("") && sch.getMonth().equals("")) {
			sch=new Schedule(String.valueOf(cal.get(Calendar.YEAR)), 
					String.valueOf(cal.get(Calendar.MONTH)+1), String.valueOf(cal.get(Calendar.DATE)), null, null);
		}
		System.out.println("sch스케줄에 입력된 값====="+sch);
		Map<String, Integer> today_info=sch.today_info(sch);
		List<Schedule> dateList=new ArrayList<>();
		
		//스케줄

		HttpSession ses=req.getSession();
		Space_InfoVO svo=(Space_InfoVO)ses.getAttribute("svo"); //세션에 저장된 공간 넘버
		log.info("svo: "+svo);
		sch.setSnum(svo.getSnum());

		List<ReservationVO> sch_list=reservationService.CalbookingInfo(sch);
		
		List<ReservationVO> sch_list_count=reservationService.CountbookingInfo(sch);
		log.info("count<<<<<<"+sch_list_count);
		m.addAttribute("sch_list_count", sch_list_count);
		
		ReservationVO[][] schedule_data_arr=new ReservationVO[32][4];
		if(!sch_list.isEmpty()) {
			int s=0;
			for(int i=0;i<sch_list.size();i++) {
				int date = Integer.parseInt(String.valueOf(sch_list.get(i).getRtstartdate())
						.substring(String.valueOf(sch_list.get(i).getRtstartdate()).length()-2,String.valueOf(sch_list.get(i).getRtstartdate()).length()));
				if(i>0) {
					int date_before = Integer.parseInt(String.valueOf(sch_list.get(i-1).getRtstartdate())
							.substring(String.valueOf(sch_list.get(i-1).getRtstartdate()).length()-2,String.valueOf(sch_list.get(i-1).getRtstartdate()).length()));
				
					if(date_before==date) {
						s=s+1;
						schedule_data_arr[date][s]=sch_list.get(i);
					}else {
						s=0;
						schedule_data_arr[date][s]=sch_list.get(i);
					}
				}else {
					schedule_data_arr[date][s]=sch_list.get(i);
				}
			}
		}
		
		
		for(int i=1; i<today_info.get("start"); i++) {
			schedule=new Schedule(null,null,null,null,null);
			dateList.add(schedule);
		}
		
		for(int i=today_info.get("startDay"); i<=today_info.get("endDay"); i++) {
			ReservationVO[] sch_date_arr3=new ReservationVO[4];
			sch_date_arr3=schedule_data_arr[i];
			
			if(i==today_info.get("today")) {
				System.out.println(i+"/"+today_info.get("today"));
				schedule=new Schedule(String.valueOf(sch.getYear()), String.valueOf(sch.getMonth()), String.valueOf(i), "today", sch_date_arr3);
			}else {
				schedule=new Schedule(String.valueOf(sch.getYear()), String.valueOf(sch.getMonth()), String.valueOf(i), "normal_date", sch_date_arr3);
			}
			dateList.add(schedule);
		}
		
		int index=7-dateList.size()%7;
		if(dateList.size()%7!=0) {
			for(int i=0;i<index;i++) {
				schedule=new Schedule(null,null,null,null,null);
				dateList.add(schedule);
			}
		}
		System.out.println(dateList);
		
		m.addAttribute("dateList", dateList);
		m.addAttribute("today_info", today_info);
		req.getSession().setAttribute("today_info", today_info);
		return "ajax/Reservation/ReservationStatusAjax";
	}
	
	@PostMapping(value="/owner/searchReservation" , produces = "application/json")
	@ResponseBody
	public List<ReservationVO> getsearchReservation(HttpSession ses, @RequestBody String rtstartdate){
		log.info("시간======"+rtstartdate);
		String splitStr=rtstartdate.substring(1, 9);
		log.info("시간 재배열======"+splitStr);
		ReservationVO rvo=new ReservationVO();
		Space_InfoVO svo=(Space_InfoVO)ses.getAttribute("svo"); //세션에 저장된 공간 넘버
		rvo.setSnum(svo.getSnum());
		rvo.setRtstartdate(splitStr);
		
		List<ReservationVO> searchArr=this.reservationService.searchReservationByFilter(rvo);
		log.info("searchArr==>"+searchArr);
		return searchArr;
	}
	
}
