package reddconomy.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import reddconomy.data.Data;
import reddconomy.data.Deposit;
import reddconomy.data.EmptyData;
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
