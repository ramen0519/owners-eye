package com.ownerseye.ownerseye.domain.example.persistence.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleEntity {

    private Long id;
    private String name;

    @Builder
    public ExampleEntity(String name) {
        this.name = name;
    }
}
