
package com.kimngan.ComesticAdmin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.DonViTinh;
import com.kimngan.ComesticAdmin.repository.DonViTinhRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DonViTinhServiceImpl implements DonViTinhService {

	@Autowired
	private DonViTinhRepository donViTinhRepository;

	@Override
	public Page<DonViTinh> searchByName(String name, PageRequest pageRequest) {
		// TODO Auto-generated method stub
		return donViTinhRepository.findByTenDonViContainingIgnoreCase(name, pageRequest);
	}

	@Override
	public Page<DonViTinh> findAll(PageRequest pageRequest) {
		// TODO Auto-generated method stub
		return donViTinhRepository.findAll(pageRequest);
	}

	@Override
	public Optional<DonViTinh> findById(Integer id) {
		// TODO Auto-generated method stub
		return donViTinhRepository.findById(id);
	}

	@Override
	public DonViTinh create(DonViTinh donViTinh) {
		// TODO Auto-generated method stub
		return donViTinhRepository.save(donViTinh);
	}

	@Override
	public Boolean existsByTenDonVi(String tenDonVi) {
		// TODO Auto-generated method stub
		return donViTinhRepository.existsByTenDonVi(tenDonVi);
	}

	@Override
	public Boolean update(DonViTinh donViTinh) {
	    Optional<DonViTinh> existingDonViTinh = donViTinhRepository.findById(donViTinh.getMaDonVi());
	    if (existingDonViTinh.isPresent()) {
	        DonViTinh updatedDonViTinh = existingDonViTinh.get();
	        updatedDonViTinh.setTenDonVi(donViTinh.getTenDonVi());
	        donViTinhRepository.save(updatedDonViTinh);
	        return true;
	    }
	    return false;
	}

	@Override
	public Optional<DonViTinh> findByTenDonVi(String tenDonVi) {
		// TODO Auto-generated method stub
		 return donViTinhRepository.findByTenDonVi(tenDonVi);	}

	@Override
	public List<DonViTinh> getAll() {
		// TODO Auto-generated method stub
		return donViTinhRepository.findAll();
	}



}
