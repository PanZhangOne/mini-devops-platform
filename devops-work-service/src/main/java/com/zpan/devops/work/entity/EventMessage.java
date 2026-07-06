package com.zpan.devops.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devops_event_message")
public class EventMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventId;

    private String topic;

    private String eventType;

    private String eventKey;

    private String sourceService;

    private String payloadJson;

    private String status;

    private Integer retryCount;

    private Integer maxRetryCount;

    private LocalDateTime nextRetryAt;

    private LocalDateTime sentAt;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
