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
	public void test() throws Exception {


	}
}
