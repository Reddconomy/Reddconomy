package it.reddconomy.common.fees;

public interface Fee{
	public long apply(long v);
	public long getRaw();
}
