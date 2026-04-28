package com.example.smartmeetingroom.service.asset;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.asset.AssetUpdateDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetTypeRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.specification.AssetSpecification;
import com.example.smartmeetingroom.util.ConfigUtil;
import com.example.smartmeetingroom.util.SecurityUtil;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void addAsset(AssetDTO dto){
        Set<AssetStatus> ALLOWED_CREATE_STATUS  = Set.of(AssetStatus.AVAILABLE, AssetStatus.PENDING_INSTALLATION);
        var assetName = StringCapitalizeUtil.capitalizeEachWord(dto.getAssetName());
        var serialNumber = dto.getSerialNumber().trim().toUpperCase();
        if (!dto.getWarrantyExpiry().isAfter(dto.getPurchaseDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Warranty expiry must be after purchase date");
        }
        var asset = assetRepository.findBySerialNumber(serialNumber);
        if (asset.isPresent() && asset.get().getIsDeleted() == false) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number already exists");
        }
        if (assetRepository.existsBySerialNumber(serialNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number already exists");
        }
        if (!ALLOWED_CREATE_STATUS.contains(dto.getAssetStatus())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status for asset creation");
        }
        if (assetRepository.existsByAssetNameAndAssetTypeIdNot(assetName, dto.getAssetTypeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, assetName + " is already exists for other type");
        }
        var assetType = assetTypeRepository.findById(dto.getAssetTypeId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset type not found")
        );
        var room = meetingRoomRepository.findById(dto.getMeetingRoomId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting room not found")
        );

        if (asset.isPresent()){
            asset.get().setAssetName(assetName);
            asset.get().setSerialNumber(dto.getSerialNumber());
            asset.get().setPurchaseDate(dto.getPurchaseDate());
            asset.get().setWarrantyExpiry(dto.getWarrantyExpiry());
            asset.get().setRoom(room);
            asset.get().setAssetType(assetType);
            asset.get().setStatus(dto.getAssetStatus());
        } else {
            var newAsset = new Asset();
            newAsset.setAssetName(assetName);
            newAsset.setSerialNumber(dto.getSerialNumber());
            newAsset.setPurchaseDate(dto.getPurchaseDate());
            newAsset.setWarrantyExpiry(dto.getWarrantyExpiry());
            newAsset.setRoom(room);
            newAsset.setAssetType(assetType);
            newAsset.setStatus(dto.getAssetStatus());
            assetRepository.save(newAsset);
        }


    }

    @Transactional
    public void deleteAsset(Long assetId, Long meetingRoomId) {
        Asset asset;
        if (meetingRoomId == null) {
            asset = assetRepository.findByIdAndIsDeletedFalse(assetId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found")
            );
        }else {
            asset = assetRepository.findByIdAndRoom_IdAndIsDeletedFalse(assetId, meetingRoomId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found in the specified room")
            );
        }
       asset.setIsDeleted(true);
    }

    public PageResponseDTO<AssetDTO> getAllAssets(int page,
                                    int size,
                                    String search,
                                    Short typeId,
                                    Long meetingRoomId,
                                    AssetStatus status,
                                    boolean onlyDeleted,
                                    List<String> sortParam) {
        var allowedSortFields = ConfigUtil.getAllowedValues("ALLOWED_SORT_FIELDS");
        var specification = AssetSpecification.getAssets(search,typeId,meetingRoomId,onlyDeleted,status);
        var orders = getOrders(sortParam, allowedSortFields);
        Sort sort = Sort.by(orders);
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        var content =  assetRepository.findAll(specification, pageable);
        return getAssetDTOPageResponseDTO(content);
    }

    @Transactional
    public void updateAsset(Long assetId, JsonNode request) {
        var userRole = SecurityUtil.getCurrentUserRole();
        if (userRole == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        var asset = assetRepository.findByIdAndIsDeletedFalse(assetId).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found.")
        );

        if (asset.getStatus() == AssetStatus.IN_USE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot update asset while it is being used.");
        }
        var dto = objectMapper.convertValue(request, AssetUpdateDTO.class);
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            // update name, extend warranty, change room , change status
            var adminAllowedUpdateFields = ConfigUtil.getAllowedValues("ADMIN_ALLOWED_UPDATE_FIELDS");
            checkAllowedFields(request, adminAllowedUpdateFields);
            // change asset name
            validateAndUpdateAssetNameAndRoom(dto, asset);

            //extend warranty
            if (dto.getWarrantyExpiry() != null) {
                if (dto.getWarrantyExpiry().isBefore(asset.getWarrantyExpiry())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "You can only extend warranty.");
                }
                asset.setWarrantyExpiry(dto.getWarrantyExpiry());
            }
            //change status
            if (dto.getAssetStatus() != null) {
                var adminAllowedUpdateStatus = ConfigUtil.getAllowedValues("ADMIN_ALLOWED_UPDATE_STATUS")
                        .stream()
                        .map(AssetStatus::valueOf)
                        .collect(Collectors.toSet());
                if (!adminAllowedUpdateStatus.contains(dto.getAssetStatus())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Admin cannot set status to " + dto.getAssetStatus());
                }
                asset.setStatus(dto.getAssetStatus());
            }
        } else if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) {
            var superAdminAllowedUpdateFields = ConfigUtil.getAllowedValues("SUPER_ADMIN_ALLOWED_UPDATE_FIELDS");
            checkAllowedFields(request, superAdminAllowedUpdateFields);
            validateAndUpdateFields(assetId, dto, asset);
        }
    }

    private void validateAndUpdateFields(Long assetId, AssetUpdateDTO dto, Asset asset) {
        validateAndUpdateAssetNameAndRoom(dto, asset);
        //change purchase date
        if (dto.getPurchaseDate() != null) {
            if (dto.getWarrantyExpiry() != null) {
                if (dto.getPurchaseDate().isAfter(dto.getWarrantyExpiry()))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New purchase date cannot be after new warranty.");
            }else if (dto.getPurchaseDate().isAfter(asset.getWarrantyExpiry())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New purchase date cannot be after existing warranty.");
            }
            asset.setPurchaseDate(dto.getPurchaseDate());
        }

        // change warranty
        if (dto.getWarrantyExpiry() != null) {
            if (dto.getWarrantyExpiry().isBefore(asset.getPurchaseDate())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Warranty cannot be before purchase date.");
            }
            asset.setWarrantyExpiry(dto.getWarrantyExpiry());
        }

        // change serial number
        if (dto.getSerialNumber() != null) {
            var serialNumber = dto.getSerialNumber().trim().toUpperCase();
            if (assetRepository.existsBySerialNumberAndIdNot(serialNumber, assetId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, serialNumber + " already exits.");
            }
            asset.setSerialNumber(serialNumber);
        }

        // change asset type
        if (dto.getAssetTypeId() != null){
            var assetName = dto.getAssetName() == null ? asset.getAssetName() : dto.getAssetName();
            if (assetRepository.existsByAssetNameAndAssetTypeIdNotAndIdNot(assetName, dto.getAssetTypeId(), assetId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, assetName + " cannot exist in multiple types");
            }
            asset.setAssetType(assetTypeRepository.getReferenceById(dto.getAssetTypeId()));
        }

        // change status
        if (dto.getAssetStatus() != null) {
            asset.setStatus(dto.getAssetStatus());
        }
    }

    private void validateAndUpdateAssetNameAndRoom(AssetUpdateDTO dto, Asset asset) {
        // change asset name
        if (dto.getAssetName() != null && !dto.getAssetName().isBlank()){
            asset.setAssetName(StringCapitalizeUtil.capitalizeEachWord(dto.getAssetName().trim()));
        }

        //change room
        if (dto.getMeetingRoomId() != null) {
            if (!asset.getRoom().getId().equals(dto.getMeetingRoomId())){
                var meetingRoom = meetingRoomRepository.findByIdAndIsDeletedFalse(dto.getMeetingRoomId()).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting room not found")
                );
                asset.setRoom(meetingRoom);
            }
        }
    }

    private static void checkAllowedFields(JsonNode request, Set<String> allowedFields) {
        request.fieldNames().forEachRemaining(
                f -> {
                    if (!allowedFields.contains(f)){
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid field: " + f);
                    }
                }
        );
    }

    private static PageResponseDTO<AssetDTO> getAssetDTOPageResponseDTO(Page<Asset> content) {
        Page<AssetDTO> dtoPage = content.map(a -> {
            var asset = new AssetDTO();
            asset.setAssetId(a.getId());
            asset.setAssetName(a.getAssetName());
            asset.setSerialNumber(a.getSerialNumber());
            asset.setPurchaseDate(a.getPurchaseDate());
            asset.setWarrantyExpiry(a.getWarrantyExpiry());
            asset.setMeetingRoomName(a.getRoom().getRoomName());
            asset.setAssetTypeName(a.getAssetType().getName());
            return asset;
        });
        var pageResponse = new PageResponseDTO<AssetDTO>();
        pageResponse.setContent(dtoPage.getContent());
        pageResponse.setPage(dtoPage.getNumber() + 1);
        pageResponse.setSize(dtoPage.getSize());
        pageResponse.setTotalPages(dtoPage.getTotalPages());
        pageResponse.setTotalElements(dtoPage.getTotalElements());
        return pageResponse;
    }

    private static List<Sort.Order> getOrders(List<String> sortParam, Set<String> allowedSortFields) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sortParam != null && !sortParam.isEmpty()) {
            for (String sortValue : sortParam) {
                if (!sortValue.isBlank()) {
                    var sortValueParts = sortValue.split(",");
                    var field = sortValueParts[0].trim();
                    var direction = sortValueParts[1].trim().length() > 1 ? sortValueParts[1].trim() : "asc";

                    if (!allowedSortFields.contains(field)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field: " + field);
                    }
                    if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort direction: " + direction);
                    }
                    Sort.Order order = direction.equalsIgnoreCase("asc") ? new Sort.Order(Sort.Direction.ASC, field) : new Sort.Order(Sort.Direction.DESC, field);
                    orders.add(order);
                }
            }
        }else {
            orders.add(new Sort.Order(Sort.Direction.ASC, "purchaseDate"));
        }
        if (orders.isEmpty()){
            orders.add(new Sort.Order(Sort.Direction.ASC, "purchaseDate"));
        }
        return orders;
    }

    public byte[] generateExcel() {
        var assets = assetRepository.getAllAssets();

        try(Workbook workbook = new XSSFWorkbook()) {
            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("yyyy-MM-dd")
            );

            Sheet sheet = workbook.createSheet("Assets");

            // headers
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Sl No");
            header.createCell(1).setCellValue("Asset Name");
            header.createCell(2).setCellValue("Serial Number");
            header.createCell(3).setCellValue("Purchase Date");
            header.createCell(4).setCellValue("Warranty");
            header.createCell(5).setCellValue("Meeting Room");
            header.createCell(6).setCellValue("Asset Type");
            header.createCell(7).setCellValue("Status");

            int rowNum = 1;
            int slNo = 1;

            for (var asset : assets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(slNo++);
                row.createCell(1).setCellValue(asset.getAssetName());
                row.createCell(2).setCellValue(asset.getSerialNumber());

                var purchaseDate = row.createCell(3);
                purchaseDate.setCellStyle(dateStyle);
                purchaseDate.setCellValue(asset.getPurchaseDate());

                var warrantyCell = row.createCell(4);
                warrantyCell.setCellStyle(dateStyle);
                warrantyCell.setCellValue(asset.getWarrantyExpiry());

                row.createCell(5).setCellValue(asset.getMeetingRoomName());
                row.createCell(6).setCellValue(asset.getAssetTypeName());
                row.createCell(7).setCellValue(asset.getAssetStatus().toString());
            }

            // adjust column width
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }catch (Exception e) {
            log.error("Error generating Excel file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate Excel file");
        }
    }

    public byte[] generatePdf() {
        var assets = assetRepository.getAllAssets();
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Asset Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            // table with columns
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            // header
            addHeaderCell(table, "Sl No");
            addHeaderCell(table, "Name");
            addHeaderCell(table, "Serial Number");
            addHeaderCell(table, "Purchase Date");
            addHeaderCell(table, "Warranty");
            addHeaderCell(table, "Room");
            addHeaderCell(table, "Asset Type");
            addHeaderCell(table, "Status");

            int slNo = 1;
            for (var asset : assets) {
                table.addCell(String.valueOf(slNo++));
                table.addCell(asset.getAssetName());
                table.addCell(asset.getSerialNumber());
                table.addCell(asset.getPurchaseDate().toString());
                table.addCell(asset.getWarrantyExpiry().toString());
                table.addCell(asset.getMeetingRoomName());
                table.addCell(asset.getAssetTypeName());
                table.addCell(asset.getAssetStatus().toString());
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            log.error("Error generating PDF file", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate PDF file"
            );
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);

        table.addCell(cell);
    }
}
