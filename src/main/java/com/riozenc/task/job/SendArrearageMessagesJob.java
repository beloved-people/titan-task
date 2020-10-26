package com.riozenc.task.job;

import com.riozenc.task.webapp.service.impl.SmsServiceImpl;
import com.riozenc.titanTool.spring.context.SpringContextHolder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class SendArrearageMessagesJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.isSendArrearageMessages("欠费短信（违约金起算日期前）");
        System.out.println("任务");
    }
}
