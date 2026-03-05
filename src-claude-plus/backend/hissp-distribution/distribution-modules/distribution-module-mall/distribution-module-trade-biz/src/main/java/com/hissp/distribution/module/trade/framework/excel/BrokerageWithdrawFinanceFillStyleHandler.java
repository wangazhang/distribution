package com.hissp.distribution.module.trade.framework.excel;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.Map;

/**
 * 给需要填写的列添加底色提示
 */
public class BrokerageWithdrawFinanceFillStyleHandler implements CellWriteHandler {

    private static final int TARGET_COLUMN = 11; // L列，从0开始
    private CellStyle cachedDataStyle;
    private CellStyle cachedHeaderStyle;
    private final Map<CellStyle, CellStyle> copiedCache = new HashMap<>();

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        if (cell == null) {
            return;
        }
        if (cell.getColumnIndex() != TARGET_COLUMN) {
            return;
        }
        Workbook workbook = cell.getSheet().getWorkbook();
        boolean header = context.getRowIndex() != null && context.getRowIndex() == 0;
        CellStyle style = header ? getHeaderStyle(workbook) : getDataStyle(workbook);
        CellStyle base = cell.getCellStyle();
        CellStyle cached = copiedCache.get(base);
        if (cached == null) {
            CellStyle newStyle = workbook.createCellStyle();
            newStyle.cloneStyleFrom(base);
            newStyle.setFillForegroundColor(style.getFillForegroundColor());
            newStyle.setFillPattern(style.getFillPattern());
            newStyle.setAlignment(style.getAlignment());
            newStyle.setVerticalAlignment(style.getVerticalAlignment());
            copiedCache.put(base, newStyle);
            cached = newStyle;
        }
        cell.setCellStyle(cached);
    }

    private CellStyle getDataStyle(Workbook workbook) {
        if (cachedDataStyle == null) {
            cachedDataStyle = workbook.createCellStyle();
            cachedDataStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            cachedDataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cachedDataStyle.setAlignment(HorizontalAlignment.CENTER);
            cachedDataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        return cachedDataStyle;
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        if (cachedHeaderStyle == null) {
            cachedHeaderStyle = workbook.createCellStyle();
            cachedHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            cachedHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cachedHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            cachedHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        return cachedHeaderStyle;
    }
}
