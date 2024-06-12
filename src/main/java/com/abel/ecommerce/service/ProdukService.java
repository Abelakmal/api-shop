package com.abel.ecommerce.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.abel.ecommerce.entity.Produk;
import com.abel.ecommerce.exeption.BadRequestexeption;
import com.abel.ecommerce.exeption.ResourceNotFoundExeption;
import com.abel.ecommerce.repository.KategoriRepository;
import com.abel.ecommerce.repository.ProdukRepository;

@Service
public class ProdukService {
    @Autowired
    private KategoriRepository kategoriRepository;

    @Autowired
    private ProdukRepository produkRepository;

    public List<Produk> findAll() {
        return produkRepository.findAll();
    }

    public Produk findById(String id) {
        return produkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption("Produk dengan id " + id + " tidak ditemukan"));
    }

    public Produk create(Produk produk) {
        if (!StringUtils.hasText(produk.getNama())) {
            throw new BadRequestexeption("Nama Tidak Boleh Kosong");
        }
        if (produk.getKategori() == null) {
            throw new BadRequestexeption("Kategori Tidak Boleh Kosong");
        }
        if (!StringUtils.hasText(produk.getKategori().getId())) {
            throw new BadRequestexeption("Kategori Id Tidak Boleh Kosong");
        }

        kategoriRepository.findById(produk.getKategori().getId())
                .orElseThrow(() -> new BadRequestexeption(
                        "Kategori ID " + produk.getKategori().getId() + " Tidak Ditemukan Dalam Database"));
        produk.setId(UUID.randomUUID().toString());
        return produkRepository.save(produk);
    }

    public Produk edit(Produk produk) {
        if (!StringUtils.hasText(produk.getId())) {
            throw new BadRequestexeption("Produk Id Harus Diisi");
        }
        if (!StringUtils.hasText(produk.getNama())) {
            throw new BadRequestexeption("Nama Tidak Boleh Kosong");
        }
        if (produk.getKategori() == null) {
            throw new BadRequestexeption("Kategori Tidak Boleh Kosong");
        }
        if (!StringUtils.hasText(produk.getKategori().getId())) {
            throw new BadRequestexeption("Kategori Id Tidak Boleh Kosong");
        }

        kategoriRepository.findById(produk.getKategori().getId())
                .orElseThrow(() -> new BadRequestexeption(
                        "Kategori ID " + produk.getKategori().getId() + " Tidak Ditemukan Dalam Database"));

        return produkRepository.save(produk);
    }

    public Produk ubahGambar(String id, String gambar) {
        Produk produk = findById(id);
        produk.setGambar(gambar);
        return produkRepository.save(produk);
    }

    public void deleteById(String id) {
        produkRepository.deleteById(id);
    }
}
