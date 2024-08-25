package com.zourui.springbootinit.utils;

import com.zourui.springbootinit.model.entity.BiChart;
import com.zourui.springbootinit.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class BiChartUtils {
    /**
     * 拼接用户输入的信息
     */
    public static String getUserInput(String goal, String chartType, MultipartFile multipartFile) {
        //系统预设
        final String prompt="你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析目标：\n" +
                "{数据分析的需求以及图表类型，如折线图，散点图等}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，严格按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象的json格式代码，注意不要出现函数，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}";

        // 分析需求：
        // 分析网站用户的增长情况
        // 原始数据：
        // 日期,用户数
        // 1号,10
        // 2号,20
        // 3号,30
        //用户输入
        StringBuilder userInput=new StringBuilder();
        userInput.append(prompt).append("\n");
        // 拼接分析目标
        String userGoal=goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType ;
        }
        userInput.append("分析目标:").append("\n").append(userGoal).append("\n");
        //压缩后的数据
        userInput.append("原始数据：").append("\n");
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(result).append("\n");
        return userInput.toString();
    }

    /**
     * 构建BiChart信息
     */
    public static BiChart getBiChart(String chartName, String goal, String chartType, String csvData, String genChart, String genResult, User loginUser) {
        //插入到数据库
        BiChart chart = new BiChart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setChartStatus("succeed");  //设置状态
        return chart;
    }
    /**
     * 构建BiChart信息(异步)
     */
    public static BiChart getBiChartAsync(String chartName, String goal, String chartType, String csvData,  String chartStatus, User loginUser) {
        //插入到数据库
        BiChart chart = new BiChart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        return chart;
    }

}