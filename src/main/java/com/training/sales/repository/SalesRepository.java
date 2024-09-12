package com.training.sales.repository;

import com.training.sales.entity.SalesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesRepository extends JpaRepository<SalesEntity, Long> {

    Optional<SalesEntity> findByInvoiceNumber(String invoiceNumber);
}
