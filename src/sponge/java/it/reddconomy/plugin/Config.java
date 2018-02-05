/*
 * Copyright (c) 2018, Simone Cervino.
 * 
 * This file is part of Reddconomy-sponge.

    Reddconomy-sponge is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Reddconomy-sponge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Reddconomy-sponge.  If not, see <http://www.gnu.org/licenses/>.
 */
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

@SuppressWarnings("serial")
public class Config extends HashMap<String,Object>{
	private static final Gson _JSON=new GsonBuilder().setPrettyPrinting().create();
	private final File _FILE;
	private static Config INSTANCE;
	
	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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
