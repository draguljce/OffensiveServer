package offensive.Server.Utilities;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import offensive.Server.Server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Environment {
	private HashMap<String, String> environmentVariables = new HashMap<>();
	
	public Environment(String[] args) {
		this.getCommandLineArgs(args);
		
		if(this.getVariable(Constants.ConfigFilePathVarName) != null)
		{
			this.loadVariablesFromFile(this.getVariable(Constants.ConfigFilePathVarName));
		}
		
		this.addDefaultValues();
	}
	
	public String getVariable(String name) {
		return this.environmentVariables.get(name);
	}
	
	public boolean containsVariable(String name) {
		return this.environmentVariables.containsKey(name);
	}
	
	private void getCommandLineArgs(String[] args) {
		if(args.length % 2 != 0) {
			this.printInstructions();
			System.exit(1);
		}
		
		for(int i = 0; i < args.length; i += 2) {
			String arg = args[i];
			
			if(arg.startsWith("-")) {
				this.environmentVariables.put(args[i].substring(1), args[i+1]);
			}
			else {
				this.printInstructions();
				System.exit(1);
			}
		}
	}
	
	private void loadVariablesFromFile(String filePath) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			Document document = documentBuilder.parse(filePath);
			
			Element rootElement = document.getDocumentElement();
			
			NodeList childNodes = rootElement.getElementsByTagName("*");
			
			for(int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				
				this.environmentVariables.put(node.getNodeName(), node.getFirstChild().getNodeValue());
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			Server.logger.error(e.getMessage(), e);
			System.exit(2);
		}
	}
	
	private void addDefaultValues() {
		if(!this.containsVariable(Constants.HandlerThreadNumVarName)) {
			this.environmentVariables.put(Constants.HandlerThreadNumVarName, Constants.HandlerThreadNumDefaultVal);
		}
		
		if(!this.containsVariable(Constants.BattleThreadNumVarName)) {
			this.environmentVariables.put(Constants.BattleThreadNumVarName, Constants.BattleThreadNumDefaultVal);
		}
		
		if(!this.containsVariable(Constants.PortNumberVarName)) {
			this.environmentVariables.put(Constants.PortNumberVarName, Constants.PortNumberDefaultVal);
		}
		
		if(!this.containsVariable(Constants.ServerSocketTimeoutVarName)) {
			this.environmentVariables.put(Constants.ServerSocketTimeoutVarName, Constants.ServerSocketTimeoutDefaultVal);
		}
	}
	
	private void printInstructions() {
		System.out.println("osrv arguments:");
		System.out.println();
		System.out.println(Constants.LogPathVarName + " - path to server log file. DEFAULT = " + Constants.DefaultLogPath);
		System.out.println();
	}
}
