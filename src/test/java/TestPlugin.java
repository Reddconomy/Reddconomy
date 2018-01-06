import net.reddconomy.plugin.ReddconomyApi;

public class TestPlugin{
	public static void main(String[] args) throws Exception {
		String _URL = "https://reddconomy.frk.wf:8099";
		ReddconomyApi eg=new ReddconomyApi(_URL);
		eg.sendCoins("nn6GTxBU1fUrWkfXHYNNLYDkXmHMtSUjiZ",100);
	}
}
