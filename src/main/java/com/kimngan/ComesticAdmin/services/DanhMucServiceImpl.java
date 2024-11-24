package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.repository.DanhMucRepository;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DanhMucServiceImpl implements DanhMucService {

	@Autowired
	private DanhMucRepository danhMucRepository;

	@Override
	public List<DanhMuc> getAll() {
		return danhMucRepository.findAll();
	}

	@Override
	public Boolean create(DanhMuc danhMuc) {
		if (danhMucRepository.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
			return false; // Trả về false nếu tên danh mục đã tồn tại
		}
		try {
			danhMucRepository.save(danhMuc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public DanhMuc findById(Integer maDanhMuc) {
		Optional<DanhMuc> optionalDanhMuc = danhMucRepository.findById(maDanhMuc);
		return optionalDanhMuc.orElse(null); // Trả về null nếu không tìm thấy danh mục
	}

	@Override
	public Boolean delete(Integer maDanhMuc) {
		try {
			danhMucRepository.deleteById(maDanhMuc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Boolean update(DanhMuc danhMuc) {
	    // Kiểm tra tên danh mục đã tồn tại, ngoại trừ chính nó
	    DanhMuc existingDanhMuc = danhMucRepository.findByTenDanhMuc(danhMuc.getTenDanhMuc());
	    if (existingDanhMuc != null && !existingDanhMuc.getMaDanhMuc().equals(danhMuc.getMaDanhMuc())) {
	        return false; // Tên danh mục đã tồn tại ở danh mục khác
	    }
	    try {
	        danhMucRepository.save(danhMuc);
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	@Override
	public Boolean existsByTenDanhMuc(String tenDanhMuc) {
	    return danhMucRepository.existsByTenDanhMuc(tenDanhMuc);
	}


	@Override
	public Page<DanhMuc> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return danhMucRepository.findAll(pageable);
	}

	@Override
	public Page<DanhMuc> searchByName(String tenDanhMuc, Pageable pageable) {
		// TODO Auto-generated method stub
		return danhMucRepository.findByTenDanhMucContainingIgnoreCase(tenDanhMuc, pageable);
	}

	

//	@Override
//	public List<DanhMuc> findByTrangThaiTrue() {
//		// TODO Auto-generated method stub
//		return danhMucRepository.findByTrangThaiTrue();
//	}

}
