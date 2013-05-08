package generated;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.plugins.testcasegenerator.TestSuiteHelper;

/*
 * Generated @ Tue May 07 18:03:32 PDT 2013
 */

public class GeneratedTestCases {

    private static final String URL = "http://localhost:8080//same-game/same-game.htm";
	private static TestSuiteHelper testSuiteHelper;
	
	private static CrawljaxConfiguration getTestConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification crawler = new CrawlSpecification(URL);
		crawler.setWaitTimeAfterEvent(2000, TimeUnit.MILLISECONDS);
		crawler.setWaitTimeAfterReloadUrl(1000, TimeUnit.MILLISECONDS);
		config.setCrawlSpecification(crawler);
		config.addPlugin(new com.crawljax.plugins.testcasegenerator.TestSuiteGenerator());
		return config;
	}	
	
	@BeforeClass
	public static void oneTimeSetUp(){
		try {
			//load needed data from xml files
			testSuiteHelper = new TestSuiteHelper(
					getTestConfiguration(),
					"src/test/java/generated/states.xml",
					"src/test/java/generated/eventables.xml", URL,"same-output");
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@AfterClass
	public static void oneTimeTearDown(){
		try {
			testSuiteHelper.tearDown();
		}catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Before
	public void setUp(){
		try {
			testSuiteHelper.goToInitialUrl();		
		}catch (Exception e) {
			fail(e.getMessage());
		}
	}	
	
	/*
	 * Test Cases
	 */
/*	 
	@Test
	public void method_0(){
		testSuiteHelper.newCurrentTestMethod("method_0");
		List<FormInput> formInputs;
		try {
////			assertTrue("DOM oracles satisfied in initial state", testSuiteHelper.compareCurrentDomWithState(0, -1));
			
			
			assertTrue("DOM oracles satisfied in initial state", testSuiteHelper.compareCurrentDomWithState(1, -1));
			testSuiteHelper.runInCrawlingPlugins();
			
			//assertTrue("DOM oracles satisfied in: 1", testSuiteHelper.compareCurrentDomWithState(1, ${event.properties.id}));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
*/
	@Test
	public void method_1_2_3_4_5(){
		testSuiteHelper.newCurrentTestMethod("method_1_2_3_4_5");
		List<FormInput> formInputs;
		try {
			assertTrue("DOM oracles satisfied in initial state", testSuiteHelper.compareCurrentDomWithState(1, -1));
			
			
			//" " TD: class="clickable" data="0.4104470161931893" id="1-3" style="background: #f00;" click xpath /HTML[1]/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]
			assertTrue("Event fired:  ", testSuiteHelper.fireEvent(1));
			testSuiteHelper.runInCrawlingPlugins();
			
			assertTrue("DOM oracles satisfied in: 2", testSuiteHelper.compareCurrentDomWithState(2, 1));
			
			//" " TD: class="clickable" data="0.850490514340408" id="2-1" style="background: #0f0;" click xpath /HTML[1]/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]
			assertTrue("Event fired:  ", testSuiteHelper.fireEvent(2));
			testSuiteHelper.runInCrawlingPlugins();
			
			assertTrue("DOM oracles satisfied in: 3", testSuiteHelper.compareCurrentDomWithState(3, 2));
			
			//" " TD: class="clickable" data="0.9373406961916094" id="1-4" style="background: none repeat scroll 0% 0% rgb(255, 0, 0);" click xpath /HTML[1]/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[2]
			assertTrue("Event fired:  ", testSuiteHelper.fireEvent(3));
			testSuiteHelper.runInCrawlingPlugins();
			
			assertTrue("DOM oracles satisfied in: 4", testSuiteHelper.compareCurrentDomWithState(4, 3));
			
			//" " TD: class="clickable" data="0.28895777701063957" id="0-3" style="background: none repeat scroll 0% 0% rgb(0, 0, 255);" click xpath /HTML[1]/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[1]
			assertTrue("Event fired:  ", testSuiteHelper.fireEvent(4));
			testSuiteHelper.runInCrawlingPlugins();
			
			assertTrue("DOM oracles satisfied in: 5", testSuiteHelper.compareCurrentDomWithState(5, 4));
			
			//" " TD: class="clickable" data="0.11002079652561617" id="1-4" style="background: none repeat scroll 0% 0% rgb(255, 0, 0);" click xpath /HTML[1]/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[2]
			assertTrue("Event fired:  ", testSuiteHelper.fireEvent(5));
			testSuiteHelper.runInCrawlingPlugins();
			
			assertTrue("DOM oracles satisfied in: 6", testSuiteHelper.compareCurrentDomWithState(6, 5));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}


}	 
