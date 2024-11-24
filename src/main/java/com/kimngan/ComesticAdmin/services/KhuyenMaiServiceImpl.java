package com.kimngan.ComesticAdmin.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.KhuyenMaiRepository;
@Service
public class KhuyenMaiServiceImpl implements  KhuyenMaiService{
	
	 @Autowired
	    private KhuyenMaiRepository khuyenMaiRepository;

	@Override
	public KhuyenMai create(KhuyenMai khuyenMai) {
		khuyenMai.setTrangThai(true); // Đảm bảo trạng thái là hoạt động khi tạo mới
        return khuyenMaiRepository.save(khuyenMai);
	}

	@Override
	public KhuyenMai update(KhuyenMai khuyenMai) {
		 // Kiểm tra xem khuyến mãi có tồn tại không
        Optional<KhuyenMai> optionalKhuyenMai = khuyenMaiRepository.findById(khuyenMai.getMaKhuyenMai());
        if (optionalKhuyenMai.isPresent()) {
            KhuyenMai existingKhuyenMai = optionalKhuyenMai.get();
            existingKhuyenMai.setTenKhuyenMai(khuyenMai.getTenKhuyenMai());
            existingKhuyenMai.setMoTa(khuyenMai.getMoTa());
            existingKhuyenMai.setPhanTramGiamGia(khuyenMai.getPhanTramGiamGia());
            existingKhuyenMai.setNgayBatDau(khuyenMai.getNgayBatDau());
            existingKhuyenMai.setNgayKetThuc(khuyenMai.getNgayKetThuc());
         // Cập nhật danh sách sản phẩm liên kết với khuyến mãi (ManyToMany)
         // Cập nhật danh sách sản phẩm
            existingKhuyenMai.getSanPhams().clear(); // Xóa sản phẩm hiện tại
            for (SanPham sp : khuyenMai.getSanPhams()) {
                existingKhuyenMai.getSanPhams().add(sp); // Thêm sản phẩm mới
                sp.getKhuyenMais().add(existingKhuyenMai); // Đồng bộ cả hai chiều
            }

            return khuyenMaiRepository.save(existingKhuyenMai);
        }
        return null; // Không tìm thấy khuyến mãi
	}

	@Override
	public Boolean delete(Integer maKhuyenMai) {
		Optional<KhuyenMai> khuyenMaiOpt = khuyenMaiRepository.findById(maKhuyenMai);
        if (khuyenMaiOpt.isPresent()) {
            KhuyenMai khuyenMai = khuyenMaiOpt.get();
            khuyenMai.setTrangThai(false); // Chuyển trạng thái về 0 (ngừng hoạt động)
            khuyenMaiRepository.save(khuyenMai);
            return true;
        }
        return false;
	}
	 

	

	@Override
	public KhuyenMai findById(Integer maKhuyenMai) {
		 // Trả về null nếu không tìm thấy
        return khuyenMaiRepository.findById(maKhuyenMai).orElse(null); 
       

	}

	 @Override
	    public Page<KhuyenMai> findAllActive(Pageable pageable) {
	        return khuyenMaiRepository.findByTrangThaiTrue(pageable);
	    }

	@Override
	public Page<KhuyenMai> searchByName(String keyword, Pageable pageable) {
		// TODO Auto-generated method stub
        return khuyenMaiRepository.findByTenKhuyenMaiContainingIgnoreCaseAndTrangThai(keyword,true, pageable);
	}

	@Override
	public List<KhuyenMai> findByTrangThai(Boolean trangThai) {
		// TODO Auto-generated method stub
		return null;
	}

}
