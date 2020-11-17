package com.atguigu.gulimall.thirdparty.component;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gulimall.thirdparty.vo.SmsVo;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: SmsComponent</p>
 * Description：
 * date：2020/6/25 14:23
 */
@Data
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Component
public class SmsComponent {

	private String accessKeyId; //LTAI4G5yG6BrzWiYWE1K46wj

	private String secret; //X7Eq9xPfc6x1giQVmxMKNN1DPiEnYO

	/**
	 * private String accessKeyId; //LTAI4G5yG6BrzWiYWE1K46wj
	 * 	private String secret; //X7Eq9xPfc6x1giQVmxMKNN1DPiEnYO
	 * 	两个参数在application.yml 中配置。
	 * @param phone 电话号码
	 * @param code  验证码
	 * @return
	 */
	public SmsVo sendCode(String phone, String code){
		SmsVo smsVo = null;
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",accessKeyId,secret);
		IAcsClient client = new DefaultAcsClient(profile);

		CommonRequest request = new CommonRequest();
		request.setSysMethod(MethodType.POST);
		request.setSysDomain("dysmsapi.aliyuncs.com"); //固定写法
		request.setSysVersion("2017-05-25"); //版本信息，因为阿里有很多版本的升级哦。
		request.setSysAction("SendSms"); //固定的action
		request.putQueryParameter("RegionId", "cn-hangzhou"); //固定的分区中国杭州
		request.putQueryParameter("PhoneNumbers", phone); //需要发送的电话号码
		request.putQueryParameter("SignName", "红亮商城"); //签名，固定写法，当时在申请的时候写的
		request.putQueryParameter("TemplateCode", "SMS_202562087"); //短信末班id,固定写法，短信末班是自己写的。
		request.putQueryParameter("TemplateParam", "{\"code\":\""+code+"\"}"); //模板中的验证按，可动态生成。
		try {
			CommonResponse response = client.getCommonResponse(request);
			String data = response.getData();
			smsVo = JSON.parseObject(data,new TypeReference<SmsVo>(){});
			System.out.println("smsVo:"+smsVo);
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		return smsVo;
	}
}
