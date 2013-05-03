package ru.hh.school.linker.server;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class HttpLinkerPipelineFactory implements ChannelPipelineFactory {

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private DispatcherChannelHandler dispatcher;
	
	@Autowired
	private HttpLinkerHandler handler;
	
	public ChannelPipeline getPipeline() throws Exception {
		
		final ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("dispatcher", dispatcher);
		pipeline.addLast("handler", handler);
		pipeline.addLast("encoder", new HttpResponseEncoder());
		return pipeline;
	}
}
