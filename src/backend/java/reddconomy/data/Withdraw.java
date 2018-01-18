package reddconomy.data;

import java.util.HashMap;
import java.util.Map;

public final class Withdraw implements Data{
	
	
	public static final class Status{
		public static final byte pending=1;
		public static final byte expired=-1;
		public static final byte complete=0;
	}
	

	/**
	 * Blockchain address where to send coins
	 */
	public String from_wallet;

	/**
	 * How many coins
	 */
	public long amount;
	
		
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Withdraw-").append(from_wallet).append(": ammount=").append(amount);
		return sb.toString();
	}
	
	

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("from_wallet",from_wallet);
		out.put("amount",amount);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		from_wallet=map.get("from_wallet").toString();
		amount=((Number)map.get("amount")).longValue();
	}
	
	@Override
	public boolean equals(Object w2o){
		if(!(w2o instanceof Withdraw))return false;
		Withdraw w2=(Withdraw)w2o;
		return from_wallet.equals(w2.from_wallet)&&amount==w2.amount;
	}
	
	
	
}
