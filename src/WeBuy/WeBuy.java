package WeBuy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WeBuy {

	@Test
	public void testProductPurchseProcess(){
		
	//Reading product names and product quantities from xls file	
		Xls_Reader xls= new Xls_Reader("C:\\workspace fauzia\\webuy.xlsx");
	int rows= xls.getRowCount("product");
	          System.out.println( +rows);
	          
	//storing product names and quantites from xls in array list and hashtable
	List <String>productnamestobeadded=new ArrayList<String>();
	Hashtable<String, String>productquantitytobeadded=new Hashtable<String, String>();
	for(int i=2;i<=rows;i++){
		
		String productnames=xls.getCellData("product", "Product name", i);
		String productquantity=xls.getCellData("product", "Quantity", i);
		
		productnamestobeadded.add(productnames);
		productquantitytobeadded.put(productnames, productquantity);
		System.out.println( "all excel products"  +productnamestobeadded);
	}
	
	
	
	
	
	WebDriver driver= new FirefoxDriver();
	driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	driver.manage().window().maximize();
	driver.get("https://uk.webuy.com");
	driver.findElement(By.xpath("//*[@id='headerSection']/div/div[2]/div[2]/a/div")).click();
	
	driver.findElement(By.xpath("//*[@id='tabgaming']")).click();
	driver.findElement(By.xpath("html/body/div[5]/div[2]/div[2]/div[2]/ul/li[4]/a[2]")).click();
	driver.findElement(By.xpath("html/body/div[5]/div[2]/div[2]/div[2]/ul/li[4]/ul/li/a")).click();
	
	List<WebElement>allnames= driver.findElements(By.xpath("//div[@class='searchRecord']/div[2]/h1/a"));
	//List<WebElement>allbuttons= driver.findElements(By.xpath("//div[@class='action']/div[1]/a[2]"));
	List<WebElement>allbuttons= driver.findElements(By.linkText("I want to buy this item"));
	System.out.println("total names..."  +allnames.size());
	System.out.println("total buttons..."  +allbuttons.size());
	int prodcount=0;
	for(int i=0;i<allnames.size();i++){
		//String names=allnames.get(i).getText();
		System.out.println("found all product names list...."  +allnames.get(i).getText());
		
		if (productnamestobeadded.contains(allnames.get(i).getText())){
			System.out.println("found all...." +allnames.get(i).getText());
			allbuttons.get(i).click();
//count products in the basket
			String count=driver.findElement(By.id("buyBasketCount")).getText();
			  
			try{
				Assert.assertEquals(count, String.valueOf( prodcount+1));
			} 
			catch (Throwable t){
				System.out.println("Actual basket count not equal to expected basket count.");
			}
//repeat these steps as elements get stale in memory
			allnames= driver.findElements(By.xpath("//div[@class='searchRecord']/div[2]/h1/a"));
			allbuttons= driver.findElements(By.xpath("//div[@class='action']/div/a[2]"));
		prodcount++;
	}
	}
		//got to view basket page and change product's quantities from xls file in "view basket" page
		//driver.findElement(By.linkText("View basket")).click();
		driver.findElement(By.xpath("//div[@class='buyBasketContent']/table/tbody/tr/td[2]/a[1]")).click();
		List<WebElement> select=driver.findElements(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr/td[1]/div/select"));
		System.out.println("total select boxes...." +select.size());
	//viewbasketcols is the products names in the basket with some extra text attached that we need to remove and it also include two extra lines
		List<WebElement> viewbasketcols=driver.findElements(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr/td[2]"));
		
		for(int y=0; y<viewbasketcols.size()-2;y++){
			//System.out.println("found in the basket...." +viewbasketcols.get(y).getText());
//get product names(key) as displayed in the xls
			
			String key=viewbasketcols.get(y).getText().split("\\n")[0];
			System.out.println(key);
			System.out.println("KEYS ARE....."+key+ "......."+productquantitytobeadded.get(key));
			WebElement dropdown=select.get(y);
			Select s=new Select(dropdown); //to select value from the lists
			s.selectByVisibleText(productquantitytobeadded.get(key));
			
			
			
		 viewbasketcols=driver.findElements(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr/td[2]"));
		 select=driver.findElements(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr/td[1]/div/select"));
			
			
		}

//to get total items price in the basket
		double sum=0.0;
		
		List<WebElement>pricecol=driver.findElements(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr/td[4]"));
		for(int i=0; i<pricecol.size();i++){
			String price=pricecol.get(i).getText().split("\\£")[1];
			System.out.println(price);
			sum=sum+Double.parseDouble(price);
			
			
		}
		//TOTAL ROWS FOR THE PRICE ALSO INCLUDE DELIVERY AND TOTAL PRICE . BUT ROW NUMBER KEEPS CHANGING DEPENDING UPON THE ITEMS ADDED
		//SO DELIVERY CHARGES WON'T BE ON THE SAME ROW ALWAYS
		//SO GET TOTAL ROWS SIZE AND DELIVERY CHARGE WILL ALWAYS BE SECOND LAST ROW
		int totalrows= driver.findElements(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr")).size();
		String delcharge=driver.findElement(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr["+(totalrows-1)+"]/td[2]")).getText();            
		String expectedval=driver.findElement(By.xpath("//div [@class='basketPageBox']/form/table/tbody/tr["+(totalrows)+"]/td[2]")).getText();
		System.out.println("total delivery...." +delcharge);
		System.out.println("total charges shown...." +expectedval);
		double deliverycharge=Double.parseDouble(delcharge.split("\\£")[1]);
		double actualtotal=deliverycharge+sum;
		double expectedtotal=Double.parseDouble(expectedval.split("\\£")[1]);
		
		
		System.out.println("expected total ...." +expectedtotal);
		System.out.println("actual total...." +actualtotal);
		Assert.assertEquals(actualtotal, expectedtotal);
	}}


	
	
	
	
			
	//driver.get("https://uk.webuy.com/search/index.php?stext=*&section=&catid=956");
	/*Set <String> windid=driver.getWindowHandles();
	System.out.println(+windid.size());
	Iterator <String> itr=windid.iterator();
	String mainwind=itr.next();
	String popup=itr.next();
	driver.switchTo().window(popup);
	driver.close();
	driver.switchTo().window(mainwind);*/
	
	
	


	

