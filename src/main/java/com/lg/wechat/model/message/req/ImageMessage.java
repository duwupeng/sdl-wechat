package com.lg.wechat.model.message.req;

/**
 * Created by eric.du on 2017-3-16.
 */
/**
 * 图片消息
 *
 */
public class ImageMessage extends BaseMessage {
    // 图片链接
    private String PicUrl;

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }
}
