package reddconomy.data;

import java.util.Map;

public interface Data{
	public boolean equals(Object obj);
	public String toString();
	public Map<String,Object> toMap();
	public void fromMap(Map<String,Object> map);
	
	
}
