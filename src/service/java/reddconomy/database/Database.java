package reddconomy.database;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * The database
 * @author Riccardo Balbo
 *
 */
public interface Database{
	// 
	void open() throws SQLException;
	boolean close();
	//	
		
	/**
	 * Ottiene le informazioni di un wallet, se il wallet non esiste, lo crea e lo inizializza
	 * @param id id del wallet
	 * @return Mappa con la roba
	 * @throws SQLException
	 */
	Map<String,Object> getWallet(String id) throws SQLException;

	/**
	 * Ritorna le informazioni di un contratto
	 * @param id id del contratto
	 * @return Mappa contenente la roba
	 * @throws SQLException
	 */
	Map<String,Object> getContract(String id) throws SQLException;

	/**
	 * Crea contratto
	 * @param walletid id di chi lo crea
	 * @param amount quantità di soldi che vuole trasferire (positivo= da chi lo accetta a chi lo crea, negativo=da chi lo crea a chi lo accetta)
	 * @return
	 * @throws SQLException
	 */
	String createContract(String walletid, long amount) throws SQLException;

	/**
	 * Accetta contratto
	 * @param contractid id del contratto
	 * @param walletid id di chi lo accetta
	 * @throws Exception solleva eccezione se non è possibile accettarlo
	 */
	void acceptContract(String contractid, String walletid) throws Exception;

	/**
	 * Riflette sul db un'operazione di withdraw.
	 * @param walletid chi lo fa
	 * @param amount quanti soldi
	 * @throws Exception solleva eccezione se non è possibile farlo
	 */
	void withdraw(String walletid, long amount) throws Exception;

	/**
	 * Prepara un'operazione di deposit
	 * @param deposit_addr Indirizzo di deposito (nella blockchain)
	 * @param wallet_id Id del ricevente
	 * @param expected_balance Quanto si deve depositare
	 * @throws Exception se non si può fare
	 */
	void prepareForDeposit(String deposit_addr, String walletid, long expected_balance) throws Exception;

	/**
	 * Ritorna i depositi in attesa di completamento e contemporaneamente ne gestisce l'expire 
	 * @param tms Tempo passato in secondi dall'ultima chiamata o 0 se prima chiamata
	 * @return Collezione di mappe contenente i dati dei depositi
	 * @throws Exception  
	 */
	Collection<Map<String,Object>> getIncompletedDepositsAndUpdate(long tms) throws Exception;

	/**
	 * Informa il db che il deposito è completo
	 * @param deposit_addr indirizzo di deposito (blockchain)
	 * @throws Exception 
	 */
	void completeDeposit(String deposit_addr) throws Exception;
	
	/**
	 * Ritorna informazioni sul deposito
	 * @param deposit_addr indirizzo di deposito (blockchain)
	 * @throws Exception 
	 */
	Map<String,Object> getDeposit(String deposit_addr) throws Exception;


}