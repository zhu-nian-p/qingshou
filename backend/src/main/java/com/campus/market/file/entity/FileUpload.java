package com.campus.market.file.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 图片上传记录（用于每用户配额统计） */
@Data
@TableName("file_upload")
public class FileUpload {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String url;

    private String filename;

    private Long size;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
