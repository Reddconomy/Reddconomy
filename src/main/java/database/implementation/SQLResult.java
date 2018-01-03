package database.implementation;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SQLResult{
	// Based on SimpleMySQLResult by Daniel Morante
	protected ResultSet RESULT_SET;
	protected Boolean EMPTY=null;
	//    private int POSITION = 0;

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

	//    private void save_position(){
	//        try{
	//            if(RESULT_SET.isBeforeFirst()){
	//                POSITION = 0;
	//            }
	//            else if(RESULT_SET.isAfterLast()){
	//                POSITION = -1;
	//            }
	//            else{
	//                POSITION = getRow();
	//            }
	//        }
	//        catch(Exception e){            
	//        }                
	//    }
	//    private void restore_position(){
	//        try{        
	//            if(POSITION == 0){
	//                RESULT_SET.beforeFirst();
	//            }
	//            else if(POSITION == -1){
	//                RESULT_SET.afterLast();
	//            }
	//            else{
	//                absolute(POSITION);
	//            }
	//        }
	//        catch(Exception e){            
	//        }                 
	//    }

	//    public boolean absolute(int row){
	//        try {return RESULT_SET.absolute(row);}
	//        catch(Exception e){return false;}
	//    }    

	//    public int getNumRows(){        
	//        int returnValue = 0;
	//        try{
	//            save_position();
	//            RESULT_SET.last();
	//            returnValue = RESULT_SET.getRow();   
	//            restore_position();
	//        }
	//        catch(Exception e){
	//        	e.printStackTrace();
	//        }
	//        return returnValue;
	//    }    

}
