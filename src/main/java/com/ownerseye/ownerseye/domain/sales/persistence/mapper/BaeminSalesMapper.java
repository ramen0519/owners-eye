package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminSalesEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaeminSalesMapper {

    void save(BaeminSalesEntity baeminSales);
}
