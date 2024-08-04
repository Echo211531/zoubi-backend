package com.zourui.springbootinit.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zourui.springbootinit.model.dto.chart.BiChartQueryRequest;
import com.zourui.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.zourui.springbootinit.model.entity.BiChart;
import com.zourui.springbootinit.model.vo.BiVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface BiChartService extends IService<BiChart> {
    QueryWrapper<BiChart> getQueryWrapper(BiChartQueryRequest bichartQueryRequest);
    /**
     * 拿到AI响应的数据
     * @param genChartByAiRequest
     * @param multipartFile
     * @return
     */
    BiVo getBiResponse(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request);

    /**
     * 拿到AI响应的数据(异步)
     */
    BiVo getBiResponseAsync(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request);
    /**
     * 拿到AI响应的数据(异步消息队列)
     */
    BiVo getBiResponseAsyncMq(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request);
    void handleChartUpdateError(long chartId, String execMessage);
    /**
     * 根据用户从redis缓存查询分页信息
     */
//    Page<BiChart> getChartPageByRedis(BiChartQueryRequest chartQueryRequest, HttpServletRequest request);
}
