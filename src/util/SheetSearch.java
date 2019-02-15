package util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

public class SheetSearch {

	public static CellReference lookFor(String thisString, Sheet inThisSheet) {
		boolean found = false;
		CellReference crAux = null;
		CellType ctAux = null;
		
		for(Row r: inThisSheet) {
			for(Cell c: r) {		
				ctAux = c.getCellType();
				if(ctAux.equals(CellType.FORMULA)) {
					if(c.getCellFormula().contains(thisString)) {
						crAux = new CellReference(c.getRowIndex(), c.getColumnIndex());
						found = true;
						break;
					}
				}
				else if (ctAux.equals(CellType.STRING)) {
					if(c.getStringCellValue().contains(thisString)) {
						crAux = new CellReference(c.getRowIndex(), c.getColumnIndex());
						found = true;
						break;
					}
				}
				else if (ctAux.equals(CellType.NUMERIC)) {
					String sAux = String.valueOf(c.getNumericCellValue());
					if(sAux.contains(thisString)) {
						crAux = new CellReference(c.getRowIndex(), c.getColumnIndex());
						found = true;
						break;
					}
				}
			}
			if (found) break;
		}
		
		return crAux;
	}
	
	public static String getStringFrom(CellReference thisCell, Sheet inThisSheet) {

		Cell c;
		CellType ct;
		
		try {
			c = inThisSheet.getRow(thisCell.getRow())
								   .getCell(thisCell.getCol());
			ct = c.getCellType();
		} 
		catch (NullPointerException e) { return ""; }
									
		if(ct.equals(CellType.FORMULA)) {
			return c.getCellFormula();
		}
		else if (ct.equals(CellType.STRING)) {
			return c.getStringCellValue();
		}
		else if (ct.equals(CellType.NUMERIC)) {
			return String.valueOf(c.getNumericCellValue());
		}
		else return "";
	}
}
