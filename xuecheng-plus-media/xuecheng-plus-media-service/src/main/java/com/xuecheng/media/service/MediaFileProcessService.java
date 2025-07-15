package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {

    /**
     * 得到媒体处理列表
     * @param shardIndex
     * @param shardTotal
     * @param processors
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int processors);

    /**
     * 线程抢占任务
     * @param taskId
     * @return
     */
    boolean startTask(Long taskId);

    /**
     * 更新 media_process表 和 media_file表
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param msg
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String msg);





}
