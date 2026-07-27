package com.zpan.devops.code.model.request;

import com.zpan.devops.common.request.PagedRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommitListRequest extends PagedRequest {

    /**
     * 只查询指定分支同步过来的commit
     */
    private String branchName;

}
