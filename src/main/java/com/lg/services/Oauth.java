package com.lg.services;

import com.alibaba.fastjson.JSON;
import com.lg.wechat.model.message.req.UserInfo;
import com.lg.wechat.util.HttpClientUtil;

import java.util.Map;

/**
 * Created by mac on 17/3/17.
 */
public class Oauth {

    public  static String getToken(String appId, String appSecret,String code){
        String url ="https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + appId+
                "&secret=" +appSecret+
                "&code="+code+
                "&grant_type=authorization_code";

        System.out.println("Oauth url:-> "+ url);

        return HttpClientUtil.httpGetRequest(url);
    }

    public  static UserInfo getSnsUserInfo(String openID, String accessToken){
        String url ="https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" +accessToken+
                "&openid=" + openID+
                "&lang=zh_CN";
        String result =  HttpClientUtil.httpGetRequest(url);
        String nickname = (String) JSON.parseObject(result, Map.class).get("nickname");
        String Unionid = (String) JSON.parseObject(result, Map.class).get("unionid");
        String headimgurl = (String) JSON.parseObject(result, Map.class).get("headimgurl");
        UserInfo userInfo = new UserInfo();
        userInfo.setHeadimgurl(headimgurl);
        userInfo.setNickname(nickname);
        userInfo.setUnionid(Unionid);
        return userInfo;

    }

}
