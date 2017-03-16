package com.lg.wechat.model.message.req;

/**
 * Created by eric.du on 2017-3-16.
 */
public class MenuMessage extends BaseMessage {
    private String EventKey;

    public String getEventKey() {
        return EventKey;
    }

    public void setEventKey(String eventKey) {
        EventKey = eventKey;
    }
}
