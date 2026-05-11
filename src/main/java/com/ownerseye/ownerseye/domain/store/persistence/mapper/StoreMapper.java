package com.ownerseye.ownerseye.domain.store.persistence.mapper;

import com.ownerseye.ownerseye.domain.store.persistence.entity.StoreEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StoreMapper {

    void save(StoreEntity store);

    Optional<StoreEntity> findByStoreIdAndUserId(@Param("storeId") Long storeId,
                                                  @Param("userId") Long userId);

    List<StoreEntity> findAllByUserId(@Param("userId") Long userId);

    void delete(@Param("storeId") Long storeId);
}
