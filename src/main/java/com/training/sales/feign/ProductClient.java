package com.training.sales.feign;

import com.training.sales.dto.ApiResponse;
import com.training.sales.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url = "${product-service-url}")
public interface ProductClient {

    @GetMapping()
    ProductResponse getProductByName(@RequestBody String productName);

    @PostMapping("/update/stock")
    ApiResponse updateStock(@RequestBody int updateStockRequest);

}
