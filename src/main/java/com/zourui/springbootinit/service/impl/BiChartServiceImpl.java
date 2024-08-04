package com.zourui.springbootinit.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zourui.springbootinit.bimq.BiMessageProducer;
import com.zourui.springbootinit.common.ChartStatus;
import com.zourui.springbootinit.common.ErrorCode;
import com.zourui.springbootinit.constant.CommonConstant;
import com.zourui.springbootinit.exception.BusinessException;
import com.zourui.springbootinit.exception.ThrowUtils;
import com.zourui.springbootinit.manager.AiManager;
import com.zourui.springbootinit.model.dto.chart.BiChartQueryRequest;
import com.zourui.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.zourui.springbootinit.model.entity.BiChart;

import com.zourui.springbootinit.model.entity.User;
import com.zourui.springbootinit.model.vo.BiVo;
import com.zourui.springbootinit.service.BiChartService;
import com.zourui.springbootinit.mapper.BiChartMapper;
import com.zourui.springbootinit.service.UserService;
import com.zourui.springbootinit.utils.BiChartUtils;
import com.zourui.springbootinit.utils.ExcelUtils;
//import com.zourui.springbootinit.utils.RedisUtils;
import com.zourui.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class BiChartServiceImpl extends ServiceImpl<BiChartMapper, BiChart>
    implements BiChartService {
    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;
    @Lazy
    @Resource
    private BiChartService chartService;
//    @Resource
//    private RedisLimiterManager redisLimiterManager;
    /**
     * 获取查询包装类
     *
     * @param bichartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<BiChart> getQueryWrapper(BiChartQueryRequest bichartQueryRequest) {
        QueryWrapper<BiChart> queryWrapper = new QueryWrapper<>();
        if (bichartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = bichartQueryRequest.getId();
        String chartName=bichartQueryRequest.getChartName();
        String goal=bichartQueryRequest.getGoal();
        String chartType = bichartQueryRequest.getChartType();
        Long userId = bichartQueryRequest.getUserId();
        String sortField = bichartQueryRequest.getSortField();
        String sortOrder = bichartQueryRequest.getSortOrder();

        queryWrapper.eq(id!=null && id>0,"id",id); //如果id!=null&id>0
        queryWrapper.like(StringUtils.isNotBlank(chartName),"chartName",chartName);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal); //目标不是空字符
        queryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",chartType); //目标不是空字符
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq( "isDelete", false);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
    //拿到AI响应的数据
    @Override
    public BiVo getBiResponse(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        User loginUser = userService.getLoginUser(request);
        //限流判断
        //每个用户一个限流器
       // redisLimiterManager.doRateLimit("genChartByAi"+loginUser.getId());

        // 也可以 省略添加prompt,使用平台AI模型提供的预设
        long biModelId = 1654785040361893889L;
        // //获得预设+用户输入的拼接数据
        String userInput = BiChartUtils.getUserInput(goal, chartType, multipartFile);
        // 处理数据
        String s = aiManager.doChat(biModelId, userInput);
        //分割 生成的信息，获取对应图表，结论信息
        String[] splits = s.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        // 首次生成的内容
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        // 插入到数据库
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        BiChart chart = BiChartUtils.getBiChart(chartName, goal, chartType, csvData, genChart, genResult, loginUser);

        boolean saveResult = chartService.save(chart);       //存入数据库
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiVo biResponse = new BiVo();
        biResponse.setGenChart(genChart);           //返回给前端
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    //拿到AI响应的数据(异步)
    @Override
    public BiVo getBiResponseAsync(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        User loginUser = userService.getLoginUser(request);
        //限流判断
        //每个用户一个限流器
        // redisLimiterManager.doRateLimit("genChartByAi"+loginUser.getId());

        // 前端发送来的数据先插入到数据库
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        String chartStatus = ChartStatus.WAIT.getValue();
        BiChart chart = BiChartUtils.getBiChartAsync(chartName, goal, chartType, csvData, chartStatus,loginUser);
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        //使用平台AI模型提供的预设
        long biModelId = 1654785040361893889L;
        //获得预设+用户输入的拼接数据
        //比如前端传来一个请求，执行完语句拼接后进入下面的异步函数，所以前端就可以继续发另外的生成图表请求，而不是等待
        String userInput = BiChartUtils.getUserInput(goal, chartType, multipartFile);

        // todo 建议处理任务队列满了后，抛异常的情况
        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，
            // 修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。

            BiChart updateChart = new BiChart();
            updateChart.setId(chart.getId());
            updateChart.setChartStatus(ChartStatus.RUNNING.getValue());   //执行中
            boolean b = chartService.updateById(updateChart);  //更新原来对于id的表
            if (!b) {
               handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
               return;  //因为更新失败了，立即退出当前的匿名类实现，从而不再执行后面的代码，导致资源浪费
            }

            //调用AI
            String s = aiManager.doChat(biModelId, userInput);
            //分割 生成的信息，获取对应图表，结论信息
            String[] splits = s.split("【【【【【");
            if (splits.length < 3) {
                handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
             }
            // 首次生成的内容
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();

            BiChart updateChartResult = new BiChart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setChartStatus(ChartStatus.SUCCEED.getValue());  //已完成
            boolean updateResult = chartService.updateById(updateChartResult);
            if (!updateResult) {
               handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
        }, threadPoolExecutor);
        BiVo biResponse = new BiVo();
        biResponse.setChartId(chart.getId());
        return biResponse;           //最后返回给前端生成成功的图表数据
    }

    @Resource
    BiMessageProducer biMessageProducer;
    /**
     *  拿到AI响应的数据(异步消息队列)
     */
    @Override
    public BiVo getBiResponseAsyncMq(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        User loginUser = userService.getLoginUser(request);
        //限流判断
        //每个用户一个限流器
        // redisLimiterManager.doRateLimit("genChartByAi"+loginUser.getId());

        // 前端发送来的数据先插入到数据库
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        String chartStatus = ChartStatus.WAIT.getValue();
        BiChart chart = BiChartUtils.getBiChartAsync(chartName, goal, chartType, csvData, chartStatus,loginUser);
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        Long chartId = chart.getId();
        biMessageProducer.sendMessage(String.valueOf(chartId));  //发消息,处理完全交给消费者
        BiVo biResponse = new BiVo();
        biResponse.setChartId(chartId);
        return biResponse;           //最后返回数据库中的图表数据
    }


    /**
     * 处理图表更新状态
     */
    public void handleChartUpdateError(long chartId, String execMessage) {
        BiChart updateChart = new BiChart();
        updateChart.setId(chartId);
        updateChart.setChartStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChart);
        if(!updateResult){
           log.error("更新图表失败状态失败" + chartId);
       }
    }

