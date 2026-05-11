package com.ownerseye.ownerseye.domain.store.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StoreSaveRequest(
        @NotBlank String storeName
) {
}
