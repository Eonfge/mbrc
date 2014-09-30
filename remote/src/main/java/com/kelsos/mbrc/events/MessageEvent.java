package com.kelsos.mbrc.events;

import com.kelsos.mbrc.interfaces.IEvent;
import org.codehaus.jackson.node.TextNode;

public class MessageEvent implements IEvent {
    private String type;
    private Object data;

    public MessageEvent() {
        this.type = "";
        this.data = null;
    }

    public MessageEvent(String type) {
        this.type = type;
        data = "";
    }

    public MessageEvent(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String getDataString() {
        String result = null;
        if (data.getClass() == TextNode.class) {
            result = ((TextNode) data).asText();
        } else if (data.getClass() == String.class) {
            result = (String) data;
        }
        return result;
    }

    public void init(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public void init(String type) {
        this.type = type;
        this.data = null;
    }

}
