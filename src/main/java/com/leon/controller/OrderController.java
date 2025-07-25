package com.leon.controller;

import com.leon.model.MessageData;
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
    @GetMapping("/history")
    public ResponseEntity<List<MessageData>> getHistory(@RequestParam LocalDate startTradeDate, LocalDate endTradeDate)
    {
        try
        {
            return ResponseEntity.ok(orderService.getHistory(startTradeDate, endTradeDate));
        }
        catch (Exception e)
        {
            log.error("ERR-1101: Error retrieving audit trail", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}