package ru.hh.school.linker.bot;

import java.net.URL;

public interface BotService {

	public int fetchSitesGraph(URL initUrl, int depth);
	
}
