package com.abel.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abel.ecommerce.entity.Keranjang;
import com.abel.ecommerce.entity.Pengguna;
import com.abel.ecommerce.entity.Pesanan;
import com.abel.ecommerce.entity.PesananItem;
import com.abel.ecommerce.entity.Produk;
import com.abel.ecommerce.exeption.BadRequestexeption;
import com.abel.ecommerce.exeption.ResourceNotFoundExeption;
import com.abel.ecommerce.model.KeranjangRequest;
import com.abel.ecommerce.model.PesananRequest;
import com.abel.ecommerce.model.PesananRespon;
import com.abel.ecommerce.model.StatusPesanan;
import com.abel.ecommerce.repository.KeranjangRepository;
import com.abel.ecommerce.repository.PesananItemRepository;
import com.abel.ecommerce.repository.PesananRepository;
import com.abel.ecommerce.repository.ProdukRepository;

@Service
public class PesananService {

    @Autowired
    private ProdukRepository produkRepository;
    @Autowired
    private PesananRepository pesananaRepository;
    @Autowired
    private PesananItemRepository pesananItemRepository;
    @Autowired
    private KeranjangService keranjangService;
    @Autowired
    private KeranjangRepository keranjangRepository;
    @Autowired
    private PesananLogService pesananLogService;

    // membuat pesanan
    public PesananRespon create(String username, PesananRequest request) {
        Pesanan pesanan = new Pesanan();
        pesanan.setId(UUID.randomUUID().toString());
        pesanan.setTanggal(new Date());
        pesanan.setNomor(generateNomorPesanan());
        pesanan.setPengguna(new Pengguna(username));
        pesanan.setStatusPesanan(StatusPesanan.DRAFT);
        pesanan.setWaktuPesanan(new Date());

        // cek produk apa ada atau tidak dan stock mencukupi atau tidak
        List<PesananItem> items = new ArrayList<>();
        for (KeranjangRequest k : request.getItems()) {
            Produk produk = produkRepository.findById(k.getProdukId())
                    .orElseThrow(() -> new BadRequestexeption("Produk ID " + k.getProdukId() + " tidak ditemukan"));
            if (produk.getStok() < k.getKuantitas()) {
                throw new BadRequestexeption("Stok tidak ditemukan");
            }
            // masukan ke pesananItem
            PesananItem pi = new PesananItem();
            pi.setId(UUID.randomUUID().toString());
            pi.setProduk(produk);
            pi.setDeskripsi(produk.getNama());
            pi.setKuantitas(k.getKuantitas());
            pi.setHarga(produk.getHarga());
            pi.setJumlah(new BigDecimal(pi.getHarga().doubleValue() * pi.getKuantitas()));
            pi.setPesanan(pesanan);
            items.add(pi);
        }
        // hitung jumlah pesanan di pesananItem
        BigDecimal jumlah = BigDecimal.ZERO;
        for (PesananItem pesananItem : items) {
            jumlah = jumlah.add(pesananItem.getJumlah());
        }
        // masukan ke tabel pesanan
        pesanan.setJumlah(jumlah);
        pesanan.setOngkir(request.getOngkir());
        pesanan.setTotal(pesanan.getJumlah().add(pesanan.getOngkir()));

        // save pesanan hapus pesanan produk dari keranjang
        Pesanan saved = pesananaRepository.save(pesanan);
        for (PesananItem pesananItem : items) {
            pesananItemRepository.save(pesananItem);
            Produk produk = pesananItem.getProduk();
            produk.setStok(produk.getStok() - pesananItem.getKuantitas());
            produkRepository.save(produk);

            Keranjang keranjang = keranjangRepository.findByPenggunaIdAndProdukId(username, produk.getId())
                    .orElseThrow(() -> new ResourceNotFoundExeption("tidak ditemukan"));
            if (keranjang.getKuantitas() - pesananItem.getKuantitas() == 0) {
                keranjangService.delete(username, produk.getId());
            } else {
                keranjangService.updateKuantitas(username, produk.getId(),
                        keranjang.getKuantitas() - pesananItem.getKuantitas());
            }
        }

        // catat log
        pesananLogService.createLog(username, pesanan, PesananLogService.DRAFT, "Pesanan sukses dibuat");
        PesananRespon pesananRespon = new PesananRespon(saved, items);
        return pesananRespon;
    }

    // generate nomor
    private String generateNomorPesanan() {
        return String.format("%016d", System.nanoTime());
    }

    // cancel pesanan yg hanya bisa oleh pengguna
    @Transactional
    public Pesanan cancelPesanan(String pesananId, String userId) {
        Pesanan pesanan = pesananaRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundExeption("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!userId.equals(pesanan.getPengguna().getId())) {
            throw new BadRequestexeption("Pesanan ini hanya dapat dipatalkan oleh yang bersangkutan");
        }
        if (!StatusPesanan.DRAFT.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestexeption("Pesanan tidak dapat dibatalkan karena sudah diproses ");
        }

        pesanan.setStatusPesanan(StatusPesanan.DIBATALKAN);
        Pesanan saved = pesananaRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.DIBATALKAN, "Pesanan sukses dibatalkan");
        return saved;
    }

    // pesanan saat pelanggan menerima pesanan
    @Transactional
    public Pesanan terimaPesanan(String pesananId, String userId) {
        Pesanan pesanan = pesananaRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundExeption("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!userId.equals(pesanan.getPengguna().getId())) {
            throw new BadRequestexeption("Pesanan ini hanya dapat dipatalkan oleh yang bersangkutan");
        }
        if (!StatusPesanan.PENGIRIMAN.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestexeption(
                    "Penerimaan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.DIBATALKAN);
        Pesanan saved = pesananaRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.DIBATALKAN, "Pesanan sukses dibatalkan");
        return saved;
    }

    // untuk menemukan all pesanan user
    public List<Pesanan> findAllPesananUser(String userId, int page, int limit) {
        return pesananaRepository.findByPenggunaId(userId,
                PageRequest.of(page, limit, Sort.by("waktuPesanan").descending()));
    }

    public List<Pesanan> search(String filterText, int page, int limit) {
        return pesananaRepository.search(filterText, PageRequest.of(page, limit, Sort.by("waktuPesanan").descending()));
    }

    // konfirmasiPembayaran
    @Transactional
    public Pesanan konfirmasiPembayaran(String pesananId, String userId) {
        Pesanan pesanan = pesananaRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundExeption("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!StatusPesanan.DRAFT.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestexeption(
                    "Konfirmasi pesanan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.PEMBAYARAN);
        Pesanan saved = pesananaRepository.save(pesanan);
        pesananLogService.createLog(
                userId,
                saved,
                PesananLogService.PEMBAYARAN, "Pembayaran sukses dikonfirmasi");
        return saved;
    }

    // packing
    @Transactional
    public Pesanan packing(String pesananId, String userId) {
        Pesanan pesanan = pesananaRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundExeption("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!StatusPesanan.PEMBAYARAN.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestexeption(
                    "Packing pesanan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.PACKING);
        Pesanan saved = pesananaRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.PACKING, "Pesanan sedang disiapkan");
        return saved;
    }

    @Transactional
    public Pesanan kirim(String pesananId, String userId) {
        Pesanan pesanan = pesananaRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundExeption("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!StatusPesanan.PACKING.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestexeption(
                    "Pengiriman pesanan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.PENGIRIMAN);
        Pesanan saved = pesananaRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.PACKING, "Pesanan sedang dikirim");
        return saved;
    }

}
