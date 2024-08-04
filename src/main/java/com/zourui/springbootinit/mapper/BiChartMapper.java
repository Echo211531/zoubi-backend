package com.zourui.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zourui.springbootinit.model.entity.BiChart;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
* @author www21
*/
public interface BiChartMapper extends BaseMapper<BiChart> {
    @MapKey("id")
    List<Map<String, Object>> queryChartData(String chartId);
}




