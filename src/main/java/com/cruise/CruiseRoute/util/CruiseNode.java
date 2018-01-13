package com.cruise.CruiseRoute.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import javax.net.ssl.HttpsURLConnection;
import com.corecruise.cruise.SessionObject;
import com.cruise.CruiseRoute.util.*;

public class CruiseNode {
	String name = null;
	boolean enabled = false;
	String server = null;
	String port = null;
	String user = "admin";
	String pass = "admin";
	String SupportedPlugins = "unknown";
	Integer serverCount = 0;
	String[] pmda;
	ArrayList<String> plugins = new ArrayList<String>();
	String getInfo = 
			"{"+
					"\"Application\" : {"+
					"    \"parameters\" : {"+
					"      \"name\" : \"Sample Web Application\","+
					"      \"id\" : \"sampleid\""+
					"    },"+
					"    \"credentials\" : {"+
					"      \"parameters\" : {"+
					"        \"password\" : \""+user+"\","+
					"        \"username\" : \""+pass+"\""+
					"      }"+
					"    },"+
					"    \"services\" : ["+
					"       {\"parameters\" : {"+
					"           \"pluginName\" : \"CruiseCorePlugin\","+
					"           \"service\" : \"GetInfoService\","+
					"           \"action\" : \"activePluginNames\""+
					"		 }"+
					"		}"+
					"	]"+
					"	}"+
					"}";
	public CruiseNode() {

	}
	
	public String[] getPmda() {
		return pmda;
	}

	public void setPmda(PluginNames pin) {
		//Set<String> hnames = new HashSet<String>();
		//for(PlugInMetaData pmd: pmda.getPlugins()) {
		//	hnames.add(pmd.getName());
		//}
		//String[] array = hnames.toArray(new String[0]);
		
		pmda = new String[pin.getPlugins().size()];
		//String[] array = pmda.getPlugins().getPlugins();//new String[pmda.getPlugins().size()];
		int i=0;
        for(Plugin p: pin.getPlugins()){
            pmda[i++] = p.getPlugin();
        }
		Arrays.sort(pmda);
		SupportedPlugins = String.join(":", pmda);
	}

	public String getSupportedPlugins() {
		return SupportedPlugins;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public CruiseNode(String sName, String sURL, String sPort) {
		name = sName;
		server = sURL;
		port = sPort;
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public ArrayList<String> getPlugins() {
		return plugins;
	}
	public void setPlugins(ArrayList<String> plugins) {
		this.plugins = plugins;
	}
	public String init(SessionObject so) throws MalformedURLException, IOException{
		String ret = null;
		ret = sendRequest(getInfo);
		return ret;
	}
	
	public Integer getServerCount() {
		return serverCount;
	}

	public void setServerCount(Integer serverCount) {
		this.serverCount = serverCount;
	}

	public void setSupportedPlugins(String supportedPlugins) {
		SupportedPlugins = supportedPlugins;
	}

	public String sendRequest(String app) throws MalformedURLException, IOException{
		StringBuffer content = null;
		if(null == server || null == port) {

		}else {

			int status = 0;
			BufferedReader in = null;
			DataOutputStream out = null;
			String inputLine;
			if(server.trim().toLowerCase().startsWith("http:")) {
				URL url = new URL(server+":"+port+"/Cruise");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				con.setRequestMethod("POST");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");


				con.setRequestProperty("Content-Type", "application/json");
				con.setConnectTimeout(5000);
				con.setReadTimeout(5000);

				con.setDoOutput(true);
				out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(app);
				out.flush();
				out.close();

				status = con.getResponseCode();
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));

				content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				con.disconnect();
			}else if(server.trim().toLowerCase().startsWith("https:")) {
				URL url = new URL(server+":"+port+"/Cruise");
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestProperty("Content-Type", "application/json");
				con.setConnectTimeout(5000);
				con.setReadTimeout(5000);

				con.setRequestMethod("POST");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				con.setDoOutput(true);
				out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(app);
				out.flush();
				out.close();

				status = con.getResponseCode();
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));

				content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				con.disconnect();
			}
		}
		return content.toString();
	}


}
