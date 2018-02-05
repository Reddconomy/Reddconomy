/*
 * Copyright (c) 2018, Simone Cervino.
 * 
 * This file is part of Reddconomy-sponge.

    Reddconomy-sponge is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Reddconomy-sponge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Reddconomy-sponge.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.reddconomy.plugin.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;

import it.reddconomy.Utils;

public class Qr{
	public static String createQR(String addr, String coin, long amount) throws WriterException {
		double damount = Utils.convertToUserFriendly(amount);
		Map<EncodeHintType,Object> hint=new HashMap<EncodeHintType,Object>();
		com.google.zxing.qrcode.encoder.QRCode code=Encoder.encode(coin!=null&&!coin.isEmpty()?coin+":"+addr+"?amount="+damount:""+damount,ErrorCorrectionLevel.L,hint);
		ByteMatrix matrix=code.getMatrix();
		System.out.println(matrix.getWidth()+"x"+matrix.getHeight());
		StringBuilder qr=new StringBuilder();
		for(int y=0;y<matrix.getHeight();y++){
			for(int x=0;x<matrix.getWidth();x++){
				if(matrix.get(x,y)==0) qr.append("\u00A7f\u2588");
				else qr.append("\u00A70\u2588");
			}
			qr.append("\n");
		}
		return qr.toString();
	}
	
	public static String createQRLink(String API_LINK, String addr, String coin, long amount) {
		double damount = Utils.convertToUserFriendly(amount);
		return API_LINK.replace("{PAYDATA}", (coin!=null&&!coin.isEmpty()?coin+":"+addr+"?amount="+damount:""+damount));
	}
}
