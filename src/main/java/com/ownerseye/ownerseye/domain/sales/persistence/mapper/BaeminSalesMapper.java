package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminSalesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BaeminSalesMapper {

    void save(BaeminSalesEntity baeminSales);

    List<BaeminSalesEntity> findAllByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                                         @Param("yearMonth") LocalDate yearMonth);
}
