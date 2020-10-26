package com.riozenc.task.job;

import com.riozenc.task.webapp.service.impl.SmsServiceImpl;
import com.riozenc.titanTool.spring.context.SpringContextHolder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class ResendMessagesJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.isResendSms();
    }
}
