package com.kimngan.ComesticAdmin.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

	private final String UPLOAD_DIR = "src/main/resources/static/upload/";

	public String storeFile(MultipartFile file) throws IOException {
	    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
	    Path filePath = Paths.get(UPLOAD_DIR + fileName);
	    Files.createDirectories(filePath.getParent()); // Tạo thư mục nếu chưa tồn tại
	    Files.write(filePath, file.getBytes()); // Ghi dữ liệu file
	    return fileName; // Trả về tên file đã lưu
	}
}
