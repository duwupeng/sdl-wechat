package com.lg.controllers;

import com.alibaba.fastjson.JSON;
import com.lg.consts.WechatConst;
import com.lg.domain.WeixinLoginUser;
import com.lg.services.Oauth;
import com.lg.wechat.model.message.req.UserInfo;
import com.lg.wechat.model.message.req.WxPaySendData;
import com.lg.wechat.util.WeChatUtil;
import com.lg.wechat.util.WxSign;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by mac on 17/3/17.
 */
@Controller
public class WxPayController {

    @RequestMapping("wechatpay/jsapi")
    String wechat(){
        return "jsapi";
    }

    /**
     * 主要是拿openId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("wechatpay/toPay")
    public ModelAndView toPay(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView();
        System.out.println("玩家准备填写充值信息了:" + request);

        //重定向Url
        String redirecUri = URLEncoder.encode(WechatConst.baseUrl + "/toPay.do");
        //用于获取成员信息的微信返回码
        String code = null;
        if( request.getParameter("code")!=null ){
            code =request.getParameter("code");
        }
        if( code == null) {
            //授权
            return authorization(redirecUri);
        }
        code =request.getParameter("code");

        System.out.println("授权code:" + code);

        // 获取用户信息
        WeixinLoginUser weixinLoginUser = getWeixinLoginUser(code);
        System.out.println(weixinLoginUser);
//
        modelAndView.addObject("openId",weixinLoginUser.getOpenID());
//      System.out.println("拿到openId:" + weixinLoginUser.getOpenID());
        // 跳转到支付界面
        String viewName = "jsapi";
        modelAndView.setViewName(viewName);
        return modelAndView;
    }

    /**
     * 授权方法
     * @param redirecUri 重定向链接
     *
     * */
    private ModelAndView authorization(String redirecUri) {
        String siteURL="redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                +WechatConst.appID
                +"&redirect_uri="+redirecUri
                +"&response_type=code"
                + "&scope=snsapi_userinfo"
                + "&state=1234#wechat_redirect";
        System.out.println("授权路径:" + siteURL);
        return new ModelAndView(siteURL);
    }


    /**
     * 获取微信授权登陆用户
     * @param code
     * @return
     * @throws Exception
     */
    private WeixinLoginUser getWeixinLoginUser(String code) throws Exception {
//        logger.debug("由code获取授权用户信息");
        Oauth oauth = new Oauth();
        // 由code获取access_token等信息
        String str = oauth.getToken(WechatConst.appID, WechatConst.appSecret,code);

        // 解析返回的json数据,获取所需的信息
        String openID = (String) JSON.parseObject(str, Map.class).get("openid");
        String accessToken = (String) JSON.parseObject(str, Map.class).get("access_token");
        String refreshToken = (String) JSON.parseObject(str, Map.class).get("refresh_token");

        // 用openid,access_token获取用户的信息,返回userinfo对象
        UserInfo userInfo = oauth.getSnsUserInfo(openID, accessToken);


        // 将用户信息放入登录session中
        WeixinLoginUser weixinLoginUser = new WeixinLoginUser();
        weixinLoginUser.setOpenID(openID);
        weixinLoginUser.setUnionID(userInfo.getUnionid());
        weixinLoginUser.setHeadImageUrl(userInfo.getHeadimgurl());
        weixinLoginUser.setNickName(userInfo.getNickname());
        weixinLoginUser.setRefreshToken(refreshToken);
        //
        int siteID = WechatConst.siteID;
        weixinLoginUser.setSiteID(siteID);
        // 返回weixinLoginUser对象
        return weixinLoginUser;
    }


