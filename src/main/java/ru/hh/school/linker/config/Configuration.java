package ru.hh.school.linker.config;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class Configuration {

	private int maxDepth;
	
	private Set<String> paths;
	
	private int maxBotTimeout;
	
	private String storagePath;

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public Set<String> getPaths() {
		return paths;
	}

	public void setPaths(Set<String> paths) {
		this.paths = paths;
	}

	public int getMaxBotTimeout() {
		return maxBotTimeout;
	}

	public void setMaxBotTimeout(int maxBotTimeout) {
		this.maxBotTimeout = maxBotTimeout;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
}
