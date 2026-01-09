package com.example.stok.service;

import com.example.stok.dto.ItemDto;
import com.example.stok.exception.InsufficientStockException;
import com.example.stok.model.Inventory;
import com.example.stok.model.Item;
import com.example.stok.model.Order;
import com.example.stok.repository.InventoryRepository;
import com.example.stok.repository.ItemRepository;
import com.example.stok.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItemsWithStock() {
        return itemRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    public Inventory addInventory(Inventory inventory) {
        // Validate Item exists
        if (inventory.getItem() == null || inventory.getItem().getId() == null) {
            throw new IllegalArgumentException("Item must be provided");
        }
        Item item = itemRepository.findById(inventory.getItem().getId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        inventory.setItem(item);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Order createOrder(Order order) {
        // Validate Item
        Item item = itemRepository.findById(order.getItem().getId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        // Calculate current stock
        int currentStock = calculateStock(item);

        if (currentStock < order.getQty()) {
            throw new InsufficientStockException("Insufficient stock for item: " + item.getName() + 
                    ". Available: " + currentStock + ", Requested: " + order.getQty());
        }

        order.setItem(item); // Ensure item is attached
        return orderRepository.save(order);
    }

    private ItemDto mapToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .currentStock(calculateStock(item))
                .build();
    }

    public int calculateStock(Item item) {
        List<Inventory> inventories = inventoryRepository.findByItem(item);
        List<Order> orders = orderRepository.findByItem(item);

        int topUp = inventories.stream()
                .filter(i -> "T".equalsIgnoreCase(i.getType()))
                .mapToInt(Inventory::getQty)
                .sum();

        int withdrawal = inventories.stream()
                .filter(i -> "W".equalsIgnoreCase(i.getType()))
                .mapToInt(Inventory::getQty)
                .sum();

        int sold = orders.stream()
                .mapToInt(Order::getQty)
                .sum();

        return (topUp - withdrawal) - sold;
    }
}
