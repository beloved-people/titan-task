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
public class SendMessagesJob implements Job {

    private Log logger = LogFactory.getLog(SendMessagesJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        logger.debug("短信---电费信息（发行）开始===="+new Date());
        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.isSendSms("电费信息（发行）");
        logger.debug("短信---电费信息（发行）结束===="+new Date());

    }
}
