package com.lg.wechat.model.message.req;

/**
 * Created by eric.du on 2017-3-16.
 */
/**
 * 文本消息
 *
 */
public class TextMessage extends BaseMessage {
    // 消息内容
    private String Content;

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}
