package com.riozenc.task.webapp.entity;

import com.riozenc.titanTool.mybatis.MybatisEntity;

import java.util.List;

/**
 * @author belov
 */
public class SmsBackup implements MybatisEntity {
    private Long id;
    private Long settlementId;
    private String mobile;
    private String content;
    private boolean isSend;
    private List<Long> settlementIds;
    private String contentTitle;
    private Integer mon;
    /**是否发送成功*/
    private boolean success;
    private String reason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Long settlementId) {
        this.settlementId = settlementId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }

    public boolean getIsSend() {
        return isSend;
    }

    public List<Long> getSettlementIds() {
        return settlementIds;
    }

    public void setSettlementIds(List<Long> settlementIds) {
        this.settlementIds = settlementIds;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public Integer getMon() {
        return mon;
    }

    public void setMon(Integer mon) {
        this.mon = mon;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
