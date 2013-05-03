package ru.hh.school.linker.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LinkServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkServer.class);
	
	private static ApplicationContext context;
	
    private int port = 8080;

    @Autowired
    private HttpLinkerPipelineFactory factory;
    
    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(factory);
        bootstrap.bind(new InetSocketAddress(port));
    }

    public static void main(String[] args) {
        
    	LOGGER.info("Service started");
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        LOGGER.info("Context initialized");
        
        final LinkServer server = context.getBean(LinkServer.class);
        if(args.length > 0){
        	server.port = Integer.parseInt(args[0]);
        }
        
        server.run();
    }
}
