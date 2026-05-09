package com.ownerseye.ownerseye.domain.fixed_cost.application.service;

import com.ownerseye.ownerseye.domain.fixed_cost.application.dto.request.FixedCostSaveRequest;
import com.ownerseye.ownerseye.domain.fixed_cost.application.dto.request.FixedCostUpdateRequest;
import com.ownerseye.ownerseye.domain.fixed_cost.application.dto.response.FixedCostResponse;
import com.ownerseye.ownerseye.domain.fixed_cost.exception.FixedCostException;
import com.ownerseye.ownerseye.domain.fixed_cost.exception.code.FixedCostErrorCode;
import com.ownerseye.ownerseye.domain.fixed_cost.persistence.entity.FixedCostEntity;
import com.ownerseye.ownerseye.domain.fixed_cost.persistence.mapper.FixedCostMapper;
import com.ownerseye.ownerseye.domain.store.persistence.entity.StoreEntity;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedCostService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final FixedCostMapper fixedCostMapper;
    private final StoreMapper storeMapper;

    @Transactional
    public Long save(Long userId, FixedCostSaveRequest request) {
        StoreEntity store = getStore(userId);
        validateOwnership(store, request.storeId());

        LocalDate yearMonth = parseYearMonth(request.yearMonth());

        fixedCostMapper.findByStoreIdAndYearMonth(request.storeId(), yearMonth)
                .ifPresent(existing -> {
                    throw new FixedCostException(FixedCostErrorCode.FIXED_COST_ALREADY_EXISTS);
                });

        FixedCostEntity fixedCost = FixedCostEntity.builder()
                .storeId(request.storeId())
                .yearMonth(yearMonth)
                .materialCost(nullToZero(request.materialCost()))
                .storeDeliveryFee(nullToZero(request.storeDeliveryFee()))
                .laborCost(nullToZero(request.laborCost()))
                .utilities(nullToZero(request.utilities()))
                .rent(nullToZero(request.rent()))
                .consumables(nullToZero(request.consumables()))
                .other(nullToZero(request.other()))
                .build();

        fixedCostMapper.save(fixedCost);
        return fixedCost.getFixedCostId();
    }

    public FixedCostResponse findByStoreIdAndYearMonth(Long userId, Long storeId, String yearMonthStr) {
        StoreEntity store = getStore(userId);
        validateOwnership(store, storeId);

        LocalDate yearMonth = parseYearMonth(yearMonthStr);
        return FixedCostResponse.from(
                fixedCostMapper.findByStoreIdAndYearMonth(storeId, yearMonth)
                        .orElseThrow(() -> new FixedCostException(FixedCostErrorCode.FIXED_COST_NOT_FOUND))
        );
    }

    public List<FixedCostResponse> findAllByStoreId(Long userId, Long storeId) {
        StoreEntity store = getStore(userId);
        validateOwnership(store, storeId);

        return fixedCostMapper.findAllByStoreId(storeId).stream()
                .map(FixedCostResponse::from)
                .toList();
    }

    @Transactional
    public void update(Long userId, Long fixedCostId, FixedCostUpdateRequest request) {
        StoreEntity store = getStore(userId);

        FixedCostEntity existing = fixedCostMapper.findById(fixedCostId)
                .orElseThrow(() -> new FixedCostException(FixedCostErrorCode.FIXED_COST_NOT_FOUND));

        validateOwnership(store, existing.getStoreId());

        fixedCostMapper.update(
                fixedCostId,
                request.materialCost() != null ? request.materialCost() : existing.getMaterialCost(),
                request.storeDeliveryFee() != null ? request.storeDeliveryFee() : existing.getStoreDeliveryFee(),
                request.laborCost() != null ? request.laborCost() : existing.getLaborCost(),
                request.utilities() != null ? request.utilities() : existing.getUtilities(),
                request.rent() != null ? request.rent() : existing.getRent(),
                request.consumables() != null ? request.consumables() : existing.getConsumables(),
                request.other() != null ? request.other() : existing.getOther()
        );
    }

    @Transactional
    public void delete(Long userId, Long fixedCostId) {
        StoreEntity store = getStore(userId);

        FixedCostEntity existing = fixedCostMapper.findById(fixedCostId)
                .orElseThrow(() -> new FixedCostException(FixedCostErrorCode.FIXED_COST_NOT_FOUND));

        validateOwnership(store, existing.getStoreId());
        fixedCostMapper.delete(fixedCostId);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private StoreEntity getStore(Long userId) {
        return storeMapper.findByUserId(userId)
                .orElseThrow(() -> new FixedCostException(FixedCostErrorCode.STORE_NOT_FOUND));
    }

    private void validateOwnership(StoreEntity store, Long requestedStoreId) {
        if (!store.getStoreId().equals(requestedStoreId)) {
            throw new FixedCostException(FixedCostErrorCode.STORE_ACCESS_DENIED);
        }
    }

    private LocalDate parseYearMonth(String yearMonthStr) {
        if (yearMonthStr == null || yearMonthStr.isBlank()) {
            throw new FixedCostException(FixedCostErrorCode.INVALID_YEAR_MONTH_FORMAT);
        }
        try {
            return YearMonth.parse(yearMonthStr, YEAR_MONTH_FORMATTER).atDay(1);
        } catch (DateTimeParseException e) {
            throw new FixedCostException(FixedCostErrorCode.INVALID_YEAR_MONTH_FORMAT);
        }
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
