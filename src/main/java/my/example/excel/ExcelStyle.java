package my.example.excel;

import static my.example.excel.ExcelBorder.*;

import java.util.List;

import org.springframework.util.Assert;

import lombok.Builder;
import lombok.Getter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

@Getter
public class ExcelStyle {
	private final String styleName;

	private final boolean boldFont;

	private final Short backgroundColor;

	private final HorizontalAlignment align;

	private final List<ExcelBorder> borders;

	private final String cellFormat;

	@Builder
	public ExcelStyle(String styleName, boolean boldFont, Short backgroundColor, HorizontalAlignment align, List<ExcelBorder> borders,
	                  String cellFormat) {
		Assert.notNull(styleName, "스타일 명을 지정 해 줘야 합니다.");
		this.styleName = styleName;
		this.boldFont = boldFont;
		this.backgroundColor = backgroundColor;
		this.align = align;
		this.borders = borders;
		this.cellFormat = cellFormat;
	}

	public CellStyle convert(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		Font font = null;

		if (boldFont) {
			font = workbook.createFont();
			font.setBold(true);
		}

		if (backgroundColor != null) {
			style.setFillForegroundColor(backgroundColor);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}

		if (align != null) {
			style.setAlignment(align);
		}

		if (borders != null) {
			for (ExcelBorder border : borders) {
				if (TOP.equals(border) || ALL.equals(border)) {
					style.setBorderTop(BorderStyle.THIN);
				}
				if (BOTTOM.equals(border) || ALL.equals(border)) {
					style.setBorderBottom(BorderStyle.THIN);
				}
				if (LEFT.equals(border) || ALL.equals(border)) {
					style.setBorderLeft(BorderStyle.THIN);
				}
				if (RIGHT.equals(border) || ALL.equals(border)) {
					style.setBorderRight(BorderStyle.THIN);
				}
			}
		}

		if (cellFormat != null) {
			DataFormat dataFormat = workbook.createDataFormat();
			style.setDataFormat(dataFormat.getFormat(cellFormat));
		}

		style.setFont(font);
		return style;
	}

}
