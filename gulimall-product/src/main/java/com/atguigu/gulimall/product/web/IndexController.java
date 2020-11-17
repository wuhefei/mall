package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.RedissonScoredSortedSet;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yuhl
 * @Date 2020/9/10 7:07
 * @Classname IndexController
 * @Description index.html中的控制类
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;
    /* 首页面跳转 */
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        List<CategoryEntity> categoryEntityList = categoryService.getLeve1Categorys();

        model.addAttribute("categorys", categoryEntityList);

        return "index";
    }

    /**
     * 获得首页商品三级分类
     * @return
     */
    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

    /*
    压力测试，
     */

    //http://127.0.0.1:10001/hello
    @ResponseBody
    @GetMapping("hello")
    public String hello(){
        //1.获取一把锁，只要所得名字一样，就是同一把锁，redis会存入redis数据库中，解锁后释放
        RLock lock = redisson.getLock("my-lock");
        //2.加锁
        //lock.lock();//阻塞式等待，相当于自旋，拿不到一致在尝试去重新拿
        // 加锁以后10秒钟自动解锁
        // 无需调用unlock方法手动解锁
        lock.lock(10, TimeUnit.SECONDS);
        //1) 所得自动续期，如果业务超长，运行期间自动给锁续期30s，不用担心业务时间长，所自动被删掉的情况发生
        //2) 加锁的业务只要运行完成，就不会给当前锁续期，及时不手动解锁，锁默认也会在30s后自动删除。
        /**
         * https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
         * 大家都知道，如果负责储存这个分布式锁的Redisson节点宕机以后，而且这个锁正好处于锁住的状态时，这个锁会出现锁死的状态。为了避免这种情况的发生，Redisson内部提供了一个监控锁的看门狗，它的作用是在Redisson实例被关闭前，不断的延长锁的有效期。默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定。
         * 1.如果我们自己设定超期时间，则按指定时间解锁，不会续期
         * 2.如果没有指定看门狗来做，会再超时时间用完三分之一是即还剩20s时，把时间重新定为30，去解决长业务问题。
         * 最佳实战，使用我们自定义的超时时间，设置长点就好了。
         * 同时，如果时间到了还没有执行完怎么办？悲剧了。只能设置长点的时间。
         *
         */
        //3.执行业务逻辑
        try {
            System.out.println("加锁成功，开始执行业务逻辑..."+Thread.currentThread().getName());
            Thread.sleep(10000); //10秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //4.释放锁
            System.out.println("释放锁"+Thread.currentThread().getName());
            lock.unlock();
        }

        return "hello";
    }

    //保证一定能读到最新的数据，修改期间，写锁是一个排它锁（互斥锁）。读锁是一个共享锁。
    //写锁没释放，读锁就必须等待
    @GetMapping("write")
    @ResponseBody
    public String writerValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
        System.out.println("读锁加锁成功"+Thread.currentThread().getId());
            rLock.lock();
            Thread.sleep(30000);
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writerValue",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁加锁释放"+Thread.currentThread().getId());
        }
        return s;

    }

    @GetMapping("read")
    @ResponseBody
    public String readValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("读锁加锁成功"+Thread.currentThread().getId());
            Thread.sleep(1000);
            s =  redisTemplate.opsForValue().get("writerValue");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁加锁释放"+Thread.currentThread().getId());
        }
        return s;

    }

    /**
     * 让信号量上下降 占一个车位，总车位数少1
     * 3车位
     * 信号量也可以作为分布式限流 使用tryAcquire()
     * @return
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //park.acquire();//阻塞式单个带
        boolean b = park.tryAcquire();
        // 试着看有没有车位，没有就溜之大吉，可以作为分布式限流，超过流量就返回false,或者提示"超过最大流量"
        if (b) {
            //可以进来秒杀。或者其他业务逻辑
        } else {
            return "已达到最大人数，您被提出了！";

        }
        return "ok";
    }

    /**
     * 让信号量上上升，走了一辆车车位数就加1
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();//离开一辆车
           return "ok";
    }

    /**
     * 8.8. 闭锁（CountDownLatch）
     * 基于Redisson的Redisson分布式闭锁（CountDownLatch）Java对象RCountDownLatch采用了与java.util.concurrent.CountDownLatch相似的接口和用法。
     *
     * RCountDownLatch latch = redisson.getCountDownLatch("anyCountDownLatch");
     * latch.trySetCount(1);
     * latch.await();
     *
     * // 在其他线程或其他JVM里
     * RCountDownLatch latch = redisson.getCountDownLatch("anyCountDownLatch");
     * latch.countDown();
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);//一共5个班，全部走才能锁门。
        door.await();//呆呆闭锁都按成
        return "放假了";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数减去1
        return id + "班的人都走了...";
    }
}
