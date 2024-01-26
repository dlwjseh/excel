package my.example.excel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootApplication
public class ExcelApplication {
	public static void main(String[] args) {
		SpringApplication.run(ExcelApplication.class, args);
	}
}
