import net.reddconomy.plugin.sponge.ReddconomyApi_sponge;

public class TestPlugin{
	public static void main(String[] args) throws Exception {
		String _URL = "https://reddconomy.frk.wf:8099";
		ReddconomyApi_sponge eg=new ReddconomyApi_sponge(_URL);
		eg.sendCoins("nn6GTxBU1fUrWkfXHYNNLYDkXmHMtSUjiZ",100);
	}
}
