package com.projects.inventoryservice.service;

import com.projects.inventoryservice.dto.InventoryResponse;
import com.projects.inventoryservice.repository.InventoryRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepo inventoryRepo;

//    @Transactional(readOnly = true)
//    public boolean isInStock(String skuCode) {
//        return inventoryRepo.findBySkuCode(skuCode).isPresent();
//    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {

        return inventoryRepo.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponse.builder().skuCode(inventory.getSkuCode())
                            .isInStock(inventory.getQuantity()>0)
                            .build()
                ).toList();
    }
}
