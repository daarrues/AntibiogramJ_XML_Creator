package util;

import java.util.ArrayList;
import java.util.List;

public class AntibioticFamilies {
	public static final List<String> families;
	
	static {
		
		families = new ArrayList<>();
		families.add("Penicillins");
		families.add("Cephalosporins");
		families.add("Carbapenems");
		families.add("Monobactams");
		families.add("Fluoroquinolones");
		families.add("Aminoglycosides");
		families.add("Glycopeptides"); //  and lipoglycopeptides
		families.add("Macrolides"); //, lincosamides and streptogramins
		families.add("Tetracyclines");
		families.add("Oxazolidinones");
				
		families.add("Miscellaneous agents");
	}
}
