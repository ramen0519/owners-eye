package com.ownerseye.ownerseye.domain.analysis.application.service;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.ChannelAnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.CostItemResponse;
import com.ownerseye.ownerseye.domain.analysis.exception.AnalysisException;
import com.ownerseye.ownerseye.domain.analysis.exception.code.AnalysisErrorCode;
import com.ownerseye.ownerseye.domain.fixed_cost.persistence.entity.FixedCostEntity;
import com.ownerseye.ownerseye.domain.fixed_cost.persistence.mapper.FixedCostMapper;
import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminAdEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminSalesEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.entity.CoupangSalesEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.entity.PosSalesEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.BaeminAdMapper;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.BaeminSalesMapper;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.CoupangSalesMapper;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.PosSalesMapper;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisService {

    private final StoreMapper storeMapper;
    private final PosSalesMapper posSalesMapper;
    private final BaeminSalesMapper baeminSalesMapper;
    private final BaeminAdMapper baeminAdMapper;
    private final CoupangSalesMapper coupangSalesMapper;
    private final FixedCostMapper fixedCostMapper;

    public AnalysisResponse analyze(Long userId, Long storeId, String yearMonthStr) {
        storeMapper.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.STORE_ACCESS_DENIED));

        LocalDate yearMonth = parseYearMonth(yearMonthStr);

        // 1. 데이터 조회
        List<PosSalesEntity> posList = posSalesMapper.findAllByStoreIdAndYearMonth(storeId, yearMonth);
        List<BaeminSalesEntity> baeminList = baeminSalesMapper.findAllByStoreIdAndYearMonth(storeId, yearMonth);
        long baeminAdFee = baeminAdMapper.findByStoreIdAndYearMonth(storeId, yearMonth)
                .map(BaeminAdEntity::getAdFee).orElse(0L);
        CoupangSalesEntity coupang = coupangSalesMapper.findByStoreIdAndYearMonth(storeId, yearMonth)
                .orElse(null);
        FixedCostEntity fixed = fixedCostMapper.findByStoreIdAndYearMonth(storeId, yearMonth)
                .orElse(null);

        // 2. POS 매출 합산
        long holRevenue = 0L;
        for (PosSalesEntity pos : posList) {
            holRevenue += pos.getTotalRevenue();
        }

        // 3. 배민1 / 가게배달 분리
        BaeminSalesEntity baemin1 = null;
        BaeminSalesEntity baeminStore = null;
        for (BaeminSalesEntity b : baeminList) {
            if ("BAEMIN1".equals(b.getDeliveryType())) {
                baemin1 = b;
            } else if ("STORE".equals(b.getDeliveryType())) {
                baeminStore = b;
            }
        }

        long baemin1Revenue = baemin1 != null ? baemin1.getOrderAmount() : 0L;
        long baeminStoreRevenue = baeminStore != null ? baeminStore.getOrderAmount() : 0L;
        long coupangRevenue = coupang != null ? coupang.getOrderAmount() : 0L;
        long totalRevenue = holRevenue + baemin1Revenue + baeminStoreRevenue + coupangRevenue;

        // 4. 배민 광고비 배분
        long baeminTotalRevenue = baemin1Revenue + baeminStoreRevenue;
        long baemin1AdFee = baeminTotalRevenue == 0 ? 0L
                : Math.round((double) baeminAdFee * baemin1Revenue / baeminTotalRevenue);
        long baeminStoreAdFee = baeminAdFee - baemin1AdFee;

        // 5. 채널별 분석
        List<ChannelAnalysisResponse> channels = new ArrayList<>();
        channels.add(buildHolChannel(holRevenue, totalRevenue, fixed));
        channels.add(buildBaemin1Channel(baemin1, baemin1Revenue, totalRevenue, baemin1AdFee, fixed));
        channels.add(buildBaeminStoreChannel(baeminStore, baeminStoreRevenue, totalRevenue, baeminStoreAdFee, fixed));
        channels.add(buildCoupangChannel(coupang, coupangRevenue, totalRevenue, fixed));

        return new AnalysisResponse(yearMonthStr, totalRevenue, channels);
    }

    private ChannelAnalysisResponse buildHolChannel(long revenue, long totalRevenue, FixedCostEntity fixed) {
        List<CostItemResponse> costs = buildFixedCosts(revenue, totalRevenue, fixed, false);
        return ChannelAnalysisResponse.of("홀", revenue, totalRevenue, costs);
    }

    private ChannelAnalysisResponse buildBaemin1Channel(BaeminSalesEntity b, long revenue,
                                                         long totalRevenue, long adFee, FixedCostEntity fixed) {
        List<CostItemResponse> costs = new ArrayList<>();
        if (b != null) {
            costs.add(CostItemResponse.of("수수료", b.getServiceFee() + b.getPaymentFee(), revenue));
            costs.add(CostItemResponse.of("배달비", b.getDeliveryCost(), revenue));
            costs.add(CostItemResponse.of("광고비", adFee, revenue));
        }
        costs.addAll(buildFixedCosts(revenue, totalRevenue, fixed, false));
        return ChannelAnalysisResponse.of("배민1", revenue, totalRevenue, costs);
    }

    private ChannelAnalysisResponse buildBaeminStoreChannel(BaeminSalesEntity b, long revenue,
                                                              long totalRevenue, long adFee, FixedCostEntity fixed) {
        List<CostItemResponse> costs = new ArrayList<>();
        if (b != null) {
            costs.add(CostItemResponse.of("수수료", b.getServiceFee() + b.getPaymentFee(), revenue));
            costs.add(CostItemResponse.of("배달비", b.getDeliveryCost(), revenue));
            costs.add(CostItemResponse.of("광고비", adFee, revenue));
        }
        costs.addAll(buildFixedCosts(revenue, totalRevenue, fixed, true));
        return ChannelAnalysisResponse.of("배민가게배달", revenue, totalRevenue, costs);
    }

    private ChannelAnalysisResponse buildCoupangChannel(CoupangSalesEntity c, long revenue,
                                                         long totalRevenue, FixedCostEntity fixed) {
        List<CostItemResponse> costs = new ArrayList<>();
        if (c != null) {
            costs.add(CostItemResponse.of("수수료", c.getServiceFee() + c.getPaymentFee(), revenue));
            costs.add(CostItemResponse.of("배달비", c.getDeliveryFee(), revenue));
            costs.add(CostItemResponse.of("광고비", c.getAdFee(), revenue));
        }
        costs.addAll(buildFixedCosts(revenue, totalRevenue, fixed, false));
        return ChannelAnalysisResponse.of("쿠팡", revenue, totalRevenue, costs);
    }

    private List<CostItemResponse> buildFixedCosts(long revenue, long totalRevenue,
                                                    FixedCostEntity fixed, boolean includeStoreDelivery) {
        if (fixed == null) return new ArrayList<>();
        List<CostItemResponse> costs = new ArrayList<>();
        costs.add(CostItemResponse.of("재료비",     allocate(fixed.getMaterialCost(), revenue, totalRevenue), revenue));
        if (includeStoreDelivery) {
            costs.add(CostItemResponse.of("가게배달료", fixed.getStoreDeliveryFee(), revenue));
        }
        costs.add(CostItemResponse.of("인건비",       allocate(fixed.getLaborCost(), revenue, totalRevenue), revenue));
        costs.add(CostItemResponse.of("수도가스전기", allocate(fixed.getUtilities(), revenue, totalRevenue), revenue));
        costs.add(CostItemResponse.of("임대료",       allocate(fixed.getRent(), revenue, totalRevenue), revenue));
        costs.add(CostItemResponse.of("소모품",       allocate(fixed.getConsumables(), revenue, totalRevenue), revenue));
        costs.add(CostItemResponse.of("기타",         allocate(fixed.getOther(), revenue, totalRevenue), revenue));
        return costs;
    }

    private long allocate(long totalCost, long channelRevenue, long totalRevenue) {
        if (totalRevenue == 0) return 0L;
        return Math.round((double) totalCost * channelRevenue / totalRevenue);
    }

    private LocalDate parseYearMonth(String yearMonthStr) {
        try {
            return LocalDate.parse(yearMonthStr + "-01");
        } catch (Exception e) {
            throw new AnalysisException(AnalysisErrorCode.INVALID_YEAR_MONTH_FORMAT);
        }
    }
}
