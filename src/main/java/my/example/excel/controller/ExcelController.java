package my.example.excel.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import my.example.excel.service.BillingInvoiceService;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ExcelController {
	private final BillingInvoiceService service;

	@GetMapping
	public void download(DownParam param, HttpServletResponse response) {
		service.getWriter(param.partnerId, param.businessId, param.ym, param.invoiceLevel, param.invoiceItem)
				.download(response);
	}
	@GetMapping("/all")
	public void downloadAll(DownParam param, HttpServletResponse response) {
		service.generateAllCspInvoiceExcel(param.businessId, param.ym, param.invoiceLevel)
				.download(response);
	}

	@Getter
	@Setter
	static class DownParam {
		Long partnerId;
		Long businessId;
		String ym;
		String invoiceLevel;
		String invoiceItem;
	}

}
