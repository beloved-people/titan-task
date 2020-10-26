package com.riozenc.task.job;

import com.riozenc.task.webapp.service.impl.SmsServiceImpl;
import com.riozenc.titanTool.spring.context.SpringContextHolder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class SendMessagesJob implements Job {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.isSendSms("电费信息（发行）");
        System.out.println("任务开始执行");
    }
}
