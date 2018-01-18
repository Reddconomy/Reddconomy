package reddconomy.data;

import java.util.HashMap;
import java.util.Map;

public final class EmptyData implements Data{
	
	@Override
	public Map<String,Object> toMap() {
		return new HashMap<String,Object>();
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		
	}

	
	@Override
	public boolean equals(Object data2){
		return data2 instanceof EmptyData;
	}
	
	@Override
	public String toString(){
		return "<Empty>";
	}
}
