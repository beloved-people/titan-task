package com.riozenc.task.webapp.service;

import org.apache.ibatis.annotations.Param;

/**
 * @author WangShun
 */
public interface ISmsService {
    void isSendSms(@Param("smsTitle") String smsTitle);

    void isResendSms();

    void isSendArrearageMessages(@Param("smsTitle") String smsTitle);

    void punishMoneySchedulerJobService();
}
