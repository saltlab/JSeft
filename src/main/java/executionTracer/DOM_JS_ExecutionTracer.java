package executionTracer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnFireEventSuccessPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.util.Helper;

public class DOM_JS_ExecutionTracer 
	implements OnFireEventSuccessPlugin, /*OnNewStatePlugin,*/PreStateCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {

		private static final int ONE_SEC = 1000;
		
		private static String outputFolder;
		private static String assertionFilename;
		
		private static JSONArray points = new JSONArray();
		
		private static final Logger LOGGER = LoggerFactory.getLogger(JSExecutionTracer.class.getName());
		
		public static String EXECUTIONTRACEDIRECTORY="jsDomExecutiontrace/";
		public static String MUTATEDEXECUTIONTRACEDIRECTORY="jsMutatedExecutiontrace/";
		private boolean execTraceForMutatedVer=false;
		
		/**
		* @param filename
		*            How to name the file that will contain the assertions after execution.
		*/
		public DOM_JS_ExecutionTracer(String filename, boolean execTraceForMutatedVer) {
		assertionFilename = filename;
		this.execTraceForMutatedVer=execTraceForMutatedVer;
		
		}
		
		/**
		* Initialize the plugin and create folders if needed.
		* 
		* @param browser
		*            The browser.
		*/
		@Override
		public void preCrawling(EmbeddedBrowser browser) {
			try {
				Helper.directoryCheck(getOutputFolder());
				if(execTraceForMutatedVer)
					Helper.directoryCheck(getOutputFolder() + MUTATEDEXECUTIONTRACEDIRECTORY);
				else
					Helper.directoryCheck(getOutputFolder() + EXECUTIONTRACEDIRECTORY);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		* Retrieves the JavaScript instrumentation array from the webbrowser and writes its contents into a file.
		* 
		* @param session
		*            The crawling session.
		* @param candidateElements
		*            The candidate clickable elements.
		*/
		
		@Override
		public void onFireEventSuccessed(Eventable eventable, List<Eventable> pathToSuccess, CrawlSession session, StateMachine stateMachine) {
		
			String filename;
			if(execTraceForMutatedVer)
				filename = getOutputFolder() + MUTATEDEXECUTIONTRACEDIRECTORY + assertionFilename+ "-";
			else
				filename = getOutputFolder() + EXECUTIONTRACEDIRECTORY + assertionFilename+ "-";
			
			filename += session.getCurrentState().getName();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			filename += dateFormat.format(date) + ".txt";
			
			try {
			
				LOGGER.info("Reading execution trace");
			
				LOGGER.info("Parsing JavaScript execution trace");
			
				
		//		session.getBrowser().executeJavaScript("sendReally();");
		/*		session.getBrowser().executeJavaScript(
						 "if (window.jscoverage_report) {jscoverage_report();}");
		*/		Thread.sleep(ONE_SEC);
				DOM_JS_Trace trace=new DOM_JS_Trace();
				String result = trace.getTraceRecord(points);
			
				PrintWriter file = new PrintWriter(filename);
			
				file.write(result);
				file.close();
				
				LOGGER.info("Saved execution trace as " + filename);
			
				points = new JSONArray();
			} catch (CrawljaxException we) {
				we.printStackTrace();
				LOGGER.error("Unable to get instrumentation log from the browser");
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		
		
	/*
		@Override
		public void onNewState(CrawlSession session) {
		
			
			String filename;
			if(execTraceForMutatedVer)
				filename=getOutputFolder() + MUTATEDEXECUTIONTRACEDIRECTORY + assertionFilename+ "-";
			else
				filename=getOutputFolder() + EXECUTIONTRACEDIRECTORY + assertionFilename+ "-";
			filename += session.getCurrentState().getName();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			filename += dateFormat.format(date) + ".txt";
			
			try {
			
				LOGGER.info("Reading execution trace");
			
				LOGGER.info("Parsing JavaScript execution trace");
			
				
				session.getBrowser().executeJavaScript("sendReally();");
				Thread.sleep(ONE_SEC);
				DOM_JS_Trace trace=new DOM_JS_Trace();
				String result = trace.getTraceRecord(points);
			//	if (!trace.getData(points).equals("")) {
				PrintWriter file = new PrintWriter(filename);
				file.write(result);
				file.close();
				
				LOGGER.info("Saved execution trace as " + filename);
			
				points = new JSONArray();
			} catch (CrawljaxException we) {
				we.printStackTrace();
				LOGGER.error("Unable to get instrumentation log from the browser");
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	*/
		
		
		
		
		
		
		/**
		* Get a list with all trace files in the executiontracedirectory.
		* 
		* @return The list.
		*/
		public List<String> allTraceFiles() {
			ArrayList<String> result = new ArrayList<String>();
			
			
			File dir;
			if(execTraceForMutatedVer)
				/* find all trace files in the trace directory */
				dir = new File(getOutputFolder() + MUTATEDEXECUTIONTRACEDIRECTORY);
			else
				/* find all trace files in the trace directory */
				dir = new File(getOutputFolder() + EXECUTIONTRACEDIRECTORY);
				
			String[] files = dir.list();
			if (files == null) {
				return result;
			}
			for (String file : files) {
				if (file.endsWith(".txt")) {
					if(execTraceForMutatedVer)
						
						result.add(getOutputFolder() + MUTATEDEXECUTIONTRACEDIRECTORY + file);
					else
						result.add(getOutputFolder() + EXECUTIONTRACEDIRECTORY + file);
				}
			}
			
			return result;
		}
	/*	
		@Override
		public void postCrawling(CrawlSession session) {
			try {
				PrintStream output = new PrintStream(getOutputFolder() + getAssertionFilename());
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	*/	
		/**
		* @return Name of the assertion file.
		*/
		public String getAssertionFilename() {
			return assertionFilename;
		}
		
		@Override
		public String getOutputFolder() {
			return Helper.addFolderSlashIfNeeded(outputFolder);
		}
		
		@Override
		public void setOutputFolder(String absolutePath) {
			outputFolder = absolutePath;
		}
		

		public static void addPoint(String string) {
			JSONArray buffer = null;
			try {
				buffer = new JSONArray(string);
				for (int i = 0; i < buffer.length(); i++) {
					points.put(buffer.get(i));
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
		}

		@Override
		public void preStateCrawling(CrawlSession session,
				List<CandidateElement> arg1) {
			session.getBrowser().executeJavaScript(
					 "if (window.jscoverage_report) {jscoverage_report();}");
			// TODO Auto-generated method stub
			
		}

/*		@Override
		public void preStateCrawling(CrawlSession session,
				List<CandidateElement> arg1) {
			String filename;
			if(execTraceForMutatedVer)
				filename=getOutputFolder() + MUTATEDEXECUTIONTRACEDIRECTORY + assertionFilename+ "-";
			else
				filename=getOutputFolder() + EXECUTIONTRACEDIRECTORY + assertionFilename+ "-";
			filename += session.getCurrentState().getName();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			filename += dateFormat.format(date) + ".txt";
			
			try {
			
				LOGGER.info("Reading execution trace");
			
				LOGGER.info("Parsing JavaScript execution trace");
			
				
				session.getBrowser().executeJavaScript("sendReally();");
				Thread.sleep(ONE_SEC);
				DOM_JS_Trace trace=new DOM_JS_Trace();
				String result = trace.getTraceRecord(points);
			//	if (!trace.getData(points).equals("")) {
				PrintWriter file = new PrintWriter(filename);
				file.write(result);
				file.close();
				
				LOGGER.info("Saved execution trace as " + filename);
			
				points = new JSONArray();
			} catch (CrawljaxException we) {
				we.printStackTrace();
				LOGGER.error("Unable to get instrumentation log from the browser");
				return;
			} catch (Exception e) {
				e.printStackTrace();
		
			
			}
		}
*/

}
