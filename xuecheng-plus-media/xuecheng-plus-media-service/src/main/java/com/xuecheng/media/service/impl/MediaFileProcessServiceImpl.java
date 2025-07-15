package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 得到媒体处理列表
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);
    }

    @Override
    public boolean startTask(Long taskId) {
        return mediaProcessMapper.startTask(taskId) <= 0 ? false : true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String msg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) return;

        if ("3".equals(status)) {
            mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(msg);
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            log.debug("周期任务-{} 更新 media_process表 任务处理状态为失败", mediaProcess.getFilename());
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        log.debug("周期任务-{} 更新 media_process表 任务处理状态为成功", mediaProcess.getFilename());
        log.debug("周期任务-{} 更新 media_files表", mediaProcess.getFilename());
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null) {
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        log.debug("周期任务-{} media_process_history表 新增数据", mediaProcess.getFilename());
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        log.debug("周期任务-{} media_process表 删除数据", mediaProcess.getFilename());
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }


}
