package mtn.sevenuplive.scales;

public class Scale {
	
	public int Degrees[];
	public ScaleName Name;
	public String label;
	
	/**
	 * The Commandments of Scales in 7up
	 * 
	 * You can have less than 7 degrees, but never more
	 * A degree cannot be more than 11
	 * 
	 * @param name
	 */
	public Scale(ScaleName name){
		Degrees = new int[7];
		switch(name){
		case MinorPentatonic:
			Name = ScaleName.MinorPentatonic;
			label = "Pentatonic (Minor)";
			Degrees = new int[5];
			Degrees[0] = 0;
			Degrees[1] = 3;
			Degrees[2] = 4;
			Degrees[3] = 5;
			Degrees[4] = 7;
			break;
		case Pentatonic:
			Name = ScaleName.Pentatonic;
			label = "Pentatonic";
			Degrees = new int[5];
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 4;
			Degrees[3] = 7;
			Degrees[4] = 9;
			break;
		case Blues:
			Name = ScaleName.Blues;
			label = "Blues";
			Degrees = new int[6];
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 3;
			Degrees[3] = 4;
			Degrees[4] = 7;
			Degrees[5] = 9;
			break;
		case MinorBlues:
			Degrees = new int[6];
			Name = ScaleName.MinorBlues;
			label = "Blues (Minor)";
			Degrees[0] = 0;
			Degrees[1] = 3;
			Degrees[2] = 5;
			Degrees[3] = 6;
			Degrees[4] = 7;
			Degrees[5] = 10;
			break;
		case Dorian:
			Name = ScaleName.Dorian;
			label = "Dorian";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 3;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 9;
			Degrees[6] = 10;
			break;
		case UltraLocrian:
			Name = ScaleName.UltraLocrian;
			label = "Ultra Locrian";
			Degrees[0] = 0;
			Degrees[1] = 1;
			Degrees[2] = 3;
			Degrees[3] = 4;
			Degrees[4] = 6;
			Degrees[5] = 8;
			Degrees[6] = 9;
			break;
		case Major:
			Name = ScaleName.Major;
			label = "Major";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 4;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 9;
			Degrees[6] = 11;
			break;
		case Yorkian:
			Name = ScaleName.Yorkian;
			label = "Yorkian";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 4;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 9;
			Degrees[6] = 10;
			break;
		case MinorSeven:
			Name = ScaleName.MinorSeven;
			label = "Minor Seven";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 3;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 9;
			Degrees[6] = 10;
			break;
		case Billian:
			Name = ScaleName.Billian;
			label = "Billian";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 3;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 8;
			Degrees[6] = 10;
			break;
		case Minor:
			Name = ScaleName.Minor;
			label = "Minor";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 3;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 9;
			Degrees[6] = 11;
			break;
		case Mullnixian:
			Name = ScaleName.Mullnixian;
			label = "Mullnixian";
			Degrees[0] = 0;
			Degrees[1] = 1;
			Degrees[2] = 3;
			Degrees[3] = 5;
			Degrees[4] = 7;
			Degrees[5] = 8;
			Degrees[6] = 10;
			break;
		case Telerium:
			Name = ScaleName.Telerium;
			label = "Telerium";
			Degrees[0] = 0;
			Degrees[1] = 2;
			Degrees[2] = 3;
			Degrees[3] = 5;
			Degrees[4] = 6;
			Degrees[5] = 8;
			Degrees[6] = 11;
			break;
		case Chromatic:
			Name = ScaleName.Chromatic;
			label = "Chromatic";
			Degrees[0] = 0;
			Degrees[1] = 1;
			Degrees[2] = 2;
			Degrees[3] = 3;
			Degrees[4] = 4;
			Degrees[5] = 5;
			Degrees[6] = 6;
			break;
		}
	}
	
	

}
