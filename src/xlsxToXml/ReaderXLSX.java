package xlsxToXml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.AntibioticFamilies;
import util.SheetSearch;

public class ReaderXLSX {
	
	private static Properties prop;
	private static Scanner s;
	private static Document d;
	
	static {
		prop = new Properties();
		String propFileName = "config.properties";
		try (
			InputStream inputStream = new FileInputStream(new File (propFileName));
		){
			prop.load(inputStream);
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		s = new Scanner(System.in);
	}

	public static void main(String[] args) {
		
		String excelsDirectory = prop.getProperty("excelsDirectory");
		String standardsDirectory = prop.getProperty("standardsDirectory");
		File dExcels = new File(excelsDirectory);
		File dStandards = new File(standardsDirectory);
		
		if(!dExcels.exists() || !dStandards.exists()) { 
			dStandards.mkdir();
			System.err.println("WARNING - At least one directory didn't exist.");
		}
		if(!dExcels.isDirectory() || !dStandards.isDirectory()) {
			System.err.println("ERROR - At least one of the targets isn't a directory.");
			return;
		}
		if(dExcels.listFiles().length == 0) {
			System.out.println(excelsDirectory + " is EMPTY!");
			System.out.println("\tLoad some standards (in excel form) there and try again.");
			return;
		}
		
		boolean valid;
		do {
		
			System.out.println("Choose a Standard:");
			System.out.println("(-1 to EXIT)\n");
			
			int numFiles = 0, option;
			for(File standard: dExcels.listFiles()) {
				System.out.println(numFiles + " -> " + standard.getName());
				numFiles++;
			}
			
			System.out.print("\nOption: ");
			option = s.nextInt();
			System.out.println();
			
			valid = option == -1 || (option >= 0 && option < numFiles);
			if(valid && option != -1) {
				
				initializeDOM();
				
				String fileName = dExcels.listFiles()[option].getName();
				readWorbook(
						dExcels+"/"+fileName
				);
				
				writeXML(standardsDirectory);
			}
		} while(!valid);
	}

	private static void initializeDOM() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			d = db.newDocument();
			d.appendChild(d.createElement("standard"));
		} 
		catch (ParserConfigurationException e) { e.printStackTrace(); }
	}

	private static void readWorbook(String pathIn) {
		Workbook wb;
		try {
			wb = WorkbookFactory.create(new File(pathIn));
			
			getMetadata(wb);
			
			Element brkpts = d.createElement("breakpoints");
			d.getDocumentElement().appendChild(brkpts);
			
			boolean foundFirst = false;
			for(Sheet st: wb) {
				if(st.getSheetName().equals("Changes")) {
					foundFirst = true;
					continue;
				}
				if(st.getSheetName().equals("Topical agents")) break;
				if(foundFirst) {
					readSheet(st, brkpts);
				}
			}
		} 
		catch (EncryptedDocumentException e) { e.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
	}

	private static void getMetadata(Workbook wb) {
		
		Element meta = d.createElement("metadata");
		d.getDocumentElement().appendChild(meta);
		
		// We pick some data from the Content sheet
		Sheet contSheet = wb.getSheet("Content");
		
		CellReference crDescription = new CellReference("A3");
		
		String desc = SheetSearch.getStringFrom(crDescription, contSheet);
		String vers = desc.substring(desc.indexOf("Version ") + 8, desc.indexOf(","));
		String val = desc.substring(desc.indexOf("valid from ") + 11);
		
		Element name = elementWithTxt("name", prop.getProperty("comitee"));
		meta.appendChild(name);
		
		Element version = elementWithTxt("version", vers);
		meta.appendChild(version);
		
		Element valid = elementWithTxt("validFrom", val);
		meta.appendChild(valid);

		// We check the symbols used in the Guidance sheet
		Sheet guidSheet = wb.getSheet("Guidance");
		
		String zone = prop.getProperty("stringZone");
		CellReference crSusceptible = SheetSearch.lookFor(zone, guidSheet);
		crSusceptible = new CellReference(crSusceptible.getRow()+1,crSusceptible.getCol());
		
		String suscep = SheetSearch.getStringFrom(crSusceptible, guidSheet);
		
		boolean G = suscep.contains(">");
		Element susceptible = G?
							elementWithTxt("susceptibleGTorG", "G") : 
							elementWithTxt("susceptibleGTorG", "GT");
		meta.appendChild(susceptible);
		
		Element resistent = G?
							elementWithTxt("resistentLTorL", "LT") :
							elementWithTxt("resistentLTorL", "L");
		meta.appendChild(resistent);
	}
	
	private static void readSheet(Sheet st, Element brkpts) {
		
		// Family BreakPoint
		Element fbrkpt = d.createElement("familyBreakPoint");
		brkpts.appendChild(fbrkpt);
		
		// Bacteria Family Name
		String bactFamName = getBacteriaFamilyName(st);
		Element bactFam = elementWithTxt("bacteriaFamily", bactFamName);
		fbrkpt.appendChild(bactFam);
		
		CellReference zoneCR = SheetSearch.lookFor(prop.getProperty("stringZone"), st);
		
		for(String antibioFam: AntibioticFamilies.families) {
			
			CellReference famCR = SheetSearch.lookFor(antibioFam, st);
			
			if(famCR == null) continue;
			
			CellReference actualCR = new CellReference(famCR.getRow()+2,famCR.getCol());
			CellReference nextCR = new CellReference(famCR.getRow()+3,famCR.getCol());
			CellReference diskCR, suscepCR, resistCR;
			
			String actualAntibio = SheetSearch.getStringFrom(actualCR, st);
			String nextAntibio = SheetSearch.getStringFrom(nextCR, st);
			String famFullName, antibioName, diskQuantity, caseString, suscString, resiString;
			
			while(!actualAntibio.equals("") || !nextAntibio.equals("")) {
				if(AntibioticFamilies.families.contains(actualAntibio)) break;
				if(!actualAntibio.equals("")){
					
					Element brkpt = d.createElement("breakpoint");
					Element antibio = d.createElement("antibiotic");
					
					antibioName = removeNotes(actualAntibio);
					Element abName = elementWithTxt("name",antibioName);
					antibio.appendChild(abName);
					
					famFullName = SheetSearch.getStringFrom(famCR, st);
					famFullName = getAntibioFamName(famFullName);
					
					Element abFam = elementWithTxt("family",famFullName);
					antibio.appendChild(abFam);
					
					Element abDisk;
					if(zoneCR != null) {
						diskCR = new CellReference(actualCR.getRow(),zoneCR.getCol()-1);
						diskQuantity = SheetSearch.getStringFrom(diskCR, st);
						diskQuantity = removeNotes(diskQuantity);
						abDisk = (diskQuantity.equals(""))? 
										elementWithTxt("diskContent", "-") :
										elementWithTxt("diskContent", diskQuantity);
					}
					else { abDisk = d.createElement("diskContent"); }
					antibio.appendChild(abDisk);
					
					brkpt.appendChild(antibio);
					
					caseString = getCase(actualAntibio);
					Element eCase = (!caseString.equals(""))?
										elementWithTxt("case", caseString) :
										d.createElement("case");
					brkpt.appendChild(eCase);
					
					Element available, susceptible, resistant;
					if(zoneCR != null) {
						
						suscepCR = new CellReference(actualCR.getRow(),zoneCR.getCol());
						resistCR = new CellReference(actualCR.getRow(), zoneCR.getCol()+1);
						
						suscString = SheetSearch.getStringFrom(suscepCR, st);
						resiString = SheetSearch.getStringFrom(resistCR, st);
						
						suscString = removeNotes(suscString);
						resiString = removeNotes(resiString);
						
						boolean noData = (suscString.equals("-") && resiString.equals("-")) ||
										 (suscString.equals("Note") && resiString.equals("Note")) ||
										 (suscString.equals("IP") && resiString.equals("IP")) ||
										 (suscString.equals("IE") && resiString.equals("IE")) ||
										 (suscString.equals("NA") && resiString.equals("NA"));
						available = (noData)?
									elementWithTxt("available","false"):
									elementWithTxt("available","true");
									
						susceptible = elementWithTxt("susceptible", suscString);
						resistant = elementWithTxt("resistant", resiString);
					}
					else {
						available = elementWithTxt("available","false");
						susceptible = elementWithTxt("susceptible","-");
						resistant = elementWithTxt("resistant","-");
					}
					
					brkpt.appendChild(available);
					brkpt.appendChild(susceptible);
					brkpt.appendChild(resistant);
					
					Element comments = d.createElement("comments");
					brkpt.appendChild(comments);
					
					fbrkpt.appendChild(brkpt);
				}
				
				actualCR = nextCR;
				nextCR = new CellReference(nextCR.getRow()+1, nextCR.getCol());
				actualAntibio = SheetSearch.getStringFrom(actualCR, st);
				nextAntibio = SheetSearch.getStringFrom(nextCR, st);
			}
		}
	}
	
	private static Element elementWithTxt(String elemTag, String elemText) {
		Element aux = d.createElement(elemTag);
		aux.appendChild(d.createTextNode(elemText));
		return aux;
	}
	
	private static String getBacteriaFamilyName(Sheet ofThisSheet) {
		CellReference cr = new CellReference("A1");
		Cell c = ofThisSheet.getRow(cr.getRow())
							.getCell(cr.getCol());
		String result = c.getStringCellValue();
		
		Matcher matcher = Pattern.compile("\\d+").matcher(result);
		if(matcher.find()) { 
			result = result.substring(0, matcher.start()); 
		}
		
		int indOfBracket = result.indexOf('(');
		if(indOfBracket != -1) {
			result = result.substring(0, indOfBracket-1);
		}
		
		result = result.replace("\r", "").replace("\n", "");
		
		return result;
	}
	
	private static String getAntibioFamName(String fullString) {
		String result = fullString;
		
		Matcher m = Pattern.compile("\\d+").matcher(result);
		if(m.find()) {
			result = result.substring(0, m.start()); 
		}
		
		return result;
	}
	
	private static String removeNotes(String fromThis) {
		String result = fromThis;
		
		if(result.contains("Note")) {
			result = "Note";
		}
		
		Matcher m = Pattern.compile("\\p{Alpha}").matcher(result);
		if(m.find()) {
			if(m.start() != 0) {
				result = result.substring(0, m.start());
			}
			else {
				m = Pattern.compile("\\d+").matcher(result);
				if(m.find()) {
					result = result.substring(0, m.start()); 
				}
				
				int indOfBracket = result.indexOf('(');
				if(indOfBracket != -1) {
					result = result.substring(0, indOfBracket-1);
				}
				
				int indOfComma = result.indexOf(',');
				if(indOfComma != -1) {
					result = result.substring(0, indOfComma);
				}
			}
		}
		
		return result.trim();
	}
	
	private static String getCase(String fromThisAntibio) {
		String result = "";
		
		int oBracket = fromThisAntibio.indexOf('(');
		if(oBracket != -1) {
			result = fromThisAntibio.substring(oBracket);
		}
		else {
			int comma = fromThisAntibio.indexOf(',');
			if(comma != -1) {
				result = fromThisAntibio.substring(comma+1);
			}
		}
		
		Matcher m = Pattern.compile("\\d+").matcher(result);
		if(m.find()) {
			result = result.substring(0, m.start()); 
		}
		
		return result;
	}
	
	// WRITING THE RESULT IN XML
	private static void writeXML(String pathOut) {
		try {
			String fileOut = getMetadata("name")+"-"+getMetadata("version")+".xml";
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(d);
			StreamResult result = new StreamResult(new File(pathOut+"/"+fileOut));
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
			
			String xslLine = "<?xml-stylesheet type=\"text/xsl\" href=\"standardsHTML.xsl\"?>";
			insertStringInFile(pathOut, fileOut, 2, xslLine);
		} 
		catch (TransformerException e) { e.printStackTrace(); }
	}
	
	private static String getMetadata(String name) {
		Element metadata = (Element) d.getDocumentElement()
									  .getElementsByTagName("metadata")
									  .item(0);
		String value = metadata.getElementsByTagName(name)
							   .item(0)
							   .getFirstChild()
							   .getTextContent();
		return value;
	}
	
	private static void insertStringInFile (String pathIn, String fileIn, int lineNum, String lineToBeInserted) {
		File inFile = new File(pathIn+"/"+fileIn);
		File outFile = new File(pathIn+"/$$$$$$$$.tmp");
		try (
			FileInputStream fis = new FileInputStream(inFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			FileOutputStream fos = new FileOutputStream(outFile);
			PrintWriter out = new PrintWriter(fos);
		){
			String thisLine = "";
			int i = 1;
			while ((thisLine = in.readLine()) != null) {
				if (i == lineNum) out.println(lineToBeInserted);
				out.println(thisLine);
				i++;
			}
			out.flush();
		} 
		catch (FileNotFoundException e) { e.printStackTrace();} 
		catch (IOException e) { e.printStackTrace();}
		
		inFile.delete();
		outFile.renameTo(inFile);
	}
}
