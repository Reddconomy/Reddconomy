package reddconomy.database.implementation;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SQLResult{
	protected ResultSet RESULT_SET;
	protected Boolean EMPTY=null;

	public SQLResult(ResultSet result){
		RESULT_SET=result;
	}

	public Map<String,String> FetchAssoc() {
		Map<String,String> map=new HashMap<String,String>();
		try{
			if(EMPTY==null)EMPTY=!RESULT_SET.next();
			for(int column=1;column<=RESULT_SET.getMetaData().getColumnCount();column++){
				map.put(RESULT_SET.getMetaData().getColumnLabel(column),RESULT_SET.getString(column));
			}
			if(EMPTY!=null)RESULT_SET.next();
		}catch(Exception e){}
		return map;
	}

	
	
	public boolean isEmpty(){
		try{
			if(EMPTY==null)EMPTY=!RESULT_SET.next();
			return EMPTY;
		}catch(Exception e){
			
		}
		return true;
	}

	public Collection<Map<String,String>> fetchAll() {
		Collection<Map<String,String>> out=new ArrayList<Map<String,String>>();
		Map<String,String> fetch=null;
		while((fetch=FetchAssoc())!=null){
			out.add(fetch);
		}
		return out;
	}

}
