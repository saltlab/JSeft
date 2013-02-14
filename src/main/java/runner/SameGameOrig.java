package runner;

import astModifier.JSModifyProxyPlugin;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;

import domMutation.DomTraceReading;
import executionTracer.AstInstrumenter;
import executionTracer.DOMAstInstrumenter;
import executionTracer.DOMExecutionTracer;
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
//		System.setProperty("webdriver.firefox.bin" ,"/ubc/ece/home/am/grads/shabnamm/program-files/firefox18/firefox/firefox");
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
		JSExecutionTracer tracer = new JSExecutionTracer("executionTrace");
		tracer.setOutputFolder(outputdir);
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
		
/*		
		String outputdir = "same-output2";
		DomTraceReading trace=new DomTraceReading(outputdir);
*/		

	}

}
