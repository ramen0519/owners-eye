package com.ownerseye.ownerseye.domain.upload.application.service;

import com.ownerseye.ownerseye.domain.sales.persistence.entity.CoupangSalesEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.CoupangSalesMapper;
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

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CoupangParserService {

    private final StoreMapper storeMapper;
    private final UploadService uploadService;
    private final CoupangSalesMapper coupangSalesMapper;

    @Transactional
    public void parse(Long userId, Long storeId, MultipartFile file, LocalDate yearMonth) {
        storeMapper.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new UploadException(UploadErrorCode.STORE_NOT_FOUND));

        UploadEntity upload = UploadEntity.builder()
                .storeId(storeId)
                .uploadType(UploadType.COUPANG.name())
                .yearMonth(yearMonth)
                .fileName(file.getOriginalFilename())
                .build();
        uploadService.save(upload);

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            long orderAmount = 0, serviceFee = 0, paymentFee = 0, deliveryFee = 0;
            long instantDiscount = 0, couponDiscount = 0, adFee = 0;

            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                if (getString(row, 0).isBlank()) continue;

                orderAmount    += getNumeric(row, 10);
                couponDiscount += getNumeric(row, 13);
                serviceFee     += getNumeric(row, 16);
                paymentFee     += getNumeric(row, 17);
                deliveryFee    += getNumeric(row, 21);
                instantDiscount += getNumeric(row, 22) + getNumeric(row, 23);
                adFee          += getNumeric(row, 36);
            }

            coupangSalesMapper.save(CoupangSalesEntity.builder()
                    .uploadId(upload.getUploadId())
                    .yearMonth(yearMonth)
                    .orderAmount(orderAmount)
                    .serviceFee(serviceFee)
                    .paymentFee(paymentFee)
                    .deliveryFee(deliveryFee)
                    .instantDiscount(instantDiscount)
                    .couponDiscount(couponDiscount)
                    .adFee(adFee)
                    .build());

            uploadService.updateStatus(upload.getUploadId(), ParseStatus.DONE.name());

        } catch (UploadException e) {
            throw e;
        } catch (Exception e) {
            log.error("[COUPANG PARSE ERROR]", e);
            uploadService.updateStatus(upload.getUploadId(), ParseStatus.FAILED.name());
            throw new UploadException(UploadErrorCode.PARSE_FAILED);
        }
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
