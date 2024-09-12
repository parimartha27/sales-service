package com.training.sales.feign;

import com.training.sales.dto.ApiResponse;
import com.training.sales.dto.FindByNameRequest;
import com.training.sales.dto.UpdateBalanceRequest;
import com.training.sales.dto.UpdateStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service", url = "${customer-service-url}")
public interface CustomerClient {

    @PostMapping("/customers/update-balance")
    ResponseEntity<ApiResponse> updateBalance(@RequestBody UpdateBalanceRequest request);

}
