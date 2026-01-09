package com.example.stok.service;

import com.example.stok.exception.InsufficientStockException;
import com.example.stok.model.Inventory;
import com.example.stok.model.Item;
import com.example.stok.model.Order;
import com.example.stok.repository.InventoryRepository;
import com.example.stok.repository.ItemRepository;
import com.example.stok.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    void testCalculateStock_Correctly() {
        Item item = Item.builder().id(1L).name("Pen").build();

        // 10 topup, 2 withdraw, 3 sold
        // Expected: (10 - 2) - 3 = 5
        when(inventoryRepository.findByItem(item)).thenReturn(Arrays.asList(
                Inventory.builder().qty(10).type("T").build(),
                Inventory.builder().qty(2).type("W").build()
        ));
        when(orderRepository.findByItem(item)).thenReturn(Arrays.asList(
                Order.builder().qty(3).build()
        ));

        int stock = storeService.calculateStock(item);
        assertEquals(5, stock);
    }

    @Test
    void testCreateOrder_Success() {
        Item item = Item.builder().id(1L).name("Pen").build();
        Order order = Order.builder().orderNo("O1").item(item).qty(5).build();

        // Stock available: 10
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByItem(item)).thenReturn(Collections.singletonList(
                Inventory.builder().qty(10).type("T").build()
        ));
        when(orderRepository.findByItem(item)).thenReturn(Collections.emptyList());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = storeService.createOrder(order);
        assertNotNull(result);
        verify(orderRepository).save(order);
    }

    @Test
    void testCreateOrder_InsufficientStock() {
        Item item = Item.builder().id(1L).name("Pen").build();
        Order order = Order.builder().orderNo("O1").item(item).qty(15).build();

        // Stock available: 10
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByItem(item)).thenReturn(Collections.singletonList(
                Inventory.builder().qty(10).type("T").build()
        ));
        when(orderRepository.findByItem(item)).thenReturn(Collections.emptyList());

        assertThrows(InsufficientStockException.class, () -> storeService.createOrder(order));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
