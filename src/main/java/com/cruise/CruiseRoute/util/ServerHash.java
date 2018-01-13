package com.cruise.CruiseRoute.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerHash {
	HashMap<String,CruiseNode> servers = new HashMap<String,CruiseNode>();
	ArrayList<String> serverNames = new ArrayList<String>();
	private static AtomicInteger ind = new AtomicInteger(0);
	
	public ServerHash(String name, CruiseNode node) {
		servers.put(name, node);
		serverNames.add(name);
	}
	public void addNode(String name, CruiseNode node) {
		if(!servers.containsKey(name)) {
			servers.put(name, node);
			serverNames.add(name);
		}
	}
	public HashMap<String, CruiseNode> getServers() {
		return servers;
	}
	public void setServers(HashMap<String, CruiseNode> servers) {
		this.servers = servers;
	}
	public AtomicInteger getInd() {
		return ind;
	}
	public void setInd(AtomicInteger ind) {
		ServerHash.ind = ind;
	}
	public CruiseNode getNextServer() {
	   String name = serverNames.get(ind.getAndAccumulate(serverNames.size(), (cur, n)->cur >= n-1 ? 0 : cur+1));
	   return servers.get(name);
	} 
	
}
