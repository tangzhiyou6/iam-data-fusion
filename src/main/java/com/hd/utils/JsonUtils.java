package com.hd.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private JsonUtils() {
    }
    public static ObjectMapper getInstance() {
        return objectMapper;
    }
    /**
     * javaBean、列表数组转换为json字符串
     */
    public static String toJSON(Object obj){
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * json 转JavaBean
     */
    public static <T> T parse(String jsonString, Class<T> clazz){
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> T parse(String jsonString, TypeReference<T> typeReferece){
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            return objectMapper.readValue(jsonString, typeReferece);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T> Map<String, T> parseMap(String jsonString, TypeReference<Map<String, T>> typeReferece){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.readValue(jsonString, typeReferece);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 与javaBean json数组字符串转换为列表
     */
    public static <T> List<T> parseList(String sourceStr, Class<T> clazz) {
        JavaType javaType = getCollectionType(ArrayList.class, clazz);
        List<T> list = null;
        try {
            list = (List<T>)objectMapper.readValue(sourceStr, javaType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Objects.isNull(list) ? Collections.EMPTY_LIST : list;
    }

    public static Map<String, Object> parseMap(Object obj) {
        if (obj == null) {
            return null;
        }
        return objectMapper.convertValue(obj, HashMap.class);
    }

    public static <T> List<T> parseCamelList(String sourceStr, Class<T> clazz) {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaType javaType = getCollectionType(ArrayList.class, clazz);
        List<T> list = null;
        try {
            list = (List<T>)objectMapper.readValue(sourceStr, javaType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Objects.isNull(list) ? Collections.EMPTY_LIST : list;
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}