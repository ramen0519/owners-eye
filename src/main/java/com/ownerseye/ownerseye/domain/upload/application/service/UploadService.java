package com.ownerseye.ownerseye.domain.upload.application.service;

import com.ownerseye.ownerseye.domain.upload.persistence.entity.UploadEntity;
import com.ownerseye.ownerseye.domain.upload.persistence.mapper.UploadMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadService {

    private final UploadMapper uploadMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(UploadEntity upload) {
        uploadMapper.save(upload);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long uploadId, String parseStatus) {
        uploadMapper.updateStatus(uploadId, parseStatus);
    }
}
