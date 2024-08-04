package com.zourui.springbootinit.utils;
import cn.hutool.core.io.FileUtil;
import com.zourui.springbootinit.common.ErrorCode;
import com.zourui.springbootinit.exception.BusinessException;
import com.zourui.springbootinit.model.dto.chart.GenChartByAiRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
//处理前端传来的参数的情况分析
public class ValidationUtil {

    private static final int CHART_NAME_LEN = 100;  //图表名字长度限制
    public static void validateGenChartByAiRequest(GenChartByAiRequest genChartByAiRequest) {
        if (StringUtils.isBlank(genChartByAiRequest.getChartType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析类型不能为空");
        }

        if (StringUtils.isBlank(genChartByAiRequest.getGoal())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        }

        if (StringUtils.isNotBlank(genChartByAiRequest.getChartName())
                && genChartByAiRequest.getChartName().length() > CHART_NAME_LEN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表名称过长");
        }
    }

    public static void validateFile(MultipartFile multipartFile, long maxSize, List<String> validFileSuffixList) {
        if (multipartFile.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超出1M");
        }

        String originalFilename = multipartFile.getOriginalFilename(); //拿到原始文件名
        String suffix = FileUtil.getSuffix(originalFilename);
        if (!validFileSuffixList.contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型不符合要求");
        }
    }
}