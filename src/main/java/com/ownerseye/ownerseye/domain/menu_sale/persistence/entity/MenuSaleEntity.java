package com.ownerseye.ownerseye.domain.menu_sale.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuSaleEntity {

    private Long menuSaleId;
    private Long storeId;
    private LocalDate yearMonth;
    private String menuName;
    private int quantity;

    @Builder
    public MenuSaleEntity(Long storeId, LocalDate yearMonth, String menuName, int quantity) {
        this.storeId = storeId;
        this.yearMonth = yearMonth;
        this.menuName = menuName;
        this.quantity = quantity;
    }
}
