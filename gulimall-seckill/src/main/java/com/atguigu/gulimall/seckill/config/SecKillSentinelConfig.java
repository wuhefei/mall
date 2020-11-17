/*
package com.atguigu.gulimall.seckill.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

*/
/**
 * <p>Title: SecKillSentinelConfig</p>
 * Description：配置请求被限制以后的处理器
 * date：2020/7/10 13:47
 * SecKillSentinelConfig 是一个配置bean系统启动后就会初始化，初始化的时候就会调用它的
 * 构造器，然后就会有一个WebCallbackManager.setUrlBlockHandler()被造出来。就可以生效了。
 *//*

@Configuration
public class SecKillSentinelConfig {
	*/
/**
	 * 固定套路，返回统一的降级信息，提示【请求流量过大】
	 *
	 * sentinel 2.2.1中的
	 *//*

	public SecKillSentinelConfig(){
		WebCallbackManager.setUrlBlockHandler((request, response, exception) -> {
			R error = R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.getWriter().write(JSON.toJSONString(error));
		});
	}
}
*/
