package ru.hh.school.linker.bot.httpclient;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HttpClientPipelinedFactory implements ChannelPipelineFactory {
	
	@Autowired
	private HttpClientHandler handler;
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		
        final ChannelPipeline pipeline = pipeline();
        pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(1*1024*1024));
		pipeline.addLast("handler", handler);
        return pipeline;
	}

}
