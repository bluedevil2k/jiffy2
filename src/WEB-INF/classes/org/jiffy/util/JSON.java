package org.jiffy.util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;

public class JSON extends HashMap<String, Object> 
{
	@Override
	public String toString()
	{
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		for (Iterator<String> iter = keySet().iterator(); iter.hasNext();)
		{
			String s = iter.next();
			node.put(s, get(s).toString());
		}
		return node.toString();
	}
	
	public static String toString(Object o) throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(o);
	}
	
	public static JSON toJSON(String text) throws Exception
	{
		JSON json = new JSON();
		if (text.indexOf("\"") == -1)
		{
			text = text.replace("{", "{\"");
			text = text.replace("=", "\":\"");
			text = text.replace(", ", "\", \"");
			text = text.replace("}", "\"}");
		}
		ObjectMapper mapper = new ObjectMapper();
		LinkedHashMap<String, Object> r = mapper.readValue(text, new TypeReference<Map<String, Object>>() {});
		for (Iterator<String> iter=r.keySet().iterator(); iter.hasNext();)
		{
			String s = iter.next();
			String value = r.get(s).toString();
			value = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.name());
			json.put(s, value);
		}
		return json;
	}
	
}
