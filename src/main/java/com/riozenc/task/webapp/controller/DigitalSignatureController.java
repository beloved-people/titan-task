//package com.riozenc.task.webapp.controller;
//
//import com.epoint.sso.client.util.HttpClientUtil;
//import com.isoftstone.sign.SignGeneration;
//import com.riozenc.task.webapp.domain.DigitalSignatureDomain;
//import com.riozenc.titanTool.common.json.utils.GsonUtils;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author belov
// * 数字签名
// */
//
//@Controller
//@RequestMapping("/digitalSignature")
//public class DigitalSignatureController {
//
//    @PostMapping("/sign")
//    @ResponseBody
//    public String sign(@RequestBody String digitalSignature) {
//        DigitalSignatureDomain digitalSignatureDomain = GsonUtils.readValue(digitalSignature, DigitalSignatureDomain.class);
//        try {
//            /**
//             * method  字符串(string)  是  method =get、post
//             * serviceId  字符串(string)  是  您申请的数据服务标识-ff8080816c6486e9016ce18f78fd0540
//             * ak  字符串(string)  是  数据服务颁发给用户的调用接口的会话key（Access Key Id）
//             * appId  字符串(string)  是  服务调用者唯一标识
//             * sign  字符串(string)  是  数字签名结果串
//             * timestamp  时间戳(timestamp)  是  时间戳，格式为：yyyyMMddHHmmss（有效时间为5分钟）
//             * pageIndex  数字(int)  是  当前页码，默认值1；系统将根据用户输入的页码返回该页的数据
//             * pageSize  数字(int)  是  每页展现数据的条数，默认15条，单页最大50条；系统将根据用户指定的每页显示条数返回搜索数据
//             */
//            Map<String, Object> param = new HashMap<>();
//            param.put("method", "post");
//            param.put("serviceId", "bf19f77708034c6ca2b709ce76b4282e");
//            param.put("ak", "803e5d5d40ac46c6969abba67e53d26e");
//            param.put("appId", "8a90fb8e5f001064015f003e21099886");
//            Date date = new Date();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//            String timestamp = simpleDateFormat.format(date);
//            param.put("timestamp", timestamp);
//            Map<String, Object> currentMap = new HashMap<>();
//            currentMap.put("ZLHTBAH", digitalSignatureDomain.getZLHTBAH());
//            currentMap.put("ZKSFZH", digitalSignatureDomain.getZKSFZH());
//            currentMap.put("key", "ed7ed49d42e0c2f2746c85db396b19b9");
//            param.put("otherParams", SignGeneration.map2Json(currentMap));
//            String sign = SignGeneration.generationSign(param, "a2cdb8c444b440fba9b9a797bc47a912");
//            System.out.println(sign);
//            param.put("sign", sign);
//            param.put("pageIndex", "1");
//            param.put("pageSize", "50");
//            param.remove("otherParams");
//            String post = post(param);
//            return post;
//        } catch (Exception var4) {
//            var4.printStackTrace();
//        }
//        return "";
//    }
//
//    public String post(Map<String, Object> param) {
//        String url = "http://10.224.134.4:8081/irsp/openApi/getData/v1";
//        String post = HttpClientUtil.post(url, param);
//        System.out.println(post);
//        return post;
//    }
//}
