package com.zourui.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 *
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 图表名称
     */
    private String chartName;
    /**
     * 图表类型
     */
    private String chartType;
    /**
     * 分析目标
     */
    private String goal;
    private static final long serialVersionUID = 1L;
}