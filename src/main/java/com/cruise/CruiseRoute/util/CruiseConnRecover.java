package com.cruise.CruiseRoute.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.corecruise.cruise.logging.Clog;

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
    							cn.init();
    						} catch (Exception e) {
    							// TODO Auto-generated catch block
    							//e.printStackTrace();
    							//Clog.ErrorLog("Error", "999990", "Attempted to resestablish Connection to "+cn.name, e);
    							System.out.println("ERROR: Connection Recover failed for server "+cn.getName()+":"+cn.getServer()+":"+cn.getPort());
    						} 
    						break;
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