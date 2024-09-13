package com.training.sales.controller;

import com.training.sales.dto.ApiResponse;
import com.training.sales.dto.PayTransactionRequest;
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
    public ResponseEntity<Object> createTransaction(@RequestBody TransactionSaleRequest request){
        return salesService.createTransaction(request);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllTransaction(){
        return salesService.getAllTransaction();
    }

    @PostMapping("/pay-sales")
    public ResponseEntity<Object> payTransaction(@RequestBody PayTransactionRequest request){
        return salesService.payTransaction(request);
    }

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<Object> getDetailSaleByInvoiceNumber(@PathVariable String invoiceNumber){
        return salesService.getSaleTransactionByInvoiceNumber(invoiceNumber);
    }


}
