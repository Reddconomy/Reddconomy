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
package reddconomy.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import reddconomy.data.Data;
import reddconomy.data.Deposit;
import reddconomy.data.EmptyData;
import reddconomy.data.NetworkInfo;
import reddconomy.data.OffchainContract;
import reddconomy.data.OffchainWallet;
import reddconomy.data.Withdraw;

public class ApiResponse{
	public static final Map<String, Class<? extends Data>> _REGISTERED_DATA=new HashMap<String, Class<? extends Data>>();
	public static void register(String id, Class<? extends Data> c){
		_REGISTERED_DATA.put(id,c);
	}

	
	public static Class<? extends Data> getClassByType(String type){
		return _REGISTERED_DATA.get(type);
	}
	public static String getTypeByClass( Class<? extends Data> c) {
		for(Entry<String, Class<? extends Data>> e:_REGISTERED_DATA.entrySet())	if(e.getClass()==c) return e.getKey();		
		return null;
	}
	
	public static void registerAll(String for_version){
		if(for_version.equals("v1")){
			ApiResponse.register("Deposit",Deposit.class);
			ApiResponse.register("Empty",EmptyData.class);
			ApiResponse.register("OffchainContract",OffchainContract.class);
			ApiResponse.register("OffchainWallet",OffchainWallet.class);
			ApiResponse.register("Withdraw",Withdraw.class);	
			ApiResponse.register("NetworkInfo",NetworkInfo.class);	
		}
	}
	

	public static ApiResponse build(){
		return new ApiResponse();
	}
	
	private ApiResponse(){
		
	}
	
	
	private int STATUS_CODE=200;
	private String STATUS="Ok";
	private Data DATA=new EmptyData();
	
	public int statusCode(){
		return STATUS_CODE;
	}
	
	public String status(){
		return STATUS;
	}
	
	public <T extends Data> T data(){
		return (T)DATA;
	}
	
	public ApiResponse success(Data data){
		STATUS_CODE=200;
		STATUS="Ok";
		DATA=data;
		return this;
	}
	
	public ApiResponse error(int code,String error){
		STATUS_CODE=code;
		STATUS=error;
		DATA=new EmptyData();
		return this;
	}
	
	public Map<String,Object> toMap(){
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("status_code",STATUS_CODE);
		out.put("status",STATUS);
		out.put("data_type",getTypeByClass(DATA.getClass()));
		out.put("data",DATA!=null?DATA.toMap():new HashMap<String,Object>());
		return out;
	}
	
	public ApiResponse fromMap(Map<String,Object>  map){		
		STATUS_CODE=((Number)map.get("status_code")).intValue();
		STATUS=map.get("status").toString();
		String data_type=map.get("data_type").toString();
		Class<? extends Data> data_class=getClassByType(data_type);		
		try{
			Data data=data_class.newInstance();
			data.fromMap(map);
			DATA=data;
		}catch(Exception e){
			e.printStackTrace();
			DATA=null;
		}	
		return this;
	}

	
}
