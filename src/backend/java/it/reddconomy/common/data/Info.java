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

import it.reddconomy.common.fees.Fees;

public final class Info implements Data{

	
	public boolean testnet;
	public String coin_short;
	public String coin;
	public long welcome_tip;

	public String welcome_funds_wallid;
	public String generic_wallid;
	public String fees_collector_wallid;
	public String null_wallid;

	public Fees fees;

	public long uptime;
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Info-")
		.append("testnet=").append(testnet)
		.append("coin_short=").append(coin_short)
		.append("coin=").append(coin)
		.append("welcome_tip=").append(welcome_tip)
		.append("welcome_funds_wallid=").append(welcome_funds_wallid)
		.append("null_wallid=").append(null_wallid)

		.append("generic_wallid=").append(generic_wallid)
		.append("fees_collector_wallid=").append(fees_collector_wallid)
		.append("fees=").append(fees)	
		.append("uptime=").append(uptime)		

		;
		
		return sb.toString();
	}
	
	

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("testnet",testnet);
		out.put("coin_short",coin_short);
		out.put("coin",coin);
		out.put("welcome_tip",welcome_tip);
		out.put("welcome_funds_wallid",welcome_funds_wallid);
		out.put("generic_wallid",generic_wallid);
		out.put("null_wallid",null_wallid);
		out.put("fees_collector_wallid",fees_collector_wallid);
		out.put("fees",fees.toMap(false));
		out.put("uptime",uptime);
		return out;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fromMap(Map<String,Object> map) {
		testnet=(Boolean)map.get("testnet");
		coin_short=map.get("coin_short").toString();
		coin=map.get("coin").toString();
		welcome_tip=((Number)map.get("welcome_tip")).longValue();
		welcome_funds_wallid=map.get("welcome_funds_wallid").toString();
		generic_wallid=map.get("generic_wallid").toString();
		null_wallid=map.get("null_wallid").toString();
		fees_collector_wallid=map.get("fees_collector_wallid").toString();
		fees=new Fees().fromMap((Map<String,Object>)map.get("fees"),false);		
		uptime=((Number)map.get("uptime")).longValue();

	}
	
	@Override
	public boolean equals(Object w2o){
		if(!(w2o instanceof Info))return false;
		Info w2=(Info)w2o;
		return testnet==w2.testnet
				&& coin_short.equals(w2.coin_short)		
				&& coin.equals(w2.coin)		
				&& welcome_tip==w2.welcome_tip
				&& welcome_funds_wallid.equals(w2.welcome_funds_wallid)		
				&& generic_wallid.equals(w2.generic_wallid)		
				&& fees_collector_wallid.equals(w2.fees_collector_wallid)	
				&& fees.equals(w2.fees)
				&&null_wallid.equals(w2.null_wallid)
				
				;
	}
	
	
	
}
