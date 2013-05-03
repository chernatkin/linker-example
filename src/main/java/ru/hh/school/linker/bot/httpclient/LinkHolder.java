package ru.hh.school.linker.bot.httpclient;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class LinkHolder {

	private final int depth; 
	
	private final int timeout;
	
	private final Set<URL> visited =  Collections.synchronizedSet(new HashSet<URL>());
	
	private final BlockingDeque<Node> queue = new LinkedBlockingDeque<Node>();
	
	private volatile long lastProcessed = System.currentTimeMillis();
	
	public LinkHolder(final int timeout, final int depth) {
		this.timeout = timeout;
		this.depth = depth;
	}

	public BlockingDeque<Node> getQueue(){
		return queue;
	}
	
	public Node addNode(final URL url, final int depth){
		final Node node = new Node(depth, url, this);
		queue.add(node);
		return node;
	}
	
	public boolean visited(URL url){
		return visited.add(url);
	}
	
	public boolean isVisited(URL url){
		return visited.contains(url);
	}
	
	public int visitedTotal(){
		return visited.size();
	}
	
	public void processing(URL url){
		lastProcessed = System.currentTimeMillis();
	}
	
	public boolean hasProcessingURLs(){
		return (System.currentTimeMillis() - lastProcessed) > timeout;
	}

	public int getDepth() {
		return depth;
	}
}
