package com.ownerseye.ownerseye.domain.upload.application.service;

import com.ownerseye.ownerseye.domain.sales.domain.constant.PosChannel;
import com.ownerseye.ownerseye.domain.sales.persistence.entity.PosSalesEntity;
import com.ownerseye.ownerseye.domain.sales.persistence.mapper.PosSalesMapper;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import com.ownerseye.ownerseye.domain.upload.domain.constant.ParseStatus;
import com.ownerseye.ownerseye.domain.upload.domain.constant.UploadType;
import com.ownerseye.ownerseye.domain.upload.exception.UploadException;
import com.ownerseye.ownerseye.domain.upload.exception.code.UploadErrorCode;
import com.ownerseye.ownerseye.domain.upload.persistence.entity.UploadEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PosParserService {

    private static final Set<String> SKIP_CHANNELS = Set.of("배달의민족", "배민1", "쿠팡이츠");

    private final StoreMapper storeMapper;
    private final UploadService uploadService;
    private final PosSalesMapper posSalesMapper;

    @Transactional
    public void parse(Long userId, Long storeId, MultipartFile file, LocalDate yearMonth) {
        storeMapper.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new UploadException(UploadErrorCode.STORE_NOT_FOUND));

        UploadEntity upload = UploadEntity.builder()
                .storeId(storeId)
                .uploadType(UploadType.POS.name())
                .yearMonth(yearMonth)
                .fileName(file.getOriginalFilename())
                .build();
        uploadService.save(upload);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(1);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String dateCell = getString(row, 0);
                if (dateCell.isBlank()) continue;

                String channelName = getString(row, 1);
                if (SKIP_CHANNELS.contains(channelName)) continue;

                Optional<PosChannel> channel = PosChannel.fromKorName(channelName);
                if (channel.isEmpty()) continue;

                PosSalesEntity posSales = PosSalesEntity.builder()
                        .uploadId(upload.getUploadId())
                        .yearMonth(yearMonth)
                        .channel(channel.get().name())
                        .totalRevenue(getNumeric(row, 2))
                        .avgPrice(getNumeric(row, 3))
                        .totalOrders(getNumeric(row, 4))
                        .build();

                posSalesMapper.save(posSales);
            }

            uploadService.updateStatus(upload.getUploadId(), ParseStatus.DONE.name());

        } catch (UploadException e) {
            throw e;
        } catch (Exception e) {
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
