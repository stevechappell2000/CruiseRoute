package com.cruise.plugins.CruiseRoute;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.corecruise.cruise.SessionObject;
import com.corecruise.cruise.logging.Clog;
import com.corecruise.cruise.services.interfaces.PluginInterface;
import com.corecruise.cruise.services.utils.GenericSessionResp;
import com.corecruise.cruise.services.utils.Services;
import com.cruise.CruiseRoute.util.CruiseConnRecover;
import com.cruise.CruiseRoute.util.CruiseNode;
import com.cruise.CruiseRoute.util.CruiseNodeList;
import com.cruise.CruiseRoute.util.Plugin;
import com.cruise.CruiseRoute.util.PluginNames;
import com.cruise.plugins.Action;
import com.cruise.plugins.ActionParameter;
import com.cruise.plugins.PlugInMetaData;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;




public class CruiseRouter implements PluginInterface{

	PlugInMetaData pmd = null;
	String QUEUE_NAME = null;
	CruiseConnRecover connRecover = null;
	public CruiseRouter() {
		//config = new CruiseProducerConfig();
		//config.initConfig();
		int x = 0;
		
    	pmd = new PlugInMetaData("CruiseRouter","0.0.1","SJC","Cruise Service Discovery and Proxy");
    	
    	pmd.getActions().add(new Action("plugInInfo", "getPlugin Information"));
    	pmd.getActions().get(x).getActionParams().add(new ActionParameter("None","false","unknown","Unused Parameter"));
    	
    	++x;
    	pmd.getActions().add(new Action("Echo", "Test API Call"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("Sample","false","unknown","Unused Parameter"));
		
    	++x;
    	pmd.getActions().add(new Action("addServer", "Inserts a new Cruise instance into the pool"));
    	pmd.getActions().get(x).getActionParams().add(new ActionParameter("serverName","true","CruiseServer","Name of server instance"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("serverURL","true","http://localhost","URL of CruiseServer"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("serverPort","true","8080","Port for server instances"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("applicationName","true","AllApplications","Associated servers with an application or process."));
		
	}

	public PlugInMetaData getPlugInMetaData() {
		// TODO Auto-generated method stub
		return pmd;
	}

	public void setPluginVendor(PlugInMetaData PMD) {
		pmd = PMD;
		
	}
	public boolean executePlugin(SessionObject so, Services service)  {
		boolean ret = false;
		String action = service.Action().trim().toLowerCase();
		GenericSessionResp gro = new GenericSessionResp();
		QUEUE_NAME = service.Parameter("QName");
		switch (action) {
		case "plugininfo":
			so.appendToResponse(pmd);
			ret = true;
			break;
		case "echo":
			gro.addParmeter("PluginEnabled", "true");
			so.appendToResponse("CruiseTest",gro);
			ret = true;
			break;
		case "addserver":
			String server = service.Parameter("serverName");
			String sURL = service.Parameter("serverURL");
			String sPort = service.Parameter("serverPort");
			CruiseNode cn = new CruiseNode(server, sURL, sPort);
			try {
				String plugInFo = cn.init();
				if(null == plugInFo) {
					cn.setEnabled(false);
				}else {

					ObjectMapper mapper = new ObjectMapper();
					PluginNames pin = mapper.readValue(plugInFo, PluginNames.class);

					for(Plugin p: pin.getPlugins()){
						System.out.println(p.getPlugin());
					}
					cn.setPmda(pin);
					cn.setEnabled(true);
					CruiseNodeList.addNode(server, cn);
					gro.addParmeter("Server Loaded", server+":"+sURL);
					so.appendToResponse("addServer:"+server,gro);
					ret = true;
				}
				if(null == connRecover) {
					connRecover = new CruiseConnRecover();
					connRecover.init();
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				Clog.Error(so, "service", "0000001", server+" failed to load:"+e.getMessage());
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				Clog.Error(so, "service", "0000002", server+" failed to load:"+e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Clog.Error(so, "service", "0000003", server+" failed to load:"+e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Clog.Error(so, "service", "0000004", server+" failed to load:"+e.getMessage());
			}
			break;
		default:
			Clog.Error(so, "service", "100.05", "Invalid Action supplied:"+action);
		}


		return ret;
	}

	public void byPass(SessionObject sessionObject) {
        CruiseNodeList.executeRemote(sessionObject);
	}

}
