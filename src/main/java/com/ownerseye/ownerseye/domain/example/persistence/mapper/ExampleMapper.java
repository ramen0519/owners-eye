package com.ownerseye.ownerseye.domain.example.persistence.mapper;

import com.ownerseye.ownerseye.domain.example.persistence.entity.ExampleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface ExampleMapper {

    void save(ExampleEntity example);

    Optional<ExampleEntity> findById(Long id);
}
