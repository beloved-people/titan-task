package com.riozenc.task.webapp.controller;

import com.epoint.sso.client.code.DES;
import com.epoint.sso.client.util.HttpClientUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riozenc.task.api.CallAPI;
import com.riozenc.task.util.PropertiesUtils;
import com.riozenc.task.util.StringUtil;
import com.riozenc.task.util.XmlUtils;
import com.riozenc.task.webapp.domain.*;
import com.riozenc.titanTool.common.json.utils.GsonUtils;
import com.riozenc.titanTool.spring.web.http.HttpResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author belov
 * 身份证验证查询
 */
@Controller
@RequestMapping("/iDVerificationQuery")
public class IDVerificationQueryController {


    @PostMapping("/postByID")
    @ResponseBody
    public HttpResult<?> postByID(@RequestBody String body) {
        //实际API地址,请修正为实际值
        String url = "http://59.211.219.71/share/makeIdCardInfo";
        IDCard idCard = GsonUtils.readValue(body, IDCard.class);
        //调用API的参数,请修正为实际值  body的值为身份证号
        Condition condition = new Condition();
        condition.setPosition("SFZMHM='" + idCard.getIdNumber()+"'");
        condition.setCurrentPage(1);
        condition.setPageSize(10);
        condition.setReturnFieldNameForm(0);
        condition.setUsername("");
        condition.setUserCitizenIDNumber(idCard.getIdNumber());
        condition.setUserCompany("");
        QueryConditions queryCondition = new QueryConditions();
        List<String> conditions = PropertiesUtils.convertObjToList(condition);
        queryCondition.setConditions(conditions);
        List<String> returnItems = new ArrayList<>();
        Row row = new Row();
        returnItems.add("SFZMHM");
        row.setReturnItems(returnItems);
        String param = "<Items>" + XmlUtils.objectToXml(queryCondition) + "<Item>" + XmlUtils.objectToXml(row) + "</Item></Items>";
        String newParam = StringUtils.replaceEach(StringUtil.replaceAllBlank(param), new String[]{"&lt;","&quot;","&apos;","&gt;"}, new String[]{"<","\"","'",">"});
        PostParams postParams = new PostParams();
        postParams.setIpadress("10.224.147.119");
        postParams.setReqstr(newParam);
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("params", postParams);
        ObjectMapper mapper = new ObjectMapper();
        String params = "";
        try {
            params = mapper.writeValueAsString(objectObjectHashMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //考虑调用凭证缓存化，一定时间后调用凭证肯定会过期，当凭证过期时，引入重试机制，即凭证需要强制更新，并重新调用API接口
        //本示例重试最多3次，3次失败则不再继续重试。
        String accessToken = "";
        String result = "";
        for (int i = 0; i < 3; i++) {
            accessToken = CallAPI.getToken(i > 0).getAccessToken();
            result = CallAPI.APIInvoke(url, accessToken, params);
            System.out.println(result);
            if (!Objects.equals(result, HttpClientUtil.UNAUTHORIZED)) {
                try {
                    result = DES.decrypt(result);
                    //获取到了正确的返回值，如有必要，对返回值进行解密处理，非必须
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new HttpResult<>();
    }

}
