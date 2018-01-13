package com.cruise.CruiseRoute.util;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.corecruise.cruise.SessionObject;
import com.corecruise.cruise.services.utils.Services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CruiseNodeList {
	private static HashMap<String, ServerHash> nodeList = new HashMap<String, ServerHash>();
	private static ArrayList<String> servers = new ArrayList<String>();

	public static void addNode(String serverName, CruiseNode inNode) {
		String plugins = inNode.getSupportedPlugins();
		if(nodeList.containsKey(plugins)) {
			nodeList.get(plugins).addNode(serverName, inNode);
		}else {
			nodeList.put(plugins, new ServerHash(serverName, inNode));
		}
		servers.add(plugins);
		servers.sort(null);
	}
	public static CruiseNode getCruiseNode(String pluginsRequested) {
		CruiseNode cn = null;
		ServerHash holdList = null;
		if(servers.contains(pluginsRequested)) {
			holdList = nodeList.get(pluginsRequested);
			int len = holdList.getServers().size();

			for(int i=0;i<len;i++) {
				cn = holdList.getNextServer();
				if(cn.isEnabled()) {
		            /**
		             * retry is a problem here, if the server is disable we are still including it in the count. 
		             * So this is bug that needs to be address - S.Chappell
		             */
					cn.setServerCount(len);
					break;
				}
			}
		}
		if(null == cn || cn.isEnabled() == false) {
			for(String s: servers) {
				if(null != cn) {
					break;
				}
				if(validate(s,pluginsRequested)) {
					holdList = nodeList.get(s);
					int len = holdList.getServers().size();
					
					for(int i=0;i<len;i++) {
						cn = holdList.getNextServer();
						if(cn.isEnabled()) {
				            /**
				             * retry is a problem here, if the server is disable we are still including it in the count. 
				             * So this is bug that needs to be address - S.Chappell
				             */
							cn.setServerCount(len);
							break;
						}
					}
				}
			}
		}
		return cn;
	}
	private static boolean validate(String serverList, String RequestList) {
		boolean ret = false;
		String[] reqList = RequestList.split(":");
		if(null != reqList && reqList.length>0) {
			for(String rl: reqList) {
				if(!serverList.contains(rl)) {
					ret = false;
					break;
				}else
				    ret = true;
			}
			
		}
		return ret;
	}
	public static void executeRemote(SessionObject sessionObject) {
		ArrayList<Services> al = sessionObject.getApplication().getServices();
		String[] array = new String[al.size()];
		int i=0;
		for(Services pmd: al) { 
		    array[i++] = pmd.PluginName();
		}
		array = Arrays.stream(array).distinct().toArray(String[]::new);
		Arrays.sort(array);
		String requested = String.join(":", array);
		boolean ok = false;
		int retry = 0;
		int retryCount = 5;
		while(ok==false && retry < retryCount) {
			CruiseNode cn = getCruiseNode(requested);
			if(null != cn) {
	            /**
	             * retry is a problem here, if the server is disable we are still including it in the count. 
	             * So this is a bug that needs to be address - S.Chappell
	             */
				retryCount = cn.getServerCount();
				
				sessionObject.getApplication().Parameter("byPass", "");
				try {
					String tmp =  cn.sendRequest("{ \"Application\":"+sessionObject.getCruiseMapper().writeValueAsString(sessionObject.getApplication())+"}");
					ObjectMapper mapper = new ObjectMapper();
					Object pin = mapper.readValue(tmp, Object.class);
					
					sessionObject.appendToResponse("CruiseRoute-"+cn.getName(), pin);
					ok = true;
					retry = retryCount;
				} catch (MalformedURLException e) {
					sessionObject.appendToResponse("CruiseRoute","("+retry+" of "+retryCount+" Attempts) Failed to call sever 100:"+cn.getServer()+":"+cn.getPort()+": "+e.getMessage());
					e.printStackTrace();
					cn.setEnabled(false);
				} catch (JsonProcessingException e) {
					sessionObject.appendToResponse("CruiseRoute","("+retry+" of "+retryCount+" Attempts) JSON Failed to call sever: 200"+cn.getServer()+":"+cn.getPort()+": "+e.getMessage());
					e.printStackTrace();
				} catch(Exception e) {
					sessionObject.appendToResponse("CruiseRoute","("+retry+" of "+retryCount+" Attempts) Unknown Failure to call sever: 300"+cn.getServer()+":"+cn.getPort()+": "+e.getMessage());
					e.printStackTrace();
				}
				++retry;
			}else {
				sessionObject.getApplication().Parameter("byPass", "");
				ok = true;
				retry = retryCount;
			}
			
		}
	}
}
