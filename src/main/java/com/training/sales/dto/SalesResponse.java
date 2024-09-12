package com.training.sales.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SalesResponse {
    private String productName;
    private String customerName;
    private String email;
    private String invoiceNumber;
    private Double totalPrice;
    private int quantity;
    private Double receivedMoney;
    private LocalDateTime createdDate;
}
