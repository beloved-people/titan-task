package com.riozenc.task.webapp.service.impl;

import com.riozenc.task.webapp.dao.SmsBackupDao;
import com.riozenc.task.webapp.entity.SmsBackup;
import com.riozenc.task.webapp.service.ISmsBackupService;
import com.riozenc.titanTool.annotation.TransactionDAO;
import com.riozenc.titanTool.annotation.TransactionService;

import java.util.List;

/**
 * @author belov
 */
@TransactionService
public class SmsBackupServiceImpl implements ISmsBackupService {
    @TransactionDAO
    private SmsBackupDao smsBackupDao;

    @Override
    public void batchBackup(List<SmsBackup> smsBackups) {
        if (smsBackups.size() != 0) {
            int size = smsBackups.size();
            for (int m = 0; m < size / 1999 + 1; m++) {
                List<SmsBackup> irregularPhoneSmsBackup = smsBackups.subList(m * 1999,
                        (m + 1) * 1999 > size ? size : (m + 1) * 1999);
                smsBackupDao.insertList(irregularPhoneSmsBackup);
            }
        }
    }

    @Override
    public void updateBackup(SmsBackup smsBackup) {
        if (smsBackup.getSuccess()) {
            smsBackup.setIsSend(true);
            smsBackup.setSuccess(true);
            smsBackup.setReason("发送成功！");
        } else {
            smsBackup.setIsSend(false);
            smsBackup.setSuccess(false);
            smsBackup.setReason("发送失败！");
        }
        smsBackupDao.update(smsBackup);
    }

    @Override
    public int update(SmsBackup smsBackup) {
        return smsBackupDao.update(smsBackup);
    }
}
