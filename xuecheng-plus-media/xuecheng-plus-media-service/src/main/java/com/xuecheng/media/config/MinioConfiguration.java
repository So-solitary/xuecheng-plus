package com.xuecheng.media.config;

import com.xuecheng.media.properties.MinioProperties;
import com.xuecheng.media.utils.MinioUtil;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinioConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioUtil minioUtil(MinioProperties minioProperties) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        return new MinioUtil(minioClient);
    }
}
