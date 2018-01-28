package com.cruise.CruiseRoute.util;

import java.util.ArrayList;
import java.util.List;

public class ConfigServerArray {
    List<ConfigServer> routes = new ArrayList<ConfigServer>();
    
    public ConfigServerArray() {
    	
    }

	public List<ConfigServer> getRoutes() {
		return routes;
	}

	public void setRoutes(List<ConfigServer> routes) {
		this.routes = routes;
	}


     
}
