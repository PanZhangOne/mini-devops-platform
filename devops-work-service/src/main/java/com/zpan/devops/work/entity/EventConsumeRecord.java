package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "devops_event_consume_record")
public class EventConsumeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventId;

    private String consumerGroup;

    private String eventType;

    private String status;

    private String errorMessage;

    private LocalDateTime consumedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
