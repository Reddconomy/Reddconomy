package reddconomy.data;

import java.util.HashMap;
import java.util.Map;

public final class Deposit implements Data{

	public static final class Status{
		public static final byte pending=1;
		public static final byte expired=-1;
		public static final byte complete=0;
	}
	

	/**
	 * Blockchain address where we expect the coins to be sent
	 */
	public String addr;
	
	/**
	 * OffchainWallet that will receive the coins
	 */
	public String receiver_wallet_id; 
	
	/**
	 * How many coins
	 */
	public long expected_balance;
	
	
	public byte status;
	
	/**
	 * Creation timestamp
	 */
	public long created=System.currentTimeMillis();
	
	
	/**
	 * Ms before expiration, if the deposit action is not completed before this value becomes <=0, the status will be updated to 'expired', 
	 * and the deposit cancelled.
	 */
	public int expiring=172800000;
	
	
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Deposit-").append(addr).append(": receiver_wallet_id=").append(receiver_wallet_id)
		.append(" expected_balance=").append(expected_balance)
		.append(" status=").append(status)
		.append(" created=").append(created)
		.append(" expiring=").append(expiring);
		return sb.toString();
	}
	

	@Override
	public boolean equals(Object d2o){
		if(!(d2o instanceof Deposit))return false;
		Deposit d2=(Deposit)d2o;
		return addr.equals(d2.addr)&&
				receiver_wallet_id.equals(d2.receiver_wallet_id)
				&&expected_balance==d2.expected_balance
				&&status==d2.status
				&&created==d2.created
				&&expiring==d2.expiring;
	}
	

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("addr",addr);
		out.put("receiver_wallet_id",receiver_wallet_id);
		out.put("expected_balance",expected_balance);
		out.put("status",status);
		out.put("created",created);
		out.put("expiring",expiring);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		addr=map.get("addr").toString();
		receiver_wallet_id=map.get("receiver_wallet_id").toString();
		expected_balance=((Number)map.get("expected_balance")).longValue();
		status=((Number)map.get("status")).byteValue();
		created=((Number)map.get("created")).longValue();
		created=((Number)map.get("expiring")).longValue();
	}
}
