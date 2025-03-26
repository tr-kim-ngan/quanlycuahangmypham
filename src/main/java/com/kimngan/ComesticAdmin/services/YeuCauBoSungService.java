package com.kimngan.ComesticAdmin.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kimngan.ComesticAdmin.entity.YeuCauBoSung;

public interface YeuCauBoSungService {
	
	 void save(YeuCauBoSung yeuCau);
	 List<YeuCauBoSung> getAll();
	 YeuCauBoSung findById(Integer id);
	 Page<YeuCauBoSung> findAll(Pageable pageable);

}
