package de.bobek.spring.storageservice.storage.internal.db;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Page<Item> findAllByUsername(String username, Pageable pageable);

    Optional<Item> findByIdAndUsername(UUID id, String username);
}
