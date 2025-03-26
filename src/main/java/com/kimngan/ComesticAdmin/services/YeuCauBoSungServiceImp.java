package com.kimngan.ComesticAdmin.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.YeuCauBoSung;
import com.kimngan.ComesticAdmin.repository.YeuCauBoSungRepository;

@Service
public class YeuCauBoSungServiceImp implements YeuCauBoSungService{
	@Autowired
    private YeuCauBoSungRepository repository;

	@Override
	public void save(YeuCauBoSung yeuCau) {
		 repository.save(yeuCau);		
	}

	@Override
	public List<YeuCauBoSung> getAll() {
		// TODO Auto-generated method stub
        return repository.findAll();
	}

	@Override
	public YeuCauBoSung findById(Integer id) {
		// TODO Auto-generated method stub
		 return repository.findById(id).orElse(null);
	}

	@Override
	public Page<YeuCauBoSung> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
	    return repository.findAll(pageable);
	}

}
