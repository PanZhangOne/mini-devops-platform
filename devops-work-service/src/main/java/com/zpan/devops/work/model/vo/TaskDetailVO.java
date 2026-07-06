package com.zpan.devops.work.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskDetailVO extends TaskVO {

    // 子任务列表
    private List<TaskVO> children;

    // 属性值
    private List<TaskPropertyValueVO> propertyValues = new ArrayList<>();

    // 评论树
    private List<TaskCommentTreeVO> comments = new ArrayList<>();

    // 活动日志
    private List<TaskActivityVO> activities = new ArrayList<>();

    // 关系列表
    private List<TaskRelationVO> relations = new ArrayList<>();
}
