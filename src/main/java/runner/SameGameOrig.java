package runner;

import astModifier.JSModifyProxyPlugin;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;
import executionTracer.AstInstrumenter;
import executionTracer.JSExecutionTracer;

public class SameGameOrig {
	
	private static final int MAX_STATES = 50;

	private SameGameOrig() {

	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            Arguments.
	 */
	public static void main(String[] args) {



		String outputdir = "same-output2";
//		System.setProperty("webdriver.firefox.bin" ,"/home/shabnam/program-files/firefox/firefox");
		CrawljaxConfiguration config = new CrawljaxConfiguration();

        
		config.setOutputFolder(outputdir);

		CrawlSpecification crawler;

		crawler = new CrawlSpecification("http://localhost:8080/same-game/same-game.htm");
		
//		crawler.addCrawlCondition("Only crawl same-game", new UrlCondition("same-game"));
		
		crawler.click("td").withAttribute("class", "clickable");

		crawler.setClickOnce(true);

		crawler.setMaximumStates(MAX_STATES);
		crawler.setDepth(2);
	

		ProxyConfiguration prox = new ProxyConfiguration();
		WebScarabWrapper web = new WebScarabWrapper();
	
		AstInstrumenter a = new AstInstrumenter();
	
		JSModifyProxyPlugin p = new JSModifyProxyPlugin(a);
		p.excludeDefaults();
		web.addPlugin(p);
		JSExecutionTracer tracer = new JSExecutionTracer("jsExecutionTrace");
		tracer.setOutputFolder(outputdir + "/jsassertions");
		config.addPlugin(tracer);


		config.addPlugin(web);

		config.setProxyConfiguration(prox);

		crawler.setWaitTimeAfterEvent(50);
		config.setCrawlSpecification(crawler);

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
