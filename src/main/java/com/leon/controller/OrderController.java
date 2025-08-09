package com.leon.controller;

import com.leon.model.InsightItem;
import com.leon.model.MessageData;
import com.leon.model.Metric;
import com.leon.model.InsightType;
import com.leon.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController
{
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private final OrderService orderService;

    @CrossOrigin
    @GetMapping("/insights")
    public ResponseEntity<List<InsightItem>> getInsights(@RequestParam String insightType, @RequestParam String metric,
                                                        @RequestParam LocalDate startDate, @RequestParam LocalDate endDate)
    {
        try
        {
            log.info("Fetching insights of type: {}, metric: {}, from: {} to: {}", insightType, metric, startDate, endDate);
            Metric metricEnum = Metric.fromString(metric);
            InsightType insightTypeEnum = InsightType.fromString(insightType);
            return ResponseEntity.ok(orderService.getInsights(insightTypeEnum, metricEnum, startDate, endDate));
        }
        catch (Exception e)
        {
            log.error("ERR-1101: Error retrieving insights", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @CrossOrigin
    @GetMapping("/history")
    public ResponseEntity<List<MessageData>> getHistory(@RequestParam LocalDate startTradeDate, @RequestParam LocalDate endTradeDate,
                                                        @RequestParam String clientCode, @RequestParam String instrumentCode, @RequestParam String ownerId)
    {
        try
        {
            log.info("Fetching trade history from: {} to: {}, clientCode: {}, instrumentCode: {}, ownerId: {}",
                     startTradeDate, endTradeDate, clientCode, instrumentCode, ownerId);
            return ResponseEntity.ok(orderService.getHistory(startTradeDate, endTradeDate, clientCode, instrumentCode, ownerId));
        }
        catch (Exception e)
        {
            log.error("ERR-1102: Error retrieving trade history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @CrossOrigin
    @GetMapping("/crosses")
    public ResponseEntity<List<MessageData>> getCrosses()
    {
        try
        {
            log.info("Fetching crosses for today: {}", LocalDate.now());
            return ResponseEntity.ok(orderService.getCrosses());
        }
        catch (Exception e)
        {
            log.error("ERR-1103: Error retrieving crosses", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}