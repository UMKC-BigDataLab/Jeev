package edu.umkc.sce_med.fh504;

public class Vaccines {

	public String[]cptCode;
	public String[]fName;
	public String[]sName;
	public int size;
	public Vaccines() {
	
		cptCode= new String [10];
		fName= new String [10];
		sName= new String [10];
		size = 10;
		
		cptCode[0]="90633";sName[0]="HepA";fName[0]="Hepatitis A";
		cptCode[1]="90645";sName[1]="Hib";fName[1]="Haemophilus Influenzae Type B";
		cptCode[2]="90655";sName[2]="Flu";fName[2]="Influenza";
		cptCode[3]="90669";sName[3]="PCV";fName[3]="Pneumococcal";
		cptCode[4]="90680";sName[4]="RV";fName[4]="Rotavirus";
		cptCode[5]="90700";sName[5]="DTaP";fName[5]="Diphtheria, Tetanus, Pertussis";
		cptCode[6]="90707";sName[6]="MMR";fName[6]="Measles, Mumps, Rubella";
		cptCode[7]="90713";sName[7]="IPV";fName[7]="Inactivated Poliovirus";
		cptCode[8]="90716";sName[8]="Varicella";fName[8]="Varicella Zoster";		
		cptCode[9]="90744";sName[9]="HepB";fName[9]="Hepatitis B";
		
		
		

	}
	
	public String lookupVaccine(String cptcode) {
		
		for(int c=0;c<10;c++)
			if(cptCode[c].equals(cptcode))
				return sName[c];	
		return null;
	}
	public int lookupVaccineIndex(String cptcode) {
		
		for(int c=0;c<10;c++)
			if(cptCode[c].equals(cptcode))
				return c;	
		return -1;
	}
	
}
