package com.zourui.springbootinit.bimq;
import cn.hutool.http.HttpRequest;
import co.elastic.clients.elasticsearch.nodes.Http;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esotericsoftware.minlog.Log;
import com.rabbitmq.client.Channel;
import com.zourui.springbootinit.common.ChartStatus;
import com.zourui.springbootinit.common.ErrorCode;
import com.zourui.springbootinit.exception.BusinessException;
import com.zourui.springbootinit.manager.AiManager;
import com.zourui.springbootinit.manager.RedisLimiterManager;
import com.zourui.springbootinit.model.entity.BiChart;
import com.zourui.springbootinit.service.BiChartService;
import com.zourui.springbootinit.service.UserService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Component
public class BiMessageConsumer {
    @Resource
    private BiChartService chartService;
    @Resource
    private AiManager aiManager;
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        Log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 消息为空，则拒绝掉消息
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接受到的消息为空");
        }

        long chartId = Long.parseLong(message);        // 获取消息
        BiChart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);  //表为空，则丢弃消息
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }

        BiChart updateChart = new BiChart();
        updateChart.setId(chart.getId());
        updateChart.setChartStatus(ChartStatus.RUNNING.getValue());   //执行中
        boolean b = chartService.updateById(updateChart);  //更新原来对于id的表
        if (!b) {
            channel.basicNack(deliveryTag, false, false);  //更新失败，丢弃消息
            chartService.handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
            return;  //因为更新失败了，立即退出当前的匿名类实现，从而不再执行后面的代码，导致资源浪费
        }
        // 更新缓存
        // chartService.updateRedisPageCache();
        //BiMessageProducer.sendMysqlAndRedis();

        //使用平台AI模型提供的预设
        long biModelId = 1654785040361893889L;
        //调用AI
        String s = aiManager.doChat(biModelId, buildUserInput(chart));
        //分割 生成的信息，获取对应图表，结论信息
        String[] splits = s.split("【【【【【");
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "AI 生成错误");
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
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        // 更新缓存
        // chartService.updateRedisPageCache();
        //BiMessageProducer.sendMysqlAndRedis();

        // 消息确认
        channel.basicAck(deliveryTag, false);

    }
    /**
     * 构造用户输入
     * @param biChart
     * @return
     */
    private String buildUserInput(BiChart biChart) {
        String goal= biChart.getGoal();
        String chartType= biChart.getChartType();
        String csvData= biChart.getChartData();
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
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }


//    @Resource
//    RedisTemplate redisTemplate;
//    @Resource
//    UserService userService;
//    @SneakyThrows
//    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
//    public void receiveMysqlAndRedis(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
//        Log.info("bi start receiveMysqlAndeRedis message = {}", message);
//
//        if (StringUtils.isBlank(message)) {
//            // 如果失败，消息拒绝
//            channel.basicNack(deliveryTag, false, false);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
//        }
//
//        int current = 1;
//        int pageSize = 2;
//        // 获取当前登录用户的Id
//        long  userId = 1816665400;
//
//        QueryWrapper<BiChart> wrapper = new QueryWrapper<>();
//        wrapper.eq("user_id", userId);
//        // 执行查询数据库操作
//        Page<BiChart> result = chartService.page(new Page<>(current, pageSize), wrapper);
//
//        // 每个用户的图表都是不一样的，所以拼接userId，就是唯一的
//        String pageUser = String.valueOf(userId);
//        // 根据需要查询的当前页码和每页大小拼接，使用pageUser作为redis键名
//        String userPageArg = pageUser+current+":"+pageSize;
//        // 删除缓存
//        Boolean delete = redisTemplate.delete(pageUser);
//        Log.debug("分页缓存删除结果为："+delete);
//        // 存储结果到缓存中
//        redisTemplate.opsForHash().put(pageUser, userPageArg, result);
//        redisTemplate.expire(pageUser, 10, TimeUnit.MINUTES);
//        // 更新缓存
//        redisTemplate.opsForHash().put(pageUser, userPageArg, result);
//        redisTemplate.expire(pageUser, 10, TimeUnit.MINUTES);
//        // 消息确认
//        channel.basicAck(deliveryTag, false);
//        Log.info("bi receiveMysqlAndeRedis message end");
//    }
}
