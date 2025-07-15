package com.xuecheng.media.utils;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class MinioUtil {

    MinioClient minioClient;


    public String getMimeType(String fileName) {
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

    public boolean upload(String bucket, String objectName, String fileName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//添加子目录
                    .filename(fileName)
                    .contentType(getMimeType(fileName))//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功, bucket: {}, objectName: {}",bucket,objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错, bucket: {}, objectName: {}, 错误原因: {}",bucket,objectName,e.getMessage(),e);
            return false;
        }
    }

    public void delete(String bucket, String objectName){
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
            log.debug("minio file: {} 删除成功", objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("minio file: {} 删除失败", objectName);
        }
    }

    public void deleteFloder(String bucket, String folderName){
        try {

            // 列出指定前缀下的所有对象
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(folderName)
                            .recursive(true)  // 设置为 true 以递归列出所有子对象
                            .build()
            );

            // 删除所有列出的对象
            for (Result<Item> result : results) {
                Item item = result.get();
                delete(bucket, item.objectName());
                System.out.println("Deleted: " + item.objectName());
            }

            System.out.println("All objects in the folder have been deleted.");
        } catch (Exception e) {
            System.err.println("Error occurred: " + e);
            e.printStackTrace();
        }
    }


    public File download(String bucket, String objectName, File file) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(file);
        ) {
            IOUtils.copy(inputStream,outputStream);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public File download(String bucket, String objectName){
        try {
            return download(bucket, objectName, File.createTempFile("minio_", ".merge"));
        } catch (Exception e) {
            throw new XueChengPlusException("创建临时文件失败");
        }
    }

    public boolean exist(String bucket, String objectName)  {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build();
        try {
            minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.info("bucket: {}, objectName: {} 不存在", bucket, objectName);
            return false;
        }
        return true;
    }

    public boolean existFolder(String bucket, String folderName) {
        boolean exists = false;
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(folderName)
                        .maxKeys(1) // 只需检查第一个对象
                        .recursive(false)
                        .build()
        );

        // 检查是否有对象匹配前缀
        for (Result<Item> result : results) {
            exists = true;
            break; // 找到第一个对象即可退出
        }
        return exists;
    }

    public boolean compose(String bucket, String objectName, List<ComposeSource> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        ComposeObjectArgs response = ComposeObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .sources(list)
                .build();
        try {
            minioClient.composeObject(response);
            log.debug("合并文件成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.debug("合并文件失败: objectName {}, 异常 {}", objectName, e.getMessage());
        }
        return false;
    }

}
