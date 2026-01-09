package com.example.stok.repository;

import com.example.stok.model.Inventory;
import com.example.stok.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByItem(Item item);
}
