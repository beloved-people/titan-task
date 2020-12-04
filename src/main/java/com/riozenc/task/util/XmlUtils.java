package com.riozenc.task.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class XmlUtils {

    public static String objectToXml(Object obj) {
        // XStream xStream = new XStream();
        // XStream的优点很多，但是也有一些小bug，比如在定义别名中的下划线“_”转换为xml后会变成“__”这个符号
        //解决下划线问题
        XStream xstream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_")));
        xstream.autodetectAnnotations(true);
        // processing annotations
        return xstream.toXML(obj);
    }
}
