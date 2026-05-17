package com.app.tastefrancesinhasbackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

// Configura el S3Client como bean para que sea inyectable en cualquier service.
// Lee toda la config (endpoint, region, credenciales) de SupabaseProperties para no hardcodear nada.
// forcePathStyle(true) es imprescindible con Supabase: usa /<bucket>/<key> en vez de subdominios virtual-hosted.
@Configuration
@RequiredArgsConstructor
public class SupabaseStorageConfig {

    private final SupabaseProperties props;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(props.s3().endpoint()))
                .region(Region.of(props.s3().region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.s3().accessKey(), props.s3().secretKey())))
                .forcePathStyle(true)
                .build();
    }
}