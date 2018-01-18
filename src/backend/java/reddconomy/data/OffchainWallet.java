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

public final class OffchainWallet implements Data{
	

	
	public static final class Status{
		public static final byte active=1;
		public static final byte deactive=0;

	}
	public String id;
	public long balance=0;
	public byte status=Status.active;
	
	@Override
	public boolean equals(Object wallet2o){
		if(!(wallet2o instanceof OffchainWallet))return false;
		OffchainWallet wallet2=(OffchainWallet)wallet2o;
		return id.equals(wallet2.id)&&balance==wallet2.balance&&status==wallet2.status;
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("Wallet-").append(id).append(": balance=").append(balance).append(" status=").append(status);
		return sb.toString();
	}

	@Override
	public Map<String,Object> toMap() {
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("id",id);
		out.put("balance",balance);
		out.put("status",status);
		return out;
	}

	@Override
	public void fromMap(Map<String,Object> map) {
		id=map.get("id").toString();
		balance=((Number)map.get("balance")).longValue();
		status=((Number)map.get("status")).byteValue();
	}
}
