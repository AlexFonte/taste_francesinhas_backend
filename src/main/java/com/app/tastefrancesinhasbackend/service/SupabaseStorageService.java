package com.app.tastefrancesinhasbackend.service;

import com.app.tastefrancesinhasbackend.config.SupabaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

// Service que sube/borra ficheros del bucket de Supabase via S3.
// El S3Client viene inyectado desde SupabaseStorageConfig (separamos config de logica).
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final SupabaseProperties props;
    private final S3Client s3Client;

    // Sube el fichero al bucket con el nombre y Content-Type indicados, devuelve la URL publica.
    public String upload(MultipartFile file, String fileName, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(fileName)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicUrl(fileName);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo para subir a Supabase", e);
        }
    }

    // Borra el objeto del bucket a partir de la URL publica que tenemos guardada en BD.
    public void delete(String publicUrl) {
        String fileName = extractFileName(publicUrl);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(props.bucket())
                .key(fileName)
                .build();
        s3Client.deleteObject(request);
    }

    // Devuelve la url descargable de la imagen
    private String buildPublicUrl(String fileName) {
        return "%s/storage/v1/object/public/%s/%s".formatted(props.url(), props.bucket(), fileName);
    }

    private String extractFileName(String publicUrl) {
        return publicUrl.substring(publicUrl.lastIndexOf('/') + 1);
    }
}