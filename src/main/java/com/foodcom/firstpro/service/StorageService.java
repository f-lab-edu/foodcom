package com.foodcom.firstpro.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final Storage storage;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    /**
     * @param file       클라이언트로부터 받은 이미지 파일
     * @param pathPrefix 버킷 내에서 파일을 분류할 경로 (예: "post-images")
     * @return 파일의 공개 URL
     */
    public String uploadFile(MultipartFile file, String pathPrefix) throws IOException {

        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String fileName = pathPrefix + "/" + uuid + "-" + originalFilename;

        // GCS에 업로드할 Blob 정보 생성
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        // GCS에 파일 업로드
        storage.create(blobInfo, file.getBytes());

        // CDN URL 반환 (Load Balancer IP 사용)
        return String.format("http://34.49.218.153/%s", fileName);
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // CDN URL: http://34.49.218.153/post-images/uuid-file.jpg
            String splitStr = "http://34.49.218.153/";

            // 기존 GCS URL도 처리 가능하도록 호환성 유지 (혹시나 해서)
            if (fileUrl.startsWith("https://storage.googleapis.com/" + bucketName + "/")) {
                splitStr = "https://storage.googleapis.com/" + bucketName + "/";
            } else if (!fileUrl.startsWith(splitStr)) {
                log.warn("지원되지 않는 URL 형식입니다: {}", fileUrl);
                return;
            }

            String blobName = fileUrl.replace(splitStr, "");

            boolean deleted = storage.delete(BlobId.of(bucketName, blobName));

            if (deleted) {
                log.info("GCS 파일 삭제 성공: {}", blobName);
            } else {
                log.warn("GCS 파일 삭제 실패 (파일 없음): {}", blobName);
            }
        } catch (Exception e) {
            log.error("GCS 파일 삭제 중 오류 발생: {}", fileUrl, e);
        }
    }
}
