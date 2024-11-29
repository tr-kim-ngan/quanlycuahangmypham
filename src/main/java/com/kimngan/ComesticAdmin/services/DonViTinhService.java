package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.DonViTinh;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


public interface DonViTinhService {

	Page<DonViTinh> searchByName(String name, PageRequest pageRequest);

    Page<DonViTinh> findAll(PageRequest pageRequest);

    Optional<DonViTinh> findById(Integer id);

    DonViTinh create(DonViTinh donViTinh);
    Boolean existsByTenDonVi(String tenDonVi);
    Boolean update (DonViTinh donViTinh);
    Optional<DonViTinh> findByTenDonVi(String tenDonVi);
    List<DonViTinh> getAll();

    boolean hasProducts(Integer unitId);
    void delete(Integer unitId);
    
}
