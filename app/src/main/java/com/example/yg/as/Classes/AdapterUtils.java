package com.example.yg.as.Classes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AdapterUtils {

    public static HashMap<String,String> convertToHashMap(Object object){
        HashMap<String,String> result = new HashMap<>();


            for ( Field field : object.getClass().getDeclaredFields() ){
                try{
                Method m = object.getClass().getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1) );
                result.put(field.getName() , String.valueOf(m.invoke(object)) );
                }
                catch(Exception e){

                }
            }

        return result;
    }

}
