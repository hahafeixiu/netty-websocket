package com.yc.netty.response.model;

import com.yc.netty.request.model.OptTypeModel;

/**
 * websocket返回基础类
 * @author ycmac
 *
 */
public class ResponseModel extends OptTypeModel{

	private int retCode;

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}
}
