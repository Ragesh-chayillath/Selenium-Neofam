import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.opentelemetry.api.internal.StringUtils;

public class BookManager {

	public static void main(String[] args) {
		XSSFWorkbook workbook = null;
		WebDriver driver = initializeDriver();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("IST"));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DataManager2 manager = new DataManager2();
		FileOutputStream outFile = null;
		try {
			FileInputStream file = new FileInputStream(new File("C:\\temp\\Book1.xlsx"));
			outFile = new FileOutputStream(new File("C:\\temp\\Book2.xlsx"));
			workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {

				Row row = rowIterator.next();
				Cell tranasactionCell = row.getCell(2);

				if (StringUtils.isNullOrEmpty(tranasactionCell.getStringCellValue())) {

					System.out.println("Processing ======================"+row.getCell(0).getNumericCellValue());
					Date date = row.getCell(1).getDateCellValue();
					String dateString = sdf.format(date).trim();

					LocalDate fromDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

					String fromDateString = fromDate.format(formatter);

					row.getCell(4).setCellType(CellType.STRING);
					String phone = row.getCell(4).getStringCellValue().trim();
					row.getCell(6).setCellType(CellType.STRING);
					String amount = row.getCell(6).getStringCellValue().trim();

					String txnId = manager.getData(phone, dateString, fromDateString, amount, driver);
					if (txnId != null) {
						tranasactionCell.setCellValue(txnId);
					}
				}
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (workbook != null && outFile != null) {
				try {
					workbook.write(outFile);
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static WebDriver initializeDriver() {
		WebDriver driver = new ChromeDriver();
		driver.get("https://app.trapeza.in/login");
		// lOGIN
		WebElement email = driver.findElement(By.name("uname"));
		email.sendKeys("trapeza@gmail.com");
		WebElement password = driver.findElement(By.name("password"));
		password.sendKeys("trapeza123");
		WebElement loginBtn = driver.findElement(By.name("usersubmit"));
		loginBtn.submit();
		return driver;
	}
}
