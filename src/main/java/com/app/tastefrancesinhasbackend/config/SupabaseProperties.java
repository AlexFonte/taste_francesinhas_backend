package com.app.tastefrancesinhasbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(
        String url,
        String bucket,
        S3 s3
) {
    public record S3(
            String endpoint,
            String region,
            String accessKey,
            String secretKey
    ) {
    }
}