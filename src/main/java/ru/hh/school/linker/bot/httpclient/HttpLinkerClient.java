package ru.hh.school.linker.bot.httpclient;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HttpLinkerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpLinkerClient.class);
	
	@Autowired
	private HttpClientPipelinedFactory factory;
	
	private final ClientBootstrap bootstrap;
	
	public HttpLinkerClient(){
		bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
	}

	@PostConstruct
	protected void postConstruct(){
		bootstrap.setPipelineFactory(factory);
	}
	
    public void get(final URL url, final Node node){
    	
    	LOGGER.debug("Connecting to {}", url);
    	
        final ChannelFuture future = bootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() : url.getPort()));
        future.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				if(!future.isSuccess()){
					LOGGER.debug("Connecting to {} failed. Cause {}", url, future.getCause());
					return;
				}
				
				final Channel ch = future.getChannel();
		        ch.setAttachment(node);
		        
		        final HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.toString());
		        request.setHeader(HttpHeaders.Names.HOST, url.getHost());
		        request.setHeader(HttpHeaders.Names.ACCEPT_CHARSET, CharsetUtil.UTF_8.name());
		        
		        ChannelFuture chFuture = future.getChannel().write(request);
		       	chFuture.addListener(CLOSE_ON_FAILURE);
			}
		});
        future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }
}
