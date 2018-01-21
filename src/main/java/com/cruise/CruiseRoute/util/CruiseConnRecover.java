package com.cruise.CruiseRoute.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CruiseConnRecover extends TimerTask {
	TimerTask timerTask = null;
	 Timer timer = null;
    @Override
    public void run() {
    	System.out.println("Looking for Dead Servers");
    	CruiseNode cn = null;
    	ArrayList<String> servers = CruiseNodeList.getServers();
    	HashMap<String, ServerHash> nodeList = CruiseNodeList.getNodeList();
    	ServerHash holdList;
    	if(null != servers) {
    		try {
    			for(String s: servers) {
    				if(null != cn) {
    					break;
    				}
    				holdList = nodeList.get(s);
    				int len = holdList.getServers().size();

    				for(int i=0;i<len;i++) {
    					cn = holdList.getNextServer();
    					if(!cn.isEnabled()) {
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
    							if(cn.getFailures()>1) {
    								if(holdList.removeNode(cn)) {
    									System.out.println("ERROR: Connection Recover failed. Removing Server after ("+cn.getFailures()+") attempts. "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    								}else {
       									System.out.println("ERROR: Connection Recover failed. REMOVING SERVER FAILED after ("+cn.getFailures()+") attempts. "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    								}
    							}else {
    								cn.setFailures(cn.getFailures()+1);
    								System.out.println("ERROR: Connection Recover failed for server ("+cn.getFailures()+") "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    							}
    							// TODO Auto-generated catch block
    							//e.printStackTrace();
    							//Clog.ErrorLog("Error", "999990", "Attempted to resestablish Connection to "+cn.name, e);
    							
    						} 
    						//break;
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
        timer.scheduleAtFixedRate(timerTask, 0, 1*60000);
        //System.out.println("TimerTask started");
        //cancel after sometime
    }


}