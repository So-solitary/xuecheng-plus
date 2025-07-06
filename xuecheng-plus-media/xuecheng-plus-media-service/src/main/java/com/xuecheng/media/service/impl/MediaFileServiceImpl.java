package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.properties.MinioProperties;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

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
        minioUtil.upload(minioProperties.getBucket().getFiles(), absolutePath, objectName);
        uploadFileParamsDto.setFileSize(file.length());
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, minioProperties.getBucket().getFiles(), objectName);
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
            mediaFiles.setBucket("/" + bucket + "/" + objectName);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败, {}", mediaFiles.toString());
                throw new XueChengPlusException("保存文件失败");
            }
            log.debug("保存文件信息到数据库成功, {}", mediaFiles.toString());
        }
        return mediaFiles;
    }


}
