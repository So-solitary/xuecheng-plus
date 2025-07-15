package com.xuecheng.test;

import com.xuecheng.media.properties.MinioProperties;
import com.xuecheng.media.utils.MinioUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
public class MinioTest {

    @Autowired
    MinioProperties minioProperties;

    @Autowired
    MinioUtil minioUtil;

    @Test
    public void test01() {
        System.out.println(minioProperties.getAccessKey());
        System.out.println(minioProperties.getBucket().getFiles());
    }

    @Test
    public void test02() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String folder = sdf.format(new Date());
        System.out.println(folder);
    }

    @Test
    public void test03() {
        minioUtil.upload("mediafiles",
                "2025/07/07/sp20250707_001553_998.png",
                "C:\\software\\Snipaste-2.9.1-Beta-x64\\history\\Z71DP2\\sp20250707_001553_998.png");
    }
}
