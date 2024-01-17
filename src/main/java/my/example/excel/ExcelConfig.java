package my.example.excel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

@Getter
@NoArgsConstructor
public class ExcelConfig {
	private final List<ExcelStyle> styles = new ArrayList<>();
	private int chunkSize = 1000;
	private int[] widths = {};

	public static ExcelConfig.Builder builder() {
		return new Builder(new ExcelConfig());
	}

	public static class Builder {
		private ExcelConfig c;
		public Builder(ExcelConfig c) {
			this.c = c;
		}

		public Builder styles(ExcelStyle... styles) {
			c.styles.addAll(Arrays.asList(styles));
			return this;
		}

		public Builder chunkSize(int chunkSize) {
			c.chunkSize = chunkSize;
			return this;
		}

		public Builder widths(int... widths) {
			c.widths = widths;
			return this;
		}

		public ExcelConfig build() {
			return this.c;
		}

	}

	public Map<String, CellStyle> convertStyle(Workbook workbook) {
		Map<String, CellStyle> styleMap = new HashMap<>();
		for (ExcelStyle style : this.styles) {
			styleMap.put(style.getStyleName(), style.convert(workbook));
		}
		return styleMap;
	}

}
