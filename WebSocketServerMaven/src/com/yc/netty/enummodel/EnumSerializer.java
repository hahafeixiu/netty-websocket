package com.yc.netty.enummodel;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EnumSerializer implements JsonSerializer<OptTypeEnum>,
		JsonDeserializer<OptTypeEnum> {

	// 对象转为Json时调用,实现JsonSerializer<PackageState>接口
	public JsonElement serialize(OptTypeEnum state, Type arg1, JsonSerializationContext arg2) {
		return new JsonPrimitive(state.ordinal());
	}

	// json转为对象时调用,实现JsonDeserializer<PackageState>接口
	public OptTypeEnum deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		if (json.getAsInt() < OptTypeEnum.values().length)
			return OptTypeEnum.values()[json.getAsInt()];
		return null;
	}

}

