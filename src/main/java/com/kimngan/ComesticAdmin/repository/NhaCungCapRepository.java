package com.kimngan.ComesticAdmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kimngan.ComesticAdmin.entity.NhaCungCap;

public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Integer> {

	@Query("SELECT n FROM NhaCungCap n WHERE n.trangThai = true")
	Page<NhaCungCap> findAllActive(Pageable pageable);

	Page<NhaCungCap> findByTenNhaCungCapContainingIgnoreCaseAndTrangThaiTrue(String tenNhaCungCap, Pageable pageable);

	Boolean existsByTenNhaCungCapAndTrangThai(String tenNhaCungCap, Boolean trangThai);

	Page<NhaCungCap> findByTrangThaiTrue(Pageable pageable);

	boolean existsByEmailNhaCungCap(String emailNhaCungCap);

	Boolean existsBySdtNhaCungCapAndTrangThai(String sdtNhaCungCap, Boolean trangThai);

	Boolean existsByEmailNhaCungCapAndTrangThai(String emailNhaCungCap, Boolean trangThai);

	List<NhaCungCap> findByTrangThaiTrue();
	
	Optional<NhaCungCap> findByTenNhaCungCapAndTrangThaiTrue(String tenNhaCungCap);


}