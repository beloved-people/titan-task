package com.riozenc.task.webapp.controller;

import com.riozenc.task.job.ResendMessagesJob;
import com.riozenc.task.job.SendMessagesJob;
import com.riozenc.task.scheduler.CommonScheduler;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangShun
 */
@RestController
@RequestMapping("/Sms")
public class SmsController {

    @Autowired
    private SendMessagesJob sendMessagesJob;
    @Autowired
    private ResendMessagesJob resendMessagesJob;

    @PostMapping("/issuingElectricityBillInfo")
    public void issuingElectricityBillInfo() throws SchedulerException {
        CommonScheduler commonScheduler = new CommonScheduler();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        commonScheduler.cronSchedulerJob1(scheduler);
        scheduler.start();
    }

    @PostMapping("/resendMessages")
    public void resendMessages() throws SchedulerException {
        CommonScheduler commonScheduler = new CommonScheduler();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        commonScheduler.cronSchedulerJob2(scheduler);
        scheduler.start();
    }

    @PostMapping("/sendArrearageMessages")
    public void sendArrearageMessages() throws SchedulerException {
        CommonScheduler commonScheduler = new CommonScheduler();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        commonScheduler.cronSchedulerJob3(scheduler);
        scheduler.start();
    }
}
