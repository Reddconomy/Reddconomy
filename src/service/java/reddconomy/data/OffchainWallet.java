package reddconomy.data;

import java.util.HashMap;
import java.util.Map;

public final class OffchainWallet implements Data{
	

	
	public static final class Status{
		public static final byte active=1;
		public static final byte deactive=0;

	}
	public String id;
	public long balance=0;
	public byte status=Status.active;
	
	@Override
	public boolean equals(Object wallet2o){
		if(!(wallet2o instanceof OffchainWallet))return false;
		OffchainWallet wallet2=(OffchainWallet)wallet2o;
		return id.equals(wallet2.id)&&balance==wallet2.balance&&status==wallet2.status;
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Wallet-").append(id).append(": balance=").append(balance).append(" status=").append(status);
		return sb.toString();
	}

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("id",id);
		out.put("balance",balance);
		out.put("status",status);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		id=map.get("id").toString();
		balance=((Number)map.get("balance")).longValue();
		status=((Number)map.get("status")).byteValue();
	}
}
