package com.kimngan.ComesticAdmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kimngan.ComesticAdmin.entity.QuyenTruyCap;

@Repository
public interface QuyenTruyCapRepository extends JpaRepository<QuyenTruyCap, Integer> {
    QuyenTruyCap findByTenQuyen(String tenQuyen);
}

