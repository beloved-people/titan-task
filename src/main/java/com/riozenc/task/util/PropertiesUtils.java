package com.riozenc.task.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author belov
 */
public class PropertiesUtils {

    private PropertiesUtils() {
    }

    public static List<String> convertObjToList(Object obj){
        ArrayList<String> list=new ArrayList<>();
        if (obj == null) {
            return null;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                try {
                    Field f = obj.getClass().getDeclaredField(field.getName());
                    f.setAccessible(true);
                    Object o = f.get(obj);
                    list.add(o.toString());
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }


}
