package com.atguigu.gulimall.auth.util;

import java.util.Random;

/**
 * @author yuhl
 * @Date 2020/9/14 20:51
 * @Classname AuthCodeGenerater
 * @Description 生成验证码
 */
public class AuthCodeGenerater {

    public static String getCode() {
        //生成一个5位数的验证码
        //验证码由 4个 （A-Z a-z） 和1个 （0-9）的字符组成
        //数字固定在最后一位即可- 例如：gAgZ6
        char[] arr = new char[26 + 26];
        int index = 0;
        for (int i = 97; i <= 122; i++) {
            arr[index] = (char)i;
            index++;
        }
        for(int i = 65; i <= 90; i++){
            arr[index] = (char)i;
            index++;
        }
        String result = "";
        Random r = new Random();
        for(int i = 0; i < 4; i++){
            int randomIndex = r.nextInt(arr.length);
            char c = arr[randomIndex];
            result = result + c;
        }
        int number = r.nextInt(10);
        result = result + number;
        return result;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(getCode());
        }

    }
}
