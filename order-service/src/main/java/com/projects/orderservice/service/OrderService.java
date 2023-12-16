package com.projects.orderservice.service;

import com.projects.orderservice.dto.InventoryResponse;
import com.projects.orderservice.dto.OrderLineItemsDto;
import com.projects.orderservice.dto.OrderRequest;
import com.projects.orderservice.models.Order;
import com.projects.orderservice.models.OrderLineItems;
import com.projects.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerWebClientBuilderBeanPostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional

public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList =
                orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode).toList();

        //Call inventory service, and place order if product is in stock

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get().uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build()).retrieve()
                        .bodyToMono(InventoryResponse[].class)
                                .block();
        boolean allProductsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
        if(Boolean.TRUE.equals(allProductsInStock)) orderRepository.save(order);
        else {
            throw new IllegalArgumentException("Product is not in stock, " +
                    "please try again later.");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
