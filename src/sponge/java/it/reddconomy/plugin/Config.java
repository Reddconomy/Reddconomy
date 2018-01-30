package it.reddconomy.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config extends HashMap<String,Object>{
	private static final Gson _JSON=new GsonBuilder().setPrettyPrinting().create();
	private final File _FILE;
	private static Config INSTANCE;
	
	public static void load() throws IOException {
		// Load config file if exists
		if(INSTANCE._FILE.exists()){
			BufferedReader reader=new BufferedReader(new FileReader(INSTANCE._FILE));
			Map<String,Object> config=_JSON.fromJson(reader,Map.class);
			INSTANCE.putAll(config);
			reader.close();
		}
	}
	
	public static void save() throws IOException{
		// Write config
		BufferedWriter config_writer=new BufferedWriter(new FileWriter(INSTANCE._FILE));
		_JSON.toJson(INSTANCE,config_writer);
		config_writer.close();
	}
	
	public static void init(File f) throws IOException{
		new Config(f);
	}

	private Config(File config_file) throws IOException{
		INSTANCE=this;
		_FILE=config_file;
		load();

		// Add missing config parameters
		putIfAbsent("ConfigVersion",8);
		putIfAbsent("url","http://127.0.0.1:8099");
		putIfAbsent("secretkey","changeme");
		putIfAbsent("qr","https://reddconomy.it/qr:text${PAYDATA}");
		putIfAbsent("csigns",true);
		putIfAbsent("debug",false);

		save();
	}

	public static <T> T getValue(String key) {
		return (T)INSTANCE.get(key);
	}
	
	public static void setValue(String key,Object val) {
		INSTANCE.put(key,val);
	}
	
	public static void replaceValue(String key,String val) throws Exception {
		Object oldval=INSTANCE.get(key);
		if(oldval==null)throw new Exception(key+" does not exist");
		if(oldval instanceof Number){
			if(val.contains("."))INSTANCE.put(key,Double.parseDouble(val));
			else INSTANCE.put(key,Long.parseLong(val));
		}else{
			INSTANCE.put(key,val);
		}
	}
}
