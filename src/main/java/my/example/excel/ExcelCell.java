package my.example.excel;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExcelCell {

	// 값
	private Object value;

	// 스타일
	private String style;

	// Merge할 행 수
	private int mergeRow = 1;

	// Merge할 열 수
	private int mergeColumn = 1;

	/**
	 * @param value 값
	 */
	public ExcelCell(Object value) {
		this.value = value != null ? value : "";
	}
	/**
	 * @param value 값
	 * @param mergeRow merge할 row
	 * @param mergeColumn merge할 column
	 */
	public ExcelCell(Object value, int mergeRow, int mergeColumn) {
		this.value = value != null ? value : "";
		this.mergeRow = mergeRow;
		this.mergeColumn = mergeColumn;
	}
	/**
	 * @param value 값
	 * @param style 셀 스타일
	 */
	public ExcelCell(Object value, String style) {
		this.value = value != null ? value : "";
		this.style = style;
	}
	/**
	 * @param value 값
	 * @param mergeRow merge할 row
	 * @param mergeColumn merge할 column
	 * @param style 셀 스타일
	 */
	@Builder
	public ExcelCell(Object value, int mergeRow, int mergeColumn, String style) {
		this.value = value != null ? value : "";
		this.style = style;
		this.mergeRow = mergeRow;
		this.mergeColumn = mergeColumn;
	}

	/**
	 * 머지하려는 셀이 있는지 여부
	 */
	public boolean isMergeCell() {
		return this.mergeColumn>1 || this.mergeRow>1;
	}

	/**
	 * 빈 cell
	 */
	public static ExcelCell empty(String style) {
		return new ExcelCell("", style);
	}
}
