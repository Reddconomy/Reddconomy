package it.reddconomy.common.fees;

public class ProportionalFee implements Fee{
	protected byte _FEE;
	
	public ProportionalFee(int fee){
		_FEE=(byte)fee;
	}
	
	@Override
	public long apply(long v) {
		long deduct= ((long)_FEE*v)/100l;
		return v-deduct;
	}
	
	@Override
	public String toString(){
		return "Fee: "+ _FEE+"%";
	}
	
	@Override
	public long getRaw(){
		return _FEE;
	}
}
