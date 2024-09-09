package com.training.sales.controller;

import com.training.sales.dto.ApiResponse;
import com.training.sales.dto.TransactionSaleRequest;
import com.training.sales.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @PostMapping("/create")
    public ResponseEntity<Object> createTransaction(@RequestBody List<TransactionSaleRequest> request){

        return salesService.createTransaction(request);
    }

}
