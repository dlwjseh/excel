package my.example.excel.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import my.example.excel.ExcelWriter;
import my.example.excel.service.BillingInvoiceService;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ExcelController {
	private final BillingInvoiceService service;

	@GetMapping
	public void download(HttpServletResponse response) {
		ExcelWriter writer = service.getWriter(1L, 5L, "202401", "FIRST",
				"BIZTOTAL");
		writer.download(response);
	}

}
