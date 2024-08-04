package com.zourui.springbootinit.model.dto.chart;

import lombok.Data;
import java.io.Serializable;


/**
 * 创建请求
 * @author zourui
 */
@Data
public class BiChartAddRequest implements Serializable {

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 图表信息
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}