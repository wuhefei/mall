package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import com.atguigu.gulimall.thirdparty.vo.SmsVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Test
    void contextLoads() {
    }


    /**
     * 使用阿里对象存储starter依赖测试成功
     * @throws FileNotFoundException
     */
    @Autowired
    OSSClient ossClient;

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void sendSmsTest(){
        SmsVo smsVo = smsComponent.sendCode("18237964056", "5201");
        System.out.println("短信回执smsVo:"+smsVo);

    }
    @Test
    public void testUploadStarter() throws FileNotFoundException {
        /*// Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-shanghai.aliyuncs.com";
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4G5yG6BrzWiYWE1K46wj";
        String accessKeySecret = "X7Eq9xPfc6x1giQVmxMKNN1DPiEnYO";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
*/
        // 上传文件流。
        InputStream inputStream = new FileInputStream("D:\\tmp\\IMG_20190718_151034.jpg");
        ossClient.putObject("gulimall-yuhl", "IMG_20190718_151034_gulimall-third-party.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传成功.");
    }
}
