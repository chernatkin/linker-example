package ru.hh.school.linker.bot.httpclient;

import java.net.URL;


public class Node {

	private final int depth; 
	
	private final URL url;

	private final LinkHolder holder;
	
	public Node(final int depth, final URL url, final LinkHolder holder) {
		this.depth = depth;
		this.url = url;
		this.holder = holder;
	}

	public int getDepth() {
		return depth;
	}

	public URL getUrl() {
		return url;
	}
	
	public LinkHolder getHolder() {
		return holder;
	}

	@Override
	public int hashCode() {		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Node [depth=" + depth + ", url=" + url + "]";
	}	
}
