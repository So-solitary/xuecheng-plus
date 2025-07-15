package com.xuecheng.media.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.MinioUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoJob {

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MinioUtil minioUtil;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @Autowired
    MediaFileService mediaFileService;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws InterruptedException {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        int processors = Runtime.getRuntime().availableProcessors();
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        int size = mediaProcessList.size();
        log.debug("周期任务-取出待处理得任务数: {}", size);

        if (size <= 0) return;

        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                try {
                    Long taskId = mediaProcess.getId();
                    boolean res = mediaFileProcessService.startTask(taskId);
                    if (!res) {
                        log.debug("周期任务 未抢到任务");
                        return;
                    }

                    String filename = mediaProcess.getFilename();
                    log.debug("周期任务-{} 开始执行转码任务", filename);


                    log.debug("周期任务-{} 开始从minio下载待处理文件", filename);
                    File originalFile = minioUtil.download(mediaProcess.getBucket(), mediaProcess.getFilePath());
                    if (originalFile == null) {
                        log.error("周期任务-{} 下载待处理文件失败, minio路径为 {}", filename, mediaProcess.getBucket() + "/" + mediaProcess.getFilePath());
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "下载待处理文件失败");
                        return;
                    }
                    log.debug("周期任务-{} 从minio下载待处理文件完成", filename);


                    log.debug("周期任务-{} 开始为转码创建临时文件", filename);
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("mp4_", ".mp4");
                    } catch (IOException e) {
                        log.error("周期任务-创建mp4临时文件失败");
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "创建mp4临时文件失败");
                        return;
                    }
                    log.debug("周期任务-{} 为转码创建临时文件完成", filename);


                    log.debug("周期任务-{} 开始执行转码任务", filename);
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                    if (!"success".equals(mp4VideoUtil.generateMp4())) {
                        log.error("周期任务-{} 执行转码任务失败", filename);
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "执行转码任务失败");
                        return;
                    }
                    log.debug("周期任务-{} 执行转码任务成功", filename);


                    log.debug("周期任务-{} 开始将转码文件上传minio", filename);
                    String objectName = getFilePath(mediaProcess.getFileId(), ".mp4");
                    if (!minioUtil.upload(mediaProcess.getBucket(), objectName, mp4File.getAbsolutePath())) {
                        log.error("周期任务-{} 转码文件上传minio失败", filename);
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "转码文件上传minio失败");
                        return;
                    }
                    log.debug("周期任务-{} 转码文件上传minio完成", filename);

                    log.debug("周期任务-{} 开始更新表", filename);
                    String url = "/" + mediaProcess.getBucket() + "/" + objectName;
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", mediaProcess.getFileId(), url, null);
                } finally {
                    countDownLatch.countDown();
                }

            });
        });
        countDownLatch.await(1, TimeUnit.MINUTES);
        // 如果转码时间超时，则任务状态会一直为4，需要另外启动一个任务，为其转码
    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
}
