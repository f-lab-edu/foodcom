package com.foodcom.firstpro.config;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcsConfig {

    @Value("${gcp.storage.key-file-path:#{null}}")
    private String keyFilePath;

    @Bean
    public Storage storage() throws IOException {
        Credentials credentials;

        if (keyFilePath != null && !keyFilePath.isEmpty()) {
            InputStream inputStream;
            System.out.println("Initializing GCS Storage using Key File: " + keyFilePath);

            // classpath: 접두사 처리 (Docker/Cloud Run 환경용)
            if (keyFilePath.startsWith("classpath:")) {
                String path = keyFilePath.substring("classpath:".length());
                inputStream = new ClassPathResource(path).getInputStream();
            } else {
                inputStream = new FileInputStream(keyFilePath);
            }
            credentials = GoogleCredentials.fromStream(inputStream);
        } else {
            System.out.println("Initializing GCS Storage using Application Default Credentials (ADC)");
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}