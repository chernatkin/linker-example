package ru.hh.school.linker.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.hh.school.linker.bot.BotService;
import ru.hh.school.linker.bot.handler.BaseHandler;
import ru.hh.school.linker.config.Configuration;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HttpLinkerHandler extends BaseHandler {
	
	private static final String URL_PARAM_NAME = "url";
	
	private static final String DEPTH_PARAM_NAME = "depth";
	
	@Autowired
	private Configuration config;
	
	@Autowired
	private BotService service;
	
	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		
		final HttpRequest request = (HttpRequest)e.getMessage();
		ctx.setAttachment(request);
		
        final Map<String, List<String>> params = new QueryStringDecoder(request.getUri()).getParameters();
        
        try{
        	final URL url = parseURL(params);
        	final int depth = parseDepth(params);
        
		    if(depth > config.getMaxDepth() || depth < 0){
		    	throw new IllegalArgumentException("depth must be integer from 0 to " + (config.getMaxDepth() - 1));
		    }

		    final int sitesQuantity = service.fetchSitesGraph(url, depth);
		    sendResponse(e.getChannel(), Integer.toString(sitesQuantity), HttpResponseStatus.OK, request);
        } catch(MalformedURLException|IllegalArgumentException nfe){
        	LOGGER.info("Input parameters not valid.", nfe);
        	sendResponse(e.getChannel(), "url must be correct full http url path, depth must be integer from 0 to " + config.getMaxDepth(), HttpResponseStatus.BAD_REQUEST, request);
        	return;
        }
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,  final ExceptionEvent e) throws Exception {
		LOGGER.error("Unhandled exception", e.getCause());
		sendResponse(e.getChannel(), "500 Internal error", HttpResponseStatus.INTERNAL_SERVER_ERROR, (HttpRequest)ctx.getAttachment());
	}

	private URL parseURL(Map<String, List<String>> params) throws MalformedURLException{
		final URL url = new URL(getSingleParamValue(params, URL_PARAM_NAME));
		if(!url.getProtocol().equals("http")){
			throw new IllegalArgumentException("Only http protocol supported");
		}
		return url;
	}
	
	private int parseDepth(Map<String, List<String>> params) throws NumberFormatException{
		return Integer.parseInt(getSingleParamValue(params, DEPTH_PARAM_NAME));
	}
}
