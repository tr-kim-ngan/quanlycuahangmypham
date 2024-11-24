package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import com.kimngan.ComesticAdmin.repository.ChiTietDonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChiTietDonHangServiceImpl implements ChiTietDonHangService {

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Override
    public ChiTietDonHang createChiTietDonHang(ChiTietDonHang chiTietDonHang) {
        return chiTietDonHangRepository.save(chiTietDonHang);
    }

    @Override
    public ChiTietDonHang updateChiTietDonHang(ChiTietDonHang chiTietDonHang) {
        if (chiTietDonHangRepository.existsById(chiTietDonHang.getId())) {
            return chiTietDonHangRepository.save(chiTietDonHang);
        }
        throw new RuntimeException("Chi tiết đơn hàng không tồn tại với mã: " + chiTietDonHang.getId());
    }

    @Override
    public void deleteChiTietDonHang(ChiTietDonHangId id) {
        if (chiTietDonHangRepository.existsById(id)) {
            chiTietDonHangRepository.deleteById(id);
        } else {
            throw new RuntimeException("Chi tiết đơn hàng không tồn tại với mã: " + id);
        }
    }

    @Override
    public ChiTietDonHang getChiTietDonHangById(ChiTietDonHangId id) {
        return chiTietDonHangRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Chi tiết đơn hàng không tồn tại với mã: " + id)
        );
    }

    @Override
    public List<ChiTietDonHang> getChiTietDonHangsByDonHangId(Integer maDonHang) {
        // Giả sử bạn thêm query để tìm theo mã đơn hàng
        return chiTietDonHangRepository.findAll(); // Thay bằng custom query nếu cần
    }

    @Override
    public ChiTietDonHang save(ChiTietDonHang chiTietDonHang) {
        return chiTietDonHangRepository.save(chiTietDonHang);
    }
}