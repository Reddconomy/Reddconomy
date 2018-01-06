package reddconomy;

public interface CentralWallet{
	public long getReceivedByAddress(String addr) throws Throwable ;	
	public void sendToAddress(String addr, long amount_long) throws Throwable;
	public String getNewAddress() throws Throwable ;
}
