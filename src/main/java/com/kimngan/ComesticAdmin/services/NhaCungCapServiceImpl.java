package com.kimngan.ComesticAdmin.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import com.kimngan.ComesticAdmin.repository.NhaCungCapRepository;

@Service
public class NhaCungCapServiceImpl implements NhaCungCapService {

	@Autowired
	private NhaCungCapRepository nhaCungCapRepository;

	@Override
	public List<NhaCungCap> getAll() {
		// TODO Auto-generated method stub
		return nhaCungCapRepository.findAll();
	}

	@Override
	public Boolean create(NhaCungCap nhaCungCap) {
		// Kiểm tra xem nhà cung cấp có cùng tên đã tồn tại hay chưa (đang hoạt động)
		if (nhaCungCapRepository.existsByTenNhaCungCapAndTrangThai(nhaCungCap.getTenNhaCungCap(), true)) {
			return false; // Nhà cung cấp đã tồn tại và đang hoạt động
		}
		try {
			nhaCungCap.setTrangThai(true); // Đảm bảo trạng thái được đặt là hoạt động khi tạo mới
			nhaCungCapRepository.save(nhaCungCap);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	// để làm thống kê 

	@Override
	public NhaCungCap findById(Integer maNhaCungCap) {
		// TODO Auto-generated method stub
		return nhaCungCapRepository.findById(maNhaCungCap).orElse(null);
	}

	@Override
	public Boolean delete(Integer maNhaCungCap) {
		// TODO Auto-generated method stub
		NhaCungCap nhaCungCap = nhaCungCapRepository.findById(maNhaCungCap).orElse(null);
		if (nhaCungCap != null) {
			nhaCungCap.setTrangThai(false);
			nhaCungCapRepository.save(nhaCungCap);
			return true;
		}
		return false;
	}

	@Override
	public Boolean update(NhaCungCap nhaCungCap) {
		// TODO Auto-generated method stub
		if (nhaCungCapRepository.existsById(nhaCungCap.getMaNhaCungCap())) {
            nhaCungCapRepository.save(nhaCungCap);
            return true;
        }
        return false;
	}

	@Override
	 public Boolean existsByTenNhaCungCapAndTrangThai(String tenNhaCungCap, Boolean trangThai) {
        return nhaCungCapRepository.existsByTenNhaCungCapAndTrangThai(tenNhaCungCap, trangThai);
    }

	@Override
	public Page<NhaCungCap> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return nhaCungCapRepository.findAll(pageable);
	}

	@Override
	public Page<NhaCungCap> searchByName(String tenNhaCungCap, Pageable pageable) {
		// TODO Auto-generated method stub
		return nhaCungCapRepository.findByTenNhaCungCapContainingIgnoreCaseAndTrangThaiTrue(tenNhaCungCap, pageable);
	}

	@Override
	public Page<NhaCungCap> findAllActive(Pageable pageable) {
		// TODO Auto-generated method stub
		 return nhaCungCapRepository.findByTrangThaiTrue(pageable);
	}

	@Override
	public Optional<NhaCungCap> findByIdOptional(Integer maNhaCungCap) {
		// TODO Auto-generated method stub
		 return nhaCungCapRepository.findById(maNhaCungCap);
	}
	@Override
    public Boolean existsBySdtNhaCungCap(String sdtNhaCungCap) {
        // Chỉ kiểm tra những nhà cung cấp có trạng thái là 1
        return nhaCungCapRepository.existsBySdtNhaCungCapAndTrangThai(sdtNhaCungCap, true);
    }
	@Override
    public Boolean existsByEmailNhaCungCap(String emailNhaCungCap) {
        // Chỉ kiểm tra những nhà cung cấp có trạng thái là 1
        return nhaCungCapRepository.existsByEmailNhaCungCapAndTrangThai(emailNhaCungCap, true);
    }

	@Override
	public List<NhaCungCap> findByTrangThaiTrue() {
		// TODO Auto-generated method stub
		return nhaCungCapRepository.findByTrangThaiTrue();
	}

	@Override
	public List<NhaCungCap> getAllActive() {
		// TODO Auto-generated method stub
		return nhaCungCapRepository.findByTrangThaiTrue(); // Lấy nhà cung cấp có trạng thái = 1 (active)
	}

	@Override
	public Optional<NhaCungCap> findByTenNhaCungCapAndTrangThaiTrue(String tenNhaCungCap) {
		// TODO Auto-generated method stub
	    return nhaCungCapRepository.findByTenNhaCungCapAndTrangThaiTrue(tenNhaCungCap);
	}
	
	

}
