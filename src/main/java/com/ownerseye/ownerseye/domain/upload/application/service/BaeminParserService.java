package com.ownerseye.ownerseye.domain.upload.application.service;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminAdEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.entity.BaeminSalesEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.BaeminAdMapper;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.BaeminSalesMapper;
import com.ownerseye.ownerseye.domain.store.persistence.entity.StoreEntity;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import com.ownerseye.ownerseye.domain.upload.domain.constant.ParseStatus;
import com.ownerseye.ownerseye.domain.upload.domain.constant.UploadType;
import com.ownerseye.ownerseye.domain.upload.exception.UploadException;
import com.ownerseye.ownerseye.domain.upload.exception.code.UploadErrorCode;
import com.ownerseye.ownerseye.domain.upload.persistence.entity.UploadEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BaeminParserService {

    private static final String BAEMIN1 = "BAEMIN1";
    private static final String STORE = "STORE";
    private static final Set<String> BAEMIN1_TYPES = Set.of("알뜰배달", "한집배달", "알뜰·한집배달");
    private static final Set<String> SKIP_TYPES = Set.of("배민포장주문", "기타");

    private final StoreMapper storeMapper;
    private final UploadService uploadService;
    private final BaeminSalesMapper baeminSalesMapper;
    private final BaeminAdMapper baeminAdMapper;

    @Transactional
    public void parse(Long userId, MultipartFile file, LocalDate yearMonth) {
        StoreEntity store = storeMapper.findByUserId(userId)
                .orElseThrow(() -> new UploadException(UploadErrorCode.STORE_NOT_FOUND));

        UploadEntity upload = UploadEntity.builder()
                .storeId(store.getStoreId())
                .uploadType(UploadType.BAEMIN.name())
                .yearMonth(yearMonth)
                .fileName(file.getOriginalFilename())
                .build();
        uploadService.save(upload);

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(1);

            // [order, service, coupon, delivery, payment, vat]
            long[] baemin1 = new long[6];
            long[] storeType = new long[6];
            long adFee = 0L;

            for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String orderType = getString(row, 4);
                if (orderType.isBlank() || SKIP_TYPES.contains(orderType)) continue;

                if (BAEMIN1_TYPES.contains(orderType)) {
                    baemin1[0] += getNumeric(row, 5) + getNumeric(row, 6);
                    baemin1[1] += getNumeric(row, 7) + getNumeric(row, 8);
                    baemin1[2] += getNumeric(row, 11);
                    baemin1[3] += getNumeric(row, 18) + getNumeric(row, 19);
                    baemin1[4] += getNumeric(row, 20) + getNumeric(row, 21);
                    baemin1[5] += getNumeric(row, 25);
                } else if ("가게배달".equals(orderType)) {
                    storeType[0] += getNumeric(row, 5) + getNumeric(row, 6);
                    storeType[1] += getNumeric(row, 9);
                    storeType[2] += getNumeric(row, 11);
                    storeType[3] += getNumeric(row, 12) + getNumeric(row, 13);
                    storeType[4] += getNumeric(row, 20) + getNumeric(row, 21);
                    storeType[5] += getNumeric(row, 25);
                } else if ("우리가게클릭".equals(orderType)) {
                    adFee += getNumeric(row, 26) + getNumeric(row, 27);
                }
            }

            if (hasData(baemin1)) {
                baeminSalesMapper.save(BaeminSalesEntity.builder()
                        .uploadId(upload.getUploadId())
                        .yearMonth(yearMonth)
                        .deliveryType(BAEMIN1)
                        .orderAmount(baemin1[0]).serviceFee(baemin1[1]).couponDiscount(baemin1[2])
                        .deliveryCost(baemin1[3]).paymentFee(baemin1[4]).vat(baemin1[5])
                        .build());
            }

            if (hasData(storeType)) {
                baeminSalesMapper.save(BaeminSalesEntity.builder()
                        .uploadId(upload.getUploadId())
                        .yearMonth(yearMonth)
                        .deliveryType(STORE)
                        .orderAmount(storeType[0]).serviceFee(storeType[1]).couponDiscount(storeType[2])
                        .deliveryCost(storeType[3]).paymentFee(storeType[4]).vat(storeType[5])
                        .build());
            }

            if (adFee > 0) {
                baeminAdMapper.save(BaeminAdEntity.builder()
                        .uploadId(upload.getUploadId())
                        .yearMonth(yearMonth)
                        .adFee(adFee)
                        .build());
            }

            uploadService.updateStatus(upload.getUploadId(), ParseStatus.DONE.name());

        } catch (UploadException e) {
            throw e;
        } catch (Exception e) {
            log.error("[BAEMIN PARSE ERROR]", e);
            uploadService.updateStatus(upload.getUploadId(), ParseStatus.FAILED.name());
            throw new UploadException(UploadErrorCode.PARSE_FAILED);
        }
    }

    private boolean hasData(long[] acc) {
        for (long v : acc) {
            if (v != 0) return true;
        }
        return false;
    }

    private String getString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private long getNumeric(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return 0L;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                String val = cell.getStringCellValue().trim().replace(",", "");
                yield val.isBlank() ? 0L : Long.parseLong(val);
            }
            default -> 0L;
        };
    }
}
