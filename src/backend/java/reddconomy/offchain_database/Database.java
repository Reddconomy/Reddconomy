/*
 * Copyright (c) 2018, Riccardo Balbo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package reddconomy.offchain_database;

import java.sql.SQLException;
import java.util.Collection;

import reddconomy.data.Deposit;
import reddconomy.data.OffchainContract;
import reddconomy.data.OffchainWallet;
import reddconomy.data.Withdraw;

/**
 * This interface represent an internal database used to handle off-chain transactions 
 * Note: No blockchain operations should be handled by classes that implement this interface
 * @author Riccardo Balbo
 *
 */
public interface Database{
	// 
	void open() throws SQLException;
	boolean close();
	//	


	/**
	 * Get an offchain wallet, create if does not exist
	 * 
	 * @param id Unique string that represent the offchain wallet (can be anything)
	 * @return An instance of OffchainWallet
	 * @throws Exception
	 */
	OffchainWallet getOffchainWallet(String id) throws Exception;

	/**
	 * Get a contract
	 * @param id Unique contract id
	 * @return OffchainContract or null if does not exist
	 * @throws SQLException
	 */
	OffchainContract getContract(String id) throws Exception;

	/**
	 * Create a contract
	 * @param walletid unique id that represent the offchain wallet that creates the contract
	 * @param amount if positive: who accepts pays, if negative: who accepts is paid
	 * @return The newly created OffchainContract
	 * @throws SQLException
	 */
	OffchainContract createContract(String walletid, long amount) throws Exception;

	/**
	 * Accept a contract
	 * @param contractid unique id that represent the contract
	 * @param walletid unique id that represent the wallet of whom accepts
	 * @throws Exception is raised if for some reason the contract can't be accepted
	 */
	OffchainContract acceptContract(String contractid, String walletid) throws Exception;

	/**
	 * Execute a withdraw on a OffchainWallet, ie. Removes coins from an OffchainWallet
	 * Note: this is not enough to perform an actual withdraw, a correspondent action must be
	 * performed from a BlockchainConnector.
	 * @param walletid Unique id of the OffchainWallet that performs the withdraw
	 * @param amount quanti soldi
	 * @throws Exception solleva eccezione se non è possibile farlo
	 */
	Withdraw withdraw(String walletid, long amount) throws Exception;

	/**
	 * Tells the backend to wait for a deposit
	 * @param deposit_addr Blockchain address where it's expected to find the coins once they are deposited (obtained from BlockchainConnector)
	 * @param wallet_id OffchainWallet where the coins will go
	 * @param expected_balance How many coins to expect
	 * @throws Exception If for some reason the deposit is not possible
	 */
	Deposit prepareForDeposit(String deposit_addr, String walletid, long expected_balance) throws Exception;

	/**
	 * Get all the pending deposits that have yet to expire 
	 * @param tms Time since last call of this method (0 if it's the firs call)
	 * @return Collection of Deposit
	 * @throws Exception  
	 */
	Collection<Deposit> getIncompletedDepositsAndUpdate(long tms) throws Exception;

	/**
	 * Tells the backend that the deposit action that was waiting on the specified addres has been completed
	 * @param deposit_addr Blockchain Address
	 * @throws Exception 
	 */
	Deposit completeDeposit(String deposit_addr) throws Exception;
	
	/**
	 * Get informations about a deposit 
	 * @param deposit_addr Deposit address
	 * @throws Exception 
	 */
	Deposit getDeposit(String deposit_addr) throws Exception;


}