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
package reddconomy.data;

import java.util.HashMap;
import java.util.Map;

public final class OffchainContract implements Data{
	
	public static final class TransactionDirection{
		public static final Object ACCEPTEDBY2CREATEDBY=new Object();
		public static final Object CREATEDBY2ACCEPTEDBY=new Object();
		public static final Object STILL=new Object();

		public static Object get(OffchainContract ctr){
			if(ctr.amount==0) return STILL;
			else if(ctr.amount>0)return ACCEPTEDBY2CREATEDBY;
			else return CREATEDBY2ACCEPTEDBY;			
		}
	}
	
	/**
	 * The id of the contract
	 */
	public String id;
	
	/**
	 * The value of the contract
	 * >0 acceptedby pays createdby
	 * <0 createdby pays acceptedby
	 * =0 do nothing
	 */
	public long amount;
	
	/**
	 * Timestamp of the creation
	 */
	public long created=System.currentTimeMillis();
	
	/**
	 * Timestamp of the acception, -1= never
	 */
	public long accepted=-1;
	
	/**
	 * Who created
	 */
	public String createdby;
	
	/**
	 * Who accepted
	 */
	public String acceptedby="";
	
	
	public long paid_in_fees=0;
	
	
	
	@Override
	public boolean equals(Object contract2o){
		if(!(contract2o instanceof OffchainContract))return false;
		OffchainContract contract2=(OffchainContract)contract2o;
		return id.equals(contract2.id)&&
				amount==contract2.amount&&created==contract2.created&&accepted==contract2.accepted&&
				createdby.equals(contract2.createdby)&&
				acceptedby.equals(contract2.acceptedby)&&paid_in_fees==contract2.paid_in_fees;
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Contract-").append(id).append(": amount=").append(amount)
		.append(" created=").append(created)
		.append(" accepted=").append(accepted)
		.append(" createdby=").append(createdby)
		.append(" acceptedby=").append(acceptedby)
		.append(" paid_in_fees=").append(paid_in_fees)

;
		return sb.toString();
	}
	
	

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("id",id);
		out.put("amount",amount);
		out.put("created",created);
		out.put("accepted",accepted);
		out.put("createdby",createdby);
		out.put("acceptedby",acceptedby);
		out.put("paid_in_fees",paid_in_fees);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		id=map.get("id").toString();
		amount=((Number)map.get("amount")).longValue();
		created=((Number)map.get("created")).longValue();
		accepted=((Number)map.get("accepted")).longValue();
		createdby=map.get("createdby").toString();
		acceptedby=map.get("acceptedby").toString();	
		paid_in_fees=((Number)map.get("paid_in_fees")).longValue();

	}

	
}
