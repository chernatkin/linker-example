package ru.hh.school.linker.bot.httpclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import ru.hh.school.linker.bot.handler.BaseHandler;
import ru.hh.school.linker.config.Configuration;

@Component
public class HttpClientHandler extends BaseHandler {
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	private static final String WWW_PREFIX = "www.";
	
	private static final Pattern REF_PATTERN = Pattern.compile("<a[\\s]+href=\"([^\">]*)\"");
	
	@Autowired
	private Configuration config;

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		
		final HttpResponse response = (HttpResponse) e.getMessage();
		final Node currentNode = (Node)e.getChannel().getAttachment();
		LOGGER.debug("Recieved page: {}, status {}", currentNode.getUrl(), response.getStatus());
		
		final HttpResponseStatus status = response.getStatus();
		final int childDepth = currentNode.getDepth() + 1;
		final boolean depthLimitExceed = childDepth > currentNode.getHolder().getDepth();
		
		if(status.equals(HttpResponseStatus.OK)){
			ChannelBuffer buf = response.getContent();
			writeToFS(buf, currentNode.getUrl());
			
			if(depthLimitExceed){ 
				closeChannel(e);
				return; 
			}
			
			Matcher m = REF_PATTERN.matcher(buf.toString(CharsetUtil.UTF_8));
			
			while(m.find()){
				String ref = m.group(1);
				if(!StringUtils.hasText(ref)){ continue; }
				ref = ref.replace(" ", "").toLowerCase();
				
				final URL refURL = new URL(currentNode.getUrl(), ref);
				if(!refURL.getProtocol().equals("http")){
					continue;
				}
				currentNode.getHolder().addNode(refURL, childDepth);
			}
		}
		else if(!depthLimitExceed && (status.equals(HttpResponseStatus.MOVED_PERMANENTLY) 
				|| status.equals(HttpResponseStatus.FOUND) 
				|| status.equals(HttpResponseStatus.SEE_OTHER) 
				|| status.equals(HttpResponseStatus.USE_PROXY) 
				|| status.equals(HttpResponseStatus.TEMPORARY_REDIRECT))){
			
			currentNode.getHolder().addNode(new URL(currentNode.getUrl(), response.getHeader(HttpHeaders.Names.LOCATION)), childDepth);
		}
		
		closeChannel(e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		LOGGER.warn("Http client exception on page " + e.getChannel().getAttachment(), e.getCause());
		closeChannel(e);
	}
	
	private void closeChannel(ChannelEvent e){
		final Channel ch = e.getChannel();
		if(ch.isOpen()) { ch.close(); }
	}
	
	private void writeToFS(final ChannelBuffer buf, final URL currentURL) throws IOException, URISyntaxException, NoSuchAlgorithmException{
		final int port = currentURL.getPort() == -1 ? currentURL.getDefaultPort() : currentURL.getPort();
		final String host = currentURL.getHost().startsWith(WWW_PREFIX) ? currentURL.getHost().replace(WWW_PREFIX, "") : currentURL.getHost();
		
		final File hostDir = new File(config.getStoragePath(), host + '_' + port);
		if(!hostDir.exists()){ hostDir.mkdir(); }
		
		final String uri = getURI(currentURL);
		final MessageDigest md = MessageDigest.getInstance("SHA-512");
		
		final String fileName = DatatypeConverter.printHexBinary(md.digest(uri.getBytes(CharsetUtil.UTF_8))) + ".html";
		final File htmlFile = new File(hostDir, fileName);
		
		try(FileOutputStream fos = new FileOutputStream(htmlFile); FileChannel fch = fos.getChannel()){
			
			final FileLock lock = fch.tryLock();
			if(lock == null) { throw new IOException("File {" + htmlFile.getAbsolutePath() + "} locked by another program."); }
			
			fch.write(ChannelBuffers.copiedBuffer(uri + "\n", CharsetUtil.UTF_8).toByteBuffer());
			fch.write(buf.toByteBuffer());
			fch.force(true);
			
			lock.release();
		}
	}
	
	private String getURI(final URL url){
		final StringBuilder uri = new StringBuilder(url.getPath());
		if(!StringUtils.hasText(url.getQuery())){
			return uri.toString();
		}
		
		final Map<String, List<String>> params = new QueryStringDecoder(url.toString()).getParameters();
		
		final String[] keys = params.keySet().toArray(EMPTY_STRING_ARRAY);
		Arrays.sort(keys);
		
		uri.append('?');
		
		for(String key : keys){
			final List<String> valuesList = params.get(key);
			if(CollectionUtils.isEmpty(valuesList)){ 
				uri.append(key).append('=').append('&');
				continue;
			}
			
			final String[] values = valuesList.toArray(EMPTY_STRING_ARRAY);
			Arrays.sort(values);
			
			for(String value : values){
				uri.append(key).append('=').append(value).append('&');
			}
		}
		
		return uri.deleteCharAt(uri.length() - 1).toString();
	}
}
