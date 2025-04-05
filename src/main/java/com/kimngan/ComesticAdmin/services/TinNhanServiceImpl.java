package com.kimngan.ComesticAdmin.services;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.TinNhan;
import com.kimngan.ComesticAdmin.repository.TinNhanRepository;

@Service
public class TinNhanServiceImpl implements TinNhanService {

	@Autowired
    private TinNhanRepository tinNhanRepository;
	
	@Override
	public List<TinNhan> getTinNhanGiuaHaiNguoi(Integer userId1, Integer userId2) {
		// TODO Auto-generated method stub
		 return tinNhanRepository.findTinNhanGiuaHaiNguoi(userId1, userId2);	}

	@Override
	public TinNhan guiTinNhan(TinNhan tinNhan) {
		// TODO Auto-generated method stub
		tinNhan.setThoiGianGui(LocalDateTime.now());
        return tinNhanRepository.save(tinNhan);
	}

	 @Override
	    public void danhDauDaDocTheoCuocHoiThoai(Integer nguoiGuiId, Integer nguoiNhanId) {
	        List<TinNhan> dsTin = tinNhanRepository.findTinNhanGiuaHaiNguoi(nguoiGuiId, nguoiNhanId);
	        for (TinNhan tin : dsTin) {
	            if (!tin.isDaDoc() && tin.getNguoiNhan().getMaNguoiDung().equals(nguoiNhanId)) {
	                tin.setDaDoc(true);
	            }
	        }
	        tinNhanRepository.saveAll(dsTin);
	    }

	    @Override
	    public Long demTinChuaDoc(Integer userId) {
	        return tinNhanRepository.countTinChuaDoc(userId);
	    }

		@Override
		public List<NguoiDung> findAllCustomersWhoChattedWithSeller(Integer sellerId) {
			// TODO Auto-generated method stub
		    return tinNhanRepository.findAllCustomersWhoChattedWithSeller(sellerId);
		}
		
		@Override
		public long demSoKhachHangChuaDoc(Integer sellerId) {
		    return tinNhanRepository.countDistinctNguoiGuiIdByNguoiNhanIdAndDaDocFalse(sellerId);
		}
		@Override
		public long demTinChuaDocGiua(Integer nguoiGuiId, Integer nguoiNhanId) {
		    return tinNhanRepository.demTinChuaDocGiua(nguoiGuiId, nguoiNhanId);
		}

		@Override
		public List<NguoiDung> findAllCustomersWhoChattedWithAnySeller() {
		    return tinNhanRepository.findAllCustomersWhoChattedWithAnySeller();
		}
		
		@Override
		public List<TinNhan> getTinNhanGiuaKhachVaTatCaNhanVien(Integer customerId) {
		    // Lấy tất cả tin nhắn mà khách là người gửi hoặc người nhận
		    List<TinNhan> all = tinNhanRepository.findTinNhanVoiTatCaNhanVien(customerId);
		    
		    // Sắp xếp theo thời gian gửi tăng dần (nếu repo chưa xử lý)
		    all.sort(Comparator.comparing(TinNhan::getThoiGianGui));

		    return all;
		}
		
		@Override
		public List<TinNhan> getTatCaTinNhanVoiKhach(Integer maKhachHang) {
		    return tinNhanRepository.findTinNhanVoiTatCaNhanVien(maKhachHang);
		}
		
		@Override
		@Transactional
		public void danhDauDaDocTuTatCaNhanVienChoKhach(Integer maKhach) {
			tinNhanRepository.danhDauDaDocTuTatCaNhanVien(maKhach);
		}


}
