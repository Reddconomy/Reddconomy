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

public final class Withdraw implements Data{
	
	
	public static final class Status{
		public static final byte pending=1;
		public static final byte expired=-1;
		public static final byte complete=0;
	}
	

	/**
	 * Blockchain address where to send coins
	 */
	public String from_wallet;

	/**
	 * How many coins
	 */
	public long amount;
	
	public long paid_in_fees=0;
	
		
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Withdraw-").append(from_wallet).append(": ammount=").append(amount)		
		.append(" paid_in_fees=").append(paid_in_fees);
		return sb.toString();
	}
	
	

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("from_wallet",from_wallet);
		out.put("amount",amount);
		out.put("paid_in_fees",paid_in_fees);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		from_wallet=map.get("from_wallet").toString();
		amount=((Number)map.get("amount")).longValue();
		paid_in_fees=((Number)map.get("paid_in_fees")).longValue();

	}
	
	@Override
	public boolean equals(Object w2o){
		if(!(w2o instanceof Withdraw))return false;
		Withdraw w2=(Withdraw)w2o;
		return from_wallet.equals(w2.from_wallet)&&amount==w2.amount&&paid_in_fees==w2.paid_in_fees;
	}
	
	
	
}
