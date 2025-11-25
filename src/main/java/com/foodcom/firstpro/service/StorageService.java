package com.foodcom.firstpro.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final Storage storage;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    /**
     * 파일을 GCS에 업로드하고 공개 URL을 반환합니다.
     * @param file 클라이언트로부터 받은 이미지 파일
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

        // 공개 URL 반환 (GCS의 기본 공개 URL 형식)
        // 버킷이 공개 액세스로 설정되어 있어야 정상적으로 접근 가능합니다.
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }
}
