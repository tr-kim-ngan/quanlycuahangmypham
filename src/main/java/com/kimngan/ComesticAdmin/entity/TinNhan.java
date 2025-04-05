package com.kimngan.ComesticAdmin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TinNhan")
public class TinNhan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id; 


    @Column(columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    
    private LocalDateTime thoiGianGui;

    private boolean daDoc = false;

    @ManyToOne
    @JoinColumn(name = "nguoiGui_id")
    private NguoiDung nguoiGui;

    @ManyToOne
    @JoinColumn(name = "nguoiNhan_id")
    private NguoiDung nguoiNhan;

    
    public TinNhan() {}
    
	
	public TinNhan(Integer id, String noiDung, String hinhAnh, LocalDateTime thoiGianGui, boolean daDoc,
			NguoiDung nguoiGui, NguoiDung nguoiNhan) {
		super();
		this.id = id;
		this.noiDung = noiDung;
		this.hinhAnh = hinhAnh;
		this.thoiGianGui = thoiGianGui;
		this.daDoc = daDoc;
		this.nguoiGui = nguoiGui;
		this.nguoiNhan = nguoiNhan;
	}



	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}

	public String getNoiDung() {
		return noiDung;
	}

	
	public void setNoiDung(String noiDung) {
		this.noiDung = noiDung;
	}

	public LocalDateTime getThoiGianGui() {
		return thoiGianGui;
	}

	public void setThoiGianGui(LocalDateTime thoiGianGui) {
		this.thoiGianGui = thoiGianGui;
	}

	public boolean isDaDoc() {
		return daDoc;
	}

	public void setDaDoc(boolean daDoc) {
		this.daDoc = daDoc;
	}

	public NguoiDung getNguoiGui() {
		return nguoiGui;
	}

	public void setNguoiGui(NguoiDung nguoiGui) {
		this.nguoiGui = nguoiGui;
	}

	public NguoiDung getNguoiNhan() {
		return nguoiNhan;
	}

	public void setNguoiNhan(NguoiDung nguoiNhan) {
		this.nguoiNhan = nguoiNhan;
	}


	public String getHinhAnh() {
		return hinhAnh;
	}


	public void setHinhAnh(String hinhAnh) {
		this.hinhAnh = hinhAnh;
	}



















}
