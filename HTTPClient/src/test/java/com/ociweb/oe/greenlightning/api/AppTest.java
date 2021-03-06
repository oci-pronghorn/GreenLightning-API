package com.ociweb.oe.greenlightning.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.oe.greenlightning.api.server.HTTPServer;

/**
 * Unit test for simple App.
 */
public class AppTest { 
	
	 private static final String host = "127.0.0.1";
	
	 @Test
	 public void testApp() {
				

		 	final boolean telemetry = false;
		    final StringBuilder result = new StringBuilder();
		    final long timeoutMS = 10_000*60;
		
		    GreenRuntime.run(new HTTPServer(host, result));

		    //System.out.println("starting up client");
	   	    
	   	    //this test will hit the above server until it calls shutdown.
		    boolean cleanExit =  GreenRuntime.testConcurrentUntilShutdownRequested(new HTTPClient(telemetry), timeoutMS);
		
		    assertTrue(cleanExit);
		    
	 }
	 


}
