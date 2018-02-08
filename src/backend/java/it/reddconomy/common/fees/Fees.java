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
package it.reddconomy.common.fees;

import java.util.HashMap;
import java.util.Map;

import it.reddconomy.Utils;

public class Fees{
	protected Fee<Long> WITHDRAW;
	protected Fee<Long> DEPOSIT;
	protected Fee<Long> TRANSACTION;
	protected BlockchainFee BLOCKCHAIN_FEE;

	public Fees(Fee<Long> withdraw,Fee<Long> deposit,Fee<Long> transaction,BlockchainFee netfee){
		WITHDRAW=withdraw;
		DEPOSIT=deposit;
		TRANSACTION=transaction;				
		BLOCKCHAIN_FEE=netfee;
	}
	
	public Fees(){
		this.WITHDRAW=new FixedFee(0);
		this.DEPOSIT=new FixedFee(0);
		this.TRANSACTION=new FixedFee(0);	
		this.BLOCKCHAIN_FEE=new BlockchainFee(0);	
		
	}
	
	public Fee<Long> getWithdrawFee(){
		return WITHDRAW;
	}
	
	public Fee<Long> getDepositFee(){
		return DEPOSIT;
	}
	
	public Fee<Long> getTransactionFee(){
		return TRANSACTION;
	}
	
	public BlockchainFee getBlockchainFee(){
		return BLOCKCHAIN_FEE;
	}
	
	private <T> T fromStringToFee(String fee,boolean userfriendly){
		if(fee.endsWith("%")){
			fee=fee.substring(0,fee.indexOf("%"));
			int nfee=Integer.parseInt(fee);
			return (T) new ProportionalFee(nfee);
		}else if(fee.endsWith("/kb")){
			fee=fee.substring(0,fee.indexOf("/kb"));
			long nfee=userfriendly?Utils.convertToInternal(Double.parseDouble(fee)):Long.parseLong(fee);						
			return (T)new BlockchainFee(nfee);
		}else{
			long nfee=userfriendly?Utils.convertToInternal(Double.parseDouble(fee)):Long.parseLong(fee);						
			return(T) new FixedFee(nfee);
		}
	}
	
	private String fromFeeToString(Object fee,boolean userfriendly){
		if(fee instanceof ProportionalFee){
			long raw=((ProportionalFee)fee).getRaw();
			return raw+"%";
		}else if(fee instanceof BlockchainFee){
			long raw=((BlockchainFee)fee).getRaw();

			if(userfriendly){
				return ""+Utils.convertToUserFriendly(raw)+"/kb";
			}else return ""+raw+"/kb";
		}else{
			long raw=((Fee)fee).getRaw();

			if(userfriendly){
				return ""+Utils.convertToUserFriendly(raw);
			}else return ""+raw;
		}
	}

	
	/**
	 * Convert Fees to Map<String,String>
	 * @param userfriendly if true, fixed fees will be represented with doubles
	 * @return
	 */
	public Map<String,String> toMap(boolean userfriendly){
		Map<String,String> out=new HashMap<String,String>();	
		out.put("withdraw_fee",fromFeeToString(WITHDRAW,userfriendly));
		out.put("deposit_fee",fromFeeToString(DEPOSIT,userfriendly));
		out.put("transaction_fee",fromFeeToString(TRANSACTION,userfriendly));		
		out.put("blockchain_fee",fromFeeToString(BLOCKCHAIN_FEE,userfriendly));		
		return out;
	}
	
	/**
	 * Read Fees from Map<String,Object>
	 * @param userfriendly if true, fixed fees are expected to be represented with doubles
	 * @return
	 */
	public Fees fromMap(Map<String,Object> map,boolean userfriendly) {
		String fee;
		fee=map.get("withdraw_fee").toString();
		WITHDRAW=fromStringToFee(fee,userfriendly);
		fee=map.get("deposit_fee").toString();
		DEPOSIT=fromStringToFee(fee,userfriendly);
		fee=map.get("transaction_fee").toString();
		TRANSACTION=fromStringToFee(fee,userfriendly);	
		fee=map.get("blockchain_fee").toString();
		BLOCKCHAIN_FEE=fromStringToFee(fee,userfriendly);	
		return this;
	}
	
	
	
}
