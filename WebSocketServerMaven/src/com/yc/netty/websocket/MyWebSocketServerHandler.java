package com.yc.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import com.google.gson.Gson;
import com.yc.netty.common.Global;
import com.yc.netty.request.model.OptTypeModel;
import com.yc.netty.response.model.ResponseModel;

public class MyWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {


	private WebSocketServerHandshaker handshaker;

	/**
	 * 连接开启
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		Global.group.add(ctx.channel());
		System.out.println("客户端与服务端连接开启");

	}

	/**
	 * 连接关闭
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		Global.group.remove(ctx.channel());
		System.out.println("客户端与服务端连接关闭");
	}

	/**
	 * 接收消息
	 */
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, ((FullHttpRequest) msg));
		} else if (msg instanceof WebSocketFrame) {
			handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
		}

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	/**
	 * 消息处理
	 * @param ctx
	 * @param frame
	 */
	private void handlerWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame) {

		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),
					(CloseWebSocketFrame) frame.retain());
			return;
		}

		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(
					new PongWebSocketFrame(frame.content().retain()));
			return;
		}

		if (frame instanceof BinaryWebSocketFrame) {
			System.out.println("-----------------");
		}
		if (!(frame instanceof TextWebSocketFrame)) {

			System.out.println("本例程仅支持文本消息，不支持二进制消息");

			throw new UnsupportedOperationException(String.format(
					"%s frame types not supported", frame.getClass().getName()));
		}

		String request = ((TextWebSocketFrame) frame).text();

		Gson gsonStr = new Gson();
//				.excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
//		        .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
//		        .serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")//时间转化为特定格式  
//		        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)//会把字段首字母大写,注:对于实体上使用了@SerializedName注解的不会生效.
//		        .setPrettyPrinting() //对json结果格式化.
//		        .setVersion(1.0)    //有的字段不是一开始就有的,会随着版本的升级添加进来,那么在进行序列化和返序列化的时候就会根据版本号来选择是否要序列化.
//		        					//@Since(版本号)能完美地实现这个功能.还的字段可能,随着版本的升级而删除,那么
//		        					//@Until(版本号)也能实现这个功能,GsonBuilder.setVersion(double)方法需要调用.
//		        .create();
		OptTypeModel optTypeModel = gsonStr.fromJson(request, OptTypeModel.class);
		int optType = optTypeModel.getOptType();
		
		ResponseModel responseHeart = new ResponseModel();
		responseHeart.setRetCode(0);
		responseHeart.setOptType(optType);
		String result = gsonStr.toJson(responseHeart);
//		int start = request.indexOf("optType");
//		int optType = Integer.valueOf(request.substring(start + 9, start + 10));
//		String result = '{' + request.substring(start - 1, start + 11) + ","
//				+ '"' + "retCode" + '"' + ':' + '0' + '}';
		TextWebSocketFrame tws = new TextWebSocketFrame(result);

		Global.group.writeAndFlush(tws);

	}

	private void handleHttpRequest(ChannelHandlerContext ctx,
			FullHttpRequest req) {

		if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {

			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));

			return;
		}

		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				"ws://192.168.55.21:7397/websocket", null, false);

		handshaker = wsFactory.newHandshaker(req);

		if (handshaker == null) {
			WebSocketServerHandshakerFactory
					.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}

	}

	private static void sendHttpResponse(ChannelHandlerContext ctx,
			FullHttpRequest req, DefaultFullHttpResponse res) {

		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),
					CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}

		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private static boolean isKeepAlive(FullHttpRequest req) {
		return false;
	}

	/**
	 * 异常关闭
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {

		cause.printStackTrace();
		ctx.close();

	}

}
