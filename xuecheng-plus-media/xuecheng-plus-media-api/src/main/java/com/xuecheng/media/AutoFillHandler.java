package com.xuecheng.media;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AutoFillHandler implements MetaObjectHandler {

    // 获取当前用户（实际项目中从安全上下文获取）
    private String getCurrentUser() {
        // 示例：实际项目中替换为从Spring Security或JWT获取
        // return SecurityContextHolder.getContext().getAuthentication().getName();
        return "system_user"; // 示例值
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时填充：createtime, createuser, updatetime, updateuser
        LocalDateTime now = LocalDateTime.now();
        String currentUser = getCurrentUser();

        // 严格填充模式（确保字段存在且类型匹配）
        this.strictInsertFill(metaObject, "createtime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatetime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createuser", String.class, currentUser);
        this.strictInsertFill(metaObject, "updateuser", String.class, currentUser);
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时填充：updatetime, updateuser
        this.strictUpdateFill(metaObject, "updatetime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateuser", String.class, getCurrentUser());
    }
}
