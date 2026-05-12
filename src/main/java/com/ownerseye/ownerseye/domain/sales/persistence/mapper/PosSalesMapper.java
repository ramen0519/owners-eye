package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.PosSalesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PosSalesMapper {

    void save(PosSalesEntity posSales);

    List<PosSalesEntity> findAllByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                                      @Param("yearMonth") LocalDate yearMonth);
}
