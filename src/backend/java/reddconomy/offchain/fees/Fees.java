package reddconomy.offchain.fees;

import java.util.HashMap;
import java.util.Map;

import reddconomy.Utils;

public class Fees{
	protected Fee WITHDRAW;
	protected Fee DEPOSIT;
	protected Fee TRANSACTION;
	
	public Fees(Fee withdraw,Fee deposit,Fee transaction){
		this.WITHDRAW=withdraw;
		this.DEPOSIT=deposit;
		this.TRANSACTION=transaction;				
	}
	
	public Fees(){
		this.WITHDRAW=new FixedFee(0);
		this.DEPOSIT=new FixedFee(0);
		this.TRANSACTION=new FixedFee(0);	
	}
	
	public Fee getWithdrawFee(){
		return WITHDRAW;
	}
	
	public Fee getDepositFee(){
		return DEPOSIT;
	}
	
	public Fee getTransactionFee(){
		return TRANSACTION;
	}
	
	private Fee fromStringToFee(String fee,boolean userfriendly){
		if(fee.endsWith("%")){
			fee=fee.substring(0,fee.indexOf("%"));
			int nfee=Integer.parseInt(fee);
			return new ProportionalFee(nfee);
		}else{
			long nfee=userfriendly?Utils.convertToInternal(Double.parseDouble(fee)):Long.parseLong(fee);						
			return new FixedFee(nfee);
		}
	}
	
	private String fromFeeToString(Fee fee,boolean userfriendly){
		long raw=fee.getRaw();
		if(fee instanceof ProportionalFee){
			return raw+"%";
		}else{
			if(userfriendly){
				return ""+Utils.convertToUserFriendly(raw);
			}else return ""+raw;
		}
	}

	
	/**
	 * Convert Fees to Map<String,String>
	 * @param userfriendly if true, fixed fees will be represented with doubles
	 * @return
	 */
	public Map<String,String> toMap(boolean userfriendly){
		Map<String,String> out=new HashMap<String,String>();	
		out.put("withdraw_fee",fromFeeToString(WITHDRAW,userfriendly));
		out.put("deposit_fee",fromFeeToString(DEPOSIT,userfriendly));
		out.put("transaction_fee",fromFeeToString(TRANSACTION,userfriendly));		
		return out;
	}
	
	/**
	 * Read Fees from Map<String,Object>
	 * @param userfriendly if true, fixed fees are expected to be represented with doubles
	 * @return
	 */
	public Fees fromMap(Map<String,Object> map,boolean userfriendly) {
		String fee;
		fee=map.get("withdraw_fee").toString();
		WITHDRAW=fromStringToFee(fee,userfriendly);
		fee=map.get("deposit_fee").toString();
		DEPOSIT=fromStringToFee(fee,userfriendly);
		fee=map.get("transaction_fee").toString();
		TRANSACTION=fromStringToFee(fee,userfriendly);		
		return this;
	}
	
	
	
}
