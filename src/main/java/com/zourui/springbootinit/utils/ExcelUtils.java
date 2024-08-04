package com.zourui.springbootinit.utils;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {
    /**
     * excel 转 csv
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        List<Map<Integer, String>> list = null;
        try {
            //使用EasyExcel库来读取Excel文件中的数据
            list = EasyExcel.read(multipartFile.getInputStream())     //创建一个Excel读取器
                    .excelType(ExcelTypeEnum.XLSX)      //Excel文件的类型为XLSX
                    .sheet()              // 获取第一个工作表
                    .headRowNumber(0)     //指定表头所在的行号
                    .doReadSync();         //执行同步读取操作，将数据读取到一个列表中
        } catch (Exception e) {
            log.error("表格处理错误", e);
        }
        if (CollUtil.isEmpty(list)) { //使用hutool工具包，如果list为空
            return "";
        }
        // 转换为 csv
        StringBuilder stringBuilder = new StringBuilder();
        // 读取表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);

        //将表头集合转换为一个Stream,过滤出所有空值后收集到list集合中
        //ObjectUtils::isNotEmpty 是一个方法引用
        // 它指向ObjectUtils类中的isNotEmpty方法，该方法用于检查一个对象是否非空
        List<String> headerList = headerMap.values().stream().
                filter((ObjectUtils::isNotEmpty)).collect(Collectors.toList());
        //先将表头用，连接加入stringBuilder中，并在最后添加换行符
        stringBuilder.append(StringUtils.join(headerList, ",")).append("\n");
        // 读取数据
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList, ",")).append("\n");
        }
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }
}
