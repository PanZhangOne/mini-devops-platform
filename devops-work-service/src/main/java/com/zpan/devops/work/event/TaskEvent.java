package com.zpan.devops.work.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TaskEvent {    /**

 * 事件 ID。
 * 用于后续幂等消费。
 */
private String eventId;
    /**
     * 事件类型。
     */
    private String eventType;
    /**
     * 来源服务。
     */
    private String sourceService;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 任务 ID。
     */
    private Long taskId;

    /**
     * 任务编号。
     */
    private String taskNo;

    /**
     * 操作用户 ID。
     */
    private Long operatorId;

    /**
     * 事件发生时间。
     */
    private LocalDateTime occurredAt;

    /**
     * 扩展数据。
     */
    private Map<String, Object> data;
}
