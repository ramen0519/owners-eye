package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminAdEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface BaeminAdMapper {

    void save(BaeminAdEntity baeminAd);

    Optional<BaeminAdEntity> findByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                                       @Param("yearMonth") LocalDate yearMonth);
}
