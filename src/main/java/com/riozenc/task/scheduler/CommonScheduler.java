package com.riozenc.task.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.riozenc.task.job.PunishMoneySchedulerJob;
import com.riozenc.task.job.ResendMessagesJob;
import com.riozenc.task.job.SendArrearageMessagesJob;
import com.riozenc.task.job.SendMessagesJob;
import com.riozenc.task.webapp.entity.MeterMoneyPenaltyConfigEntity;
import com.riozenc.titanTool.properties.Global;
import com.riozenc.titanTool.spring.web.client.TitanTemplate;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.*;
import java.util.Calendar;

/**
 * @author belov
 */
@Configuration
public class CommonScheduler {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private TitanTemplate titanTemplate;

    /**
     * 将多个任务随本项目启动而启动
     */
    public void run() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            cronSchedulerJob1(scheduler);
            cronSchedulerJob2(scheduler);
            cronSchedulerJob3(scheduler);
            cronSchedulerJob4(scheduler);
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 电费发行短信服务
     * 每个月26日下午5点执行一次
     *
     * @param scheduler
     */
    public void cronSchedulerJob1(Scheduler scheduler) {
        JobDetail jobDetail = JobBuilder.newJob(SendMessagesJob.class)
                .withIdentity("sendMessages" + System.currentTimeMillis(),
                        "sendMessagesOfElectricityBillInfo").build();

        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                .cronSchedule(Global.getConfig("cronScheduleExpression1"));

        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("sendMessagesTrigger" + System.currentTimeMillis(),
                        "sendMessagesTriggerGroup" + System.currentTimeMillis())
                .withSchedule(cronScheduleBuilder).build();
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 短信备发服务
     * 每月7，14，26号9:20和10:20各执行一次
     *
     * @param scheduler
     */
    public void cronSchedulerJob2(Scheduler scheduler) {
        JobDetail jobDetail = JobBuilder.newJob(ResendMessagesJob.class)
                .withIdentity("resendMessages" + System.currentTimeMillis(),
                        "resendMessages").build();
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                .cronSchedule(Global.getConfig("cronScheduleExpression2"));
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("resendMessagesTrigger" + System.currentTimeMillis(),
                        "resendMessagesTriggerGroup" + System.currentTimeMillis())
                .withSchedule(cronScheduleBuilder)
                .build();
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送欠费短信
     * 每月7号和14号8:10各执行一次
     *
     * @param scheduler
     */
    public void cronSchedulerJob3(Scheduler scheduler) {
        JobDetail jobDetail = JobBuilder.newJob(SendArrearageMessagesJob.class)
                .withIdentity("sendArrearageMessages" + System.currentTimeMillis(),
                        "sendArrearageMessages").build();
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                .cronSchedule(Global.getConfig("cronScheduleExpression3"));
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("sendArrearageMessagesTrigger" + System.currentTimeMillis(),
                        "sendArrearageMessagesTriggerGroup" + System.currentTimeMillis())
                .withSchedule(cronScheduleBuilder).build();
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行违约金计算定时任务
     * 每天凌晨2点执行一次
     *
     * @param scheduler
     */
    public void cronSchedulerJob4(Scheduler scheduler) {
        // 获取当前时间
        // 与数据库中的设置的时间进行匹配
        // 有则执行，无则结束
        Integer meterMoneyPenaltyConfigsSize = findMeterMoneyPenaltyConfigsSize();
        System.out.println("长度"+meterMoneyPenaltyConfigsSize);
        if (meterMoneyPenaltyConfigsSize <= 0) {
            JobDetail jobDetail = JobBuilder.newJob(PunishMoneySchedulerJob.class)
                    .withIdentity("meterMoneyPenalty" + System.currentTimeMillis(),
                            "meterMoneyPenalty").build();
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                    .cronSchedule(Global.getConfig("cronScheduleExpression4"));
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("meterMoneyPenaltyTrigger" + System.currentTimeMillis(),
                            "meterMoneyPenaltyTriggerGroup" + System.currentTimeMillis())
                    .withSchedule(cronScheduleBuilder).build();
            try {
                scheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer findMeterMoneyPenaltyConfigsSize() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        /*java.sql.Date dateTime = new java.sql.Date(System.currentTimeMillis());*/
        Date date=new Date();
        Integer year = Integer.parseInt(String.valueOf(calendar.get(Calendar.YEAR)));
        MeterMoneyPenaltyConfigEntity meterMoneyPenaltyConfigEntity = new MeterMoneyPenaltyConfigEntity();
        meterMoneyPenaltyConfigEntity.setYear(year);
        meterMoneyPenaltyConfigEntity.setTime(date);
        System.out.println("dateTime====="+date);
        List<MeterMoneyPenaltyConfigEntity> meterMoneyPenaltyConfigList;
        Map<String, Object> params = new HashMap<>();
        params.put("meterMoneyPenaltyConfig", meterMoneyPenaltyConfigEntity);
        try {
            meterMoneyPenaltyConfigList = this.titanTemplate.postJson("BILLING-SERVER",
                    "billingServer/MeterMoneyPenaltyConfig/findMeterMoneyPenaltyConfigsByWhere", new HttpHeaders(), params,
                    new TypeReference<List<MeterMoneyPenaltyConfigEntity>>() {
                    });
            return meterMoneyPenaltyConfigList.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
