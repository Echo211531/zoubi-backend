package com.zourui.springbootinit.model.dto.chart;

import com.zourui.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 查询请求
 * @author zourui
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BiChartQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 创建图表用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}