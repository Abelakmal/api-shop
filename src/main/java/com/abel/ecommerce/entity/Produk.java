package com.abel.ecommerce.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Produk {
    @Id
    private String id;
    private String nama;
    private String deskripsi;
    private String gambar;
    @ManyToOne
    private Kategori kategori;
    private BigDecimal harga;
    private Double stok;
}
