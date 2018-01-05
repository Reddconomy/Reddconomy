package reddconomy;

public interface CentralWallet{
	public long getReceivedByAddress(String addr) throws Throwable ;	
	public void sendToAddress(String addr, long ammount_long) throws Throwable;
	public String getNewAddress() throws Throwable ;
}
