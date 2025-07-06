package com.xuecheng.media.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "minio")  // 绑定 minio 前缀的配置
@Data
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private Bucket bucket;  // 三级嵌套配置

    // 三级嵌套类
    @Data
    public static class Bucket {
        private String files;      // 对应 minio.bucket.files
        private String videofiles; // 对应 minio.bucket.videofiles
    }

}
