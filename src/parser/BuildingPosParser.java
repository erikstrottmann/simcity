package parser;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import parser.BuildingDef.CreatorEnum;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;

/*
 * Parses the configuration files for building placement,
 * returns a list of BuildingDef with a String name and a
 * LocationTypeEnum type. The type will be None for when
 * it is specified not to have a building.
 * 
 * You pass down a stream to a file to the static function;
 * for an example, look at the first few lines of the unit test.
 * 
 * For instructions on how to build the file and an example,
 * open parser/test/BuildingPosTest.csv using Excel or your 
 * favorite text editor.
 * 
 * MAY throw exceptions for incorrectly formated files. May also ignore
 * the incorrect line, so look out.
 */

public class BuildingPosParser {	
	
	static public List<BuildingDef>
			parseBuildingPos(BufferedInputStream stream) throws Exception {
		
		Scanner scanner = new Scanner(stream);
		
		List<BuildingDef> response = new ArrayList<BuildingDef>();
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.charAt(0) == '#' ||
					line.charAt(0) == '"') continue;
			
			int buildingInt = Integer.parseInt(line.substring(0, 1));
			String name = line.substring(2);
			
			LocationTypeEnum buildingType;
			CreatorEnum buildingCreator;
			
			switch (buildingInt) {
//			case 0:
//				buildingType = LocationTypeEnum.None;
//				buildingCreator = CreatorEnum.UNKNOWN;
//				break;
			case 1:
				buildingType = LocationTypeEnum.Restaurant;
				buildingCreator = CreatorEnum.ERIK;
				break;
			case 2:
				buildingType = LocationTypeEnum.Bank;
				buildingCreator = CreatorEnum.JACK;
				break;
			case 3:
				buildingType = LocationTypeEnum.Market;
				buildingCreator = CreatorEnum.ANTHONY;
				break;
			case 4:
				buildingType = LocationTypeEnum.House;
				buildingCreator = CreatorEnum.ZACH;
				break;
//			case 5:
//				buildingType = LocationTypeEnum.Apartment;
//				buildingCreator = CreatorEnum.ZACH;
//				break;
			case 6:
				buildingType = LocationTypeEnum.Hospital;
				buildingCreator = CreatorEnum.VICTORIA;
				break;
			case 7:
				buildingType = LocationTypeEnum.Restaurant;
				buildingCreator = CreatorEnum.ANTHONY;
				break;
			case 8:
				buildingType = LocationTypeEnum.Restaurant;
				buildingCreator = CreatorEnum.JACK;
				break;
			case 9:
				buildingType = LocationTypeEnum.Restaurant;
				buildingCreator = CreatorEnum.VICTORIA;
				break;
			case 0:
				buildingType = LocationTypeEnum.Restaurant;
				buildingCreator = CreatorEnum.ZACH;
				break;
			case 5:
				buildingType = LocationTypeEnum.Restaurant;
				buildingCreator = CreatorEnum.DIEGO;
				break;
			default:
				throw new Exception("Parser was given an int that does "
						+ "not correspond to any building type.");
			}
			
			response.add(new BuildingDef(name, buildingType, buildingCreator));
			
		}
	
		
		if (scanner != null) scanner.close();
		
		for (int i = 0; i < 6; i++) {
			response.add(new BuildingDef("", LocationTypeEnum.None, CreatorEnum.UNKNOWN));
		}
		
		
		return response;
	}
}
