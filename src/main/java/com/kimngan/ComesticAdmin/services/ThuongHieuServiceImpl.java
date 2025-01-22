package com.kimngan.ComesticAdmin.services;
import com.kimngan.ComesticAdmin.entity.ThuongHieu;
import com.kimngan.ComesticAdmin.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ThuongHieuServiceImpl implements ThuongHieuService {

	 @Autowired
	    private ThuongHieuRepository thuongHieuRepository;
	
	@Override
	public List<ThuongHieu> getAll() {
		// TODO Auto-generated method stub
	    return thuongHieuRepository.findAll(Sort.by(Sort.Direction.DESC, "maThuongHieu"));

	}

	@Override
	public Boolean create(ThuongHieu thuongHieu) {
		// TODO Auto-generated method stub
		try {
            thuongHieuRepository.save(thuongHieu);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

	@Override
	public ThuongHieu findById(Integer maThuongHieu) {
		// TODO Auto-generated method stub
		return thuongHieuRepository.findById(maThuongHieu).orElse(null);
	}

	@Override
	public Boolean delete(Integer maThuongHieu) {
		// TODO Auto-generated method stub
		 try {
	            thuongHieuRepository.deleteById(maThuongHieu);
	            return true;
	        } catch (Exception e) {
	            return false;
	        }
	}

	@Override
	public Boolean update(ThuongHieu thuongHieu) {
		// TODO Auto-generated method stub
		try {
            thuongHieuRepository.save(thuongHieu);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

	@Override
	public Boolean existsByTenThuongHieu(String tenThuongHieu) {
		// TODO Auto-generated method stub
		 return thuongHieuRepository.existsByTenThuongHieu(tenThuongHieu);
	}

	@Override
	public Page<ThuongHieu> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
	    return thuongHieuRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "maThuongHieu")));
	}

	@Override
	public Page<ThuongHieu> searchByName(String tenThuongHieu, Pageable pageable) {
		// TODO Auto-generated method stub
        return thuongHieuRepository.findByTenThuongHieuContainingIgnoreCase(tenThuongHieu, pageable);
	}

	@Override
	public boolean hasProducts(Integer brandId) {
		// TODO Auto-generated method stub
		  ThuongHieu thuongHieu = thuongHieuRepository.findById(brandId).orElse(null);
	        return thuongHieu != null && thuongHieu.getSanPhams() != null && !thuongHieu.getSanPhams().isEmpty();

	}

	@Override
	public List<ThuongHieu> getAllBrands() {
		// TODO Auto-generated method stub
		return thuongHieuRepository.findAll();
	}

}
