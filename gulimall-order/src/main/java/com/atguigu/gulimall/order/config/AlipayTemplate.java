package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用户支付宝的支付接口类
 */
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000116662314";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDHBzD/wScAQ/RjixG1sk4JnxwUXrrFqd81OrWxLJXuUrDgdt/z3ftthk4SAEQBWE0KywPoalhuBB992//2gy7WMtv0D0/OuBqosm5giP5JWx5OoGR+oItC60TJxAJyeypHoyQSl5jeSXAbwaeQuMlr7Hnc8dHFljWj4LFPvacBR3JsCAV/W88W3GTFN6Zb7lVSI71tbjutjjxHTcrxocNnvQDjy40tkKS/X8XPEwXXCVuGdsWs0mOwjgrNKna105SlP03D8w2N8i0v4MueFO0VIcq25SBvSuypLHqYheePEq2DWfyNkqTkHXXNss/L39NFGOTPJR7AWhs1y/qJKWbnAgMBAAECggEBAKIcnG+lX2Qa9cPi8cDihCfYHqIE6vK3w+Rydq3imBjv57xdd1sGs5XpcEu/RaUmzFH/PKahgHfUCeaVPK8I+Sxlbr0jf6jsUmKYO8mLrDwJ6Oy4PkLZ97TBN110t12SLsT9ABPYwR5GCPfCy/7YVNAAZN5vZaMgD371NNezrYUewi8ZX6j81KSmxLP1KxKf8JXVm6eZX+j9uAp5Izk6mc0s2OYkd/yPf1HyFA8LpDRlDVLxCdHFe6v0RZefz32Vc4XBbeHEWREzDKYoohjsSmENBirASazxWYvKGY0AzPI0HSNe/ikZhVrxj3XLla1KhSBcDS0d+3EEpeIOXtreC+kCgYEA5gYNTufBCVUmzua5iEnrlIM5MUI3P4MjOHo/Vt5dC+/oSxOQEmQ20PbtxwWqVnpgl1fMFuabbUf0LIlPTfJFEFv8xbZohJQlGC3Rkk5M+6x2lOk4KFjvdZg6JulsW96kfhDGvoDjCaODvKwDtwPEdvWe8sk22Z+F8DMfCsOFUAsCgYEA3YEQF/yTc4ID9cen9IL+L8cB1BCuEdreVlRbaWS5dqBWUZ3aAswxQsrDttYIXd99aN38mDsEhOMc1O2levI26HtmOQ/O9NB7DVa1DvVBWY9/bJwjeBkkurHW9f6SmqbbnfoSmEVYE+Qezp1bCkGMPDy/jD4wQaHVQwxI8Q0lQhUCgYAlYhhpmXqi5Kbg+1ht0O2rfkQeQE+wT8S6mguPN6ZRmVTcVrqUcpsCDQ+Jx97uHQdzsT4m/qWF9iq1zj8IQPZ+eSS0kTedTGJW8qNbrsQOJvJCs0CANiv8pZfV2RvWNnHvVxSlw1aiq2Nul1onNUKJvtNtS8xRQ/7Fkdlaq3oQDwKBgCfplp3L3EHm/pGt/TyY0faH9HNi4grDlmaV4rbV+HlAoO0p9VulsYaohKWQYYeQusdHoLlJgSH0zoArpjLGLFCt05iamZ8RZercYdmlJbZengZTCC0e0rh1TRecXIxTXqVTp0Aa1qkW5No0DnCFqSA9jgJWPlnFMGlZcFdHGByVAoGBAJsu47RtB71QJT6am/BuYe5eO7JrGL8iyyC7BOLJD1u7Ts2wD46Fa6Nj6LGUC/oJPmA+A0JwmrFDVOZShiEKHnniee8CnNeNhPs+leixr+aDGUpkjqIHXEDXeCcmCUH2u7knqUhQoHDk3/azqC5VLRmREeUEWfXdz0LQVsaBDj8y";

	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhcD6IZ0m8BiFKHzWbmIDtWCx7qFC8hV76XEG1XvH2Ui0y+91ikHfQf0uQlSOLZILhdrOmCohQFNOfo7/10HWhocUw2hfr5TusB7d5zkREDngAC5NMOlQBDp+b2m3Ph+YzPALeg0TuTzBh+3ASRGUHeYjfFwQslckDU/qnOhovEbD7Vmsq4FW49UbH7I5/CeB2ExnUJWbQcmmhfANs9UfdLK830CBDQYaN1MFI0zkUiMHWfA0Ny/AIsFCKgraqromxAv/45E9GNtwWo/bgWrsHlhfaxpNMRVoHUAtIT4jscMtuV3CndON64N7BoDdYDust+nE0DLjXWOfEOoS9WU6mQIDAQAB";
	// 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息 支付宝用的是最大努力通知方式
    //private String notify_url = "http://member.gulimall.com/memberOrder.html";
    //https://cloud.zhexi.tech/xd/remote/mapping 哲西云中的穿透地址，
    //payed/notify 异步回调的controller 这个controller中会对数据库中订单数据进行【付款成功】装填的更新，然后跳转
    private String notify_url = "http://4w3atcjy10.52http.tech/payed/notify";
    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 自动关单时间 15分钟。
    private String timeout = "15m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        // 30分钟内不付款就会自动关单
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\"" + timeout + "\"," //支付宝的窗口期，过来就无法付款了。
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        return result;
    }
}
