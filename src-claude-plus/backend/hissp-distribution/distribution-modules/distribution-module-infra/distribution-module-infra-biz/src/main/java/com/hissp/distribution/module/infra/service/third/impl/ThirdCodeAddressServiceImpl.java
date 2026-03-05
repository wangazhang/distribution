package com.hissp.distribution.module.infra.service.third.impl;

import com.hissp.distribution.module.infra.dal.dataobject.third.ThirdCodeAddressDO;
import com.hissp.distribution.module.infra.dal.mysql.third.ThirdCodeAddressMapper;
import com.hissp.distribution.module.infra.service.third.ThirdCodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ThirdCodeAddressServiceImpl implements ThirdCodeAddressService {

    @Resource
    private ThirdCodeAddressMapper mapper;

    @Override
    public List<ThirdCodeAddressDO> listProvinces(String provider) {
        return mapper.selectByLevel(provider, 1);
    }

    @Override
    public List<ThirdCodeAddressDO> listCities(String provider, String parentCode) {
        return mapper.selectByParent(provider, parentCode);
    }

    @Override
    public List<ThirdCodeAddressDO> listDistricts(String provider, String parentCode) {
        return mapper.selectByParent(provider, parentCode);
    }

    @Override
    @Transactional
    public int importFromExcel(String provider, String version, String filePath) {
        int count = 0;
        try (FileInputStream fis = new FileInputStream(filePath); Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            List<ThirdCodeAddressDO> batch = new ArrayList<>();
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) { skipHeader = false; continue; }
                if (row == null) continue;
                String code = getString(row, 0);
                String name = getString(row, 1);
                Integer level = parseInt(getString(row, 2));
                String parent = getString(row, 3);
                if (code == null || name == null || level == null) continue;
                ThirdCodeAddressDO po = ThirdCodeAddressDO.builder()
                        .provider(provider)
                        .code(code.trim())
                        .name(name.trim())
                        .level(level)
                        .parentCode(parent != null ? parent.trim() : null)
                        .sort(0)
                        .status(0)
                        .version(version)
                        .build();
                batch.add(po);
                if (batch.size() >= 1000) {
                    batch.forEach(mapper::insert);
                    count += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                batch.forEach(mapper::insert);
                count += batch.size();
            }
            log.info("[ThirdCodeAddressService][import ok] provider={}, version={}, count={}", provider, version, count);
        } catch (Exception e) {
            log.error("[ThirdCodeAddressService][import error] filePath={}, err={}", filePath, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return count;
    }

    @Override
    @Transactional
    public int importFromExcel(String provider, String version, java.io.InputStream inputStream) {
        int count = 0;
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            List<ThirdCodeAddressDO> batch = new ArrayList<>();
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) { skipHeader = false; continue; }
                if (row == null) continue;
                String code = getString(row, 0);
                String name = getString(row, 1);
                Integer level = parseInt(getString(row, 2));
                String parent = getString(row, 3);
                if (code == null || name == null || level == null) continue;
                ThirdCodeAddressDO po = ThirdCodeAddressDO.builder()
                        .provider(provider)
                        .code(code.trim())
                        .name(name.trim())
                        .level(level)
                        .parentCode(parent != null ? parent.trim() : null)
                        .sort(0)
                        .status(0)
                        .version(version)
                        .build();
                batch.add(po);
                if (batch.size() >= 1000) {
                    batch.forEach(mapper::insert);
                    count += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                batch.forEach(mapper::insert);
                count += batch.size();
            }
            log.info("[ThirdCodeAddressService][import ok(stream)] provider={}, version={}, count={}", provider, version, count);
        } catch (Exception e) {
            log.error("[ThirdCodeAddressService][import error(stream)] err={}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return count;
    }

    private String getString(Row row, int cell) {
        if (row.getCell(cell) == null) return null;
        row.getCell(cell).setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        String v = row.getCell(cell).getStringCellValue();
        return v != null ? v.trim() : null;
    }

    private Integer parseInt(String s) {
        try { return s != null ? Integer.parseInt(s.trim()) : null; } catch (Exception e) { return null; }
    }
}
