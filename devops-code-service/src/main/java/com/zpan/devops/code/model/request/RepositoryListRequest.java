package com.zpan.devops.code.model.request;

import lombok.Data;

@Data
public class RepositoryListRequest {

    private Long projectId;

    private String namespace;

    private String status;

    private String visibility;

    private String keyword;

    private Integer pageNo = 1;

    private Integer pageSize = 20;
}