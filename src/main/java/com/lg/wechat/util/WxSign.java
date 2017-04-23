package com.lg.wechat.util;

import com.lg.wechat.model.message.req.WxPaySendData;
import com.lg.wechat.model.message.req.WxPrepayData;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by mac on 17/3/17.
 */
public class WxSign {
    /**
     　　* 创建签名
     　　* @param parameters
     　　* @param key
     　　* @return
     　　*/
    @SuppressWarnings("rawtypes")
    public static String createSign(SortedMap<Object,Object> parameters, String key){
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            if(null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + key);
        String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
        return sign;
    }


    public static void main(String[] args) throws Exception{
        String xml="<xml><return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "<appid><![CDATA[wx2d10663316262e38]]></appid>\n" +
                "<mch_id><![CDATA[1461950502]]></mch_id>\n" +
                "<nonce_str><![CDATA[OT0zWUjGluYkgdIg]]></nonce_str>\n" +
                "<sign><![CDATA[098E51CCBDD34D180FA253B40CD386E2]]></sign>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<prepay_id><![CDATA[wx20170423190519feee9215c90343443771]]></prepay_id>\n" +
                "<trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "</xml>";

        //解析xml
        XStream stream = new XStream(new DomDriver());
        stream.alias("xml", WxPaySendData.class);
        WxPaySendData wxReturnData = (WxPaySendData)stream.fromXML(xml);

        System.out.println(wxReturnData.getAppid());

//        JAXBContext context = JAXBContext.newInstance(WxPrepayData.class);
//        Unmarshaller unmarshaller = context.createUnmarshaller();
//        WxPrepayData student = (WxPrepayData)unmarshaller.unmarshal(new StringReader(xml));
    }
}
