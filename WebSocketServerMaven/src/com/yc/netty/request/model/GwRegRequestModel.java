package com.yc.netty.request.model;

/**
 * 网关注册请求类
 * @author ycmac
 *
 */
public class GwRegRequestModel extends OptTypeModel {

	private String gwSn;

	public String getGwSn() {
		return gwSn;
	}

	public void setGwSn(String gwSn) {
		this.gwSn = gwSn;
	}
}
