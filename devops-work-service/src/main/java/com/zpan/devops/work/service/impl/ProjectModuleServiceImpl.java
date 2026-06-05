package com.zpan.devops.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.work.entity.ProjectModule;
import com.zpan.devops.work.entity.Task;
import com.zpan.devops.work.mapper.ProjectModuleMapper;
import com.zpan.devops.work.mapper.TaskMapper;
import com.zpan.devops.work.model.request.ProjectModuleCreateRequest;
import com.zpan.devops.work.model.request.ProjectModuleUpdateRequest;
import com.zpan.devops.work.model.vo.ProjectModuleTreeVO;
import com.zpan.devops.work.model.vo.ProjectModuleVO;
import com.zpan.devops.work.service.ProjectModuleService;
import com.zpan.devops.work.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectModuleServiceImpl implements ProjectModuleService {

    private final ProjectModuleMapper projectModuleMapper;

    private final TaskMapper taskMapper;

    private final ProjectService projectService;


    @Override
    public ProjectModuleVO create(Long projectId, ProjectModuleCreateRequest request, Long currentUserId) {
        checkProjectExist(projectId);
        checkProjectModuleCode(projectId, request.getCode());

        LocalDateTime now = LocalDateTime.now();
        ProjectModule projectModule = ProjectModule.builder()
                .projectId(projectId)
                .parentId(request.getParentId())
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .createdBy(currentUserId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        projectModuleMapper.insert(projectModule);
        return toVO(projectModule);
    }

    @Override
    public List<ProjectModuleVO> listByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectModule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectModule::getProjectId, projectId);
        queryWrapper.orderByAsc(ProjectModule::getSortOrder);

        return projectModuleMapper.selectList(queryWrapper).stream().map(this::toVO).toList();
    }

    @Override
    public List<ProjectModuleTreeVO> treeByProjectId(Long projectId) {
        checkProjectExist(projectId);

        LambdaQueryWrapper<ProjectModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModule::getProjectId, projectId);
        wrapper.orderByAsc(ProjectModule::getSortOrder);
        wrapper.orderByAsc(ProjectModule::getId);

        List<ProjectModuleTreeVO> nodes = projectModuleMapper.selectList(wrapper)
                .stream().map(this::toTreeVO).toList();
        Map<Long, ProjectModuleTreeVO> nodeMap = nodes.stream().collect(Collectors.toMap(ProjectModuleTreeVO::getId, node -> node));
        List<ProjectModuleTreeVO> roots = nodes.stream().filter(node -> node.getParentId() == null).toList();

        for (ProjectModuleTreeVO node : nodes) {
            Long parentId = node.getParentId();
            if (parentId == null) {
                continue;
            }
            ProjectModuleTreeVO parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    @Override
    public ProjectModuleVO getById(Long id) {
        return toVO(findOrThrow(id));
    }

    @Override
    public ProjectModuleVO update(Long id, ProjectModuleUpdateRequest request) {
        var projectModule = findOrThrow(id);
        LocalDateTime now = LocalDateTime.now();

        projectModule.setName(request.getName());
        projectModule.setCode(request.getCode());
        projectModule.setDescription(request.getDescription());
        projectModule.setSortOrder(request.getSortOrder());
        projectModule.setUpdatedAt(now);
        projectModuleMapper.updateById(projectModule);
        return toVO(projectModule);
    }

    @Override
    public void delete(Long id) {
        var projectModule = findOrThrow(id);
        projectModuleMapper.deleteById(projectModule);
    }

    private ProjectModule findOrThrow(Long id) {
        var projectModule = projectModuleMapper.selectById(id);
        if (projectModule == null) {
            throw new BizException(ErrorCode.PROJECT_MODULE_NOT_FOUND);
        }
        return projectModule;
    }

    private void checkProjectExist(Long projectId) {
        var exists = projectService.existsById(projectId);
        if (!exists) {
            throw new BizException(ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    private void checkProjectModuleCode(Long projectId, String code) {
        LambdaQueryWrapper<ProjectModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModule::getProjectId, projectId)
                .eq(ProjectModule::getCode, code);
        var count = projectModuleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "该项目下已存在同编码模块，请换一个编码再试");
        }
    }

    private Long countChildren(Long parentId) {
        LambdaQueryWrapper<ProjectModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectModule::getParentId, parentId);

        return projectModuleMapper.selectCount(wrapper);
    }

    private Long countTasks(Long moduleId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getModuleId, moduleId);

        return taskMapper.selectCount(wrapper);
    }

    private void sortTree(List<ProjectModuleTreeVO> nodes) {
        nodes.sort(Comparator.comparing(ProjectModuleTreeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProjectModuleTreeVO::getId));

        for (ProjectModuleTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private ProjectModuleVO toVO(ProjectModule projectModule) {
        if (projectModule == null) {
            return null;
        }

        return ProjectModuleVO.builder()
                .id(projectModule.getId())
                .projectId(projectModule.getProjectId())
                .parentId(projectModule.getParentId())
                .name(projectModule.getName())
                .code(projectModule.getCode())
                .description(projectModule.getDescription())
                .sortOrder(projectModule.getSortOrder())
                .createdBy(projectModule.getCreatedBy())
                .createdAt(projectModule.getCreatedAt())
                .updatedAt(projectModule.getUpdatedAt())
                .build();
    }

    private ProjectModuleTreeVO toTreeVO(ProjectModule projectModule) {
        if (projectModule == null) {
            return null;
        }

        return ProjectModuleTreeVO.builder()
                .id(projectModule.getId())
                .projectId(projectModule.getProjectId())
                .parentId(projectModule.getParentId())
                .name(projectModule.getName())
                .code(projectModule.getCode())
                .description(projectModule.getDescription())
                .sortOrder(projectModule.getSortOrder())
                .createdBy(projectModule.getCreatedBy())
                .createdAt(projectModule.getCreatedAt())
                .updatedAt(projectModule.getUpdatedAt())
                .build();
    }
}
