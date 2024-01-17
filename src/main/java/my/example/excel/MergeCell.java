package my.example.excel;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"row", "col"})
public class MergeCell {
	private final int row;
	private final int col;
	private final String style;

	public MergeCell(int row, int col, String style) {
		this.row = row;
		this.col = col;
		this.style = style;
	}

}
