package com.riozenc.task.api;

import com.epoint.sso.client.util.AssertionTokenUtil;
import com.epoint.sso.client.util.HttpClientUtil;
import com.epoint.sso.client.validation.Assertion;

/**
 * @author belov
 */
public class CallAPI {

    /**
     * 客户端账号标识,请修正为实际值
     */
    static String AppKey = "eae5****-****-****-****-********58a7";
    /**
     * 客户端账号密码,请修正为实际值
     */
    static String AppSecret = "d0c9****-****-****-****-********096c";
    /**
     * 共享交换平台获取token地址,请修正为实际值
     */
    static String SSOUrl = "http://59.211.219.71/share/token";
    /**
     * 调用凭证，考虑凭证需要远程调用接口获取，需要静态化，不用每次调用重新获取
     */
    static Assertion TokenInfo;

    /**
     * 生成调用凭证
     * 此方法预期会出现3类异常情况，针对各类异常的信息和处置方法
     * 1."error='invalid_client', description='非法的ClientID'",请检查AppKey值是否配置准确
     * 2."error='unauthorized_client', description='未验证通过的客户端身份'",请检查AppSecret值是否配置准确
     * 3."org.apache.oltu.oauth2.common.exception.OAuthSystemException: java.io.FileNotFoundException",请检查SSOUrl地址是否配置准确
     */
    public static Assertion getToken(boolean refresh) {
        //如果凭证已经生成且不需要强制更新的情况下，则只需要获取原有凭证即可，避免不必要的远程调用
        if (TokenInfo == null || refresh) {
        //利用SDK方法获取调用凭证，传入客户端账号、密码和认证平台地址，采用的是客户端认证模式
            TokenInfo = AssertionTokenUtil.getAssertionStateless(AppKey, AppSecret, SSOUrl);
        }
        return TokenInfo;
    }

    /**
     * 调用最终API方法
     */
    public static String APIInvoke(String Url, String Access_Token, String Params) {
        //实际需要调用的API的地址，将调用凭证token作为参数传入
        char isTokenParam = '?';
        if (Url.indexOf(isTokenParam) > 0) {
            Url = Url + "&access_token=" + Access_Token;
        } else {
            Url = Url + "?access_token=" + Access_Token;
        }
        //通过SDK的HttpClientUtil调用API，获得返回值
        return HttpClientUtil.postBody(Url, Params);
    }
}
