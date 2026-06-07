package com.siat;

// Apache POI imports (Excel)
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// iText 7 imports (PDF)
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javax.swing.JTable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportUtil {

    /**
     * Export a {@link JTable} to a PDF file using iText 7.
     *
     * @param table    the Swing table containing the data
     * @param destFile destination file (must end with .pdf)
     * @throws Exception if an error occurs while writing the PDF
     */
    public static void exportTableToPdf(JTable table, File destFile) throws Exception {
        // Create PDF writer and document
        try (PdfWriter writer = new PdfWriter(destFile);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            int columnCount = table.getColumnCount();
            // Create a table with equal column widths
            Table pdfTable = new Table(UnitValue.createPercentArray(columnCount)).useAllAvailableWidth();
            pdfTable.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));

            // Header row – bold background
            for (int col = 0; col < columnCount; col++) {
                com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(table.getColumnName(col)))
                        .setBackgroundColor(ColorConstants.DARK_GRAY)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER);
                pdfTable.addHeaderCell(headerCell);
            }

            // Data rows
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < columnCount; col++) {
                    Object value = table.getValueAt(row, col);
                    com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(value == null ? "" : value.toString()))
                            .setTextAlignment(TextAlignment.LEFT);
                    pdfTable.addCell(cell);
                }
            }
            document.add(pdfTable);
        }
    }

    /**
     * Export a {@link JTable} to an Excel (.xlsx) file using Apache POI.
     *
     * @param table    the Swing table containing the data
     * @param destFile destination file (must end with .xlsx)
     * @throws IOException if an I/O error occurs while writing the workbook
     */
    public static void exportTableToExcel(JTable table, File destFile) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Inventario");

            // Header style – bold with dark background
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < table.getColumnCount(); col++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(col);
                cell.setCellValue(table.getColumnName(col));
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(col);
            }

            // Data rows
            for (int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
                Row row = sheet.createRow(rowIdx + 1);
                for (int col = 0; col < table.getColumnCount(); col++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
                    Object value = table.getValueAt(rowIdx, col);
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                workbook.write(fos);
            }
        }
    }
}
