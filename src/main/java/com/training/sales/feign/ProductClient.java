package com.training.sales.feign;

import com.training.sales.dto.ApiResponse;
import com.training.sales.dto.FindByNameRequest;
import com.training.sales.dto.ProductResponse;
import com.training.sales.dto.UpdateStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url = "${product-service-url}")
public interface ProductClient {

    @PostMapping("/products/name")
    ResponseEntity<ApiResponse> getProductByName(@RequestBody FindByNameRequest request);

    @PostMapping("/products/update/stock")
    ResponseEntity<ApiResponse> updateStockProduct(@RequestBody UpdateStockRequest updateStockRequest);

}
