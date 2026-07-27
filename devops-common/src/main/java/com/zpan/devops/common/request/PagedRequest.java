package com.zpan.devops.common.request;

import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class PagedRequest {

    public static final Integer DEFAULT_PAGE_SIZE = 20;

    private Integer pageNo = 1;

    @Max(value = 100, message = "数量不能大于100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    private String keyword;

    public Integer getPageNo() {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        return pageNo;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return pageSize;
    }

    public String getKeyword() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
