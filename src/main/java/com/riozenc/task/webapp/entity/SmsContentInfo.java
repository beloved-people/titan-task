package com.riozenc.task.webapp.entity;

import com.riozenc.titanTool.mybatis.MybatisEntity;

/**
 * @author WangShun
 */
public class SmsContentInfo implements MybatisEntity {

    /**流水号*/
    private Integer smsId;
    /**短信标题*/
    private String smsTitle;
    /**短信模板*/
    private String smsContentTemplate;
    /**备注*/
    private String smsRemark;

    public Integer getSmsId() {
        return smsId;
    }

    public void setSmsId(Integer smsId) {
        this.smsId = smsId;
    }

    public String getSmsTitle() {
        return smsTitle;
    }

    public void setSmsTitle(String smsTitle) {
        this.smsTitle = smsTitle;
    }

    public String getSmsContentTemplate() {
        return smsContentTemplate;
    }

    public void setSmsContentTemplate(String smsContentTemplate) {
        this.smsContentTemplate = smsContentTemplate;
    }

    public String getSmsRemark() {
        return smsRemark;
    }

    public void setSmsRemark(String smsRemark) {
        this.smsRemark = smsRemark;
    }
}
