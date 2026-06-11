package com.ownerseye.ownerseye.domain.menu_sale.application.service;

import com.ownerseye.ownerseye.domain.menu_sale.exception.MenuSaleException;
import com.ownerseye.ownerseye.domain.menu_sale.exception.code.MenuSaleErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuSaleParserService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String MENU_COL_HEADER = "주문메뉴";

    private static final String PARSE_PROMPT = """
            아래는 음식점의 주문 내역 데이터입니다. 각 행은 주문 1건이며, 주문한 메뉴와 옵션이 쉼표로 구분되어 있습니다.
            '+ '로 시작하지 않는 항목이 메뉴명이고, '+ '로 시작하는 항목은 옵션입니다.

            주 메뉴의 판매 횟수를 집계해주세요.

            [집계 규칙]
            1. 주 메뉴(음식 단품)만 집계하세요. 표기 변형(예: 공백 차이, "~피자" 등 접미사)은 동일 메뉴로 정규화하세요.
            2. 음료(콜라, 스프라이트 등), 소스류, 피클, 사이드 메뉴는 모두 제외하세요.
            3. 도우, 크러스트, 사이즈(L/M/R), 치즈 추가 등 옵션('+ '로 시작)은 집계하지 마세요.
            4. 하프앤하프(반반 메뉴) 처리:
               - "하프앤하프" 자체를 1회 집계
               - 하프앤하프 다음에 오는 '+ '로 시작하는 옵션 중 주 메뉴에 해당하는 항목은 "하프앤하프-[메뉴명]"으로 별도 집계
               - 예: "하프앤하프,+ L,+ 직화불고기,+ 렌치베이컨포테이토"
                     → 하프앤하프 1회 + 하프앤하프-직화불고기 1회 + 하프앤하프-렌치베이컨포테이토 1회
            5. 세트 메뉴 처리:
               - 세트명 자체는 집계하지 마세요.
               - 세트 내에서 명시된 단품 메뉴명('+ [메뉴명]')은 단품으로 집계하세요.
            6. 같은 메뉴는 모두 합산하세요.

            데이터:
            %s
            """;

    public List<MenuSaleItem> parse(MultipartFile file) {
        String content = extractText(file);
        log.info("[MenuSaleParser] 추출된 텍스트 길이: {}", content.length());

        try {
            MenuSaleParseResult result = chatClientBuilder.build()
                    .prompt(PARSE_PROMPT.formatted(content))
                    .call()
                    .entity(MenuSaleParseResult.class);

            if (result == null || result.items() == null) {
                throw new MenuSaleException(MenuSaleErrorCode.AI_PARSE_FAILED);
            }
            log.info("[MenuSaleParser] 파싱된 메뉴 수: {}", result.items().size());
            return result.items();
        } catch (MenuSaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MenuSaleParser] AI 파싱 실패", e);
            throw new MenuSaleException(MenuSaleErrorCode.AI_PARSE_FAILED);
        }
    }

    private String extractText(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 1 ? workbook.getSheetAt(1) : workbook.getSheetAt(0);
            StringBuilder sb = new StringBuilder();
            int menuColIndex = -1;

            for (Row row : sheet) {
                if (menuColIndex == -1) {
                    for (Cell cell : row) {
                        if (MENU_COL_HEADER.equals(getCellText(cell))) {
                            menuColIndex = cell.getColumnIndex();
                            break;
                        }
                    }
                    continue;
                }
                Cell menuCell = row.getCell(menuColIndex);
                if (menuCell == null) continue;
                String menuText = getCellText(menuCell);
                if (!menuText.isBlank()) {
                    sb.append(menuText).append("\n");
                }
            }

            if (menuColIndex == -1) {
                log.error("[MenuSaleParser] '주문메뉴' 컬럼을 찾을 수 없음");
                throw new MenuSaleException(MenuSaleErrorCode.EXCEL_READ_FAILED);
            }
            return sb.toString();
        } catch (MenuSaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MenuSaleParser] 엑셀 읽기 실패", e);
            throw new MenuSaleException(MenuSaleErrorCode.EXCEL_READ_FAILED);
        }
    }

    private String getCellText(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    public record MenuSaleParseResult(List<MenuSaleItem> items) {}

    public record MenuSaleItem(String menuName, int quantity) {}
}
