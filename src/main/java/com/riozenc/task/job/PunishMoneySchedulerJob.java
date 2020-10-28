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

/**
 * @author belov
 */
@Component
@DisallowConcurrentExecution
public class PunishMoneySchedulerJob implements Job {
    private Log logger = LogFactory.getLog(PunishMoneySchedulerJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        logger.debug("违约金计算开始===="+new Date());

        SmsServiceImpl smsServiceImpl = SpringContextHolder.getBean("smsServiceImpl");
        smsServiceImpl.punishMoneySchedulerJobService();
        logger.debug("违约金计算结束===="+new Date());

    }
}
