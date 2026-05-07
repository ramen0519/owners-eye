package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.CoupangSalesEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoupangSalesMapper {

    void save(CoupangSalesEntity coupangSales);
}
