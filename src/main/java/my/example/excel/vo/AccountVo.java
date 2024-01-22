package my.example.excel.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "accountId")
public class AccountVo {
	private final Long accountId;
	private final String businessName;
	private Double usage = 0d;
	private Double discountedUsage = 0d;
	private Double supportEnterprise = 0d;
	private Double supportDiscount = 0d;
	private Double wonSum = 0d;

	public AccountVo(Long accountId, String businessName) {
		this.accountId = accountId;
		this.businessName = businessName;
	}

	public void addUsage(double usage) {
		this.usage += usage;
	}

	public void calculateDiscountedUsage(double discountRate) {
		this.discountedUsage = this.usage - (this.usage * discountRate * 0.01);
	}

	public void calculateSupportEnterprise(double supportRate) {
		this.supportEnterprise = this.usage * supportRate * 0.01;
	}

	public void calculateSupportDiscount(double discountRate) {
		this.supportDiscount = this.supportEnterprise - (this.supportEnterprise * discountRate * 0.01);
	}

	public double getDollarSum() {
		return this.discountedUsage + this.supportDiscount;
	}

	public void calculateWonSum(double exchangeRate) {
		this.wonSum = this.getDollarSum() * exchangeRate;
	}

	public double getWonSumWithVat() {
		return this.wonSum * 1.1;
	}
}
