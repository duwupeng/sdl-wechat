package com.lg.services;

import com.lg.wechat.model.message.req.UserInfo;
import com.lg.wechat.util.HttpClientUtil;

/**
 * Created by mac on 17/3/17.
 */
public class Oauth {

    public  static String getToken(String code,String appId, String appSecret){
        String url ="https://api.weixin.qq.com/sns/oauth2/access_token?appid=" +
                "APPID" +appId+
                "&secret=" +appSecret+
                "&code="+code+
                "&grant_type=authorization_code";
        return HttpClientUtil.httpGetRequest(url);
    }

    public  static UserInfo getSnsUserInfo(String openID, String accessToken){
        String url ="https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
        System.out.println("url:-> "+ HttpClientUtil.httpGetRequest(url));


        return new UserInfo();
    }

}
