package com.riozenc.task.webapp.dao;

import com.riozenc.task.webapp.entity.SmsBackup;
import com.riozenc.titanTool.annotation.TransactionDAO;
import com.riozenc.titanTool.spring.webapp.dao.AbstractTransactionDAOSupport;
import org.apache.ibatis.session.ExecutorType;

import java.util.List;

@TransactionDAO
public class SmsBackupDao extends AbstractTransactionDAOSupport {

    public int insert(SmsBackup smsBackup) {
        return getPersistanceManager().insert(getNamespace() +
                ".insert", smsBackup);
    }

    public int insertList(List<SmsBackup> smsBackups) {
        return getPersistanceManager(ExecutorType.BATCH).insertList(getNamespace() +
                ".insert", smsBackups);
    }

    public List<SmsBackup> findByWhere(SmsBackup smsBackup) {
        return getPersistanceManager().find(getNamespace()+".findByWhere", smsBackup);
    }

    public int delete(SmsBackup smsBackup) {
        return getPersistanceManager().delete(getNamespace()+".delete", smsBackup);
    }

    public int update(SmsBackup smsBackup) {
        return getPersistanceManager().update(getNamespace() + ".update", smsBackup);
    }
}