    /**
     　　* 点击确认充值 统一下单,获得预付id(prepay_id)
     　　* @param request
     　　* @param response
     　　* @return
     　　 */
   @ResponseBody
   @RequestMapping({"wechatpay/pay"})
    public WxPaySendData prePay(HttpServletRequest request, HttpServletResponse response, String openId){
        WxPaySendData result = new WxPaySendData();
        try {
            //商户订单号
            String out_trade_no = WeChatUtil.getOut_trade_no();
           //产品价格,单位：分
           Integer total_fee = 1;
           //客户端ip
            String ip = request.getRemoteAddr();
            //支付成功后回调的url地址
            String notify_url = "http://easygoleagal.com.cn/callback";

            //统一下单
            String strResult = WeChatUtil.unifiedorder("testPay", out_trade_no, total_fee, ip, notify_url,openId);

            System.out.println("strResult: " + strResult);

            //解析xml
//             XStream stream = new XStream(new DomDriver());
//            stream.alias("xml", WxPaySendData.class);
//            Object obj = stream.fromXML(strResult);
//
//            System.out.println("Object: " + obj);
//
//            WxPaySendData wxReturnData = (WxPaySendData)obj;
            StringReader sr = new StringReader(strResult);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            String resultCode= doc.getElementsByTagName("result_code").item(0).getTextContent();
            String returnCode = doc.getElementsByTagName("return_code").item(0).getTextContent();
            String prepayId = doc.getElementsByTagName("prepay_id").item(0).getTextContent();

            //两者都为SUCCESS才能获取prepay_id
            if( resultCode.equals("SUCCESS") && returnCode.equals("SUCCESS") ){
                        //业务逻辑，写入订单日志(你自己的业务) .....
                String timeStamp = WeChatUtil.getTimeStamp();//时间戳
                String nonce_str = WeChatUtil.getNonceStr();//随机字符串
        //注：上面这两个参数，一定要拿出来作为后续的value，不能每步都创建新的时间戳跟随机字符串，不然H5调支付API，会报签名参数错误
                result.setResult_code(resultCode);
                result.setAppid(WechatConst.appID);
                result.setTimeStamp(timeStamp);
                result.setNonce_str(nonce_str);
                result.setPackageStr("prepay_id="+prepayId);
                result.setSignType("MD5");

        //     WeChatUtil.unifiedorder(1) 下单操作中，也有签名操作，那个只针对统一下单，要区别于下面的paySign
                //第二次签名,将微信返回的数据再进行签名
                SortedMap<Object,Object> signMap = new TreeMap<Object,Object>();
                signMap.put("appId", WechatConst.appID);
                signMap.put("timeStamp", timeStamp);
                signMap.put("nonceStr", nonce_str);
                signMap.put("package", "prepay_id="+prepayId);  //注：看清楚，值为：prepay_id=xxx,别直接放成了wxReturnData.getPrepay_id()
                signMap.put("signType", "MD5");

                String paySign = WxSign.createSign(signMap,  WechatConst.KEY);//支付签名

                result.setSign(paySign);
            }else{
                    result.setResult_code("fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
      }
         return result;
    }

    /**
     * 支付回调接口
     * @param request
     * @return
     */
    @RequestMapping("/callback")
    public void callBack(HttpServletRequest request, HttpServletResponse response){
   response.setContentType("text/xml;charset=UTF-8");
    try {
      InputStream is = request.getInputStream();
      String result = IOUtils.toString(is, "UTF-8");
            if("".equals(result)){
                response.getWriter().write("<xm><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[参数错误！]]></return_msg></xml>");
                return ;
            }
     //解析xml
    XStream stream = new XStream(new DomDriver());
    stream.alias("xml", WxPaySendData.class);
    WxPaySendData wxPaySendData = (WxPaySendData)stream.fromXML(result);
    System.out.println(wxPaySendData.toString());

        String appid = wxPaySendData.getAppid();
        String mch_id =wxPaySendData.getMch_id();
        String nonce_str = wxPaySendData.getNonce_str();
            String out_trade_no = wxPaySendData.getOut_trade_no();
            String total_fee = wxPaySendData.getTotal_fee();
            //double money = DBUtil.getDBDouble(DBUtil.getDBInt(wxPaySendData.getTotal_fee())/100.0);
            String trade_type = wxPaySendData.getTrade_type();
            String openid =wxPaySendData.getOpenid();
            String return_code = wxPaySendData.getReturn_code();
            String result_code = wxPaySendData.getResult_code();
            String bank_type = wxPaySendData.getBank_type();
            Integer cash_fee = wxPaySendData.getCash_fee();
            String fee_type = wxPaySendData.getFee_type();
            String is_subscribe = wxPaySendData.getIs_subscribe();
            String time_end = wxPaySendData.getTime_end();
            String transaction_id = wxPaySendData.getTransaction_id();
            String sign = wxPaySendData.getSign();

            //签名验证
            SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
            parameters.put("appid",appid);
            parameters.put("mch_id",mch_id);
            parameters.put("nonce_str",nonce_str);
            parameters.put("out_trade_no",out_trade_no);
            parameters.put("total_fee",total_fee);
            parameters.put("trade_type",trade_type);
            parameters.put("openid",openid);
            parameters.put("return_code",return_code);
            parameters.put("result_code",result_code);
            parameters.put("bank_type",bank_type);
            parameters.put("cash_fee",cash_fee);
            parameters.put("fee_type",fee_type);
            parameters.put("is_subscribe",is_subscribe);
            parameters.put("time_end",time_end);
            parameters.put("transaction_id",transaction_id);

            String sign2 = WxSign.createSign(parameters, WechatConst.KEY);

            if(sign.equals(sign2)){//校验签名，两者需要一致，防止别人绕过支付操作，不付钱直接调用你的业务，不然，哈哈，你老板会很开心的 233333.。。。
                if(return_code.equals("SUCCESS") && result_code.equals("SUCCESS")){
                    //业务逻辑(先判断数据库中订单号是否存在，并且订单状态为未支付状态)
           //do something ...


                    //request.setAttribute("out_trade_no", out_trade_no);
                    //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                    response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
                }else{
                    response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[交易失败]]></return_msg></xml>");
                }
            }else{
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名校验失败]]></return_msg></xml>");
            }
            response.getWriter().flush();
            response.getWriter().close();
            return ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
