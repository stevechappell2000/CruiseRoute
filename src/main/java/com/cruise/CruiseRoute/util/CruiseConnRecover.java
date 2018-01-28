package com.cruise.CruiseRoute.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.corecruise.core.CoreCruise;
import com.corecruise.cruise.config.CruisePluginEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CruiseConnRecover extends TimerTask {
	TimerTask timerTask = null;
	Timer timer = null;
	Integer retries = 5;
	Integer delay = 5;
    @Override
    public void run() {
    	CruisePluginEnvironment  c = CoreCruise.getCruiseConfig("CruiseRouter");
    	if(null != c) {
	    	if(null != c.getPluginProperties().getProperty("numberOfRetries")) {
	    		try {
	    			retries = new Integer(c.getPluginProperties().getProperty("numberOfRetries")).intValue();
	    		}catch(Exception e) {
	    			retries = 5;
	    		}
	    	}
	    	if(null != c.getPluginProperties().getProperty("retryDelayMinutes")) {
	    		try {
	    			delay = new Integer(c.getPluginProperties().getProperty("retryDelayMinutes")).intValue();
	    		}catch(Exception e) {
	    			delay = 5;
	    		}	
	    	}	
    	}
    		
    		
        reset();
    }
    public void reset() {
    	//System.out.println("Looking for Dead Servers");
    	CruiseNode cn = null;
    	ArrayList<String> servers = CruiseNodeList.getServers();
    	HashMap<String, ServerHash> nodeList = CruiseNodeList.getNodeList();
    	ServerHash holdList;
    	if(null != servers) {
    		try {
    			Set<String> keys = nodeList.keySet();
    			for(String s: keys) {
    				holdList = nodeList.get(s);
    				Set<String> nkeys = holdList.getServers().keySet();
    				for(String nkey: nkeys) {
    					cn = holdList.getServers().get(nkey);
    					if(null != cn && (cn.isEnabled()==false)) {
    						System.out.println("trying to restart "+cn.getName());
    						try {
    							String plugInFo = cn.init();
    							
    							ObjectMapper mapper = new ObjectMapper();
    							PluginNames pin = mapper.readValue(plugInFo, PluginNames.class);

    							for(Plugin p: pin.getPlugins()){
    								System.out.println(p.getPlugin());
    							}
    							cn.setPmda(pin);
    							cn.setEnabled(true);
    							System.out.println("Connection recovered for "+cn.getName());
    							
    						} catch (Exception e) {
    							cn.setFailures(cn.getFailures()+1);
    							if(cn.getFailures()>retries) {
    								if(holdList.removeNode(cn)) {
    									System.out.println("ERROR: Connection Recover failed. Removing Server after ("+cn.getFailures()+") attempts. "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    								}else {
       									System.out.println("ERROR: Connection Recover failed. REMOVING SERVER FAILED after ("+cn.getFailures()+") attempts. "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    								}
    							}else {
    								System.out.println("ERROR: Connection Recover failed for server ("+cn.getFailures()+") "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    							}
    							
    						} 
    					}
    				}

    			}
    		}catch(Exception e) {
    			e.printStackTrace();

    		}
    	}

    }
    public void killIt() {
        timer.cancel();
        System.out.println("TimerTask cancelled");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void init(){
        timerTask = new CruiseConnRecover();
        //running timer task as daemon thread
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, delay*60000);
        //System.out.println("TimerTask started");
        //cancel after sometime
    }


}