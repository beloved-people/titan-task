package com.riozenc.task.job;

import com.riozenc.task.webapp.service.impl.SmsServiceImpl;
import com.riozenc.titanTool.spring.context.SpringContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@DisallowConcurrentExecution
public class ResendMessagesJob implements Job {
    private Log logger = LogFactory.getLog(ResendMessagesJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        logger.debug("短信---重发开始===="+new Date());

        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.isResendSms();
        logger.debug("短信---重发结束===="+new Date());

    }
}
