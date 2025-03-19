package com.kimngan.ComesticAdmin.services;
import com.kimngan.ComesticAdmin.entity.KiemKeKho;
import com.kimngan.ComesticAdmin.repository.KiemKeKhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class KiemKeKhoServiceImpl implements KiemKeKhoService {

	 @Autowired
	    private KiemKeKhoRepository kiemKeKhoRepository;

	    @Override
	    public List<KiemKeKho> getAll() {
	        return kiemKeKhoRepository.findAll();
	    }

	    @Override
	    public List<KiemKeKho> getByShiftId(Integer maLichSu) {
	        return kiemKeKhoRepository.findByLichSuCaLamViec_MaLichSu(maLichSu);
	    }

	    @Override
	    public void save(KiemKeKho kiemKeKho) {
	        kiemKeKhoRepository.save(kiemKeKho);
	    }

		@Override
		public List<KiemKeKho> findByLichSuCaLamViecId(Integer maLichSu) {
			// TODO Auto-generated method stub
	        return kiemKeKhoRepository.findByLichSuCaLamViec_MaLichSu(maLichSu);

		}

		@Override
		public int getDeltaKiemKe(Integer maSanPham) {
			// TODO Auto-generated method stub
			Integer delta = kiemKeKhoRepository.getDeltaKiemKe(maSanPham);
		    return (delta != null) ? delta : 0;
		}

		@Override
		public Page<KiemKeKho> findByDaXetDuyet(Boolean daXetDuyet, Pageable pageable) {
			// TODO Auto-generated method stub
			return kiemKeKhoRepository.findByDaXetDuyet(daXetDuyet, pageable);
		}

		@Override
		public int countAllByShift(Integer maLichSu) {
			// TODO Auto-generated method stub
		    return kiemKeKhoRepository.countAllByShift(maLichSu);
		}

		@Override
		public Integer getLastApprovedStock(Integer maSanPham) {
			// TODO Auto-generated method stub
			return kiemKeKhoRepository.getLastApprovedStock(maSanPham);
		}

		
}
