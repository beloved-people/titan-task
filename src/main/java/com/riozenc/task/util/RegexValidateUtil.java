package com.riozenc.task.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValidateUtil {

    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = true;
        Pattern regex = Pattern
                .compile("^\\+?(\\(?0{0,2}(86)?\\)?)1(((\\d{2}|(44)\\d{2}|(65)\\d{2}|(0648)|(0647))\\d{8}))$");
        Matcher matcher = regex.matcher(mobileNumber);
        flag = matcher.matches();
        return flag;
    }
}
