package com.riozenc.task.job;

import com.riozenc.task.webapp.service.impl.SmsServiceImpl;
import com.riozenc.titanTool.spring.context.SpringContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@DisallowConcurrentExecution
public class SendArrearageMessagesJob implements Job {
    private Log logger = LogFactory.getLog(SendArrearageMessagesJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("短信---欠费短信（发行）开始===="+new Date());

        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.isSendArrearageMessages("欠费短信（违约金起算日期前）");
        logger.debug("短信---欠费短信（发行）结束===="+new Date());

    }
}
