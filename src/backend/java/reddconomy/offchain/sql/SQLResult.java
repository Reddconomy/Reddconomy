/*
 * Copyright (c) 2018, Riccardo Balbo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package reddconomy.offchain.sql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of an SQL query
 * @author Riccardo Balbo
 *
 */
public class SQLResult{
	protected ResultSet RESULT_SET;
	protected Boolean EMPTY=null;

	public SQLResult(ResultSet result){
		RESULT_SET=result;
	}

	public Map<String,Object> fetchAssoc() {
		Map<String,Object> map=new HashMap<String,Object>();
		try{
			if(EMPTY==null)EMPTY=!RESULT_SET.next();
			for(int column=1;column<=RESULT_SET.getMetaData().getColumnCount();column++){
				map.put(RESULT_SET.getMetaData().getColumnLabel(column),RESULT_SET.getObject(column));
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

	public Collection<Map<String,Object>> fetchAll() {
		Collection<Map<String,Object>> out=new ArrayList<Map<String,Object>>();
		Map<String,Object> fetch=null;
		while((fetch=fetchAssoc())!=null){
			out.add(fetch);
		}
		return out;
	}

}
