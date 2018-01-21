package com.cruise.CruiseRoute.util;

import java.util.ArrayList;
import java.util.List;
//JsonRootName("Plugins")
public class PluginNames {
	
	String Runtime = "";
	
    private List<Plugin> plugins = new ArrayList<Plugin>();

	public List<Plugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<Plugin> plugins) {
		this.plugins = plugins;
	}

	public String getRuntime() {
		return Runtime;
	}

	public void setRuntime(String runtime) {
		Runtime = runtime;
	}
    //public 

}
