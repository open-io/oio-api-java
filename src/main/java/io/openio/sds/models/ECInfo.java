package io.openio.sds.models;

import java.util.Map;

import io.openio.sds.common.OioConstants;
import io.openio.sds.common.Strings;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class ECInfo {

	private static final String ALGO_KEY = "algo";
	private static final String K_KEY = "k";
	private static final String M_KEY = "m";
	
	private final String algo;
	private final int k;
	private final int m;
	
	private ECInfo(String algo, int k, int m){
		this.algo = algo;
		this.k = k;
		this.m = m;
	}
	
	public static ECInfo fromString(String ecstr){
		if(!ecstr.startsWith(OioConstants.EC_PREFIX))
			return null;		
		Map<String, String> params = Strings.toMap(ecstr.substring(3), ",", "=");
		return new ECInfo(params.get(ALGO_KEY),
				Integer.valueOf(params.get(K_KEY)),
				Integer.valueOf(params.get(M_KEY)));
	}
	
	public String algo(){
		return algo;
	}
	
	public int k(){
		return k;
	}
	
	public int m(){
		return m;
	}
	
}
