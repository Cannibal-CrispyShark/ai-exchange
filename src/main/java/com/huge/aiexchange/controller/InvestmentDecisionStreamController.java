package com.huge.aiexchange.controller;

import com.huge.aiexchange.service.AiInvestmentStreamService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI投资决策流式Controller
 * 使用SSE（Server-Sent Events）提供流式投资决策API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/investment")
public class InvestmentDecisionStreamController {

    @Resource
    private AiInvestmentStreamService aiInvestmentStreamService;

    // 存储活跃的SSE连接
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    // 心跳任务调度器
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(3);


    /**
     * 执行AI投资决策（流式）
     * 使用SSE实时返回决策过程和结果
     *
     * @param modelId        AI模型ID
     * @param riskPreference 风险偏好 (conservative/moderate/aggressive)
     * @return SseEmitter 流式响应
     */
    @GetMapping(value = "/{modelId}/decide-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter makeInvestmentDecisionStream(
            @PathVariable Integer modelId,
            @RequestParam(defaultValue = "moderate") String riskPreference) {

        String emitterKey = modelId + "_" + System.currentTimeMillis();
        // 设置超时时间为30分钟（防止长时间分析被中断）
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.put(emitterKey, emitter);
        
        // 心跳计数器
        AtomicInteger heartbeatCount = new AtomicInteger(0);

        // 清理连接
        emitter.onCompletion(() -> {
            log.info("SSE连接完成: {}", emitterKey);
            emitters.remove(emitterKey);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: {}", emitterKey);
            emitters.remove(emitterKey);
        });
        emitter.onError((e) -> {
            log.error("SSE连接错误: {}", emitterKey, e);
            emitters.remove(emitterKey);
        });

        // 发送连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\": \"连接成功\", \"modelId\": " + modelId + ", \"timeout\": 1800000}"));
        } catch (IOException e) {
            log.error("发送连接成功事件失败", e);
            emitter.completeWithError(e);
            return emitter;
        }

        // 启动心跳机制：每15秒发送一次心跳，防止连接被代理服务器断开
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                if (emitters.containsKey(emitterKey)) {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("{\"count\": " + heartbeatCount.incrementAndGet() + ", \"timestamp\": " + System.currentTimeMillis() + "}"));
                    log.debug("发送心跳: {}", emitterKey);
                }
            } catch (Exception e) {
                log.debug("心跳发送失败，连接可能已断开: {}", emitterKey);
                emitters.remove(emitterKey);
            }
        }, 15, 15, TimeUnit.SECONDS);

        // 启动流式决策流程
        aiInvestmentStreamService.makeInvestmentDecisionStream(modelId, riskPreference, emitter);

        return emitter;
    }

    /**
     * 取消投资决策流
     *
     * @param modelId AI模型ID
     */
    @PostMapping("/{modelId}/cancel")
    public void cancelInvestmentDecision(@PathVariable Integer modelId) {
        emitters.forEach((key, emitter) -> {
            if (key.startsWith(modelId + "_")) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("cancelled")
                            .data("{\"message\": \"用户取消\"}"));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("发送取消事件失败", e);
                }
            }
        });
    }
}
