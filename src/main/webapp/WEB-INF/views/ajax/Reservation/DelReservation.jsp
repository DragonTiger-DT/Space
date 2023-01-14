<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:import url="/Spacetop" charEncoding="utf-8" />
<div id="DelR">
<form name="DelR" method="post" action="${pageContext.request.contextPath}/DelR">
<input type="text" id="rtnum" name="rtnum" value="${drvo.rtnum}" class="form-control">
<input type="text" id="totalprice" name="totalprice" value="${drvo.totalprice}" class="form-control">
<input type="text" id="userid" name="userid" value="${loginUser.userid}" class="form-control">
<input type="text" name="to" id="to" class="form-control"  value="${loginUser.hp}">
<table>
	<thead class="header">
		<tr class="title">
			<th>예약취소 요청</th>
		</tr>
	</thead>

	<tbody class="body">
		<tr class="body_one">
			<td>예약한 공간</td>
			<td>${sivo.sname}</td>
		</tr>
		
		<tr>
			<td>예약기간</td>
			<td>${drvo.rtstartdate}. ${drvo.rtstart} ~ ${drvo.rtend}</td>
		</tr>
		
		<tr>
			<td>예약자명</td>
			<td>${drvo.userid}</td>
		</tr>
		
		<tr>
			<td>예약인원</td>
			<td>${drvo.rtnumber}</td>
		</tr>
	
		<tr class="body_two">
			<td rowspan="5">취소 사유 선택</td>
			<td>
				<p><label><input type="checkbox" name="nb[]" value="01">단순변심</label></p>
			</td>
		</tr>
		
		<tr>
			<td><p><label><input type="checkbox" name="nb[]" value="02">예약정보 변경(일정,연락처)/중복예약</label></p></td>
		</tr>
		<tr>
			<td><p><label><input type="checkbox" name="nb[]" value="03">호스트 요청에 따른 이용불가</label></p></td>
		</tr>
		<tr>
			<td><p><label><input type="checkbox" name="nb[]" value="04">이용조건 불만</label></p></td>
		</tr>
		<tr>
			<td><p><label><input type="checkbox" name="nb[]" value="05">기타</label></p></td>
		</tr>
		
		<tr>
			<td>피드백</td>
			<td><textarea cols="50" rows="5" placeholder="더 나은 space가 되도록 하겠습니다"></textarea></td>
		</tr>
	
		<tr class="body_three">
			<td>예약 금액</td>
			<td><fmt:formatNumber value="${drvo.totalprice}" pattern="###,###"/></td>
		</tr>
		
		<tr>
			<td>총 할인 금액</td>
			<td></td>
		</tr>
		
		<tr>
			<td>예상 취소 수수료</td>
			<td></td>
		</tr>
		
		<tr>
			<td>환불 포인트</td>
			<td><fmt:formatNumber value="${drvo.totalprice}" pattern="###,###"/></td>
		</tr>
	</tbody>
</table>
	<button class="btn">예약취소</button>
	<button type="button" class="btn" onclick="history.back()">Back</button>
</form>       
</div>
<c:import url="/Spacefoot" charEncoding="utf-8" />