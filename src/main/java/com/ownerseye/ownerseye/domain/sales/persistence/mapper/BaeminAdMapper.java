package com.ownerseye.ownerseye.domain.sales.persistence.mapper;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminAdEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaeminAdMapper {

    void save(BaeminAdEntity baeminAd);
}
