package com.ownerseye.ownerseye.domain.upload.persistence.entity;

import com.ownerseye.ownerseye.domain.upload.domain.constant.ParseStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadEntity {

    private Long uploadId;
    private Long storeId;
    private String uploadType;
    private LocalDate yearMonth;
    private String fileName;
    private String parseStatus;
    private LocalDateTime createdAt;

    @Builder
    public UploadEntity(Long storeId, String uploadType, LocalDate yearMonth, String fileName) {
        this.storeId = storeId;
        this.uploadType = uploadType;
        this.yearMonth = yearMonth;
        this.fileName = fileName;
        this.parseStatus = ParseStatus.PENDING.name();
    }
}
