package reddconomy.offchain.fees;

public interface Fee{
	public long apply(long v);
	public long getRaw();
}
