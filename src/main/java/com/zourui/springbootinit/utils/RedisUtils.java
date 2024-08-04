package com.zourui.springbootinit.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

//public class RedisUtils {
//
//    /**
//     * 将对象转换成Page对象
//     */
//    @SuppressWarnings("unchecked")
//    public static  <T> Page<T> convertToPage(Object obj, Class<T> clazz) {
//        if (obj != null && obj instanceof Map) {
//            Map<String, Object> resultMap = (Map<String, Object>) obj;
//            if (resultMap.containsKey("records")) {
//                Page<T> page = new Page<>();
//                page.setRecords((List<T>) resultMap.get("records"));
//                page.setCurrent(((Number) resultMap.getOrDefault("current", 0)).longValue());
//                page.setSize(((Number) resultMap.getOrDefault("size", 0)).longValue());
//                page.setTotal(((Number) resultMap.getOrDefault("total", 0)).longValue());
//
//                // 可选：设置其他分页属性
//                // page.setXXX(...)
//
//                return page;
//            }
//        }
//        return null;
//    }
//}