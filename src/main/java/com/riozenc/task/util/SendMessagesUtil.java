package com.riozenc.task.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riozenc.task.webapp.dto.Message;
import com.riozenc.task.webapp.entity.ReturnedMessages;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * 短信发送工具类
 */
@Component
public class SendMessagesUtil {


    public static boolean isSendSuccess(List<Message> messages) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonList = null;
        try {
            jsonList = objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println(jsonList);
        HttpEntity<String> formEntity = new HttpEntity<>(jsonList, headers);
        ResponseEntity<String> stringResponseEntity = restTemplate
                .postForEntity("http://966068.cn/xk4j/system/gxgdsms.do", formEntity,
                        String.class);
        String body = stringResponseEntity.getBody();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ReturnedMessages returnedMessages = null;
        try {
            returnedMessages = objectMapper.readValue(stringResponseEntity.getBody(),
                    ReturnedMessages.class);
            return returnedMessages.getSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
