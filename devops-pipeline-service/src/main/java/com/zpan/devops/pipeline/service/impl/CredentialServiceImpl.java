package com.zpan.devops.pipeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpan.devops.common.exception.BizException;
import com.zpan.devops.common.exception.ErrorCode;
import com.zpan.devops.pipeline.entity.Credential;
import com.zpan.devops.pipeline.enums.CredentialType;
import com.zpan.devops.pipeline.mapper.CredentialMapper;
import com.zpan.devops.pipeline.model.request.CredentialCreateRequest;
import com.zpan.devops.pipeline.model.request.CredentialUpdateRequest;
import com.zpan.devops.pipeline.model.vo.CredentialSecretVO;
import com.zpan.devops.pipeline.model.vo.CredentialVO;
import com.zpan.devops.pipeline.service.CredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final CredentialMapper credentialMapper;

    @Override
    public CredentialVO create(CredentialCreateRequest request, Long currentUserId) {
        validateCredentialType(request.getCredentialType());
        validateSecret(request.getSecretValue());
        validateNameNotExists(request.getProjectId(), request.getName(), null);

        LocalDateTime now = LocalDateTime.now();
        Credential credential = new Credential();
        credential.setProjectId(request.getProjectId());
        credential.setName(request.getName());
        credential.setCredentialType(request.getCredentialType());
        credential.setUsername(request.getUsername());
        credential.setSecretValue(request.getSecretValue());
        credential.setDescription(request.getDescription());
        credential.setCreatedBy(currentUserId);
        credential.setCreatedAt(now);
        credential.setUpdatedAt(now);
        credentialMapper.insert(credential);

        return toVO(credential);
    }

    @Override
    public List<CredentialVO> list(Long projectId) {
        LambdaQueryWrapper<Credential> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.and(w -> w.eq(Credential::getProjectId, projectId).or().isNull(Credential::getProjectId));
        }

        wrapper.orderByDesc(Credential::getCreatedAt);
        return credentialMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public CredentialVO getById(Long id) {
        return  toVO(getCredentialOrThrow(id));
    }

    @Override
    public CredentialVO update(Long id, CredentialUpdateRequest request) {
        Credential credential = getCredentialOrThrow(id);
        validateCredentialType(request.getCredentialType());
        validateNameNotExists(credential.getProjectId(), request.getName(), id);
        credential.setName(request.getName());
        credential.setCredentialType(request.getCredentialType());
        credential.setUsername(request.getUsername());
        credential.setDescription(request.getDescription());

        if (request.getSecretValue() != null && !request.getSecretValue().isBlank()) {
            credential.setSecretValue(request.getSecretValue());
        }

        credential.setUpdatedAt(LocalDateTime.now());
        credentialMapper.updateById(credential);
        return toVO(credential);
    }

    @Override
    public void delete(Long id) {
        getCredentialOrThrow(id);
        credentialMapper.deleteById(id);
    }

    @Override
    public CredentialSecretVO getSecretById(Long id) {
        Credential credential = getCredentialOrThrow(id);

        CredentialSecretVO vo = new CredentialSecretVO();
        vo.setId(credential.getId());
        vo.setProjectId(credential.getProjectId());
        vo.setName(credential.getName());
        vo.setCredentialType(credential.getCredentialType());
        vo.setUsername(credential.getUsername());
        vo.setSecretValue(credential.getSecretValue());

        return vo;
    }

    private CredentialVO toVO(Credential credential) {
        CredentialVO vo = new CredentialVO();

        vo.setId(credential.getId());
        vo.setProjectId(credential.getProjectId());
        vo.setName(credential.getName());
        vo.setCredentialType(credential.getCredentialType());
        vo.setUsername(credential.getUsername());
        vo.setDescription(credential.getDescription());
        vo.setCreatedBy(credential.getCreatedBy());
        vo.setCreatedAt(credential.getCreatedAt());
        vo.setUpdatedAt(credential.getUpdatedAt());

        if (CredentialType.isValid(credential.getCredentialType())) {
            vo.setCredentialTypeDescription(
                    CredentialType.valueOf(credential.getCredentialType()).getDescription()
            );
        }
        return vo;
    }

    private Credential getCredentialOrThrow(Long id) {
        return credentialMapper.selectById(id);
    }

    private void validateCredentialType(String type) {
        if (type == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "凭据类型不能为空");
        }
        if (!CredentialType.isValid(type)) {
            throw new BizException(ErrorCode.CREDENTIAL_TYPE_INVALID);
        }
    }

    private void validateSecret(String secret) {
    }

    private void validateNameNotExists(Long projectId, String name, Long excludeId) {
        LambdaQueryWrapper<Credential> wrapper = new LambdaQueryWrapper<>();

        if (projectId == null) {
            wrapper.isNull(Credential::getProjectId);
        } else {
            wrapper.eq(Credential::getProjectId, projectId);
        }
        wrapper.eq(Credential::getName, name);
        if (excludeId != null) {
            wrapper.ne(Credential::getId, excludeId);
        }
        Long count = credentialMapper.selectCount(wrapper);

        if (count > 0) {
            throw new BizException(ErrorCode.CREDENTIAL_NAME_EXISTS);
        }
    }
}
