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
public class WorkbookConfig {
	private final List<ExcelStyle> styles = new ArrayList<>();
	private String fileName = "excel";
	private int chunkSize = 1000;

	public static WorkbookConfig.Builder builder() {
		return new Builder(new WorkbookConfig());
	}

	public static class Builder {
		private WorkbookConfig c;
		public Builder(WorkbookConfig c) {
			this.c = c;
		}

		public Builder fileName(String fileName) {
			c.fileName = fileName;
			return this;
		}

		public Builder styles(ExcelStyle... styles) {
			c.styles.addAll(Arrays.asList(styles));
			return this;
		}

		public Builder chunkSize(int chunkSize) {
			c.chunkSize = chunkSize;
			return this;
		}

		public WorkbookConfig build() {
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
