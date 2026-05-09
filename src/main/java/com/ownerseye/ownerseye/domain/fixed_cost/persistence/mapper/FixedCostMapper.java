package com.ownerseye.ownerseye.domain.fixed_cost.persistence.mapper;

import com.ownerseye.ownerseye.domain.fixed_cost.persistence.entity.FixedCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface FixedCostMapper {

    void save(FixedCostEntity fixedCost);

    Optional<FixedCostEntity> findByStoreIdAndYearMonth(@Param("storeId") Long storeId,
                                                        @Param("yearMonth") LocalDate yearMonth);

    Optional<FixedCostEntity> findById(@Param("fixedCostId") Long fixedCostId);

    List<FixedCostEntity> findAllByStoreId(@Param("storeId") Long storeId);

    void update(@Param("fixedCostId") Long fixedCostId,
                @Param("materialCost") long materialCost,
                @Param("storeDeliveryFee") long storeDeliveryFee,
                @Param("laborCost") long laborCost,
                @Param("utilities") long utilities,
                @Param("rent") long rent,
                @Param("consumables") long consumables,
                @Param("other") long other);

    void delete(@Param("fixedCostId") Long fixedCostId);
}
