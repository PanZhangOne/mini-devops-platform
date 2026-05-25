package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.Project;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.enums.ProjectStatus;
import com.zpan.devops.work.mapper.ProjectMapper;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.model.request.ProjectCreateRequest;
import com.zpan.devops.work.model.request.ProjectUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectVO;
import com.zpan.devops.work.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    private final TaskMapper taskMapper;

    @Override
    public ProjectVO create(ProjectCreateRequest request) {
        validateProjectCodeNotExists(request.getCode());

        LocalDateTime now = LocalDateTime.now();

        Project project = new Project();
        project.setName(request.getName());
        project.setCode(request.getCode());
        project.setDescription(request.getDescription());
        project.setOwnerId(request.getOwnerId());
        project.setStatus(ProjectStatus.PLANNING.name());
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        projectMapper.insert(project);
        return toVO(project);
    }

    @Override
    public List<ProjectVO> list() {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Project::getCreatedAt);

        return projectMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = getProjectOrThrow(id);
        return toVO(project);
    }

    @Override
    public ProjectVO update(Long id, ProjectUpdateRequest request) {
        Project project = getProjectOrThrow(id);

        if (!ProjectStatus.isValid(request.getStatus())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "项目状态不合法");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(request.getOwnerId());
        project.setStatus(request.getStatus());
        project.setUpdatedAt(LocalDateTime.now());

        projectMapper.updateById(project);

        return toVO(project);
    }

    @Override
    public void delete(Long id) {
        Project project = getProjectOrThrow(id);

        // 如果项目中存在任务，就不允许删除
        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getProjectId, id);

        Long taskCount = taskMapper.selectCount(taskWrapper);
        if (taskCount > 0) {
            throw new BizException(ErrorCode.PROJECT_HAS_TASKS);
        }


        projectMapper.deleteById(project.getId());
    }

    @Override
    public void checkProjectExist(Long id) {
        getProjectOrThrow(id);
    }

    @Override
    public boolean existsById(Long id) {
        return projectMapper.selectById(id) != null;
    }

    private void validateProjectCodeNotExists(String code) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getCode, code);

        Long count = projectMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.PROJECT_CODE_EXISTS);
        }

    }

    private Project getProjectOrThrow(Long id) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getId, id);
        Project project = projectMapper.selectOne(wrapper);
        if (project == null) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setCode(project.getCode());
        vo.setDescription(project.getDescription());
        vo.setOwnerId(project.getOwnerId());
        vo.setStatus(project.getStatus());
        vo.setCreatedAt(project.getCreatedAt());
        vo.setUpdatedAt(project.getUpdatedAt());

        if (ProjectStatus.isValid(project.getStatus())) {
            vo.setStatusDescription(ProjectStatus.valueOf(project.getStatus()).getDescription());
        }

        return vo;
    }
}
