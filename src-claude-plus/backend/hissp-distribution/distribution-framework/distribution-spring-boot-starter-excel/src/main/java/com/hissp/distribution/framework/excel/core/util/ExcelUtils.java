package com.hissp.distribution.framework.excel.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.excel.write.handler.WriteHandler;
import com.hissp.distribution.framework.common.util.http.HttpUtils;
import com.hissp.distribution.framework.dict.core.DictFrameworkUtils;
import com.hissp.distribution.framework.excel.core.annotations.DictFormat;
import com.hissp.distribution.framework.excel.core.handler.SelectSheetWriteHandler;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Excel 工具类
 *
 * @author 芋道源码
 */
public class ExcelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param <T>       泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        write(response, filename, sheetName, head, data, Collections.emptyList());
    }

    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data, List<WriteHandler> extraHandlers) throws IOException {
        String finalFileName = filename;
        if (!(finalFileName.endsWith(".xlsx") || finalFileName.endsWith(".xls"))) {
            finalFileName = finalFileName + ".xlsx";
        } else if (finalFileName.endsWith(".xls")) {
            finalFileName = finalFileName.substring(0, finalFileName.length() - 4) + ".xlsx";
        }
        var excelWriter = EasyExcel.write(response.getOutputStream(), head)
                .autoCloseStream(false)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new SelectSheetWriteHandler(head))
                .registerConverter(new LongStringConverter());
        if (extraHandlers != null) {
            extraHandlers.forEach(excelWriter::registerWriteHandler);
        }
        excelWriter.sheet(sheetName).doWrite(data);
        response.setHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(finalFileName));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        int sheetNo = getFirstDataSheetNo(bytes);
        List<T> list = EasyExcel.read(new ByteArrayInputStream(bytes), head, null)
                .autoCloseStream(false)  // 不要自动关闭，交给 Servlet 自己处理
                .registerConverter(new LongStringConverter()) // 兼容 Long 字段字符串形式
                .sheet(sheetNo)
                .headRowNumber(1)
                .doReadSync();
        if (!list.isEmpty()) {
            LOGGER.info("[ExcelUtils][read] file={}, sheetNo={}, rows={}, firstRow={}",
                    file.getOriginalFilename(), sheetNo, list.size(), list.get(0));
        } else {
            LOGGER.info("[ExcelUtils][read] file={}, sheetNo={}, rows=0", file.getOriginalFilename(), sheetNo);
        }

        if (!list.isEmpty() && list.stream().allMatch(ExcelUtils::isAllFieldNull)) {
            LOGGER.warn("[ExcelUtils][read] file={} easyExcel解析结果为空字段，尝试使用 POI 解析", file.getOriginalFilename());
            list = readWithPoi(bytes, sheetNo, head);
        }
        return list;
    }

    private static int getFirstDataSheetNo(byte[] bytes) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String name = workbook.getSheetName(i);
                if (!"字典sheet".equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }
        return 0;
    }

    private static <T> boolean isAllFieldNull(T bean) {
        if (bean == null) {
            return true;
        }
        try {
            for (Field field : bean.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(bean) != null) {
                    return false;
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return true;
    }

    private static <T> List<T> readWithPoi(byte[] bytes, int sheetNo, Class<T> head) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(sheetNo);
            if (sheet == null) {
                return Collections.emptyList();
            }
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return Collections.emptyList();
            }
            Map<Integer, Field> columnFieldMap = buildColumnFieldMap(headerRow, head, formatter);
            if (columnFieldMap.isEmpty()) {
                LOGGER.warn("[ExcelUtils][readWithPoi] headerRow 未匹配到任何字段");
                return Collections.emptyList();
            }
            List<T> result = new ArrayList<>();
            int firstDataRow = sheet.getFirstRowNum() + 1;
            for (int rowNum = firstDataRow; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                T bean = instantiate(head);
                boolean hasValue = false;
                for (Map.Entry<Integer, Field> entry : columnFieldMap.entrySet()) {
                    int colIndex = entry.getKey();
                    Field field = entry.getValue();
                    Cell cell = row.getCell(colIndex);
                    String cellValue = cell == null ? null : formatter.formatCellValue(cell);
                    if (StrUtil.isBlank(cellValue)) {
                        continue;
                    }
                    Object converted = convertValue(field, cellValue);
                    if (converted != null) {
                        hasValue = true;
                    }
                    setFieldValue(bean, field, converted);
                }
                if (hasValue) {
                    result.add(bean);
                }
            }
            LOGGER.info("[ExcelUtils][readWithPoi] rows={}", result.size());
            return result;
        } catch (Exception ex) {
            LOGGER.error("[ExcelUtils][readWithPoi] 异常", ex);
            throw ex;
        }
    }

    private static <T> Map<Integer, Field> buildColumnFieldMap(Row headerRow, Class<T> head, DataFormatter formatter) {
        Map<Integer, Field> map = new HashMap<>();
        Map<String, Field> headerToField = new HashMap<>();
        for (Field field : head.getDeclaredFields()) {
            field.setAccessible(true);
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null && excelProperty.value().length > 0) {
                for (String header : excelProperty.value()) {
                    if (StrUtil.isNotBlank(header)) {
                        headerToField.put(header.trim(), field);
                    }
                }
            } else {
                headerToField.put(field.getName(), field);
            }
        }
        for (int i = headerRow.getFirstCellNum(); i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                continue;
            }
            String header = formatter.formatCellValue(cell);
            if (StrUtil.isBlank(header)) {
                continue;
            }
            Field field = headerToField.get(header.trim());
            if (field != null) {
                map.put(i, field);
            }
        }
        return map;
    }

    private static Object convertValue(Field field, String cellValue) {
        try {
            DictFormat dictFormat = field.getAnnotation(DictFormat.class);
            String value = cellValue.trim();
            if (dictFormat != null) {
                String dictValue = DictFrameworkUtils.parseDictDataValue(dictFormat.value(), value);
                if (dictValue == null) {
                    LOGGER.warn("[ExcelUtils][convertValue] dict parse fail field={}, label={}", field.getName(), value);
                    return null;
                }
                value = dictValue;
            }
            Class<?> type = field.getType();
            if (type.equals(String.class)) {
                return value;
            }
            if (type.equals(Integer.class) || type.equals(int.class)) {
                return Convert.toInt(value);
            }
            if (type.equals(Long.class) || type.equals(long.class)) {
                return Convert.toLong(value);
            }
            if (type.equals(BigDecimal.class)) {
                return new BigDecimal(value);
            }
            if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                return Convert.toBool(value);
            }
            return Convert.convert(type, value);
        } catch (Exception ex) {
            LOGGER.warn("[ExcelUtils][convertValue] 转换失败 field={}, value={}, err={}", field.getName(), cellValue, ex.getMessage());
            return null;
        }
    }

    private static <T> T instantiate(Class<T> head) {
        try {
            return head.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("创建实例失败: " + head.getName(), ex);
        }
    }

    private static void setFieldValue(Object target, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            LOGGER.warn("[ExcelUtils][setFieldValue] 赋值失败 field={}, value={}, err={}", field.getName(), value, ex.getMessage());
        }
    }

}
