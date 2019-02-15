package Test;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Trial {
	
	

	public static void main(String[] args) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document d = db.newDocument();
			d.appendChild(d.createElement("a"));
		
			Element B = d.createElement("b");
			d.getDocumentElement().appendChild(B);
			
			B.appendChild(Trial.tryMethod());
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(d);
			StreamResult result = new StreamResult(new File("results.xml"));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException e) { e.printStackTrace(); }
		catch (TransformerException e) { e.printStackTrace(); }
	}

	private static Element tryMethod() {
		Document d = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			d = db.newDocument();
			d.appendChild(d.createElement("c"));
			
			Element D = d.createElement("d");
			d.getDocumentElement().appendChild(D);
			
			
		}
		catch (ParserConfigurationException e) { e.printStackTrace(); }
		return d.getDocumentElement();
	}
}
