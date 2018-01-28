package com.cruise.CruiseRoute.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	public boolean removeNode(CruiseNode node) {
		boolean ret = false;
		if(servers.containsKey(node.getName())) {
			servers.remove(node.getName());
			serverNames.remove(node.getName());
		    ret = true;
		}
		return ret;
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
	@JsonIgnore
	public ArrayList<CruiseNode> getServersArray(){
		ArrayList<CruiseNode> al = new ArrayList<CruiseNode>();
		Set<String> keys = servers.keySet();
		for(String key: keys) {
			al.add(servers.get(key));
		}
		return al;
	}
	@JsonIgnore
	public CruiseNode getNextServer() {
	   CruiseNode cn = null;
	   try {
		   String name = serverNames.get(ind.getAndAccumulate(serverNames.size(), (cur, n)->cur >= n-1 ? 0 : cur+1));
		   cn = servers.get(name);
	   }catch(Exception e) {
		   cn = null;
	   }
	   return cn;
	} 
	
}
