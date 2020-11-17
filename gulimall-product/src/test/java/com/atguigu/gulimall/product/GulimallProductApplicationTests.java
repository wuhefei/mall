package com.atguigu.gulimall.product;

//
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testredissonClient(){
        System.out.println(redissonClient);
    }

    @Test
    public void teststringRedisTemplate(){
        //保存进redia
        stringRedisTemplate.opsForValue().set("hello","world"+UUID.randomUUID().toString());

        System.out.println("保存成功");

        String hello = stringRedisTemplate.opsForValue().get("hello");
        System.out.println("去除刚才保存的值hello:"+ hello);


    }

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(255L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));

    }
    //brand品牌中新增华为数据
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("哈哈1哈");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");
    }

    //修改刚刚新增的数据描述信息
    @Test
    void contextLoads2() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("修改啦啊华为");
        brandService.updateById(brandEntity);
    }

    /**
     * 使用单独的阿里对象存储依赖测试成功
     * @throws FileNotFoundException
     */
//    @Test
//    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shanghai.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4G5yG6BrzWiYWE1K46wj";
//        String accessKeySecret = "X7Eq9xPfc6x1giQVmxMKNN1DPiEnYO";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("D:\\tmp\\111.png");
//        ossClient.putObject("gulimall-yuhl", "111.png", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传成功.");
//    }

//    /**
//     * 使用阿里对象存储starter依赖测试成功
//     * @throws FileNotFoundException
//     */
//    @Autowired
//    OSSClient ossClient;
//
//    @Test
//    public void testUploadStarter() throws FileNotFoundException {
//        /*// Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shanghai.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4G5yG6BrzWiYWE1K46wj";
//        String accessKeySecret = "X7Eq9xPfc6x1giQVmxMKNN1DPiEnYO";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//*/
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("D:\\tmp\\IMG_20190718_151034.jpg");
//        ossClient.putObject("gulimall-yuhl", "IMG_20190718_151034.jpg", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传成功.");
//    }
}
