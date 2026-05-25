package com.ownerseye.ownerseye.domain.store.persistence.mapper;

import com.ownerseye.ownerseye.domain.store.persistence.entity.StoreEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface StoreMapper {

    Optional<StoreEntity> findByUserId(Long userId);
}
