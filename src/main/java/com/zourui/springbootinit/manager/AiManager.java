package com.zourui.springbootinit.manager;

import com.esotericsoftware.minlog.Log;
import com.zourui.springbootinit.common.ErrorCode;
import com.zourui.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiManager {

//    @Resource
//    private YuCongMingClient yuCongMingClient;
    @Resource
    private SparkClient sparkClient;
    /**
     * 鱼智能AI 对话
     */
//    public String doChat(long modelId, String message) {
//        DevChatRequest devChatRequest = new DevChatRequest();
//        devChatRequest.setModelId(modelId);
//        devChatRequest.setMessage(message);
//        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
//        if (response == null) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
//        }
//        return response.getData().getContent();
//    }

    /**
     *讯飞大模型
     */
    public String doChat(String message) {
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        //这段代码是给模型添加预设，因为我在BiChartUtils已经手动添加了预设，所以不添加了
        //messages.add(SparkMessage.systemContent("请分析下面问题"));

        messages.add(SparkMessage.userContent(message));  //给ai的问题
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
            // 消息列表
                .messages(messages)
        // 模型回答的tokens的最大长度,非必传，默认为2048。
        // V1.5取值为[1,4096]
        // V2.0取值为[1,8192]
        // V3.0取值为[1,8192]
                .maxTokens(2048)
// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.4)
// 指定请求版本，默认使用最新4.0版本
                .apiVersion(SparkApiVersion.V4_0)
                .build();
        String result ="";
        String useToken = " ";
        try {
            // 同步调用
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            SparkTextUsage textUsage = chatResponse.getTextUsage();
            result = chatResponse.getContent();   //返回结果
            useToken = "提问tokens：" + textUsage.getPromptTokens()
                    + "，回答tokens：" + textUsage.getCompletionTokens()
                    + "，总消耗tokens：" + textUsage.getTotalTokens();
            Log.info(useToken);
        } catch (SparkException e) {
            Log.error("Ai调用发生异常了：" + e.getMessage());
        }
        return result;
    }




}