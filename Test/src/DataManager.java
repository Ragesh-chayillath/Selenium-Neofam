import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.opentelemetry.api.internal.StringUtils;

public class DataManager {

	public String getData(String phone, String date, String fromDateString, String amount, WebDriver driver) {

		Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(05l));

		driver.get("https://app.trapeza.in/customers/search");

		WebElement mobile = driver.findElement(By.name("cusmobile"));
		mobile.sendKeys((phone));

		WebElement searchBtn = driver
				.findElement(By.xpath("/html/body/div/div[2]/div[2]/div/div/div[2]/div/div/div[2]/form/button"));
		searchBtn.submit();

		waitOneSec();
		if (findElemnt(driver, "\"/html/body/div/div[2]/div[2]/div/div/div[3]/div/div/div[2]/div/h5\"")) {
			return null;
		}

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("file-datatable")));

		WebElement table = driver.findElement(By.id("file-datatable"));

		List<WebElement> logs = table.findElements(By.tagName("a")).stream().filter(i -> {
			return i.getText().equalsIgnoreCase("Logs");
		}).collect(Collectors.toList());

		if (logs.size() == 1) {
			WebElement log = logs.get(0);
			String logsUrl = log.getAttribute("href");
			driver.get(logsUrl);
			waitOneSec();
			WebElement fromDate = driver.findElement(By.xpath("//*[@id=\"fromdate\"]"));
			fromDate.sendKeys(fromDateString);

			WebElement toDate = driver.findElement(By.xpath("//*[@id=\"todate\"]"));
			toDate.sendKeys(fromDateString);

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("select2-type-container")));

			WebElement type = driver.findElement(By.id("select2-type-container"));
			type.click();

			WebElement lis = driver.findElements(By.tagName("li")).stream().filter(i -> {
				return i.getText().equals("Neokred");
			}).findFirst().get();
			lis.click();

			WebElement searchBtn2 = driver
					.findElement(By.xpath("/html/body/div/div[2]/div[2]/div/div/div[2]/div/div/div[2]/form/button"));
			searchBtn2.submit();
//			wait3Sec();

			if (findElemnt(driver, "/html/body/div/div[2]/div[2]/div/div/div[3]/div/div/div[2]/div/h5")) {
				return null;
			}

			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//*[@id=\"file-datatable_filter\"]/label/input")));

			WebElement sort = driver.findElement(By.xpath("//*[@id=\"file-datatable_length\"]/label/span"));
			sort.click();
			WebElement sort100 = driver.findElements(By.className("select2-results__option")).stream().filter(s -> {
				return s.getText().equals("100");
			}).findFirst().get();
			sort100.click();

			WebElement searchBtn3 = driver.findElement(By.xpath("//*[@id=\"file-datatable_filter\"]/label/input"));
			searchBtn3.sendKeys(date);
			waitOneHalfSec();
			boolean take = true;
			if (hasNoRecords(driver, wait)) {
				searchBtn3.clear();
				searchBtn3.sendKeys(date.substring(0, date.length() - 1));
				if (hasNoRecords(driver, wait)) {
					searchBtn3.clear();
					searchBtn3.sendKeys(date.substring(0, date.length() - 3));
					if (hasNoRecords(driver, wait)) {
						searchBtn3.clear();
						searchBtn3.sendKeys(date.substring(0, date.length() - 4));
						if (hasNoRecords(driver, wait)) {
							searchBtn3.clear();
							searchBtn3.sendKeys(date.substring(0, date.length() - 6));
							if (hasNoRecords(driver, wait)) {
								searchBtn3.clear();
								searchBtn3.sendKeys(date.substring(0, date.length() - 7));
								if (hasNoRecords(driver, wait)) {
									searchBtn3.clear();
									searchBtn3.sendKeys(date.substring(0, date.length() - 9));
									if (hasNoRecords(driver, wait)) {
										take = false;
									}
								}
							}
						}
					}
				}
			}
			if (take) {

				WebElement dataTable = driver.findElement(By.id("file-datatable")).findElement(By.tagName("tbody"));

				List<WebElement> rows = dataTable.findElements(By.tagName("tr"));

				for (WebElement row : rows) {
					List<WebElement> slides = row.findElements(By.className("side-menu"));

					WebElement firstSlide = slides.get(0);
					WebElement secondSlide = slides.get(1);

					JavascriptExecutor js = (JavascriptExecutor) driver;

					for (WebElement slide : slides) {
						WebElement element = slide.findElement(By.tagName("ul"));
						js.executeScript("arguments[0].setAttribute('style', 'display:contents')", element);
					}

					WebElement post = firstSlide.findElement(By.tagName("label"));
					WebElement response = secondSlide.findElement(By.tagName("label"));

					if (!StringUtils.isNullOrEmpty(response.getText()) && !StringUtils.isNullOrEmpty(post.getText())) {

						JSONObject objResponse = new JSONObject(response.getText());
						JSONObject objPost = new JSONObject(post.getText());
						String amt = objPost.has("amount") ? String.valueOf( objPost.get("amount")): null;
						if (!StringUtils.isNullOrEmpty(amt) && amt.equals(amount)) {
							if (objResponse.has("details")) {
								JSONObject details = null;
								try {
									details = objResponse.getJSONObject("details");
									if (details.has("txnRefNo")) {
										String txn = details.getString("txnRefNo");
										return txn;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private boolean findElemnt(WebDriver driver, String xpath) {
		try {
			driver.findElement(By.xpath(xpath));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private Boolean hasNoRecords(WebDriver driver, Wait<WebDriver> wait) {
		try {
			waitOneHalfSec();
			driver.findElement(By.xpath("//*[@id=\"file-datatable\"]/tbody/tr/td[2]/ul[1]/li/a"));
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	private void waitOneSec() {
		try {
			Thread.sleep(1000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void waitOneHalfSec() {
		try {
			Thread.sleep(500l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void wait3Sec() {
		try {
			Thread.sleep(3000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
