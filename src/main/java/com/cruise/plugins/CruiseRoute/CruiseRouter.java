package com.cruise.plugins.CruiseRoute;


import java.io.IOException;

import javax.script.ScriptEngineManager;

import com.corecruise.core.CoreCruise;
import com.corecruise.cruise.SessionObject;
import com.corecruise.cruise.config.CruisePluginEnvironment;
import com.corecruise.cruise.logging.Clog;
import com.corecruise.cruise.services.interfaces.PluginInterface;
import com.corecruise.cruise.services.utils.GenericSessionResp;
import com.corecruise.cruise.services.utils.Services;
import com.cruise.CruiseRoute.util.ConfigServer;
import com.cruise.CruiseRoute.util.ConfigServerArray;
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

	ScriptEngineManager sem = null;
	CruisePluginEnvironment config = null;
	String pluginName = "CruiseRouter";
	public CruiseRouter() {
		if(null == config)
			config = CoreCruise.getCruiseConfig(pluginName);
		
		int x = 0;
		
    	pmd = new PlugInMetaData(pluginName,"0.0.1","SJC","Cruise Service Discovery and Proxy");
    	
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
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("startInactive","false","true","If 'true' the server will be brought online, if false, it is placed in an inactive state and will be recovered on the next refresh."));

    	++x;
    	pmd.getActions().add(new Action("serverInfo", "returns all the information about attached servers."));
    	pmd.getActions().get(x).getActionParams().add(new ActionParameter("service","true","CruiseServer","Name of server instance"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("serverName","false","http://localhost","URL of CruiseServer"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("serverPort","false","8080","Port for server instances"));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("applicationName","false","AllApplications","Associated servers with an application or process."));
		pmd.getActions().get(x).getActionParams().add(new ActionParameter("startInactive","false","false","When set to 'true' an attempt will be made to recover any dead or new connections before returning system information."));

	
	   // init service

	
	}

	public PlugInMetaData getPlugInMetaData() {

		return pmd;
	}

	public void setPluginVendor(PlugInMetaData PMD) {
		pmd = PMD;
		
	}
	public boolean executePlugin(SessionObject so, Services service)  {
		boolean ret = false;
		String server = null;
		String sURL =  null;
		String sPort =  null;
		String inActive = null;
		CruiseNode cn =  null;
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
			server = service.Parameter("serverName");
			sURL = service.Parameter("serverURL");
			sPort = service.Parameter("serverPort");
			inActive = service.Parameter("startInactive");
			String plugins = service.Parameter("plugIns");
			ret = getConnection(gro, so,plugins,cn,server,sURL,sPort,inActive);
			break;
		case "serverinfo":
			server = service.Parameter("serverName");
			sURL = service.Parameter("serverURL");
			sPort = service.Parameter("serverPort");
			inActive = service.Parameter("startInactive");
			//CruiseNode cn = new CruiseNode(server, sURL, sPort);
			try {

				if(null == connRecover) {
					connRecover = new CruiseConnRecover();
					connRecover.init();
				}else {
					if(null != inActive && (inActive.equalsIgnoreCase("true"))) {
						connRecover.reset();
					}
				}
				CruiseNodeList.getJSON(so);
				ret = true;

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
    private boolean getConnection(GenericSessionResp gro, SessionObject so, String plugins, CruiseNode cn, String server,String sURL,String sPort,String sActive) {
    	boolean ret = false;

		String plugInFo =  null;
		PluginNames pin = null;
		try {
	    	//Prepare Connection**********************
			cn = new CruiseNode(server, sURL, sPort);
			//****************************************
			//If sActive = true, then try and get server information
			if(null == sActive || sActive.trim().length()<1 || sActive.equalsIgnoreCase("true")) {
				plugInFo = cn.init();
			}
			//if the server was not found, then just build a dummy connection. This assumes the server will eventually be live
			if(null == plugInFo) {
				if(null != plugins) {
					pin = new PluginNames();
					String[] p = plugins.split(":");
					for(String pKey: p) {
						pin.getPlugins().add(new Plugin(pKey));
					}
					cn.setPmda(pin);	
				}
				cn.setEnabled(false);
				CruiseNodeList.addNode(server, cn);
				if(null != so) {
					gro.addParmeter("Server NOT Loaded", server+":"+sURL);
					so.appendToResponse("addServer:"+server,gro);
				}
				ret = true;
			}else {
                //The server was found, so map the actuals
				ObjectMapper mapper = new ObjectMapper();
				pin = mapper.readValue(plugInFo, PluginNames.class);

				for(Plugin p: pin.getPlugins()){
					System.out.println(p.getPlugin());
				}
				cn.setPmda(pin);
				cn.setEnabled(true);
				CruiseNodeList.addNode(server, cn);
				if(null != so) {
					gro.addParmeter("Server Loaded", server+":"+sURL);
					so.appendToResponse("addServer:"+server,gro);
				}
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

    	return ret;
    }

	@Override
	public boolean initPlugin() {
		System.out.println(config.getPluginProperties());
		String routes = config.getPluginProperties().getProperty("routes");
		if(null != routes) {
			ConfigServerArray pin = null;
			ObjectMapper mapper = new ObjectMapper();
			try {
				pin = mapper.readValue("{\"routes\":"+routes+"}", ConfigServerArray.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(null != pin) {
				boolean ret = false;
				for(ConfigServer cs: pin.getRoutes()) {
					CruiseNode cn = null;
					ret = getConnection(null, null , null ,cn, cs.getServerName(),cs.getServerURL(),cs.getServerPort(),cs.getActive());
				}
			}
		}
		return false;
	}
}
