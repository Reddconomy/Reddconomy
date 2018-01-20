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

package reddconomy;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;




public class Utils{
	
	/**
	 * Convert value to internal representation (long)
	 * @param value
	 * @return
	 */
	public static long convertToInternal(double value){
		return (long)(value*100000000L);
	}
	
	/**
	 * Convert to user friendly (double) representation 
	 * @param value
	 * @return
	 */
	public static double convertToUserFriendly(long value){
		return (value/100000000.);
	}
	
	public static String hmac(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
		Mac sha256_HMAC=Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key=new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
		sha256_HMAC.init(secret_key);
		return new String(Base64.getEncoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))),"UTF-8");
	}	
}
