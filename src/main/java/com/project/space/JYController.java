package com.project.space;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.space.admin.service.AdminSpaceInquiryVO;
import com.project.space.domain.FeedbackVO;
import com.project.space.domain.Mem_InfoVO;
import com.project.space.domain.PointVO;
import com.project.space.domain.ReasonVO;
import com.project.space.domain.ReservationVO;
import com.project.space.domain.Space_InfoVO;
import com.project.space.domain.mem_space_res_view;
import com.project.space.reservation.MessageDTO;
import com.project.space.reservation.Schedule;
import com.project.space.reservation.SmsResponseDTO;
import com.project.space.reservation.SmsService;
import com.project.space.reservation.service.ReservationService;
import com.project.space.spaceinfo.service.SpaceInfoService;
import com.project.space.user.service.Mem_InfoService;

import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class JYController {
	
	@Inject
	private SpaceInfoService spaceinfoService;
	
	@Inject
	private ReservationService reservationService;
	
	@Inject
	private Mem_InfoService meminfoService;
	
	@Inject
	private SmsService smsService;
	
	@GetMapping(value="/Reservation")
	public String resSpace(Model m, @RequestParam(defaultValue="0") int snum, HttpSession ses) {
		log.info("snum: "+snum);
		
		ses.setAttribute("snum", snum);  //????????? snum ??????
		
		Space_InfoVO svo=this.spaceinfoService.selectBySnum(snum);
		
		Date nowTime=new Date();
		SimpleDateFormat sf=new SimpleDateFormat("yyyyMMdd");
		String now=sf.format(nowTime);
		log.info("now: "+now);
		m.addAttribute("now", now);
		
		log.info(svo+"<<<<????????????");
		m.addAttribute("svo",svo);
		ses.setAttribute("svo", svo);
		return "ajax/Reservation/ReservationMain1";
	}
	
	@RequestMapping(value = "/ReservationAjax", method=RequestMethod.GET)
	public String calendarAjax(Model m, HttpServletRequest req, Schedule sch) {
		Calendar cal=Calendar.getInstance();
		Schedule schedule;
		
		if(sch.getDate().equals("") && sch.getMonth().equals("")) {
			sch=new Schedule(String.valueOf(cal.get(Calendar.YEAR)), 
					String.valueOf(cal.get(Calendar.MONTH)+1), String.valueOf(cal.get(Calendar.DATE)), null, null);
		}
		System.out.println("sch???????????? ????????? ???====="+sch);
		Map<String, Integer> today_info=sch.today_info(sch);
		List<Schedule> dateList=new ArrayList<>();
		
		//?????????

		HttpSession ses=req.getSession();
		Space_InfoVO svo=(Space_InfoVO)ses.getAttribute("svo"); //????????? ????????? ?????? ??????
		log.info("svo: "+svo);
		sch.setSnum(svo.getSnum());

		List<ReservationVO> sch_list=reservationService.CalbookingInfo(sch);

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
		return "ajax/Reservation/ReservationAjax";
	}
	
	
	
	@PostMapping(value="/ReservationModal", produces="application/json")
	@ResponseBody
	public ModelMap ReservationModal(@RequestBody Map<String,String> pay) {
		log.info("?????????"+pay);
		ReservationVO rvo=new ReservationVO();
		rvo.setSnum(Integer.parseInt(pay.get("rtspace")));  //????????????
		rvo.setUserid(pay.get("rtuser")); //???????????????
		rvo.setRtstartdate(pay.get("rtyear"), pay.get("rtmonth"), pay.get("rtdate"));  //???????????????
		rvo.setRtstart(pay.get("rtstartTime")); //????????????
		rvo.setRtend(pay.get("rtendTime"));  //????????????
		rvo.setTotalTime(pay.get("rtstartTime"), pay.get("rtendTime")); //????????????
		rvo.setRtnumber(Integer.parseInt(pay.get("rtcount")));  //????????????
		rvo.setCountprice(Integer.parseInt(pay.get("rtcount")), Integer.parseInt(pay.get("rtminn")), Integer.parseInt(pay.get("rtecost"))); //???????????????
		rvo.setTimePrice(rvo.getTotalTime(), Integer.parseInt(pay.get("rtbcost"))); //???????????????
		rvo.setTotalPrice(rvo.getCountPrice(), rvo.getTimePrice()); //??? ????????????
		log.info("?????????"+rvo);
		
//		Mem_InfoVO pointM=this.meminfoService.getUser(pay.get("rtuser"));
		ModelMap m=new ModelMap();
		
		int cst=Integer.parseInt(rvo.getRtstart().substring(0, 2));
		log.info("????????? startTime"+cst);
		int ced=Integer.parseInt(rvo.getRtend().substring(0, 2));
		log.info("????????? endTime"+ced);
		
		List<ReservationVO> TimeT=reservationService.bookingTimeInfo(rvo);
		log.info("db????????????"+TimeT);
		int dst[]=new int[TimeT.size()];
		int ded[]=new int[TimeT.size()];
		
		if(TimeT!=null && TimeT.size()!=0) {
			for(int i=0;i<TimeT.size();i++) {
				dst[i]=Integer.parseInt(TimeT.get(i).getRtstart().substring(0,2));
				log.info("db ???????????? ??????=="+dst);
				ded[i]=Integer.parseInt(TimeT.get(i).getRtend().substring(0,2));
				log.info("db ????????? ??????=="+ded);
				if(cst!=dst[i] && cst!=ded[i] && ced!=dst[i] && ced!=ded[i]) {
					while(cst<ded[i]) {
						if(ced<dst[i]) {
							m.addAttribute("result",rvo);
							return m;
						}else {
							m.addAttribute("result",0);
							return m;
						} //if
					} //while
				}else {
					m.addAttribute("result",0);
					return m;
				} //if
			} //for
		} //if
		m.addAttribute("result",rvo);
		return m;
	}
	
	@PostMapping(value="/ReservationPayment")
	   public String ReservationPayment(Model m, HttpSession ses, @ModelAttribute ReservationVO rtvo, @ModelAttribute("messageDto") MessageDTO messageDto) 
	         throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, 
	         UnsupportedEncodingException, HttpClientErrorException {
	      log.info("rtvo insert=="+rtvo);
	      log.info("message: "+messageDto);
	      String date = rtvo.getRtstartdate();
	      String year = date.substring(0,4);
	      String month = date.substring(4,6);
	      String day = date.substring(6,8);
	      messageDto.setContent(rtvo.getUserid()+"??? "+year+"??? "+month+"??? "+day+"??? "+"?????? ????????? ?????????????????????");

	      String str="";
	      String loc="";
	      
	      int cst=Integer.parseInt(rtvo.getRtstart().substring(0, 2));
	      log.info("????????? startTime"+cst);
	      int ced=Integer.parseInt(rtvo.getRtend().substring(0, 2));
	      log.info("????????? endTime"+ced);
	      
	      List<ReservationVO> TimeT=reservationService.bookingTimeInfo(rtvo);
	      log.info("db????????????"+TimeT);
	      int dst[]=new int[TimeT.size()];
	      int ded[]=new int[TimeT.size()];
	      
	      if(TimeT!=null && TimeT.size()!=0) {
	         for(int i=0;i<TimeT.size();i++) {
	            dst[i]=Integer.parseInt(TimeT.get(i).getRtstart().substring(0,2));
	            log.info("db ???????????? ??????=="+dst);
	            ded[i]=Integer.parseInt(TimeT.get(i).getRtend().substring(0,2));
	            log.info("db ????????? ??????=="+ded);
	            if(cst!=dst[i] && cst!=ded[i] && ced!=dst[i] && ced!=ded[i]) {
	               while(cst<ded[i]) { //?while????????? ??????????????????
	                  if(ced<dst[i]) {
	                     int res=this.reservationService.insertBooking(rtvo);  //?????? ?????????
	                     
	                     if(res>0) {
	                        int uur=this.reservationService.updateUserRes(rtvo); //????????? ????????? ??????
	                        if(uur>0) {
	                           PointVO check=new PointVO();
	                           Space_InfoVO svo=this.spaceinfoService.selectBySnum(rtvo.getSnum());
	                             check.setUserid(svo.getUserid());
	                             check.setPlusPoint(rtvo.getTotalprice());
	                             int usp=this.reservationService.PlusSpacePoint(check); //????????? ????????? +
	                        }
	                        //SmsResponseDTO response = smsService.sendSms(messageDto);  //?????? ??????
	                     }
	                     str=(res>0)? "????????? ?????????????????????":"?????? ???????????? ????????? ?????????";
	                     loc=(res>0)? "/space/user/MyReservation":"/space/user/pointAdd";
	                     m.addAttribute("message", str);
	                     m.addAttribute("loc", loc);
	                     return "msg";
	                     
	                  }else {
	                     str="?????? ????????? ???????????????";
	                     loc="javascript:history.back()";
	                     m.addAttribute("message", str);
	                     m.addAttribute("loc", loc);
	                     return "msg";
	                  } //if
	               } //while
	            }else {
	               str="?????? ????????? ???????????????";
	               loc="javascript:history.back()";
	               m.addAttribute("message", str);
	               m.addAttribute("loc", loc);
	               return "msg";
	            } //if
	         } //for
	      }
	      int res=this.reservationService.insertBooking(rtvo);  //?????? ?????????
	      
	      if(res>0) {
	         int uur=this.reservationService.updateUserRes(rtvo); //????????? ????????? ??????
	         if(uur>0) {
	            PointVO check=new PointVO();
	            Space_InfoVO svo=this.spaceinfoService.selectBySnum(rtvo.getSnum());
	              check.setUserid(svo.getUserid());
	              check.setPlusPoint(rtvo.getTotalprice());
	              int usp=this.reservationService.PlusSpacePoint(check); //????????? ????????? +
	         }
	         //SmsResponseDTO response = smsService.sendSms(messageDto);  //?????? ??????
	      }
	      
	      str=(res>0)? "????????? ?????????????????????":"?????? ???????????? ????????? ?????????";
	      loc=(res>0)? "/space/user/MyReservation":"/space/user/pointAdd";
	      m.addAttribute("message", str);
	      m.addAttribute("loc", loc);
	      return "msg";
	   }
	
	@RequestMapping(value = "/user/MyReservation", method = RequestMethod.GET)
	public String myreservation(Model m, HttpServletRequest req) {
		log.info("connected myreservation.");
		
		HttpSession ses=req.getSession();
		Mem_InfoVO rvo=(Mem_InfoVO)ses.getAttribute("loginUser"); //????????? ????????? ?????? ????????? ??????
		log.info("rvo: "+rvo);
		List<mem_space_res_view> resArr=this.reservationService.BookingView(rvo.getUserid());
		
		Date nowTime=new Date();
		SimpleDateFormat sf=new SimpleDateFormat("yyyyMMdd");
		String now=sf.format(nowTime);
		log.info("now: "+now);
		
		m.addAttribute("resArr", resArr);
		m.addAttribute("now", now);
		
		return "ajax/Pages/MyReservation";
	}
	
	@GetMapping("/user/DelReservation")
	public String DelResModal(Model m, @RequestParam(defaultValue="0") int rtnum) {
		log.info("rtnum=="+rtnum);

		ReservationVO drvo=this.reservationService.getBooking(rtnum);
		Space_InfoVO sivo=this.spaceinfoService.selectBySnum(drvo.getSnum());
		m.addAttribute("drvo", drvo);
		m.addAttribute("sivo", sivo);
		return "ajax/Reservation/DelReservation";
	}
	
	@PostMapping(value="/DelR")
	public String DeleteReservation(Model m, @ModelAttribute FeedbackVO fbvo, @ModelAttribute("messageDto") MessageDTO messageDto) 
			throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, 
			UnsupportedEncodingException, HttpClientErrorException {
		log.info("fbvo delete=="+fbvo);
		log.info("message: "+messageDto);
		
		
		ReservationVO rtvo=this.reservationService.getBooking(fbvo.getRtnum()); //???????????? ????????????
		fbvo.setTotalprice(rtvo.getTotalprice());
		fbvo.setSnum(rtvo.getSnum());
		log.info("setting fbvo========"+fbvo);
		String date = rtvo.getRtstartdate();
		String year = date.substring(0,4);
		String month = date.substring(4,6);
		String day = date.substring(6,8);
		//messageDto.setContent(rtvo.getUserid()+"??? "+year+"??? "+month+"??? "+day+"??? "+"??? ????????? ?????? ???????????????");
		messageDto.setContent(rtvo.getUserid()+"??? 2023??? 01??? 27??? "+"??? ????????? ?????? ???????????????");
		int res=this.reservationService.deleteBooking(fbvo.getRtnum()); //???????????? ?????? ??????
		if(res>0) {
			int fb=this.reservationService.insertFeedback(fbvo); //???????????? ?????????
			int up=this.reservationService.updateUserPoint(fbvo); //????????? ????????? +
			
			PointVO check=new PointVO();
			Space_InfoVO svo=this.spaceinfoService.selectBySnum(fbvo.getSnum());
			check.setUserid(svo.getUserid());
			check.setPlusPoint(rtvo.getTotalprice());
	        int usp=this.reservationService.MinusSpacePoint(check); //????????? ????????? ??????
	        
			//SmsResponseDTO response = smsService.sendSms(messageDto);
			String str="????????? ?????????????????????";
			String loc="/space/user/MyReservation";
			m.addAttribute("message", str);
			m.addAttribute("loc", loc);
			return "msg";
		}else {
			String str="?????? ?????? ??????";
			String loc="/space/user/MyReservation";
			
			m.addAttribute("message", str);
			m.addAttribute("loc", loc);
			return "msg";
		}
	}
	
	@GetMapping("/owner/MyFeedback")
	public String mySpaceEdit(Model m , HttpSession ses) {
		log.info("connected Feedback");
	    String userid = ((Mem_InfoVO)ses.getAttribute("loginUser")).getUserid();
	    
	    List<FeedbackVO> fArr=this.reservationService.checkFeedback(userid);
	    List<Space_InfoVO> sArr=this.spaceinfoService.selectByUserid(userid);
	    List<ReasonVO> rArr=this.reservationService.getReasonAll();
	    m.addAttribute("fArr",fArr);
	    m.addAttribute("sArr",sArr);
	    m.addAttribute("rArr",rArr);
		return "ajax/OwnerPage/MyFeedback";
	}
	
	@PostMapping(value="/owner/searchFeedback" , produces = "application/json")
	@ResponseBody
	public List<FeedbackVO> getsearchFeedback(@RequestBody Map<String,String> filter){
		
		List<FeedbackVO> spaceArr=this.reservationService.searchFeedbackByFilter(filter);
		log.info("memArr==>"+spaceArr);
		return spaceArr;
	}
	
}
