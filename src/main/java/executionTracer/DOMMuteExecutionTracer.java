package executionTracer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

public class DOMMuteExecutionTracer implements PreStateCrawlingPlugin, OnNewStatePlugin, PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {
	private static final int ONE_SEC = 1000;
	private String stateName;
	private static String outputFolder;
	private static String assertionFilename;
	
	
	private static final Logger LOGGER = Logger.getLogger(JSExecutionTracer.class.getName());
	
	public static final String EXECUTIONTRACEDIRECTORY = "domMuteExecutiontrace/";
	/**
	* @param filename
	*            How to name the file that will contain the assertions after execution.
	*/
	public DOMMuteExecutionTracer(String filename,String stateName) {
	assertionFilename = filename;
	this.stateName=stateName;
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
			Helper.directoryCheck(getOutputFolder() + EXECUTIONTRACEDIRECTORY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void postCrawling(CrawlSession session) {
		try {
			PrintStream output = new PrintStream(getOutputFolder() + getAssertionFilename());
		
	
			/* close the output file */
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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

	@Override
	public void onNewState(CrawlSession session) {
		try {
			ArrayList<Element> elemList=new ArrayList<Element>();
			StringBuffer result=new StringBuffer();
			String filename = getOutputFolder() + EXECUTIONTRACEDIRECTORY + "domMuteExecutiontrace-";	
			filename += session.getCurrentState().getName();	
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			filename += dateFormat.format(date) + ".txt";
			PrintWriter file = new PrintWriter(filename);
			result.append("state::" + session.getCurrentState().getName()+ "\n" +"===========================================================================\n");
			elemList=getDOMElements(session);
			for(int i=0;i<elemList.size();i++){
				Element elem=elemList.get(i);
				result.append("tagName::" + elem.getTagName() + "\n");
				result.append("xpath::" + XPathHelper.getXPathExpression(elem) + "\n");
				for(int j=0;j<elem.getAttributes().getLength();j++){
					String attrName=elem.getAttributes().item(j).getNodeName();
					String attrValue=elem.getAttributes().item(j).getNodeValue();
					result.append(attrName + "::" + attrValue + "\n");
					
				}
				result.append("===========================================================================\n");
			}
			
			
			file.write(result.toString());
			file.close();
		
		} catch (SAXException | IOException e) {
			
			e.printStackTrace();
		}
		
	}

	@Override
	public void preStateCrawling(CrawlSession session,
			List<CandidateElement> candidateElements) {
		

//		if(session.getCurrentState().getName().compareTo(stateName)>=0){
			try {
				ArrayList<Element> elemList=new ArrayList<Element>();
				StringBuffer result=new StringBuffer();
				String filename = getOutputFolder() + EXECUTIONTRACEDIRECTORY + "domMuteExecutiontrace-";	
				filename += session.getCurrentState().getName();	
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = new Date();
				filename += dateFormat.format(date) + ".txt";
				PrintWriter file = new PrintWriter(filename);
				result.append("state::" + session.getCurrentState().getName()+ "\n" +"===========================================================================\n");
				elemList=getDOMElements(session);
				for(int i=0;i<elemList.size();i++){
					Element elem=elemList.get(i);
					result.append("tagName::" + elem.getTagName() + "\n");
					result.append("xpath::" + XPathHelper.getXPathExpression(elem) + "\n");
					for(int j=0;j<elem.getAttributes().getLength();j++){
						String attrName=elem.getAttributes().item(j).getNodeName();
						String attrValue=elem.getAttributes().item(j).getNodeValue();
						result.append(attrName + "::" + attrValue + "\n");
						
					}
					result.append("===========================================================================\n");
				}
				
				
				file.write(result.toString());
				file.close();
			
			} catch (SAXException | IOException e) {
				
				e.printStackTrace();
			}
//		}
		
	}
	
	private ArrayList<Element> getDOMElements(CrawlSession session) throws SAXException, IOException{
		Document doc = Helper.getDocument(session.getCurrentState().getDom());
		DocumentTraversal traversal = (DocumentTraversal) doc;
		TreeWalker walker = traversal.createTreeWalker(doc.getDocumentElement(),
				NodeFilter.SHOW_ELEMENT, null, true);
		ArrayList<Element> elemList=new ArrayList<Element>();
		traverseLevel(walker, "",elemList);
		return elemList;
	}

	private static final void traverseLevel(TreeWalker walker, String indent, ArrayList<Element> elemList) {
		Node parent = walker.getCurrentNode();
		elemList.add(((Element) parent));
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			traverseLevel(walker, indent + '\t',elemList);
		}
		walker.setCurrentNode(parent);
	}

}
