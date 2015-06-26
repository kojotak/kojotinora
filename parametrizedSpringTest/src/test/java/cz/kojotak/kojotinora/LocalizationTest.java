package cz.kojotak.kojotinora;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

@RunWith(Parameterized.class)
@ContextConfiguration(locations = {"/spring-context.xml"})
public class LocalizationTest {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private Locale locale;
	
	private final TestContextManager testContextManager = new TestContextManager(getClass());
	private final String klic;
	
	public LocalizationTest(String klic){
		this.klic = klic;
	}
	
	@Before
	public void before() throws Exception{
		//toto nastavi autowired zavislosti
		testContextManager.prepareTestInstance(this);
	}
		
	@Parameters(name = "{index}:{0}")
	public static Collection<Object[]> vygenerujSeznamLokalizacnichKlicu() {
		Collection<Object[]> data = new ArrayList<Object[]>();
		List<? extends Class<? extends Enum<?>>> enumy = asList(Konstanty.class);
		for(Class<? extends Enum<?>> clz : enumy){
			for(Enum<?> polozka : clz.getEnumConstants()){
				data.add(new Object[]{ polozka.getClass().getName() + "." + polozka.name() });
			}
		}
		return data;
	}
	
	@Test
	public void testLokalizace() {
		try{
			messageSource.getMessage(klic, null, locale);
		}catch(NoSuchMessageException e){
			Assert.fail("Neexistuje lokalizace pro "+klic);
		}
	}
	
}
