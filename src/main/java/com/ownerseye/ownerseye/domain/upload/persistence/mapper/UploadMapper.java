package com.ownerseye.ownerseye.domain.upload.persistence.mapper;

import com.ownerseye.ownerseye.domain.upload.persistence.entity.UploadEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UploadMapper {

    void save(UploadEntity upload);

    void updateStatus(@Param("uploadId") Long uploadId, @Param("parseStatus") String parseStatus);
}
