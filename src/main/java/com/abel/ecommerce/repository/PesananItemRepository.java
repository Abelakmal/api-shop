package com.abel.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abel.ecommerce.entity.PesananItem;

public interface PesananItemRepository extends JpaRepository<PesananItem, String> {

}
