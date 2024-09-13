package com.training.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.sales.constant.Constant;
import com.training.sales.dto.*;
import com.training.sales.entity.SalesEntity;
import com.training.sales.feign.CustomerClient;
import com.training.sales.feign.ProductClient;
import com.training.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final ProductClient productClient;
    private final CustomerClient customerClient;
    private final ObjectMapper mapper;

    public ResponseEntity<Object> createTransaction(TransactionSaleRequest request) {

        log.info("hit product client - getProductByName: {}", request.getProductName());
        FindByNameRequest nameRequest = new FindByNameRequest(request.getProductName());
        ApiResponse apiResponse = new ApiResponse();
        try {
            apiResponse = Objects.requireNonNull(productClient.getProductByName(nameRequest).getBody());
        }catch (Exception ex){
            log.info("Error hit product service with message: " + ex.getMessage());
        }

        ProductResponse product = mapper.convertValue(apiResponse.getOutputSchema(), ProductResponse.class);

        log.info("validate product response: {} and request: {}", product, request);
        if(product == null) {
            return generateErrorResponse(apiResponse.getErrorSchema().getErrorCode(),
                    apiResponse.getErrorSchema().getErrorMessage(), HttpStatus.NOT_FOUND);
        }

        if(product.getStock() < request.getQuantity()){
            return generateErrorResponse(Constant.BAD_REQUEST,
                    "Product not available for quantity you requested", HttpStatus.BAD_REQUEST);
        }

        log.info("Update product stock");
        UpdateStockRequest updateStockRequest = UpdateStockRequest.builder().productName(request.getProductName()).quantity(request.getQuantity()).build();

        try {
            productClient.updateStockProduct(updateStockRequest);
        }catch (Exception ex){
            log.info("Error hit update stock product with message: " + ex.getMessage());
        }

        Double totalPrice = calculateTotalPrice(request.getQuantity(), product.getPrice());
        String invoiceNumber = generateInvoiceNumber();

        log.info("Save data to DB");
        SalesEntity entity = saveData(product, request, invoiceNumber, totalPrice);

        TransactionSaleResponse response = TransactionSaleResponse.builder()
                .invoiceNumber(entity.getInvoiceNumber())
                .totalPrice(entity.getTotalPrice())
                .productName(entity.getProductName())
                .createdDate(entity.getCreatedDate())
                .build();

        return generateResponse(Constant.SUCCESS, "Sale transaction successfully", response, HttpStatus.OK);
    }

    public ResponseEntity<Object> getAllTransaction(){
        var transactions = salesRepository.findAll();

        List<SalesResponse> responses = new ArrayList<>();

        for(SalesEntity transaction : transactions){
            SalesResponse response = mapper.convertValue(transaction, SalesResponse.class);
            responses.add(response);
        }

        return generateResponse(Constant.SUCCESS, "Success", responses, HttpStatus.OK);
    }

    public ResponseEntity<Object> payTransaction(PayTransactionRequest request){

        Optional<SalesEntity> salesEntity = salesRepository.findByInvoiceNumber(request.getInvoiceNumber());

        if(salesEntity.isEmpty()){
            return generateResponse(Constant.BAD_REQUEST, "Data not found", null, HttpStatus.BAD_REQUEST);
        }

        SalesEntity sales = salesEntity.get();

        if(request.getPaymentAmount() < sales.getTotalPrice()){
            return generateResponse(Constant.BAD_REQUEST, "Payment amount must be equal with total price", null, HttpStatus.BAD_REQUEST);
        }

        UpdateBalanceRequest updateBalanceRequest = UpdateBalanceRequest.builder()
                .email(request.getEmail())
                .totalPrice(sales.getTotalPrice())
                .build();

        ApiResponse apiResponse = new ApiResponse();
        try {
            apiResponse = Objects.requireNonNull(customerClient.updateBalance(updateBalanceRequest).getBody());
        }catch (Exception ex){
            log.info("Failed to update balance with message: {}", ex.getMessage());
        }

        CustomerResponse customer = mapper.convertValue(apiResponse.getOutputSchema(), CustomerResponse.class);

        if(apiResponse.getErrorSchema().getErrorCode().equalsIgnoreCase("C-404")){
            return generateResponse(apiResponse.getErrorSchema().getErrorCode(), apiResponse.getErrorSchema().getErrorMessage(),
                    null, HttpStatus.NOT_FOUND);
        }

        if(apiResponse.getErrorSchema().getErrorCode().equalsIgnoreCase("C-400")){
            return generateResponse(Constant.BAD_REQUEST, apiResponse.getErrorSchema().getErrorMessage(),
                    null, HttpStatus.BAD_REQUEST);
        }

        sales.setEmail(customer.getEmail());
        sales.setCustomerName(customer.getName());
        sales.setReceivedMoney(request.getPaymentAmount());
        sales.setStatus("PAID");
        sales.setUpdatedDate(LocalDateTime.now());

        salesRepository.save(sales);

        return generateResponse(Constant.SUCCESS, "Success do payment sale", null, HttpStatus.OK);
    }

    public ResponseEntity<Object> getSaleTransactionByInvoiceNumber(String invoiceNumber){
        Optional<SalesEntity> sales = salesRepository.findByInvoiceNumber(invoiceNumber);

        if(sales.isEmpty()){
            return generateErrorResponse(Constant.BAD_REQUEST,
                    "Data Not Found", HttpStatus.BAD_REQUEST);
        }

        SalesResponse response = mapper.convertValue(sales.get(), SalesResponse.class);

        return generateResponse(Constant.SUCCESS, "Success", response, HttpStatus.OK);
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

    private SalesEntity saveData(
            ProductResponse product, TransactionSaleRequest request,
            String invoiceNumber, Double totalPrice){
        SalesEntity salesEntity = SalesEntity.builder()
                .productName(product.getName())
                .invoiceNumber(invoiceNumber)
                .totalPrice(totalPrice)
                .quantity(request.getQuantity())
                .status("WAITING_FOR_PAYMENT")
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
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
