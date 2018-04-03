package com.ociweb.oe.greenlightning.api;

import com.ociweb.gl.api.ClientHostPortInstance;
import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.StartupListener;

public class HTTPGetBehaviorChained implements StartupListener {
	
	private GreenCommandChannel cmd;

    private ClientHostPortInstance session;
	
	public HTTPGetBehaviorChained(GreenRuntime runtime, ClientHostPortInstance session) {
		this.cmd = runtime.newCommandChannel(NET_REQUESTER);
		this.session = session;
	}

	@Override
	public void startup() {
		
		cmd.httpGet(session, "/testPageB");
		
	}

}
