package com.riozenc.task.runner;

import com.riozenc.task.scheduler.CommonScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyStartupRunner implements CommandLineRunner {

    @Autowired
    public CommonScheduler commonScheduler;

    @Override
    public void run(String... args) throws Exception {
        commonScheduler.run();
    }
}
