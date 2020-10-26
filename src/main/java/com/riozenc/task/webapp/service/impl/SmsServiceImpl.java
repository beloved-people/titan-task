package com.riozenc.task.webapp.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.riozenc.task.util.MonUtils;
import com.riozenc.task.util.RegexValidateUtil;
import com.riozenc.task.util.SendMessagesUtil;
import com.riozenc.task.webapp.dao.SmsBackupDao;
import com.riozenc.task.webapp.dao.SmsDao;
import com.riozenc.task.webapp.domain.ArrearageDomain;
import com.riozenc.task.webapp.dto.Message;
import com.riozenc.task.webapp.dto.TextMessageDto;
import com.riozenc.task.webapp.entity.SmsBackup;
import com.riozenc.task.webapp.entity.SmsContentInfo;
import com.riozenc.task.webapp.service.ISmsBackupService;
import com.riozenc.task.webapp.service.ISmsService;
import com.riozenc.titanTool.annotation.TransactionDAO;
import com.riozenc.titanTool.annotation.TransactionService;
import com.riozenc.titanTool.spring.web.client.TitanTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author belov
 */
@TransactionService
public class SmsServiceImpl implements ISmsService {

    @Autowired
    private TitanTemplate titanTemplate;

    @TransactionDAO
    private SmsDao smsDAO;

    @TransactionDAO
    private SmsBackupDao smsBackupDao;

    @Autowired
    @Qualifier("smsBackupServiceImpl")
    private ISmsBackupService smsBackupService;

