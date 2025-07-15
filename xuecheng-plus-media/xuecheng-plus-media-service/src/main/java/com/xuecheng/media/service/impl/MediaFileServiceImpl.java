package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.properties.MinioProperties;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.MinioUtil;
import io.minio.ComposeSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioUtil minioUtil;

    @Autowired
    MinioProperties minioProperties;

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaProcessMapper mediaProcessMapper;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        if (pageParams.getPageNo() == null || pageParams.getPageSize() == null) {
            pageParams = PageParams.builder()
                    .PageNo(1l)
                    .PageSize(10L)
                    .build();
        }

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }



    /**
     * 获取文件的md5
     * @param file
     * @return
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件默认存储目录路径 年/月/日
     * @return
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String folder = sdf.format(new Date());
        return folder;
    }

    /**
     * 上传文件
     * @param companyId
     * @param uploadFileParamsDto
     * @param absolutePath
     * @return
     */
    @Override
    public UploadFileResultDto uploadFile(
            Long companyId,
            UploadFileParamsDto uploadFileParamsDto,
            String absolutePath) {
        File file = new File(absolutePath);
        if (!file.exists()) {
            throw new XueChengPlusException("文件不存在");
        }
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String fileMd5 = getFileMd5(file);
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath + fileMd5 + extension;
        minioUtil.upload(minioProperties.getBucket().getFiles(), objectName, absolutePath);
        uploadFileParamsDto.setFileSize(file.length());
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, minioProperties.getBucket().getFiles(), objectName);
        //准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(
            Long companyId,
            String fileMd5,
            UploadFileParamsDto uploadFileParamsDto,
            String bucket,
            String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            int insert = mediaFilesMapper.insert(mediaFiles);
            // todo xcplus_content.course_base 也需要变更pic

            if (insert < 0) {
                log.error("保存文件信息到数据库失败, {}", mediaFiles.toString());
                throw new XueChengPlusException("保存文件失败");
            }
            addWaitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功, {}", mediaFiles.toString());
        }
        return mediaFiles;
    }

    /**
     * 添加待处理任务
     * @param mediaFiles
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = minioUtil.getMimeType(extension);
        if ("video/x-msvideo".equals(mimeType)) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }
        String bucket = mediaFiles.getBucket();
        String filePath = mediaFiles.getFilePath();
        return minioUtil.exist(bucket, filePath) ? RestResponse.success(true) : RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        return minioUtil.exist(minioProperties.getBucket().getVideofiles(), chunkFilePath) ?
                RestResponse.success(true) : RestResponse.success(false);
    }

    /**
     * 上传分块文件
     * @param fileMd5
     * @param chunk
     * @param absolutePath
     * @return
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String absolutePath) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunk;
        return minioUtil.upload(minioProperties.getBucket().getVideofiles(), chunkFilePath, absolutePath) ?
                RestResponse.success(true) : RestResponse.success(false);
    }

    /**
     * 合并分块文件
     * @param companyId
     * @param fileMd5
     * @param chunkTotal
     * @param uploadFileParamsDto
     * @return
     */
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) throws IOException {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(minioProperties.getBucket().getVideofiles())
                        .object(chunkFileFolderPath + i)
                        .build()).collect(Collectors.toList());

        String minioUrl = minioProperties.getBucket().getVideofiles() + "/"+ chunkFileFolderPath;

        String filename = uploadFileParamsDto.getFilename();
        String objectName = getFilePathByMd5(fileMd5, filename.substring(filename.lastIndexOf(".")));

        // 1 minio 合并文件
        log.debug("合并文件-{} minio开始合并文件: {}", filename, minioUrl);
        if (!minioUtil.compose(minioProperties.getBucket().getVideofiles(), objectName, sourceObjectList)) {
            log.error("合并文件-{} minio合并文件失败: {}", filename, objectName);
            return RestResponse.validfail(false, "minio合并文件失败");
        }
        log.debug("合并文件-{} minio合并文件完成: {}", filename, minioUrl);

        // 2 下载minio合并后的文件
        log.debug("合并文件-{} 开始下载minio合并后的文件: {}", filename, minioUrl);
        File minioFile = minioUtil.download(minioProperties.getBucket().getVideofiles(), objectName);
        if (minioFile == null) {
            log.error("合并文件-{} 下载minio合并后的文件失败: {}", filename, objectName);
            return RestResponse.validfail(false, "minio合并文件失败");
        }
        log.debug("合并文件-{} 下载minio合并后的文件完成: {}", filename, minioUrl);

        // 3 将合并文件和下载合并后的文件进行md5校验
        log.debug("合并文件-{} 将本地文件和minio合并后的文件进行md5校验: {}", filename, minioUrl);
        InputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(minioFile);
            String md5 = DigestUtils.md5Hex(fileInputStream);
            if (!fileMd5.equals(md5)) {
                log.error("合并文件-{} 本地文件和minio合并后的文件进行md5校验失败: {}", filename, minioUrl);
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败");
            }
            log.debug("合并文件-{} 本地文件和minio合并后的文件进行md5校验完成: {}", filename, minioUrl);
            uploadFileParamsDto.setFileSize(minioFile.length());
            mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto,minioProperties.getBucket().getVideofiles(), objectName);
        } catch (IOException e) {
            log.debug("合并文件-{} 校验文件失败, 文件md5: {}, 异常: {}", filename, fileMd5, e.getMessage());
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败");
        } finally {
            if (minioFile != null) {
                minioFile.delete();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            log.debug("合并文件-{} 开始清理chunk文件: {}", filename, minioUrl);
            if (!minioUtil.existFolder(minioProperties.getBucket().getVideofiles(), chunkFileFolderPath)) {
                log.debug("合并文件-{} 没有可须清理chunk文件: {}", filename, minioUrl);
            }
            minioUtil.deleteFloder(minioProperties.getBucket().getVideofiles(), chunkFileFolderPath);
            log.debug("合并文件-{} 清理chunk文件完成: {}", filename, minioUrl);
        }
        return RestResponse.success(true);
    }



    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 + fileExt;
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/chunk/";
    }


}
