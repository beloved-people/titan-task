package com.riozenc.task.webapp.service;

import com.riozenc.task.webapp.entity.SmsBackup;

import java.util.List;

public interface ISmsBackupService {
    void batchBackup(List<SmsBackup> smsBackups);

    void updateBackup(SmsBackup smsBackup);

    int update(SmsBackup smsBackup);
}
