package com.riozenc.task.webapp.domain;

/**
 * @author belov
 */
public class DigitalSignatureDomain {

    private String SFZMHM; // 身份证号码
    private String ZLHTBAH; // 备案号

    public String getSFZMHM() {
        return SFZMHM;
    }

    public void setSFZMHM(String SFZMHM) {
        this.SFZMHM = SFZMHM;
    }

    public String getZLHTBAH() {
        return ZLHTBAH;
    }

    public void setZLHTBAH(String ZLHTBAH) {
        this.ZLHTBAH = ZLHTBAH;
    }
}
