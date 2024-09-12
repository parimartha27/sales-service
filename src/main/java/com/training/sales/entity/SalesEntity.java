package com.training.sales.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SALES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SalesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name can't be null, empty or blank")
    private String productName;

    @NotBlank(message = "Customer name can't be null, empty or blank")
    private String customerName;

    @NotBlank(message = "Customer email can't be null, empty or blank")
    private String email;

    @NotBlank(message = "Invoice number can't be null, empty or blank")
    private String invoiceNumber;

    @Min(value = 1, message="Total price must be greater than 0")
    private Double totalPrice;

    @Min(value = 1, message="Quantity must be greater than 0")
    private int quantity;

    private Double receivedMoney;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @CreationTimestamp
    private LocalDateTime updatedDate;
}