//    @Resource
//    RedisTemplate redisTemplate;
//    /**
//     * 从redis获取图表数据
//     */
//    @Override
//    public Page<BiChart> getChartPageByRedis(BiChartQueryRequest chartQueryRequest, HttpServletRequest request) {
//        int current = chartQueryRequest.getCurrent();
//        int pageSize = chartQueryRequest.getPageSize();
//
//
//        User loginUser = userService.getLoginUser(request);
//        Long userId =loginUser.getId();
//        // 每个用户的图表都是不一样的，所以拼接userId，就是唯一的
//        String pageUser = userId.toString();
//        // 根据需要查询的当前页码和每页大小拼接，使用pageUser作为redis键名
//        String userPageArg = pageUser+current+":"+pageSize;
//        //将查询结果缓存到 Redis 中
//        Object userPageInfoObj =  redisTemplate.opsForHash().get(pageUser, userPageArg);
//        if ("".equals(userPageInfoObj)){
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        // 如果数据不存在，则先查数据库，查不到，存到redis
//        if (userPageInfoObj == null){
//            // 执行查询数据库操作
//            Page<BiChart> result = chartService.page(new Page<>(current, pageSize), getQueryWrapper(chartQueryRequest));
//
//            if (result.getRecords().isEmpty()) {
//                // 如果查询结果为空，则设置一个空字符串，避免频繁查询数据库
//                redisTemplate.opsForHash().put(pageUser, userPageArg, "");
//                // 设置过期时间，防止缓存在长时间内一直为空集合而无法更新
//                redisTemplate.expire(pageUser,10 , TimeUnit.MINUTES);
//                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//            }
//            // 删除缓存
//            Boolean delete = redisTemplate.delete(pageUser);
//            log.debug("分页缓存删除结果为："+delete);
//            // 存储结果到缓存中
//            redisTemplate.opsForHash().put(pageUser, userPageArg, result);
//            redisTemplate.expire(pageUser, 10, TimeUnit.MINUTES);
//            userPageInfoObj =  redisTemplate.opsForHash().get(pageUser, userPageArg);
//        }
//        // 将对象转换成Page对象
//        Page<BiChart> cacheResult = RedisUtils.convertToPage(userPageInfoObj, BiChart.class);
//        return cacheResult;
//    }

}




