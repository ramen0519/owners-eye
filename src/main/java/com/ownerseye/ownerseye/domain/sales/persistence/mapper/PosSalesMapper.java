package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.PosSalesEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PosSalesMapper {

    void save(PosSalesEntity posSales);
}