    /**
     * 电费发行短信服务
     *
     * @param smsTitle 短信模板标题
     */
    @Override
    public void isSendSms(String smsTitle) {
        //1. 根据标题查询短信模板
        SmsContentInfo smsContentInfo = new SmsContentInfo();
        smsContentInfo.setSmsTitle(smsTitle);
        List<SmsContentInfo> smsContentInfos = smsDAO.findByWhere(smsContentInfo);
        if (smsContentInfos == null || smsContentInfos.size() < 1) {
            throw new RuntimeException("数据库没有短信模板信息表," +
                    "请检查相关配置表 SMS_CONTENT_INFO");
        }
        String smsContentTemplate = smsContentInfos.get(0).getSmsContentTemplate();

        //2. 根据当前月份、已欠费、未发送查询欠费信息
        String mon = MonUtils.getMon();
        ArrearageDomain arrearageDomain = new ArrearageDomain();
        arrearageDomain.setMon(Integer.parseInt(mon));
        arrearageDomain.setIsSettle(0);
        arrearageDomain.setIsSend(false);
        Map<Long, List<ArrearageDomain>> arrearageDomainsCollect = findByIsSettleMonAndIsSend(arrearageDomain);

        if (arrearageDomainsCollect.size() == 0) {
            //TODO 日志
            throw new RuntimeException("发送时间:" + new Date() + "," +
                    "cronScheduleExpression1 任务无要发送的短信");
        }

        //3. 生成短信、备份信息
        List<Message> messages = new ArrayList<>();
        List<TextMessageDto> textMessages = new ArrayList<>();
        List<SmsBackup> smsBackups = new ArrayList<>();
        List<SmsBackup> irregularPhoneSmsBackups = new ArrayList<>();

        //按结算户遍历处理数据 汇总数据
        for (Map.Entry<Long, List<ArrearageDomain>> arrearageDomainKey : arrearageDomainsCollect.entrySet()) {

            Long key = arrearageDomainKey.getKey();
            List<ArrearageDomain> entryArrearageDomainList = arrearageDomainKey.getValue();
            //欠费
            BigDecimal totalOweMoney = entryArrearageDomainList.stream().filter(e -> e.getOweMoney() != null)
                    .map(ArrearageDomain::getOweMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //电费
            BigDecimal totalReceivable = entryArrearageDomainList.stream().filter(e -> e.getReceivable() != null)
                    .map(ArrearageDomain::getReceivable)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //电量
            BigDecimal totalPower = entryArrearageDomainList.stream().filter(e -> e.getTotalPower() != null)
                    .map(ArrearageDomain::getTotalPower)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //发行时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String createDate = sdf.format(entryArrearageDomainList.get(0).getCreateDate());

            //短信信息
            Object[] placeholders = new Object[]{entryArrearageDomainList.get(0).getSettlementName(),
                    entryArrearageDomainList.get(0).getSettlementNo(),
                    entryArrearageDomainList.get(0).getMon().toString().substring(0, 4),
                    entryArrearageDomainList.get(0).getMon().toString().substring(4, 6),
                    totalPower.setScale(0, RoundingMode.HALF_UP) + "",
                    totalReceivable.setScale(2, RoundingMode.HALF_UP) + "",
                    totalOweMoney.setScale(2, RoundingMode.HALF_UP) + "",
                    createDate.substring(0, 4), createDate.substring(5, 7),
                    createDate.substring(8, 10), createDate.substring(11, 13)};
            String msg = MessageFormat.format(smsContentTemplate, placeholders);

            //备份
            SmsBackup smsBackup = new SmsBackup();
            smsBackup.setSettlementId(key);
            smsBackup.setContent(msg);
            smsBackup.setContentTitle(smsTitle);
            smsBackup.setMon(Integer.parseInt(mon));
            smsBackup.setIsSend(false);
            smsBackup.setSuccess(false);

            if (entryArrearageDomainList.get(0).getSettlementPhone() != null
                    && RegexValidateUtil.checkMobileNumber(entryArrearageDomainList.get(0).getSettlementPhone())) {
                smsBackup.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                smsBackup.setReason("");
                TextMessageDto textMessageDto = new TextMessageDto();
                textMessageDto.setSettlementId(key);
                textMessageDto.setContent(msg);
                textMessageDto.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                textMessageDto.setContentTitle(smsTitle);
                textMessages.add(textMessageDto);
                Message message = new Message();
                message.setContent(msg);
                message.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                messages.add(message);
                smsBackups.add(smsBackup);
            } else {
                smsBackup.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                smsBackup.setReason("电话号码错误或者为空");
                irregularPhoneSmsBackups.add(smsBackup);
            }
        }
        // 4.对号码不正确的信息进行备份
        this.smsBackupService.batchBackup(irregularPhoneSmsBackups);

        // 5.按照电话号码对短信对象集合分组
        Map<String, List<Message>> messagesCollect =
                messages.stream().filter(t -> t.getMobile() != null).collect(Collectors.groupingBy(Message::getMobile));
        //多个结算户 同一个电话号
        List<Message> duplicateMessages = new ArrayList<>();
        //一个计算户 一个电话号
        List<Message> uniqueMessages = new ArrayList<>();
        for (Map.Entry<String, List<Message>> messageKey : messagesCollect.entrySet()) {
            List<Message> messagesGroup = messageKey.getValue();
            if (messagesGroup.size() > 1) {
                duplicateMessages.addAll(messagesGroup);
            } else {
                uniqueMessages.addAll(messagesGroup);
            }
        }
        // 6.按照电话号码对短信备份对象集合分组
        Map<String, List<SmsBackup>> smsBackupCollect = smsBackups.stream().filter(t -> t.getMobile() != null).collect(Collectors.groupingBy(SmsBackup::getMobile));
        List<SmsBackup> duplicateSmsBackups = new ArrayList<>();
        List<SmsBackup> uniqueSmsBackups = new ArrayList<>();
        for (Map.Entry<String, List<SmsBackup>> smsBackupKey : smsBackupCollect.entrySet()) {
            List<SmsBackup> smsBackupGroup = smsBackupKey.getValue();
            if (smsBackupGroup.size() > 1) {
                duplicateSmsBackups.addAll(smsBackupGroup);
            } else {
                uniqueSmsBackups.addAll(smsBackupGroup);
            }
        }

        // 7.按照电话号码对短信传输对象集合分组
        Map<String, List<TextMessageDto>> textMessageDTOCollect = textMessages.stream().filter(t -> t.getMobile() != null).collect(Collectors.groupingBy(TextMessageDto::getMobile));
        List<TextMessageDto> duplicateTextMessageDTOs = new ArrayList<>();
        List<TextMessageDto> uniqueTextMessageDTOs = new ArrayList<>();
        for (Map.Entry<String, List<TextMessageDto>> textMessageDTOKey : textMessageDTOCollect.entrySet()) {
            List<TextMessageDto> textMessageDtoGroup = textMessageDTOKey.getValue();
            if (textMessageDtoGroup.size() > 1) {
                duplicateTextMessageDTOs.addAll(textMessageDtoGroup);
            } else {
                uniqueTextMessageDTOs.addAll(textMessageDtoGroup);
            }
        }

        // 8.对不重复电话号码的短信进行先备份再发送后修改
        if (uniqueMessages.size() != 0) {
            // 批量备份不重复号码的短信
            this.smsBackupService.batchBackup(uniqueSmsBackups);
            // 每隔2000条进行发送
            int messagesSize = uniqueMessages.size();
            for (int m = 0; m < messagesSize / 1999 + 1; m++) {

                List<Message> newMessagesList = uniqueMessages.subList(m * 1999,
                        (m + 1) * 1999 > messagesSize ? messagesSize : (m + 1) * 1999);

                // 发送短信接口
                boolean sendSuccess = SendMessagesUtil.isSendSuccess(newMessagesList);

                // 欠费表中IS_SEND=1（表示短信已经发送）
                List<TextMessageDto> newList = uniqueTextMessageDTOs.subList(m * 1999,
                        (m + 1) * 1999 > messagesSize ? messagesSize : (m + 1) * 1999);

                List<Long> settlementIds =
                        newList.stream().filter(t -> t.getSettlementId() != null)
                                .map(TextMessageDto::getSettlementId).distinct()
                                .collect(Collectors.toList());

                if (settlementIds.size() == 0) {
                    continue;
                }
                ArrearageDomain arrearage = new ArrearageDomain();
                arrearage.setMon(Integer.parseInt(mon));
                arrearage.setSettlementIds(settlementIds);
                updateSendBySettlementIdsAndMon(arrearage);
                // 对备份库中短信的字段进行修改
                SmsBackup smsBackup = new SmsBackup();
                smsBackup.setSuccess(sendSuccess);
                smsBackup.setSettlementIds(settlementIds);
                smsBackup.setContentTitle(smsTitle);
                this.smsBackupService.updateBackup(smsBackup);
            }
        }
        // 9.对重复电话号码的短信进行先备份再发送后修改
        if (duplicateMessages.size() != 0) {
            List<Long> successSettIds = new ArrayList<>();
            List<Long> errorSettIds = new ArrayList<>();
            this.smsBackupService.batchBackup(duplicateSmsBackups);
            // 单条发送
            int messagesSize = duplicateMessages.size();
            for (int i = 0; i < messagesSize; i++) {
                List<Message> newMessagesList = new ArrayList<>();
                newMessagesList.add(duplicateMessages.get(i));
                boolean sendSuccess = SendMessagesUtil.isSendSuccess(newMessagesList);
                List<TextMessageDto> newTextMessageDtoList = new ArrayList<>();
                newTextMessageDtoList.add(duplicateTextMessageDTOs.get(i));
                List<Long> settlementIds = newTextMessageDtoList.stream().filter(t -> t.getSettlementId() != null)
                        .map(TextMessageDto::getSettlementId).distinct()
                        .collect(Collectors.toList());
                if (settlementIds.size() == 0) {
                    continue;
                }

                if(sendSuccess==true){
                    successSettIds.addAll(settlementIds);
                }else{
                    errorSettIds.addAll(settlementIds);
                }
            }
            //批量更新数据
            ArrearageDomain arrearage = new ArrearageDomain();
            arrearage.setMon(Integer.parseInt(mon));
            arrearage.setSettlementIds(successSettIds);
            updateSendBySettlementIdsAndMon(arrearage);


            SmsBackup successSmsBackup = new SmsBackup();
            successSmsBackup.setSuccess(true);
            successSmsBackup.setMon(Integer.parseInt(mon));
            successSmsBackup.setSettlementIds(successSettIds);
            successSmsBackup.setContentTitle(smsTitle);
            this.smsBackupService.updateBackup(successSmsBackup);


            SmsBackup errorSmsBackup = new SmsBackup();
            errorSmsBackup.setSuccess(false);
            errorSmsBackup.setMon(Integer.parseInt(mon));
            errorSmsBackup.setSettlementIds(errorSettIds);
            errorSmsBackup.setContentTitle(smsTitle);
            this.smsBackupService.updateBackup(errorSmsBackup);
        }
    }

    /**
     * 短信备发服务
     */
    @Override
    public void isResendSms() {
        SmsBackup smsBackup = new SmsBackup();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String mon = MonUtils.getMon();
        smsBackup.setMon(Integer.parseInt(mon));
        smsBackup.setIsSend(false);
        smsBackup.setSuccess(false);
        if (day == 27) {
            smsBackup.setContentTitle("电费信息（发行）");
        }
        if (day == 8 || day == 15) {
            smsBackup.setContentTitle("欠费短信（违约金起算日期前）");
        }
        List<SmsBackup> smsBackups = smsBackupDao.findByWhere(smsBackup);

        if (smsBackups.size() != 0) {
            List<TextMessageDto> textMessages = new ArrayList<>();
            List<Message> messages = new ArrayList<>();
            //过滤手机号不正确的
            smsBackups= smsBackups.stream().filter(s -> s.getSettlementId() != null && s.getMobile() != null
                    && RegexValidateUtil.checkMobileNumber(s.getMobile())).collect(Collectors.toList());
            smsBackups.stream().forEach(s -> {
                TextMessageDto textMessageDto = new TextMessageDto();
                textMessageDto.setSettlementId(s.getSettlementId());
                textMessageDto.setContent(s.getContent());
                textMessageDto.setMobile(s.getMobile());
                Message message = new Message();
                message.setMobile(s.getMobile());
                message.setContent(s.getContent());
                messages.add(message);
                textMessages.add(textMessageDto);
            });

            // 5.按照电话号码对短信对象集合分组
            Map<String, List<Message>> messagesCollect =
                    messages.stream().filter(t -> t.getMobile() != null).collect(Collectors.groupingBy(Message::getMobile));
            //多个结算户 同一个电话号
            List<Message> duplicateMessages = new ArrayList<>();
            //一个计算户 一个电话号
            List<Message> uniqueMessages = new ArrayList<>();
            for (Map.Entry<String, List<Message>> messageKey : messagesCollect.entrySet()) {
                List<Message> messagesGroup = messageKey.getValue();
                if (messagesGroup.size() > 1) {
                    duplicateMessages.addAll(messagesGroup);
                } else {
                    uniqueMessages.addAll(messagesGroup);
                }
            }
            // 6.按照电话号码对短信备份对象集合分组
            Map<String, List<SmsBackup>> smsBackupCollect = smsBackups.stream().filter(t -> t.getMobile() != null).collect(Collectors.groupingBy(SmsBackup::getMobile));
            List<SmsBackup> duplicateSmsBackups = new ArrayList<>();
            List<SmsBackup> uniqueSmsBackups = new ArrayList<>();
            for (Map.Entry<String, List<SmsBackup>> smsBackupKey : smsBackupCollect.entrySet()) {
                List<SmsBackup> smsBackupGroup = smsBackupKey.getValue();
                if (smsBackupGroup.size() > 1) {
                    duplicateSmsBackups.addAll(smsBackupGroup);
                } else {
                    uniqueSmsBackups.addAll(smsBackupGroup);
                }
            }

            // 7.按照电话号码对短信传输对象集合分组
            Map<String, List<TextMessageDto>> textMessageDTOCollect = textMessages.stream().filter(t -> t.getMobile() != null).collect(Collectors.groupingBy(TextMessageDto::getMobile));
            List<TextMessageDto> duplicateTextMessageDTOs = new ArrayList<>();
            List<TextMessageDto> uniqueTextMessageDTOs = new ArrayList<>();
            for (Map.Entry<String, List<TextMessageDto>> textMessageDTOKey : textMessageDTOCollect.entrySet()) {
                List<TextMessageDto> textMessageDtoGroup = textMessageDTOKey.getValue();
                if (textMessageDtoGroup.size() > 1) {
                    duplicateTextMessageDTOs.addAll(textMessageDtoGroup);
                } else {
                    uniqueTextMessageDTOs.addAll(textMessageDtoGroup);
                }
            }



            if (uniqueMessages.size() != 0) {
                // 每隔2000条进行发送
                int messagesSize = uniqueMessages.size();
                for (int m = 0; m < messagesSize / 1999 + 1; m++) {

                    List<Message> newMessagesList = uniqueMessages.subList(m * 1999,
                            (m + 1) * 1999 > messagesSize ? messagesSize : (m + 1) * 1999);

                    // 发送短信接口
                    boolean sendSuccess = SendMessagesUtil.isSendSuccess(newMessagesList);

                    // 欠费表中IS_SEND=1（表示短信已经发送）
                    List<TextMessageDto> newList = uniqueTextMessageDTOs.subList(m * 1999,
                            (m + 1) * 1999 > messagesSize ? messagesSize : (m + 1) * 1999);

                    List<Long> settlementIds =
                            newList.stream().filter(t -> t.getSettlementId() != null)
                                    .map(TextMessageDto::getSettlementId).distinct()
                                    .collect(Collectors.toList());

                    if (settlementIds.size() == 0) {
                        continue;
                    }
                    ArrearageDomain arrearage = new ArrearageDomain();
                    arrearage.setMon(Integer.parseInt(mon));
                    arrearage.setSettlementIds(settlementIds);
                    updateSendBySettlementIdsAndMon(arrearage);
                    // 对备份库中短信的字段进行修改
                    SmsBackup paramSmsBackup = new SmsBackup();
                    paramSmsBackup.setSuccess(sendSuccess);
                    paramSmsBackup.setSettlementIds(settlementIds);
                    paramSmsBackup.setContentTitle(smsBackup.getContentTitle());
                    this.smsBackupService.updateBackup(paramSmsBackup);
                }
            }
            // 9.对重复电话号码的短信进行先备份再发送后修改
            if (duplicateMessages.size() != 0) {
                List<Long> successSettIds = new ArrayList<>();
                List<Long> errorSettIds = new ArrayList<>();
                // 单条发送
                int messagesSize = duplicateMessages.size();
                for (int i = 0; i < messagesSize; i++) {
                    List<Message> newMessagesList = new ArrayList<>();
                    newMessagesList.add(duplicateMessages.get(i));
                    boolean sendSuccess = SendMessagesUtil.isSendSuccess(newMessagesList);
                    List<TextMessageDto> newTextMessageDtoList = new ArrayList<>();
                    newTextMessageDtoList.add(duplicateTextMessageDTOs.get(i));
                    List<Long> settlementIds = newTextMessageDtoList.stream().filter(t -> t.getSettlementId() != null)
                            .map(TextMessageDto::getSettlementId).distinct()
                            .collect(Collectors.toList());
                    if (settlementIds.size() == 0) {
                        continue;
                    }

                    if(sendSuccess==true){
                        successSettIds.addAll(settlementIds);
                    }else{
                        errorSettIds.addAll(settlementIds);
                    }
                }
                //批量更新数据
                ArrearageDomain arrearage = new ArrearageDomain();
                arrearage.setMon(Integer.parseInt(mon));
                arrearage.setSettlementIds(successSettIds);
                updateSendBySettlementIdsAndMon(arrearage);


                SmsBackup successSmsBackup = new SmsBackup();
                successSmsBackup.setSuccess(true);
                successSmsBackup.setMon(Integer.parseInt(mon));
                successSmsBackup.setSettlementIds(successSettIds);
                successSmsBackup.setContentTitle(smsBackup.getContentTitle());
                this.smsBackupService.updateBackup(successSmsBackup);


                SmsBackup errorSmsBackup = new SmsBackup();
                errorSmsBackup.setSuccess(false);
                errorSmsBackup.setMon(Integer.parseInt(mon));
                errorSmsBackup.setSettlementIds(errorSettIds);
                errorSmsBackup.setContentTitle(smsBackup.getContentTitle());
                this.smsBackupService.updateBackup(errorSmsBackup);
            }
        }

    }

    /**
     * 发送欠费短信
     *
     * @param smsTitle 短信模板标题
     */
    @Override
    public void isSendArrearageMessages(String smsTitle) {
        SmsContentInfo smsContentInfo = new SmsContentInfo();
        smsContentInfo.setSmsTitle(smsTitle);
        List<SmsContentInfo> smsContentInfos = smsDAO.findByWhere(smsContentInfo);
        if (smsContentInfos == null || smsContentInfos.size() < 1) {
            throw new RuntimeException("数据库没有短信模板信息表," +
                    "请检查相关配置表 SMS_CONTENT_INFO");
        }
        String smsContentTemplate = smsContentInfos.get(0).getSmsContentTemplate();
        ArrearageDomain arrearageDomain = new ArrearageDomain();
        String mon = MonUtils.getMon();
        String lastMon = MonUtils.getLastMon(mon);
        arrearageDomain.setMon(Integer.parseInt(lastMon));
        arrearageDomain.setIsSettle(0);
        arrearageDomain.setIsSend(true);
        Map<Long, List<ArrearageDomain>> arrearageDomainsCollect =
                findByIsSettleMonAndIsSend(arrearageDomain);
        List<Message> messages = new ArrayList<>();
        List<TextMessageDto> textMessages = new ArrayList<>();
        List<SmsBackup> smsBackups = new ArrayList<>();
        List<SmsBackup> irregularPhoneSmsBackups = new ArrayList<>();
        for (Map.Entry<Long, List<ArrearageDomain>> arrearageDomainKey : arrearageDomainsCollect.entrySet()) {
            Long key = arrearageDomainKey.getKey();
            List<ArrearageDomain> entryArrearageDomainList = arrearageDomainKey.getValue();
            BigDecimal totalOweMoney = entryArrearageDomainList.stream().filter(e -> e.getOweMoney() != null)
                    .map(ArrearageDomain::getOweMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Object[] placeholders = new Object[]{entryArrearageDomainList.get(0).getSettlementName(),
                    entryArrearageDomainList.get(0).getSettlementNo(),
                    mon.substring(4, 6), "0" + 3, totalOweMoney.setScale(2, RoundingMode.HALF_UP) + ""};
            String msg = MessageFormat.format(smsContentTemplate, placeholders);
            SmsBackup smsBackup = new SmsBackup();
            smsBackup.setSettlementId(key);
            smsBackup.setContent(msg);
            smsBackup.setContentTitle(smsTitle);
            smsBackup.setMon(Integer.parseInt(mon));
            smsBackup.setIsSend(false);
            smsBackup.setSuccess(false);
            if (entryArrearageDomainList.get(0).getSettlementPhone() != null
                    && RegexValidateUtil.checkMobileNumber(entryArrearageDomainList.get(0).getSettlementPhone())) {
                smsBackup.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                smsBackup.setReason("");
                TextMessageDto textMessageDto = new TextMessageDto();
                textMessageDto.setSettlementId(key);
                textMessageDto.setContent(msg);
                textMessageDto.setContentTitle(smsTitle);
                textMessageDto.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                textMessages.add(textMessageDto);
                Message message = new Message();
                message.setContent(msg);
                message.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                messages.add(message);
                smsBackups.add(smsBackup);
            } else {
                smsBackup.setMobile(entryArrearageDomainList.get(0).getSettlementPhone());
                smsBackup.setReason("电话号码错误或者为空");
                irregularPhoneSmsBackups.add(smsBackup);
            }
        }
        // 对号码不正确的信息进行备份
        this.smsBackupService.batchBackup(irregularPhoneSmsBackups);
        // 按照电话号码对短信对象集合分组
        Map<String, List<Message>> messagesCollect = messages.stream().collect(Collectors.groupingBy(Message::getMobile));
        List<Message> duplicateMessages = new ArrayList<>();
        List<Message> uniqueMessages = new ArrayList<>();
        for (Map.Entry<String, List<Message>> messageKey : messagesCollect.entrySet()) {
            List<Message> messagesGroup = messageKey.getValue();
            if (messagesGroup.size() > 1) {
                duplicateMessages.addAll(messagesGroup);
            } else {
                uniqueMessages.addAll(messagesGroup);
            }
        }
        // 按照电话号码对短信备份对象集合分组
        Map<String, List<SmsBackup>> smsBackupCollect = smsBackups.stream().collect(Collectors.groupingBy(SmsBackup::getMobile));
        List<SmsBackup> duplicateSmsBackups = new ArrayList<>();
        List<SmsBackup> uniqueSmsBackups = new ArrayList<>();
        for (Map.Entry<String, List<SmsBackup>> smsBackupKey : smsBackupCollect.entrySet()) {
            List<SmsBackup> smsBackupGroup = smsBackupKey.getValue();
            if (smsBackupGroup.size() > 1) {
                duplicateSmsBackups.addAll(smsBackupGroup);
            } else {
                uniqueSmsBackups.addAll(smsBackupGroup);
            }
        }
        // 按照电话号码对短信传输对象集合分组
        Map<String, List<TextMessageDto>> textMessageDTOCollect = textMessages.stream().collect(Collectors.groupingBy(TextMessageDto::getMobile));
        List<TextMessageDto> duplicateTextMessageDTOs = new ArrayList<>();
        List<TextMessageDto> uniqueTextMessageDTOs = new ArrayList<>();
        for (Map.Entry<String, List<TextMessageDto>> textMessageDTOKey : textMessageDTOCollect.entrySet()) {
            List<TextMessageDto> textMessageDtoGroup = textMessageDTOKey.getValue();
            if (textMessageDtoGroup.size() > 1) {
                duplicateTextMessageDTOs.addAll(textMessageDtoGroup);
            } else {
                uniqueTextMessageDTOs.addAll(textMessageDtoGroup);
            }
        }
//         备份并发送不重复的短信
        if (uniqueMessages.size() != 0) {
            int size = uniqueSmsBackups.size();
            this.smsBackupService.batchBackup(uniqueSmsBackups);
            int messagesSize = uniqueSmsBackups.size();
            for (int m = 0; m < messagesSize / 1999 + 1; m++) {
                List<Message> newMessagesList = uniqueMessages.subList(m * 1999,
                        (m + 1) * 1999 > size ? size : (m + 1) * 1999);
                boolean sendSuccess = SendMessagesUtil.isSendSuccess(newMessagesList);
                List<TextMessageDto> newList = uniqueTextMessageDTOs.subList(m * 1999,
                        (m + 1) * 1999 > size ? size : (m + 1) * 1999);
                List<Long> settlementIds =
                        newList.stream().filter(t -> t.getSettlementId() != null)
                                .map(TextMessageDto::getSettlementId).distinct()
                                .collect(Collectors.toList());
                if (settlementIds.size() == 0) {
                    continue;
                }
                SmsBackup smsBackup = new SmsBackup();
                smsBackup.setSuccess(sendSuccess);
                smsBackup.setSettlementIds(settlementIds);
                smsBackup.setContentTitle(smsTitle);
                this.smsBackupService.updateBackup(smsBackup);
            }
        }

        if (duplicateMessages.size() != 0) {
            this.smsBackupService.batchBackup(duplicateSmsBackups);
            int messagesSize = duplicateMessages.size();
            for (int i = 0; i < messagesSize; i++) {
                List<Message> newMessagesList = new ArrayList<>();
                newMessagesList.add(duplicateMessages.get(i));
                boolean sendSuccess = SendMessagesUtil.isSendSuccess(newMessagesList);
                List<TextMessageDto> newTextMessageDtoList = new ArrayList<>();
                newTextMessageDtoList.add(duplicateTextMessageDTOs.get(i));
                List<Long> settlementIds = newTextMessageDtoList.stream().filter(t -> t.getSettlementId() != null)
                        .map(TextMessageDto::getSettlementId).distinct()
                        .collect(Collectors.toList());
                if (settlementIds.size() == 0) {
                    continue;
                }
                SmsBackup smsBackup = new SmsBackup();
                smsBackup.setSuccess(sendSuccess);
                smsBackup.setSettlementIds(settlementIds);
                smsBackup.setContentTitle(smsTitle);
                this.smsBackupService.updateBackup(smsBackup);
            }
        }

    }

    @Override
    public void punishMoneySchedulerJobService() {
        generatePenaltyDataTask();
    }


    /**
     * 欠费查询，并按照结算户id分组
     *
     * @param arrearageDomain 查询欠费的条件
     * @return 欠费信息
     */
    public Map<Long, List<ArrearageDomain>> findByIsSettleMonAndIsSend(
            ArrearageDomain arrearageDomain) {
        List<ArrearageDomain> arrearageDomains = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("arrearage", arrearageDomain);
        try {
            arrearageDomains = this.titanTemplate.postJson("BILLING-SERVER",
                    "billingServer/arrearage/findByIsSettleMonAndIsSend", new HttpHeaders(), params,
                    new TypeReference<List<ArrearageDomain>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }

        Map<Long, List<ArrearageDomain>> arrearageMapBySettId =
                arrearageDomains.stream().filter(a -> a.getSettlementId() != null).collect(Collectors.groupingBy(ArrearageDomain::getSettlementId));

        return arrearageMapBySettId;
    }

    /**
     * 更新欠费表“是否发送”（IS_SEND）字段
     *
     * @param arrearageDomain 更新的内容：IS_SEND=1（表示短信已经发送），更新的条件：当前月份、结算户号
     */
    public void updateSendBySettlementIdsAndMon(ArrearageDomain arrearageDomain) {
        ArrearageDomain arrearage = new ArrearageDomain();
        arrearage.setMon(arrearageDomain.getMon());
        arrearage.setSettlementIds(arrearageDomain.getSettlementIds());
        Map<String, Object> params = new HashMap<>();
        params.put("arrearage", arrearage);
        Integer quantity;
        try {
            quantity = this.titanTemplate.postJson("BILLING-SERVER",
                    "billingServer/arrearage/updateSendBySettlementIdsAndMon", new HttpHeaders(), params,
                    new TypeReference<Integer>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 执行计算欠费信息
     */
    public void generatePenaltyDataTask() {
        try {
            this.titanTemplate.postJson("BILLING-SERVER",
                    "billingServer/moneyPenalty/generatePenaltyDataTask", new HttpHeaders(), null,
                    new TypeReference<Integer>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
