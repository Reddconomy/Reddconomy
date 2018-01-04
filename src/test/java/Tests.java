import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import database.Database;
import database.implementation.SQLLiteDatabase;

public class Tests{

	public static Object[] createSqliteDB() throws Exception{
		File f=File.createTempFile("reddconomy_test","");
		System.out.println("Create temp file "+f.getAbsolutePath());
		SQLLiteDatabase db=new SQLLiteDatabase(f.getAbsolutePath());
		db.open();
		return new Object[]{f,db};
	}
	
	public static void closeSqliteDB(Object[] data){
		Database db=(Database)data[1];
		File f=(File)data[0];
		db.close();
		f.delete();
	}
	
	

	
	
	@Test
	public void testContract() throws Exception{
		Object db_data[]=createSqliteDB();

		Database db=(Database)db_data[1];
		String wallet_id=UUID.randomUUID().toString();
		long ammount=10;
		String contract_id=db.createContract(wallet_id, ammount);
		Map<String,Object> contract=db.getContract(contract_id);
		long cammount=(long)contract.get("ammount");
		assertTrue(ammount+"=/="+cammount,ammount==cammount);
				
		closeSqliteDB(db_data);

	}
	
	@Test
	public void testDepositPrepareAndCompleteSQLite() throws Exception{
		Object db_data[]=createSqliteDB();
		Database db=(Database)db_data[1];
	
		String wallet_id=UUID.randomUUID().toString();
		String addr1=UUID.randomUUID().toString();
		String addr2=UUID.randomUUID().toString();
		System.out.println("Address1: "+addr1);
		System.out.println("Address2: "+addr2);
		
		db.prepareForDeposit(addr1,wallet_id,10);
		db.prepareForDeposit(addr2,wallet_id,20);

		Collection<Map<String,Object>> incomplete=db.getIncompletedDepositsAndUpdate(10);
		int i=incomplete.size();
		assertTrue("Incomplete deposits are "+i+" while 2 expected",i==2);
		
		db.completeDeposit(addr1);
		db.completeDeposit(addr2);
		
		incomplete=db.getIncompletedDepositsAndUpdate(10);
		i=incomplete.size();
		assertTrue(i==0);
		
		
		
		
		closeSqliteDB(db_data);
	}
	
	@Test
	public void testContract() throws Exception{
		Object db_data[]=createSqliteDB();

		Database db=(Database)db_data[1];
		String wallet_id1=UUID.randomUUID().toString();
		String wallet_id2=UUID.randomUUID().toString();
		
		db.prepareForDeposit("fakeaddr",wallet_id2,10);
		db.completeDeposit("fakeaddr");

		long ammount1=10;
		long ammount2=-3;
		
		
		String contract1_id=db.createContract(wallet_id1, ammount1);
		System.out.println("Contract1 " +contract1_id);
		String contract2_id=db.createContract(wallet_id1, ammount2);
		System.out.println("Contract2 " +contract2_id);

		Map<String,Object> contract1=db.getContract(contract1_id);
		long cammount=(long)contract1.get("ammount");
		assertTrue(ammount1+"=/="+cammount,ammount1==cammount);

		
		
		db.acceptContract(contract1_id,wallet_id2);
		db.acceptContract(contract2_id,wallet_id2);

		
		Map<String,Object> wallet1=db.getWallet(wallet_id1);
		Map<String,Object> wallet2=db.getWallet(wallet_id2);
		
		long balance1=(long)wallet1.get("balance");
		System.out.println("Balance1 "+balance1);
		
		long balance2=(long)wallet2.get("balance");
		System.out.println("Balance2 "+balance2);

		assertTrue(balance1==7);
		assertTrue(balance2==3);
		
		
		closeSqliteDB(db_data);

	}
	
	@Test
	public void test() throws Exception {


	}
}
