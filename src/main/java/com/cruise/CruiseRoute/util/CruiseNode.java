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

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	long hitCount = 0;
	//long successCount = 0;
	long failures = 0;
	ArrayList<String> plugins = new ArrayList<String>();
	long[] usage;
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
	@JsonIgnore
	public String[] getPmda() {
		return pmda;
	}
    public void updateUsage(String[] servicesCalled) {
    	try {
    		for(String key: servicesCalled) {
    			++usage[plugins.indexOf(key)];
    		}
    		++hitCount;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	public void setPmda(PluginNames pin) {
		//Set<String> hnames = new HashSet<String>();
		//for(PlugInMetaData pmd: pmda.getPlugins()) {
		//	hnames.add(pmd.getName());
		//}
		//String[] array = hnames.toArray(new String[0]);
		
		pmda = new String[pin.getPlugins().size()];
		usage = new long[pmda.length];
		//String[] array = pmda.getPlugins().getPlugins();//new String[pmda.getPlugins().size()];
		int i=0;
        for(Plugin p: pin.getPlugins()){
            pmda[i] = p.getPlugin();
            plugins.add(p.getPlugin());
            usage[i] = 0;
            ++i;
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
	@JsonIgnore
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
	@JsonIgnore
	public ArrayList<String> getPlugins() {
		return plugins;
	}
	public ArrayList<plugInUsage> getPluginInfo(){
		
		ArrayList<plugInUsage> pi = new ArrayList<plugInUsage>();
		if(null != pmda) {
			int x = pmda.length;
			for(int i=0;i<x;i++) {
				pi.add(new plugInUsage(pmda[i],usage[i]));
			}
		}
		return pi;
	}
	public void setPlugins(ArrayList<String> plugins) {
		this.plugins = plugins;
	}
	public String init() throws MalformedURLException, IOException{
		String ret = null;
		try {
		ret = sendRequest(getInfo);
		}catch(Exception e) {
			System.out.println("Connection not established:"+e.getMessage());
		}
		return ret;
	}
	
	public Integer getServerCount() {
		return serverCount;
	}

	public void setServerCount(Integer serverCount) {
		this.serverCount = serverCount;
	}
    @JsonIgnore
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
			}
			if(status > 306) {
				this.setEnabled(false);
			}else {
				//++successCount;
				this.setEnabled(true);
			}
			
		}
		return content.toString();
	}
	public long getHitCount() {
		return hitCount;
	}
	public void setHitCount(long hitCount) {
		this.hitCount = hitCount;
	}
	public long getFailures() {
		return failures;
	}
	public void setFailures(long failedConnections) {
		this.failures = failedConnections;
	}

    class plugInUsage{
    	String Plugin;
    	long Usage;
    	public plugInUsage(String pluginName, long usageCount) {
    		Plugin = pluginName;
    		Usage = usageCount;
    	}
		public String getPlugin() {
			return Plugin;
		}
		public void setPlugin(String plugin) {
			Plugin = plugin;
		}
		public long getUsage() {
			return Usage;
		}
		public void setUsage(long usage) {
			Usage = usage;
		}
    	
    }
}
