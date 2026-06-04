package com.ownerseye.ownerseye.domain.menu_sale.persistence.mapper;

import com.ownerseye.ownerseye.domain.menu_sale.persistence.entity.MenuSaleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MenuSaleMapper {

    void save(MenuSaleEntity menuSale);

    void deleteByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                     @Param("yearMonth") LocalDate yearMonth);

    List<MenuSaleEntity> findAllByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                                      @Param("yearMonth") LocalDate yearMonth);
}
