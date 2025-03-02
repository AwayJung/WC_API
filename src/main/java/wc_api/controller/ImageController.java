package wc_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wc_api.service.ImageService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Value("${app.config.file.path}")
    private String uploadPath;

    @GetMapping("/{imageName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) throws IOException {
        System.out.println("============= 이미지 요청 시작 =============");
        System.out.println("요청된 이미지 파일명: " + imageName);

        // 파일 시스템 확인
        File uploadDir = new File(uploadPath);
        File imageFile = new File(uploadDir, imageName);

        System.out.println("이미지 설정 경로: " + uploadPath);
        System.out.println("이미지 절대 경로: " + imageFile.getAbsolutePath());
        System.out.println("업로드 디렉토리 존재 여부: " + uploadDir.exists());
        System.out.println("업로드 디렉토리 읽기 권한: " + uploadDir.canRead());
        System.out.println("이미지 파일 존재 여부: " + imageFile.exists());
        System.out.println("이미지 파일 읽기 권한: " + (imageFile.exists() ? imageFile.canRead() : "파일 없음"));

        // 디렉토리 내용 확인
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            System.out.println("디렉토리 내 파일 목록:");
            File[] files = uploadDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    System.out.println(" - " + file.getName() + " (" + file.length() + " bytes)");
                }
            } else {
                System.out.println("디렉토리가 비어있거나 파일을 나열할 수 없습니다.");
            }
        }

        System.out.flush(); // 버퍼 강제 출력

        try {
            System.out.println("이미지 서비스 호출 시작");
            Resource resource = imageService.loadImageAsResource(imageName);
            System.out.println("이미지 서비스 호출 완료");

            if (resource == null) {
                System.err.println("리소스가 null입니다");
                return ResponseEntity.notFound().build();
            }

            System.out.println("리소스 존재 여부: " + resource.exists());
            System.out.println("리소스 URI: " + resource.getURI());
            System.out.println("리소스 파일명: " + resource.getFilename());
            System.out.flush();

            if (!resource.exists()) {
                System.err.println("리소스가 존재하지 않습니다");
                return ResponseEntity.notFound().build();
            }

            // 파일 확장자로 Content-Type 결정
            String contentType = determineContentType(imageName);
            System.out.println("결정된 Content-Type: " + contentType);

            // 최종 응답 생성
            ResponseEntity<Resource> response = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

            System.out.println("응답 준비 완료. 상태: " + response.getStatusCodeValue());
            System.out.flush();
            return response;
        } catch (Exception e) {
            System.err.println("============= 이미지 로딩 에러 =============");
            System.err.println("요청 이미지: " + imageName);
            System.err.println("에러 메시지: " + e.getMessage());
            System.err.println("에러 원인: " + (e.getCause() != null ? e.getCause().getMessage() : "알 수 없음"));
            e.printStackTrace(System.err);
            System.err.flush();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    private String determineContentType(String filename) {
        if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (filename.toLowerCase().endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    @GetMapping("/check")
    public ResponseEntity<String> checkImageService() {
        return ResponseEntity.ok(imageService.getStorageInfo());
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugImageConfig() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // 파일 경로 정보
            debug.put("uploadPath", uploadPath);

            File uploadDir = new File(uploadPath);
            debug.put("absolutePath", uploadDir.getAbsolutePath());
            debug.put("exists", uploadDir.exists());
            debug.put("isDirectory", uploadDir.exists() && uploadDir.isDirectory());
            debug.put("canRead", uploadDir.canRead());
            debug.put("canWrite", uploadDir.canWrite());

            // 디렉토리 내용
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                File[] files = uploadDir.listFiles();
                if (files != null) {
                    List<Map<String, Object>> fileInfos = Arrays.stream(files)
                            .map(file -> {
                                Map<String, Object> fileInfo = new HashMap<>();
                                fileInfo.put("name", file.getName());
                                fileInfo.put("size", file.length());
                                fileInfo.put("lastModified", file.lastModified());
                                fileInfo.put("readable", file.canRead());
                                return fileInfo;
                            })
                            .collect(Collectors.toList());
                    debug.put("files", fileInfos);
                }
            }

            // 컨트롤러, 서비스 정보
            debug.put("controllerMapping", "/images");
            debug.put("serviceStatus", "active");

            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return ResponseEntity.internalServerError().body(debug);
        }
    }
}