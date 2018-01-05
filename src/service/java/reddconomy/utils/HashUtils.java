package reddconomy.utils;
/**
 * Copyright (c) 2015, Riccardo Balbo
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


import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;




public class HashUtils{
	public static String hmac(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
		Mac sha256_HMAC=Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key=new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
		sha256_HMAC.init(secret_key);

		return new String(Base64.getEncoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))),"UTF-8");
	}
	

	/**
	 * @deprecated Replaced with HashUtils.hash(File file,"MD5");
	 */
	@Deprecated
	public static String md5File(File f) throws Exception {
		return hash(f,"MD5");
	}

	/**
	 * @deprecated Replaced with HashUtils.hash(String mex,"MD5");
	 */
	@Deprecated
	public static String md5(String mex) throws Exception {
		return hash(mex,"MD5");
	}

	public static String hash(String mex) {
		return hash(mex,"SHA-512");
	}

	public static String hash(File f) throws Exception {
		return hash(f,"SHA-512");
	}

	public static String hash(String mex, String algo) {
		try{
			MessageDigest md=MessageDigest.getInstance(algo);
			md.update(mex.getBytes());
			byte[] mb=md.digest();
			String out="";
			for(int i=0;i<mb.length;i++){
				byte temp=mb[i];
				String s=Integer.toHexString(new Byte(temp));
				while(s.length()<2)
					s="0"+s;
				s=s.substring(s.length()-2);
				out+=s;
			}
			return out;
		}catch(Exception e){
			return mex;
		}
	}

	public static String hash(File f, String algo) throws Exception {
		MessageDigest md=MessageDigest.getInstance(algo);
		FileInputStream is=new FileInputStream(f);
		md.reset();
		byte[] bytes=new byte[2048];
		int numBytes;
		while((numBytes=is.read(bytes))!=-1)
			md.update(bytes,0,numBytes);
		byte[] mb=md.digest();
		is.close();
		String out="";
		for(int i=0;i<mb.length;i++){
			byte temp=mb[i];
			String s=Integer.toHexString(new Byte(temp));
			while(s.length()<2)
				s="0"+s;
			s=s.substring(s.length()-2);
			out+=s;
		}
		return out;

	}

	public static void main(String[] _a) throws Exception {
		char password[]=new char[]{'j','m','o','n','k','e','y','_','e','n','g','i','n','e'};
		char salt[]=new char[]{123,211,1,23,45,66,54,2,99,32,59,77,11,9};
		for(int i=0;i<password.length;i++)password[i]=(char)(password[i]^salt[i]);
		System.out.println("Salt: "+new String(salt));
		System.out.println("password^salt: "+new String(password));
		
		MessageDigest md=MessageDigest.getInstance("MD5");
		md.update(new String(password).getBytes());
		byte[] mb=md.digest();
		String out="";
		for(int i=0;i<mb.length;i++){
			byte temp=mb[i];
			String s=Integer.toHexString(new Byte(temp));
			while(s.length()<2)s="0"+s;
			s=s.substring(s.length()-2);
			out+=s;
		}
		
		System.out.println("MD5 Hash: "+out);
		
		md=MessageDigest.getInstance("SHA-512");
		md.update(new String(password).getBytes());
		mb=md.digest();
		out="";
		for(int i=0;i<mb.length;i++){
			byte temp=mb[i];
			String s=Integer.toHexString(new Byte(temp));
			while(s.length()<2)s="0"+s;
			s=s.substring(s.length()-2);
			out+=s;
		}
		
		System.out.println("SHA 512 Hash: "+out);
	}
}
