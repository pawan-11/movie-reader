package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import util.Util;


public class JSONParser {

	public JSONObject word_map;
	
	public boolean parse(File json_file) {
		try {
			String json_string = "", line;
			BufferedReader reader = new BufferedReader(new FileReader(json_file));
			while ((line = reader.readLine()) != null)
				json_string += line;
			
			reader.close();
			
			JSONObject j = (JSONObject)JSONValue.parse(json_string);
			word_map = j;		
			return j.keySet().size() != 0;
		} catch (Exception e) {
			//e.printStackTrace();
			//Util.print("response file not in json format");
			return false;
		}
	}
	
	public static boolean save(Map<String, List<String>> map, String json_file) {
		json_file += ".json";
		try {
			File response_dir = new File(Paths.get(Util.getParentDir(), "responses").toUri().getPath());
			if (!response_dir.exists())
				if (!response_dir.mkdir()) return false;
			String path = Paths.get(response_dir.getPath(), json_file).toString();
			FileWriter writer = new FileWriter(path, false);
			JSONObject j = new JSONObject(map);
			if (j.keySet().size() != 0)
				j.writeJSONString(writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
