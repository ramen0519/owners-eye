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
            아래는 피자 음식점의 주문 내역 데이터입니다. 각 행은 주문 1건이며, 주문한 메뉴와 옵션이 쉼표로 구분되어 있습니다.
            '+ '로 시작하지 않는 항목이 메뉴명이고, '+ '로 시작하는 항목은 옵션입니다.

            피자 메뉴의 월별 판매 횟수를 집계해주세요.

            [피자 메뉴 목록]
            콤비네이션, 페퍼로니, 리얼치즈, 직화불고기, 렌치베이컨포테이토, 파파오네스페셜, 소보루골드고구마,
            스위트고구마무스, 슈퍼하와이언, 킹쉬림프, 핫치킨쉬림프, 베이컨체다치즈, 올인원감자농장,
            콘치즈마요, 할라페퍼로니, 연탄갈비, 스페셜핫불고기, 베페로니, 콰트로피자, 훈제치킨

            [집계 규칙]
            1. 위 피자 메뉴 목록에 해당하는 것만 집계하세요. 표기 변형(예: "콤비네이션피자", "콤비네이션 피자" → "콤비네이션")은 동일 메뉴로 처리하세요.
            2. 음료(콜라, 스프라이트 등), 소스(갈릭디핑소스, 핫소스, 수제갈릭소스 등), 피클, 핫윙, 새우링, 닭다리 등 사이드는 모두 제외하세요.
            3. 하프앤하프(반반 피자) 처리:
               - "하프앤하프" 자체를 1회 집계
               - 하프앤하프 다음에 오는 '+ '로 시작하는 옵션 중 위 피자 목록에 해당하는 항목은 "하프앤하프-[피자명]"으로 별도 집계
               - 예: "하프앤하프,+ L,+ 치즈크러스트 추가,+ 직화불고기,+ 렌치베이컨포테이토"
                     → 하프앤하프 1회 + 하프앤하프-직화불고기 1회 + 하프앤하프-렌치베이컨포테이토 1회
            4. 세트 메뉴(두판세트, 패밀리두판세트, Passione 클래식 세트, Papaonne 반반 세트 등):
               - 세트 내에서 명시된 피자명('+ [피자명]' 또는 별도 행으로 나오는 피자명)을 단품으로 집계하세요.
               - 세트명 자체는 집계하지 마세요.
            5. 도우(흑미도우, 씬도우 등), 크러스트(치즈크러스트, 고구마 엣지 등), 사이즈(L/M/R), 치즈 추가 등 옵션은 집계하지 마세요.
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
            Sheet sheet = workbook.getSheetAt(1);
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
