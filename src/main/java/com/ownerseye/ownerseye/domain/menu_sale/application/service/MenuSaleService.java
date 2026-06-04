package com.ownerseye.ownerseye.domain.menu_sale.application.service;

import com.ownerseye.ownerseye.domain.menu_sale.exception.MenuSaleException;
import com.ownerseye.ownerseye.domain.menu_sale.exception.code.MenuSaleErrorCode;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.entity.MenuSaleEntity;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.mapper.MenuSaleMapper;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuSaleService {

    private final StoreMapper storeMapper;
    private final MenuSaleMapper menuSaleMapper;
    private final MenuSaleParserService menuSaleParserService;

    @Transactional
    public void upload(Long userId, Long storeId, String yearMonthStr, MultipartFile file) {
        storeMapper.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new MenuSaleException(MenuSaleErrorCode.STORE_ACCESS_DENIED));

        LocalDate yearMonth = LocalDate.parse(yearMonthStr + "-01");

        List<MenuSaleParserService.MenuSaleItem> parsed = menuSaleParserService.parse(file);

        // 같은 월 데이터 교체
        menuSaleMapper.deleteByStoreIdAndYearMonth(storeId, yearMonth);

        for (MenuSaleParserService.MenuSaleItem item : parsed) {
            MenuSaleEntity entity = MenuSaleEntity.builder()
                    .storeId(storeId)
                    .yearMonth(yearMonth)
                    .menuName(item.menuName())
                    .quantity(item.quantity())
                    .build();
            menuSaleMapper.save(entity);
        }
        log.info("[MenuSaleService] 판매 데이터 저장 완료 - storeId={}, yearMonth={}, 메뉴 수={}", storeId, yearMonth, parsed.size());
    }
}
