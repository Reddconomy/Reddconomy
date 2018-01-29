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
package it.reddconomy.common.data;

import java.util.HashMap;
import java.util.Map;

public final class Deposit implements Data{

	public static final class Status{
		public static final byte pending=1;
		public static final byte expired=-1;
		public static final byte complete=0;
	}
	

	/**
	 * Blockchain address where we expect the coins to be sent
	 */
	public String addr;
	
	/**
	 * OffchainWallet that will receive the coins
	 */
	public String receiver_wallet_id; 
	
	/**
	 * How many coins
	 */
	public long expected_balance;
	
	
	public byte status;
	
	/**
	 * Creation timestamp
	 */
	public long created=System.currentTimeMillis();
	
	
	/**
	 * Ms before expiration, if the deposit action is not completed before this value becomes <=0, the status will be updated to 'expired', 
	 * and the deposit cancelled.
	 */
	public int expiring=172800000;

	public long paid_in_fees=0;
	
	
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Deposit-").append(addr).append(": receiver_wallet_id=").append(receiver_wallet_id)
		.append(" expected_balance=").append(expected_balance)
		.append(" status=").append(status)
		.append(" created=").append(created)
		.append(" expiring=").append(expiring)
		.append(" paid_in_fees=").append(paid_in_fees);

		return sb.toString();
	}
	

	@Override
	public boolean equals(Object d2o){
		if(!(d2o instanceof Deposit))return false;
		Deposit d2=(Deposit)d2o;
		return addr.equals(d2.addr)&&
				receiver_wallet_id.equals(d2.receiver_wallet_id)
				&&expected_balance==d2.expected_balance
				&&status==d2.status
				&&created==d2.created
				&&expiring==d2.expiring&&
				paid_in_fees==d2.paid_in_fees;
	}
	

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("addr",addr);
		out.put("receiver_wallet_id",receiver_wallet_id);
		out.put("expected_balance",expected_balance);
		out.put("status",status);
		out.put("created",created);
		out.put("expiring",expiring);
		out.put("paid_in_fees",paid_in_fees);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		addr=map.get("addr").toString();
		receiver_wallet_id=map.get("receiver_wallet_id").toString();
		expected_balance=((Number)map.get("expected_balance")).longValue();
		status=((Number)map.get("status")).byteValue();
		created=((Number)map.get("created")).longValue();
		expiring=((Number)map.get("expiring")).intValue();
		paid_in_fees=((Number)map.get("paid_in_fees")).longValue();
	}
}
