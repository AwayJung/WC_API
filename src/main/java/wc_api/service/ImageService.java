package wc_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    private final Path fileStorageLocation;
    private final String uploadDirPath;

    public ImageService(@Value("${app.config.file.path}") String uploadDir) {
        this.uploadDirPath = uploadDir;
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("이미지 저장 경로 생성 완료: " + this.fileStorageLocation);
        } catch (Exception ex) {
            System.err.println("이미지 저장 경로 생성 실패: " + ex.getMessage());
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일명 생성 (충돌 방지를 위해 UUID 사용)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String imageName = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path targetLocation = this.fileStorageLocation.resolve(imageName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("이미지 저장 완료: " + targetLocation);

        return imageName;
    }

    public Resource loadImageAsResource(String imageName) throws IOException {
        System.out.println("이미지 로딩 시작: " + imageName);
        System.out.println("저장 경로: " + this.fileStorageLocation.toString());

        Path filePath = this.fileStorageLocation.resolve(imageName).normalize();
        System.out.println("전체 파일 경로: " + filePath.toString());
        System.out.println("파일 존재 여부: " + Files.exists(filePath));
        System.out.println("파일 접근 가능 여부: " + Files.isReadable(filePath));

        // 디렉토리 내 모든 파일 목록 출력
        File directory = new File(uploadDirPath);
        if (directory.exists()) {
            System.out.println("디렉토리 존재함. 파일 목록:");
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println(" - " + file.getName() + " (크기: " + file.length() + "바이트)");
                }
            }
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + imageName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + imageName, ex);
        }
    }

    public void deleteImage(String imageName) throws IOException {
        if (imageName == null || imageName.isEmpty()) {
            return;
        }

        Path targetLocation = this.fileStorageLocation.resolve(imageName);
        Files.deleteIfExists(targetLocation);
        System.out.println("이미지 삭제 완료: " + targetLocation);
    }

    public String getStorageInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Storage location: ").append(fileStorageLocation.toString()).append("\n");
        info.append("Absolute path: ").append(fileStorageLocation.toAbsolutePath().toString()).append("\n");
        info.append("Directory exists: ").append(Files.exists(fileStorageLocation)).append("\n");

        try {
            info.append("Files in directory:\n");
            Files.list(fileStorageLocation).forEach(path -> {
                info.append(" - ").append(path.getFileName()).append("\n");
                try {
                    info.append("   Size: ").append(Files.size(path)).append(" bytes\n");
                    info.append("   Readable: ").append(Files.isReadable(path)).append("\n");
                } catch (IOException e) {
                    info.append("   Error getting file info: ").append(e.getMessage()).append("\n");
                }
            });
        } catch (IOException e) {
            info.append("Error listing files: ").append(e.getMessage()).append("\n");
        }

        return info.toString();
    }
}