package my.example.excel;

import static my.example.excel.ExcelBorder.*;

import java.util.List;

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

	private boolean boldFont;

	private Short backgroundColor;

	private HorizontalAlignment align;

	private List<ExcelBorder> borders;

	private String cellFormat;

	private Short fontSize;

	private String fontName;

	public ExcelStyle(String styleName) {
		this.styleName = styleName;
	}

	public static Builder builder(String styleName) {
		return new Builder(new ExcelStyle(styleName));
	}
	public static Builder builder(ExcelStyle parentStyle, String styleName) {
		ExcelStyle style = new ExcelStyle(styleName);
		style.boldFont = parentStyle.boldFont;
		style.backgroundColor = parentStyle.backgroundColor;
		style.align = parentStyle.align;
		style.borders = parentStyle.borders;
		style.cellFormat = parentStyle.cellFormat;
		style.fontSize = parentStyle.fontSize;
		style.fontName = parentStyle.fontName;
		return new Builder(style);
	}

	public static class Builder {
		private final ExcelStyle s;

		public Builder(ExcelStyle s) {
			this.s = s;
		}

		public Builder boldFont(Boolean boldFont) {
			this.s.boldFont = boldFont;
			return this;
		}

		public Builder backgroundColor(Short backgroundColor) {
			this.s.backgroundColor = backgroundColor;
			return this;
		}

		public Builder align(HorizontalAlignment align) {
			this.s.align = align;
			return this;
		}

		public Builder borders(List<ExcelBorder> borders) {
			this.s.borders = borders;
			return this;
		}

		public Builder cellFormat(String cellFormat) {
			this.s.cellFormat = cellFormat;
			return this;
		}

		public Builder fontSize(Integer fontSize) {
			this.s.fontSize = fontSize.shortValue();
			return this;
		}

		public Builder fontName(String fontName) {
			this.s.fontName = fontName;
			return this;
		}

		public ExcelStyle build() {
			return this.s;
		}
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

		if (fontSize != null) {
			if (font == null) {
				font = workbook.createFont();
			}
			font.setFontHeightInPoints(fontSize);
		}

		if (fontName != null) {
			if (font == null) {
				font = workbook.createFont();
			}
			font.setFontName(fontName);
		}

		style.setFont(font);
		return style;
	}

}
