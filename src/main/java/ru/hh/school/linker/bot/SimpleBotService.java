package ru.hh.school.linker.bot;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.hh.school.linker.bot.httpclient.HttpLinkerClient;
import ru.hh.school.linker.bot.httpclient.LinkHolder;
import ru.hh.school.linker.bot.httpclient.Node;
import ru.hh.school.linker.config.Configuration;

@Component
public class SimpleBotService implements BotService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBotService.class);
	
	@Autowired
	private HttpLinkerClient httpClient;
	
	@Autowired
	private Configuration config;
	
	public int fetchSitesGraph(final URL initUrl, final int depth) {
		
		final LinkHolder holder = new LinkHolder(config.getMaxBotTimeout(), depth); 
		
		final File rootDir = new File(config.getStoragePath());
		if(!rootDir.exists()) { rootDir.mkdir(); }
		
		holder.addNode(initUrl, 0);
		while(!holder.getQueue().isEmpty() || !holder.hasProcessingURLs()){
			
			try {
				final Node node = holder.getQueue().poll(1000, TimeUnit.MILLISECONDS);
				if(node == null || !holder.visited(node.getUrl())) { continue; }
				
				holder.processing(node.getUrl());
				httpClient.get(node.getUrl(), node);
			} catch (InterruptedException e) {
				LOGGER.warn("Current thread was interrupted. {}", Thread.currentThread());
				break;
			}
		}
		
		return holder.visitedTotal();
	}

}
