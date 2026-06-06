package com.ownerseye.ownerseye.domain.chat.domain.tool;

import com.ownerseye.ownerseye.domain.menu_sale.persistence.entity.MenuSaleEntity;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.mapper.MenuSaleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class MenuSaleTool {

    private final MenuSaleMapper menuSaleMapper;
    private final Long userId;
    private final Long storeId;

    public MenuSaleTool(MenuSaleMapper menuSaleMapper, Long userId, Long storeId) {
        this.menuSaleMapper = menuSaleMapper;
        this.userId = userId;
        this.storeId = storeId;
    }

    @Tool(description = "특정 연월의 메뉴별 판매 수량을 조회합니다. yearMonth는 'yyyy-MM' 형식입니다. 예: '2026-04'. '지난달', '이번달' 같은 표현은 현재 날짜 기준으로 변환하세요.")
    public String getMenuSales(String yearMonth) {
        try {
            LocalDate date = LocalDate.parse(yearMonth + "-01");
            List<MenuSaleEntity> sales = menuSaleMapper.findAllByStoreIdAndYearMonth(storeId, date);

            if (sales.isEmpty()) {
                return yearMonth + " 메뉴별 판매 데이터가 없습니다.";
            }

            return format(yearMonth, sales);
        } catch (Exception e) {
            log.error("[MenuSaleTool] 조회 실패: storeId={}, yearMonth={}", storeId, yearMonth, e);
            return yearMonth + " 메뉴 판매 데이터 조회 중 오류가 발생했습니다.";
        }
    }

    private String format(String yearMonth, List<MenuSaleEntity> sales) {
        StringBuilder sb = new StringBuilder();
        sb.append(yearMonth).append(" 메뉴별 판매량\n\n");

        sales.stream()
                .sorted(Comparator.comparingInt(MenuSaleEntity::getQuantity).reversed())
                .forEach(s -> sb.append("- ").append(s.getMenuName())
                        .append(": ").append(s.getQuantity()).append("개\n"));

        int total = sales.stream().mapToInt(MenuSaleEntity::getQuantity).sum();
        sb.append("\n총 판매: ").append(total).append("개");
        return sb.toString();
    }
}
