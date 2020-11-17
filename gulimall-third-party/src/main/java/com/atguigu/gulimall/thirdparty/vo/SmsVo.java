package com.atguigu.gulimall.thirdparty.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author yuhl
 * @Date 2020/9/14 17:03
 * @Classname SmsVo
 * @Description TODO
 *  * {"Message":"OK","RequestId":"8FA45F7A-8513-4F2C-BF4C-774A3BD0BE13","BizId":"369324200059555319^0","Code":"OK"}
 *  对阿里短信的返回结果进行封装
 */
@Data
@ToString
public class SmsVo {
    private String Message;
    private String RequestId;
    private String BizId;
    private String Code;
}
