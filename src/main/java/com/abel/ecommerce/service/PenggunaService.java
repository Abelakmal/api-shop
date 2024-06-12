package com.abel.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.abel.ecommerce.entity.Pengguna;
import com.abel.ecommerce.exeption.BadRequestexeption;
import com.abel.ecommerce.exeption.ResourceNotFoundExeption;
import com.abel.ecommerce.repository.PenggunaRepository;

@Service
public class PenggunaService {

    @Autowired
    private PenggunaRepository penggunaRepository;

    public Pengguna findById(String id) {
        return penggunaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption("Pengguna dengan id " + id + " tidak ditemukan"));
    }

    public List<Pengguna> findAll() {
        return penggunaRepository.findAll();
    }

    public Pengguna create(Pengguna pengguna) {
        if (!StringUtils.hasText(pengguna.getId())) {
            throw new BadRequestexeption("Username Harus Diisi");
        }
        if (penggunaRepository.existsById(pengguna.getId())) {
            throw new BadRequestexeption("Username " + pengguna.getId() + " sudah terdaftar");
        }
        if (!StringUtils.hasText(pengguna.getEmail())) {
            throw new BadRequestexeption("Email Harus Diisi");
        }
        if (penggunaRepository.existsByEmail(pengguna.getEmail())) {
            throw new BadRequestexeption("Email: " + pengguna.getEmail() + " Sudah Terdaftar");
        }

        pengguna.setIsAktif(true);
        return penggunaRepository.save(pengguna);
    }

    public Pengguna edit(Pengguna pengguna) {
        if (!StringUtils.hasText(pengguna.getId())) {
            throw new BadRequestexeption("Username Harus Diisi");
        }
        if (!StringUtils.hasText(pengguna.getEmail())) {
            throw new BadRequestexeption("Email Harus Diisi");
        }
        return penggunaRepository.save(pengguna);
    }

    public void deleteById(String id) {
        penggunaRepository.deleteById(id);
    }
}
