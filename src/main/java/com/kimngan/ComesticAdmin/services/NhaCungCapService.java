package com.kimngan.ComesticAdmin.services;

import java.util.List;
import java.util.Optional;

import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NhaCungCapService {
	// Định nghĩa các CRUD
	List<NhaCungCap> getAll();

	Boolean create(NhaCungCap nhaCungCap);

	NhaCungCap findById(Integer maNhaCungCap);

	Boolean delete(Integer maNhaCungCap); // Xóa bằng cách thay đổi trạng thái

	Boolean update(NhaCungCap nhaCungCap);

	//Boolean existsByTenNhaCungCap(String tenNhaCungCap);
	Boolean existsByTenNhaCungCapAndTrangThai(String tenNhaCungCap, Boolean trangThai);


	// Phân trang
	Page<NhaCungCap> findAll(Pageable pageable);

	// Tìm kiếm nhà cung cấp theo tên
	Page<NhaCungCap> searchByName(String tenNhaCungCap, Pageable pageable);
	
	// Thêm phương thức tìm tất cả nhà cung cấp đang hoạt động
	
	Page<NhaCungCap> findAllActive(Pageable pageable); 
	 
	 
	// Tìm kiếm nhà cung cấp theo ID với Optional
    Optional<NhaCungCap> findByIdOptional(Integer maNhaCungCap);
    Boolean existsByEmailNhaCungCap(String emailNhaCungCap);
    Boolean existsBySdtNhaCungCap(String sdtNhaCungCap);
	//boolean existsByEmailNhaCungCap(String email);
    
    //Thêm phương thức tìm nhà cung cấp có trạng thái hoạt động (true):

    List<NhaCungCap> findByTrangThaiTrue();
    
    List<NhaCungCap> getAllActive();
    
    Optional<NhaCungCap> findByTenNhaCungCapAndTrangThaiTrue(String tenNhaCungCap);
    void deleteById(Integer maNhaCungCap);



}
