package com.example.stok.controller;

import com.example.stok.dto.ItemDto;
import com.example.stok.model.Inventory;
import com.example.stok.model.Item;
import com.example.stok.model.Order;
import com.example.stok.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StoreService storeService;

    // Items
    @PostMapping("/items")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        return ResponseEntity.ok(storeService.createItem(item));
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemDto>> getAllItems() {
        return ResponseEntity.ok(storeService.getAllItemsWithStock());
    }

    // Inventory
    @PostMapping("/inventory")
    public ResponseEntity<Inventory> addInventory(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(storeService.addInventory(inventory));
    }

    // Orders
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(storeService.createOrder(order));
    }
}
