package ru.hh.school.linker.bot.handler;

import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


public class BaseHandler extends SimpleChannelHandler {

	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	protected void logRequest(final HttpRequest request){
		LOGGER.info("Request URI:{}", request.getUri());
	}
	
	protected void logResponse(final HttpRequest request, final HttpResponseStatus status){
		LOGGER.info("Response URI:{}, status:{}", request != null ? request.getUri() : "null", status);
	}
	
	protected void sendResponse(final Channel ch, final String content, final HttpResponseStatus status, final HttpRequest request){
		logResponse(request, status);
		
		final HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		res.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
		res.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=" + CharsetUtil.UTF_8.name());
		final ChannelFuture future = ch.write(res);
		
		future.addListener(ChannelFutureListener.CLOSE);
		future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
	}
	
	protected String getSingleParamValue(final Map<String, List<String>> paramsMap, final String name) throws IllegalArgumentException{
		
		final List<String> params = paramsMap.get(name);
		
		if(params == null || params.isEmpty()) {
			throw new IllegalArgumentException("GET parameter {" + name + "} is required");
		}
		
		if(params.size() != 1) {
			throw new IllegalArgumentException("GET parameter {" + name + "} must be only one");
		}
		
		final String param = params.get(0);
		
		if(param == null || !StringUtils.hasText(param)){
			throw new IllegalArgumentException("GET parameter {" + name + "} must not be empty");
		}
		
		return param.trim();
	}
}
