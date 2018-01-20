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
package reddconomy.blockchain;

/**
 * Interface that describes a class used to query the blockchain 
 * @author Riccardo Balbo
 *
 */
public interface BlockchainConnector{
	/**
	 * Get balance of specified address
	 * @param addr A valid blockchain address
	 * @return Amount of coins
	 * @throws Throwable
	 */
	public long getReceivedByAddress(String addr) throws Throwable ;	
	
	/**
	 * Send coins to a specified address
	 * @param addr A valid blockchain address
	 * @param amount_long Amount to send 
	 * @throws Throwable
	 */
	public void sendToAddress(String addr, long amount_long) throws Throwable;
	
	/**
	 * Get new blockchain address
	 * @return new address
	 * @throws Throwable
	 */
	public String getNewAddress() throws Throwable ;
	
	public boolean isTestnet()  throws Throwable ;
	
	public boolean hasEnoughCoins(long v) throws Throwable;
	
	public void waitForSync();
}
