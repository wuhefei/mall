package com.atguigu.gulimall.seckill.scheduel;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>Title: SeckillSkuScheduled</p>
 * Description：秒杀商品定时上架		[秒杀的定时任务调度]
 * date：2020/7/6 17:28
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

	@Autowired
	private SeckillService seckillService;

	@Autowired
	private RedissonClient redissonClient;

	private final String upload_lock = "seckill:upload:lock";
	/**
	 * 这里应该是幂等的
	 * 连续三天上架。
	 *  三秒执行一次：*没有空格/3 * * * * ?
	 *  8小时执行一次：0 0 0-8 * * ?
	 */
	// TODO ：  幂等性处理
	@Scheduled(cron = "*/30 * * * * ?")
	public void uploadSeckillSkuLatest3Day(){
		log.info("\n上架秒杀商品的信息");
		// 1.重复上架无需处理 加上分布式锁（原因是部署多个，各个都到时间了。） 状态已经更新 释放锁以后其他人才获取到最新状态
		RLock lock = redissonClient.getLock(upload_lock);
		lock.lock(10, TimeUnit.SECONDS); //分布式锁，锁定
		try {
			seckillService.uploadSeckillSkuLatest3Day();
		} finally {
			lock.unlock();  //分布式锁，解锁
		}
	}
}
