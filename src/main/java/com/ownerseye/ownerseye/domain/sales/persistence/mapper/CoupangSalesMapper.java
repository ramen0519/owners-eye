package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.CoupangSalesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface CoupangSalesMapper {

    void save(CoupangSalesEntity coupangSales);

    void deleteByStoreIdAndYearMonth(@Param("storeId") Long storeId, @Param("yearMonth") LocalDate yearMonth);

    Optional<CoupangSalesEntity> findByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                                           @Param("yearMonth") LocalDate yearMonth);
}
