package com.xuecheng.test;

import com.xuecheng.media.properties.MinioProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
public class MinioTest {

    @Autowired
    MinioProperties minioProperties;


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
}
