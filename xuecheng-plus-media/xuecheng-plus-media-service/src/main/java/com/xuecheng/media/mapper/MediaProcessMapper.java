package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("select * from media_process " +
            "where id % #{shardTotal} = #{shardIndex} and status in ('1','3') and fail_count < 3 " +
            "limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardIndex")int shardIndex, @Param("shardTotal") int shardTotal, @Param("count") int count);

    @Update("update media_process set status = '4' where status in ('1','3') and fail_count < 3 and id=#{taskId}")
    int startTask(Long taskId);

    @Update("update media_process set status = '3' where status = '4' and fail_count < 3")
    int resetTimeoutTask();
}
