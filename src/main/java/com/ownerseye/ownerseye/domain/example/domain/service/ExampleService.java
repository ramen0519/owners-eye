package com.ownerseye.ownerseye.domain.example.domain.service;

import com.ownerseye.ownerseye.domain.example.application.dto.response.ExampleResponse;
import com.ownerseye.ownerseye.domain.example.exception.ExampleException;
import com.ownerseye.ownerseye.domain.example.exception.code.ExampleErrorCode;
import com.ownerseye.ownerseye.domain.example.persistence.entity.ExampleEntity;
import com.ownerseye.ownerseye.domain.example.persistence.mapper.ExampleMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleService {

    private final ExampleMapper exampleMapper;

    @Transactional
    public Long save(String name) {
        ExampleEntity example = ExampleEntity.builder()
                .name(name)
                .build();
        exampleMapper.save(example);
        return example.getId();
    }

    public ExampleResponse findById(Long id) {
        return ExampleResponse.from(
                exampleMapper.findById(id)
                        .orElseThrow(() -> new ExampleException(ExampleErrorCode.EXAMPLE_NOT_FOUND))
        );
    }
}
