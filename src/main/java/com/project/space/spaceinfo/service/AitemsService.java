package com.project.space.spaceinfo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.project.space.domain.Space_InfoVO;
import com.project.space.spaceinfo.mapper.SpaceInfoMapper;

import lombok.extern.log4j.Log4j;

@Log4j
@Service
@PropertySource("classpath:/config/props/NaverAitemsAPI.properties")
public class AitemsService {
	@Inject
	private SpaceInfoMapper sMapper;
	@Value("${naver.aitems.api.SecretKey}")
	private String SECRETKEY;
	@Value("${naver.aitems.api.AccessKey}")
	private String ACCESSKEY;
	
	public List<Space_InfoVO> getRecSpace(String userid) throws Exception{
		String baseurl="https://aitems.apigw.ntruss.com";
		String serviceId = "77md0h6wia2";
		String targetId = userid;
		
		Date now = new Date();
		//log.info("now.getTime()>>"+now.getTime());
		String timestamp = Long.toString(now.getTime());
		//log.info(timestamp);
		
		URL url=new URL(baseurl+"/api/v1/services/"+serviceId+"/infers/lookup?type=personalRecommend&targetId="+targetId);
		URLConnection urlCon=url.openConnection();
		HttpURLConnection con=(HttpURLConnection)urlCon;
		StringBuffer response=new StringBuffer();
		//con.setRequestProperty("Host", "aitems.apigw.ntruss.com");
		//con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("x-ncp-iam-access-key", ACCESSKEY);
		String signature = makeAitemsSignature(timestamp,serviceId,targetId);
		//log.info("signature===>"+signature);
		con.setRequestProperty("x-ncp-apigw-signature-v2", signature);
		con.setRequestProperty("x-ncp-apigw-timestamp", timestamp);
		//con.setRequestMethod("GET");//???????????? ??????
		con.connect();
		
		//https://aitems.apigw.ntruss.com/api/v1/services/{serviceId}/infers/lookup?type={type}&targetId={targetId}<<?????? url method=GET
		//serviceId = ????????? ???????????????
		//type = personalRecommend(????????? ??????), relatedItem(??????????????????), pop(??????????????????) ?????? ??????
		//targetId = ????????? ID
		String responseData = "";
		int responseCode=con.getResponseCode();
		log.info("responseCode===="+responseCode);
		BufferedReader br;
		if(responseCode==200) {
			br=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
		}else {
			log.info("Error??????: "+responseCode);
			br=new BufferedReader(new InputStreamReader(con.getErrorStream(),"UTF-8"));
		}
		while((responseData=br.readLine()) != null) {
			response.append(responseData);
		}
		log.info("response====>>>"+response);
		
		return ResponseToRecommendSpace(response);
	}
	//https://api-fin.ncloud-docs.com/docs/common-ncpapi ??????????????? ???????????????
	public String makeAitemsSignature(String ts,String serviceId,String targetId) throws Exception{
		String space = " ";					// one space
		String newLine = "\n";					// new line
		String method = "GET";					// method
		String url = "/api/v1/services/"+serviceId+"/infers/lookup?type=personalRecommend&targetId="+targetId;// url (include query string)
		String timestamp = ts;			// current timestamp (epoch)
		String accessKey = ACCESSKEY;			// access key id (from portal or sub account)
		String secretKey = SECRETKEY;

		String message = new StringBuilder()
			.append(method)
			.append(space)
			.append(url)
			.append(newLine)
			.append(timestamp)
			.append(newLine)
			.append(accessKey)
			.toString();

		SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(signingKey);

		byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
		String encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
		//Base64.encodeBase64String(rawHmac); <<????????? commons-codec-1.5.jar ?????? ????????????????????? ?????????
		//?????? util??? Base64 ??????
		
	  return encodeBase64String;
	}
	
	public List<Space_InfoVO> ResponseToRecommendSpace(StringBuffer values) throws Exception{
		JSONParser jp = new JSONParser();
		JSONObject jobj = new JSONObject(); 
		jobj = (JSONObject) jp.parse(values.toString());
		
		//log.info("jobj>>"+jobj);
		//log.info("jobj.values===>>"+jobj.get("values"));
		JSONArray arr = (JSONArray) jobj.get("values");
		log.info("arr==>"+arr);
		
		List<Space_InfoVO> PRArr = new ArrayList<>(); //????????? ???????????????
		int j = 0;
		for(Object i : arr) {
			if(sMapper.selectBySnum(Integer.parseInt((String)i))!=null) {
				PRArr.add(sMapper.selectBySnum(Integer.parseInt((String)i)));
				//log.info(PRArr.get(j));
				//j++;
			}
			//sMapper.selectBySnum(Integer.parseInt((String)i));
		}
		PRArr = PRArr.stream().distinct().collect(Collectors.toList()); //steram.distinct??? ????????? List??? ???????????? ????????????
		log.info("??????????????? ???????????????==>"+PRArr);
		return PRArr;
	}
}
