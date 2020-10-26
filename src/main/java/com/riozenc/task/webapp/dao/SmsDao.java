package com.riozenc.task.webapp.dao;

import com.riozenc.task.webapp.entity.SmsContentInfo;
import com.riozenc.titanTool.annotation.TransactionDAO;
import com.riozenc.titanTool.spring.webapp.dao.AbstractTransactionDAOSupport;

import java.util.List;

@TransactionDAO
public class SmsDao extends AbstractTransactionDAOSupport {

    public List<SmsContentInfo> findByWhere(SmsContentInfo smsContentInfo) {
       return getPersistanceManager().find(getNamespace()+".findByWhere", smsContentInfo);
    }

}
