package my.example.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelWriter {
	private final SXSSFWorkbook workbook;
	private SXSSFSheet currentSheet;
	private int currentRowNumber = 0;
	private final Set<MergeCell> mergeCells = new HashSet<>();
	private final ExcelConfig config;
	private final Map<String, CellStyle> styles;

	public ExcelWriter(ExcelConfig config) {
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		workbook.setCompressTempFiles(true);
		currentSheet = workbook.createSheet();
		this.workbook = workbook;
		this.config = config;
		this.styles = config.convertStyle(workbook);
		if (this.config.getWidths().length > 0) {
			int col = 0;
			for (int width : this.config.getWidths()) {
				currentSheet.setColumnWidth(col++, width*260);
			}
		}
	}

	public void write(OutputStream os) throws IOException {
		this.workbook.write(os);
	}

	public void download(HttpServletResponse response) {
		try (ServletOutputStream outputStream = response.getOutputStream()) {
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=" + config.getFileName() + ".xlsx");
			write(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addRow(List<ExcelCell> excelCells) {
		Row row = this.currentSheet.createRow(this.currentRowNumber);
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

		if (this.currentRowNumber!=0 && this.currentRowNumber%config.getChunkSize() == 0) {
			try {
				currentSheet.flushRows();
			} catch (IOException e) {
				throw new RuntimeException("SXSSF Flush 중에 에러가 발생했습니다: " + e.getMessage());
			}
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
		currentSheet.addMergedRegion(new CellRangeAddress(firstRow, maxRow, firstCol, maxCol));
	}

	public static Builder builder() {
		return new Builder(new ExcelWriter(new ExcelConfig()));
	}
	public static Builder builder(ExcelConfig config) {
		return new Builder(new ExcelWriter(config));
	}

	public static class Builder {
		private final ExcelWriter writer;

		private Builder(ExcelWriter writer) {
			this.writer = writer;
		}

		public Builder addRows(List<List<ExcelCell>> excelRows) {
			for (List<ExcelCell> excelCells : excelRows) {
				this.writer.addRow(excelCells);
			}
			return this;
		}

		public Builder addRows(Supplier<List<List<ExcelCell>>> contentSupplier) {
			return this.addRows(contentSupplier.get());
		}

		public <T> Builder addRows(T t, Function<T, List<List<ExcelCell>>> contentFunction) {
			return this.addRows(contentFunction.apply(t));
		}

		public <T, R> Builder addRows(T t, R r, BiFunction<T, R, List<List<ExcelCell>>> contentFunction) {
			return this.addRows(contentFunction.apply(t, r));
		}

		public Builder add(List<Object> values, String style) {
			this.writer.addRow(values.stream().map(v -> new ExcelCell(v, style)).collect(Collectors.toList()));
			return this;
		}

		public Builder add(Supplier<List<Object>> valueSupplier, String style) {
			return this.add(valueSupplier.get(), style);
		}

		public Builder add(List<ExcelCell> excelCells) {
			this.writer.addRow(excelCells);
			return this;
		}

		public Builder add(Supplier<List<ExcelCell>> contentSupplier) {
			return this.add(contentSupplier.get());
		}

		public <T> Builder add(T t, Function<T, List<ExcelCell>> contentFunction) {
			return this.add(contentFunction.apply(t));
		}

		public Builder addGap(int rowSize) {
			this.writer.currentRowNumber += rowSize;
			return this;
		}

		public ExcelWriter build() {
			return this.writer;
		}

		public void write(OutputStream os) throws IOException {
			this.writer.workbook.write(os);
		}

	}

}
