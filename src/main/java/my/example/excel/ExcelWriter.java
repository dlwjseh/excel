package my.example.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import lombok.Getter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelWriter {
	private final SXSSFWorkbook workbook;
	@Getter
	private final WorkbookConfig config;
	@Getter
	private final Map<String, CellStyle> styles;

	public ExcelWriter(WorkbookConfig config) {
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		workbook.setCompressTempFiles(true);
		this.workbook = workbook;
		this.config = config;
		this.styles = config.convertStyle(workbook);
	}

	public SXSSFSheet createSheet(String sheetName) {
		return this.workbook.createSheet(sheetName.replaceAll("\\[", "(")
				.replaceAll("]", ")"));
	}

	public void write(OutputStream os) throws IOException {
		this.workbook.write(os);
	}

	public void download(HttpServletResponse response) {
		String fileName = new String(this.config.getFileName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

		try (ServletOutputStream outputStream = response.getOutputStream()) {
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			write(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addSheet(Consumer<ExcelSheetWriter> consumer) {
		this.addSheet("sheet", consumer);
	}

	public void addSheet(String sheetName, Consumer<ExcelSheetWriter> consumer) {
		ExcelSheetWriter sheetWriter = new ExcelSheetWriter(this, sheetName);
		consumer.accept(sheetWriter);
		sheetWriter.clear();
	}

	public void addSheet(String sheetName, List<List<ExcelCell>> rows) {
		ExcelSheetWriter sheetWriter = new ExcelSheetWriter(this, sheetName);
		sheetWriter.addRows(rows);
		sheetWriter.clear();
	}

	public void addSheet(String sheetName, List<List<ExcelCell>> rows, List<Integer> widths) {
		ExcelSheetWriter sheetWriter = new ExcelSheetWriter(this, sheetName);
		sheetWriter.setWidths(widths);
		sheetWriter.addRows(rows);
		sheetWriter.clear();
	}

}
