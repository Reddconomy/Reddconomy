package reddconomy.offchain.fees;

import reddconomy.Utils;

public class FixedFee implements Fee{
	protected final long _FEE;
	
	public FixedFee(long fee){
		_FEE=fee;
	}
	
	@Override
	public long apply(long v) {	
		return v-_FEE;
	}
	
	@Override
	public long getRaw(){
		return _FEE;
	}

	@Override
	public String toString(){
		return "Fee: "+ Utils.convertToUserFriendly(_FEE)+" (fixed)";
	}
}
