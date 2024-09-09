package com.training.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.sales.dto.ApiResponse;
import com.training.sales.dto.ProductResponse;
import com.training.sales.dto.TransactionSaleRequest;
import com.training.sales.dto.TransactionSaleResponse;
import com.training.sales.entity.SalesEntity;
import com.training.sales.feign.ProductClient;
import com.training.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final ProductClient productClient;

    public ResponseEntity<Object> createTransaction(TransactionSaleRequest request) {

        LocalDateTime time = LocalDateTime.now();

        log.info("Start sales process at: {}", time);
        ProductResponse product = productClient.getProductByName(request.getProductName());

        if(product == null) {
            return generateErrorResponse("P-404", "Product not found", HttpStatus.NOT_FOUND);
        }

        if(product.getStock() < request.getQuantity()){
            return generateErrorResponse("S-400", "Product not available for quantity you requested", HttpStatus.BAD_REQUEST);
        }

        Double totalPrice = calculateTotalPrice(request.getQuantity(), product.getPrice());
        String invoiceNumber = generateInvoiceNumber();

        SalesEntity entity = mappingSalesEntity(product, request, invoiceNumber, totalPrice, time);

        TransactionSaleResponse response = TransactionSaleResponse.builder()
                .invoiceNumber(entity.getInvoiceNumber())
                .totalPrice(entity.getTotalPrice())
                .customerEmail(entity.getCustomerEmail())
                .productName(entity.getProductName())
                .salesDate(entity.getSalesDate())
                .build();

        return generateResponse("S-200", "Sale transaction successfully", response, HttpStatus.OK);
    }

    private String generateInvoiceNumber(){
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String formattedDate = timestamp.format(formatter);
        return "INV" + formattedDate;
    }

    private Double calculateTotalPrice(int quantity, Double price){
        return quantity * price;
    }

    private SalesEntity mappingSalesEntity(
            ProductResponse product, TransactionSaleRequest request,
            String invoiceNumber, Double totalPrice, LocalDateTime time){
        SalesEntity salesEntity = SalesEntity.builder()
                .productName(product.getName())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .invoiceNumber(invoiceNumber)
                .totalPrice(totalPrice)
                .quantity(request.getQuantity())
                .salesDate(time)
                .build();

        salesRepository.save(salesEntity);

        return salesEntity;
    }

    private ResponseEntity<Object> generateResponse(String errorCode, String errorMessage, Object outputSchema, HttpStatus httpStatus){
        ApiResponse response = ApiResponse.builder()
                .errorSchema(ApiResponse.ErrorSchema.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())
                .outputSchema(outputSchema)
                .build();

        return new ResponseEntity<>(response, httpStatus);
    }

    private ResponseEntity<Object> generateErrorResponse(String errorCode, String errorMessage, HttpStatus httpStatus){
       ApiResponse.ErrorSchema errorSchema = ApiResponse.ErrorSchema.builder()
               .errorCode(errorCode)
               .errorMessage(errorMessage)
               .build();

       return new ResponseEntity<>(errorSchema, httpStatus);
    }

}
