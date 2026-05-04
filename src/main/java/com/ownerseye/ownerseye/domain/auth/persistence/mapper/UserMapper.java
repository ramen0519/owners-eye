package com.ownerseye.ownerseye.domain.auth.persistence.mapper;

import com.ownerseye.ownerseye.domain.auth.persistence.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {

    void save(UserEntity user);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
