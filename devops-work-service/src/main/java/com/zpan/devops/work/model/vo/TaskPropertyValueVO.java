package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskPropertyValueVO {


    private Long id;

    private Long taskId;

    private Long propertyId;

    private String propertyCode;

    private String propertyName;

    private String propertyType;

    private String propertyTypeDescription;

    private Boolean required;

    private String optionsJson;

    private String valueText;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
