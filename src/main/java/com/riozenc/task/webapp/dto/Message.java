package com.riozenc.task.webapp.dto;

public class Message {

    private String content; // 短信内容
    private String mobile; // 用户电话号码

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
