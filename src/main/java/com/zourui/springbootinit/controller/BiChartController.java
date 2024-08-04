package com.zourui.springbootinit.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zourui.springbootinit.annotation.AuthCheck;
import com.zourui.springbootinit.common.BaseResponse;
import com.zourui.springbootinit.common.DeleteRequest;
import com.zourui.springbootinit.common.ErrorCode;
import com.zourui.springbootinit.common.ResultUtils;
import com.zourui.springbootinit.constant.UserConstant;
import com.zourui.springbootinit.exception.BusinessException;
import com.zourui.springbootinit.exception.ThrowUtils;
import com.zourui.springbootinit.model.dto.chart.*;
import com.zourui.springbootinit.model.entity.BiChart;
import com.zourui.springbootinit.model.entity.User;
import com.zourui.springbootinit.model.vo.BiVo;
import com.zourui.springbootinit.service.BiChartService;
import com.zourui.springbootinit.service.UserService;
import com.zourui.springbootinit.utils.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


/**
 * 图表接口
 * @author zourui
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class BiChartController {

    @Resource
    private BiChartService chartService;
    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     */
    @PostMapping("/add")
    public BaseResponse<Long> addBiChart(@RequestBody BiChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        BiChart chart = new BiChart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());

        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newBiChartId = chart.getId();
        return ResultUtils.success(newBiChartId);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteBiChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        BiChart oldBiChart = chartService.getById(id);
        ThrowUtils.throwIf(oldBiChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可删除
        if (!oldBiChart.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateBiChart(@RequestBody BiChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        BiChart chart = new BiChart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);


        long id = chartUpdateRequest.getId();
        // 判断是否存在
        BiChart oldBiChart = chartService.getById(id);
        ThrowUtils.throwIf(oldBiChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<BiChart> getBiChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        BiChart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }


    /**
     * 分页获取列表（封装类）
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<BiChart>> listBiChartByPage(@RequestBody BiChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<BiChart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
//      Page<BiChart> chartPage = chartService.getChartPageByRedis(chartQueryRequest,request);
        return ResultUtils.success(chartPage);
    }


    /**
     * 智能分析
     *
     */
    @PostMapping("/gen")
    public BaseResponse<BiVo> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                           GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验，使用抛异常工具类，如果目标为空，则抛异常
        ValidationUtil.validateGenChartByAiRequest(genChartByAiRequest);
        /**
         * 三个参数：文件，文件最大大小，后缀限制
         */
         //图表上传文件后缀白名单
        List<String> VALID_FILE_SUFFIX= Arrays.asList("xlsx","csv","xls","json");
        //校验文件大小
        ValidationUtil.validateFile(multipartFile, 1024*1024L, VALID_FILE_SUFFIX);

        //调用service层，核心逻辑：获取响应给前端的图表json以及结论数据
        BiVo biResponse = chartService.getBiResponse(genChartByAiRequest, multipartFile, request);
        return ResultUtils.success( biResponse);    //方法类型改为BiVo
    }
    /**
     * 智能分析（异步）
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiVo> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验，使用抛异常工具类，如果目标为空，则抛异常
        ValidationUtil.validateGenChartByAiRequest(genChartByAiRequest);
        /**
         * 三个参数：文件，文件最大大小，后缀限制
         */
        //图表上传文件后缀白名单
        List<String> VALID_FILE_SUFFIX= Arrays.asList("xlsx","csv","xls","json");
        //校验文件大小
        ValidationUtil.validateFile(multipartFile, 1024*1024L, VALID_FILE_SUFFIX);

        //调用service层，核心逻辑：获取响应给前端的图表json以及结论数据
        BiVo biResponse = chartService.getBiResponseAsync(genChartByAiRequest, multipartFile, request);
        return ResultUtils.success( biResponse);    //方法类型改为BiVo
    }

    /**
     * 智能分析（异步消息队列）
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiVo> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验，使用抛异常工具类，如果目标为空，则抛异常
        ValidationUtil.validateGenChartByAiRequest(genChartByAiRequest);
        /**
         * 三个参数：文件，文件最大大小，后缀限制
         */
        //图表上传文件后缀白名单
        List<String> VALID_FILE_SUFFIX= Arrays.asList("xlsx","csv","xls","json");
        //校验文件大小
        ValidationUtil.validateFile(multipartFile, 1024*1024L, VALID_FILE_SUFFIX);

        //调用service层，核心逻辑：获取响应给前端的图表json以及结论数据
        BiVo biResponse = chartService.getBiResponseAsyncMq(genChartByAiRequest, multipartFile, request);
        return ResultUtils.success( biResponse);    //方法类型改为BiVo
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editBiChart(@RequestBody BiChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        BiChart chart = new BiChart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        BiChart oldBiChart = chartService.getById(id);
        ThrowUtils.throwIf(oldBiChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldBiChart.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

}
