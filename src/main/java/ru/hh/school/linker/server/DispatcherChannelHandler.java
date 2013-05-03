package ru.hh.school.linker.server;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.hh.school.linker.bot.handler.BaseHandler;
import ru.hh.school.linker.config.Configuration;

@Component
public class DispatcherChannelHandler extends BaseHandler {

	@Autowired
	private Configuration config;
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)	throws Exception {
		
		final HttpRequest request = (HttpRequest)e.getMessage();
		final String uri = request.getUri();
		LOGGER.debug("Recieved request:{}", uri);
		
		final QueryStringDecoder qdecoder = new QueryStringDecoder(uri);
		
		if(!config.getPaths().contains(qdecoder.getPath())){
			sendResponse(e.getChannel(), "404 Not Found", HttpResponseStatus.NOT_FOUND, request);
			return;
		}
		
		ctx.sendUpstream(e);
	}	
}
