package com.ociweb.oe.greenlightning.api;

import com.ociweb.gl.api.ClientHostPortInstance;
import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.HTTPRequestService;
import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.gl.api.Payloadable;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.PubSubService;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.pronghorn.pipe.ChannelReader;
import com.ociweb.pronghorn.pipe.StructuredReader;
import com.ociweb.pronghorn.util.Appendables;

public class HTTPGetBehaviorSingle implements StartupListener, HTTPResponseListener, PubSubListener {
	
	private ClientHostPortInstance session;
	private final HTTPRequestService clientService;
	private final PubSubService pubSubService;

	int countDown = 4000;
	long reqTime = 0;
	
	public HTTPGetBehaviorSingle(GreenRuntime runtime, ClientHostPortInstance session) {
		this.session = session;
		GreenCommandChannel cmd = runtime.newCommandChannel();
		clientService = cmd.newHTTPClientService();
		pubSubService = cmd.newPubSubService();
		
	}


	@Override
	public void startup() {
		pubSubService.publishTopic("next");
	}

	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {

		reader.openPayloadData( (r)-> {
			StructuredReader s = r.structured();
			int value1 = s.readInt(Fields.AGE);
			String value2 = s.readText(Fields.NAME);
			
			//System.out.println(value1+"  "+value2);
		});
		
		pubSubService.publishTopic("next");
		
		return true;
	}


	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		if (--countDown<=0) {
			clientService.httpGet(session, "/shutdown?key=shutdown");
			pubSubService.publishTopic("shutdown");
		}
		
		reqTime = System.nanoTime();
		return clientService.httpGet(session, "/testPageB");

	}

}
