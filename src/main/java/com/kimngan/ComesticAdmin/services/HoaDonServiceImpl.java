package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.repository.HoaDonRepository;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Override
    public List<HoaDon> getAllHoaDons() {
        return hoaDonRepository.findAll();
    }

    @Override
    public HoaDon getHoaDonById(Integer id) {
        return hoaDonRepository.findById(id).orElse(null);
    }

    @Override
    public HoaDon saveHoaDon(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }

	@Override
	public HoaDon getHoaDonByDonHang(DonHang donHang) {
		// TODO Auto-generated method stub
		 return hoaDonRepository.findByDonHang(donHang);
	}

	@Override
	public Page<HoaDon> getAllHoaDons(Pageable pageable) {
		// TODO Auto-generated method stub
		 return hoaDonRepository.findAll(pageable);
	}

	@Override
	public List<HoaDon> getHoaDonsByCustomer(String username) {
		// TODO Auto-generated method stub
		 return hoaDonRepository.findByCustomerUsername(username);
	}

	@Override
	public HoaDon findById(Integer id) {
		// TODO Auto-generated method stub
		 return hoaDonRepository.findById(id).orElse(null);
	}
}