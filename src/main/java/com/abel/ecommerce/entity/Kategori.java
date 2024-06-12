package com.abel.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Kategori {

    @Id
    private String id;
    private String nama;
}
