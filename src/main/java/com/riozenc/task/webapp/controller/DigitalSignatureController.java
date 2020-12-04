package com.riozenc.task.webapp.controller;

import com.isoftstone.sign.SignGeneration;
import com.riozenc.task.webapp.domain.DigitalSignatureDomain;
import com.riozenc.titanTool.common.json.utils.GsonUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author belov
 * 数字签名
 */

@Controller
@RequestMapping("/digitalSignature")
public class DigitalSignatureController {

    @PostMapping("/sign")
    @ResponseBody
    public String sign(@RequestBody String digitalSignature) {
        DigitalSignatureDomain digitalSignatureDomain = GsonUtils.readValue(digitalSignature, DigitalSignatureDomain.class);
        try {
            Map<String, String> param = new HashMap<>();
            param.put("serviceId", "bf19f77708034c6ca2b709ce76b4282e");
            param.put("ak", "803e5d5d40ac46c6969abba67e53d26e");
            param.put("appId", "8a90fb8e5f001064015f003e21099886");
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = simpleDateFormat.format(date);
            param.put("timestamp", timestamp);
            param.put("method", "post");
            Map<String, Object> currentMap = new HashMap<>();
            currentMap.put("SFZMHM", digitalSignatureDomain.getSFZMHM());
            currentMap.put("ZLHTBAH", digitalSignatureDomain.getZLHTBAH());
            currentMap.put("key", "ed7ed49d42e0c2f2746c85db396b19b9");
            param.put("otherParams", SignGeneration.map2Json(currentMap));
            String sign = SignGeneration.generationSign(param, "a2cdb8c444b440fba9b9a797bc47a912");
            System.out.println(sign);
            return sign;
        } catch (Exception var4) {
            var4.printStackTrace();
        }
        return "";
    }
}
