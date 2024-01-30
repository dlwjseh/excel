package my.example.excel;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;

public class ExcelSheetWriter {
	private final SXSSFSheet sheet;
	private final Set<MergeCell> mergeCells = new HashSet<>();
	private final Map<String, CellStyle> styles;
	private final int chunkSize;
	private int currentRowNumber = 0;

	public ExcelSheetWriter(ExcelWriter writer, String sheetName) {
		this.sheet = writer.createSheet(sheetName);
		this.styles = writer.getStyles();
		this.chunkSize = writer.getConfig().getChunkSize();
	}

	public void addRows(List<List<ExcelCell>> excelRows) {
		for (List<ExcelCell> excelCells : excelRows) {
			this.addRow(excelCells);
		}
	}

	public void addRows(Supplier<List<List<ExcelCell>>> contentSupplier) {
		this.addRows(contentSupplier.get());
	}

	public <T> void addRows(T t, Function<T, List<List<ExcelCell>>> contentFunction) {
		this.addRows(contentFunction.apply(t));
	}

	public <T, R> void addRows(T t, R r, BiFunction<T, R, List<List<ExcelCell>>> contentFunction) {
		this.addRows(contentFunction.apply(t, r));
	}

	public void addRow(List<Object> values, String style) {
		this.addRow(values.stream().map(v -> new ExcelCell(v, style)).collect(Collectors.toList()));
	}

	public void addRow(Supplier<List<Object>> valueSupplier, String style) {
		this.addRow(valueSupplier.get(), style);
	}

	public void addRow(Supplier<List<ExcelCell>> contentSupplier) {
		this.addRow(contentSupplier.get());
	}

	public <T> void addRow(T t, Function<T, List<ExcelCell>> contentFunction) {
		this.addRow(contentFunction.apply(t));
	}

	public void addGap(int rowSize) {
		this.currentRowNumber += rowSize;
	}

	public void addRow(List<ExcelCell> excelCells) {
		Row row = this.sheet.createRow(this.currentRowNumber);
		int columnNumber = 0;

		// row의 cell 추가
		for (ExcelCell cell : excelCells) {
			while (!fillMergeStyle(row, columnNumber)) { // merge됐던 cell이라면 style만 채우고 넘어감
				columnNumber++;
			}
			addColumn(row, columnNumber, cell);
			columnNumber++;
		}

		// 아직 merge됐던 cell이 뒤에 남아있는지 검사하고 있으면 style 채움
		int maxMergeColNum = this.mergeCells.stream()
				.filter(mc -> row.getRowNum() == mc.getRow())
				.mapToInt(MergeCell::getCol).max().orElse(0);
		while (maxMergeColNum >= columnNumber) {
			if (fillMergeStyle(row, columnNumber)) break;
			columnNumber++;
		}

		this.currentRowNumber++;
	}

	private boolean fillMergeStyle(Row row, int columnNumber) {
		MergeCell mergeCell = new MergeCell(this.currentRowNumber, columnNumber, null);
		Optional<MergeCell> any = this.mergeCells.stream().filter(m -> m.equals(mergeCell)).findAny();
		if (any.isEmpty()) {
			return true;
		}
		addColumn(row, columnNumber, ExcelCell.empty(any.get().getStyle()));
		return false;
	}

	private void addColumn(Row row, int columnNumber, ExcelCell excelCell) {
		Cell cell = row.createCell(columnNumber);
		setCellValue(cell, excelCell.getValue());

		if (excelCell.getStyle() != null) {
			CellStyle cellStyle = this.styles.get(excelCell.getStyle());
			if (cellStyle == null) {
				throw new RuntimeException(excelCell.getStyle() + " Style을 미리 세팅 해 주세요");
			}
			cell.setCellStyle(cellStyle);
		}

		if (excelCell.isMergeCell()) {
			this.merge(row.getRowNum(), excelCell.getMergeRow(), columnNumber, excelCell.getMergeColumn(),
					excelCell.getStyle());
		}
	}

	private static void setCellValue(Cell cell, Object value) {
		if (value instanceof Double) {
			cell.setCellValue((Double) value);
		} else if (value instanceof Long) {
			cell.setCellValue((Long) value);
		} else if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else {
			cell.setCellValue(String.valueOf(value));
		}
	}

	private void merge(int firstRow, int rowSize, int firstCol, int colSize, String style) {
		int row = firstRow;
		int col = firstCol;
		int maxRow = firstRow + rowSize - 1;
		int maxCol = firstCol + colSize - 1;

		while(row<=maxRow) {
			while(col<=maxCol) {
				this.mergeCells.add(new MergeCell(row, col, style));
				col++;
			}
			row++;
			col = firstCol;
		}
		sheet.addMergedRegion(new CellRangeAddress(firstRow, maxRow, firstCol, maxCol));
	}

	public void setWidths(List<Integer> widths) {
		if (!CollectionUtils.isEmpty(widths)) {
			int col = 0;
			for (int width : widths) {
				sheet.setColumnWidth(col++, width*260);
			}
		}
	}

	public void clear() {
		try {
			sheet.flushRows();
		} catch (IOException e) {
			throw new RuntimeException("SXSSF Flush 중에 에러가 발생했습니다: " + e.getMessage());
		}
	}

}
