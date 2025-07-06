package com.xuecheng.media.utils;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;

import java.io.FileOutputStream;
import java.io.FilterInputStream;

@AllArgsConstructor
@Slf4j
public class MinioUtil {

    MinioClient minioClient;


    private String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(extension == null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    public void upload(String bucket, String objectName, String fileName) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//添加子目录
                    .filename(fileName)
                    .contentType(getMimeType(fileName))//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            log.debug("上传文件到minio成功, bucket:{}, objectName:{}",bucket,objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错, bucket:{}, objectName:{}, 错误原因:{}",bucket,objectName,e.getMessage(),e);
        }
    }

    public void delete(String bucket, String objectName){
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    public void download(String bucket, String objectName, String fileName) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(fileName);
        ) {
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
