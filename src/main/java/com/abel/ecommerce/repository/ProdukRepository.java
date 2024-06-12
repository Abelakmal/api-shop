package com.abel.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abel.ecommerce.entity.Produk;

public interface ProdukRepository extends JpaRepository<Produk, String> {

}
