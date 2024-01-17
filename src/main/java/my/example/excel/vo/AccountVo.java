package my.example.excel.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "accountId")
public class AccountVo {
	private Long accountId;
	private String businessName;

	public AccountVo(Long accountId, String businessName) {
		this.accountId = accountId;
		this.businessName = businessName;
	}
}
