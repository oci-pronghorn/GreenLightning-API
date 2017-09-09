package com.ociweb.oe.greenlightning.api;

import com.ociweb.gl.api.MQTTBridge;
import com.ociweb.gl.api.MQTTQoS;
import com.ociweb.gl.api.GreenApp;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.Builder;
import com.ociweb.oe.greenlightning.api.behaviors.EgressBehavior;
import com.ociweb.oe.greenlightning.api.behaviors.IngressBehavior;
import com.ociweb.oe.greenlightning.api.behaviors.TimeBehavior;

public class MQTTClient implements GreenApp {
	private MQTTBridge mqttConfig;
	
	//install mosquitto - replace 127.0.0.1 if using a different broker
	//
	//to monitor call >    mosquitto_sub -v -t '#' -h 127.0.0.1
	//to test call >       mosquitto_pub -h 127.0.0.1 -t 'external/topic/ingress' -m 'hello'

	@Override
	public void declareConfiguration(Builder builder) {
		final String brokerHost = "127.0.0.1";
		//final String brokerHost = "172.16.10.28"; // Nathan's PC
		//final String brokerHost = "thejoveexpress.local"; // Raspberry Pi0
		// Create a single mqtt client
		mqttConfig = builder.useMQTT(brokerHost, 1883, false, "MQTTClientTest",200) //default of 10 in flight
							.cleanSession(true)	
							.keepAliveSeconds(10)
							.will(false, MQTTQoS.atMostOnce, "last/will", blobWriter -> {blobWriter.writeBoolean(true);});

		// Timer rate
		builder.setTimerPulseRate(300); 
		
		builder.enableTelemetry();
	}

	@Override
	public void declareBehavior(final GreenRuntime runtime) {
		// The external/internal topic translation is not necessary.
		// The bridge calls may be made with one topic specified
		final String internalEgressTopic = "internal/topic/egress";
		final String externalEgressTopic = "external/topic/egress";
		final String internalIngressTopic = "internal/topic/ingress";
		final String externalIngressTopic = "external/topic/ingress";
		final String localTestTopic = "localtest";

		final MQTTQoS transQos = MQTTQoS.atLeastOnce;
		final MQTTQoS subscribeQos = MQTTQoS.atLeastOnce;

		// Inject the timer that publishes topic/egress
		TimeBehavior internalEgressTopicProducer = new TimeBehavior(runtime, internalEgressTopic);
		runtime.addTimePulseListener(internalEgressTopicProducer);
		// Convert the internal topic/egress to external for mqtt
		runtime.bridgeTransmission(internalEgressTopic, externalEgressTopic, mqttConfig).setQoS(transQos);
;
		// Subscribe to MQTT topic/ingress (created by mosquitto_pub example in comment above)
		runtime.bridgeSubscription(internalIngressTopic, externalIngressTopic, mqttConfig).setQoS(subscribeQos);
		// Listen to internal/topic/ingress and publish localtest
		IngressBehavior mqttBrokerListener = new IngressBehavior(runtime, localTestTopic);
		runtime.registerListener(mqttBrokerListener)
				.addSubscription(internalIngressTopic, mqttBrokerListener::receiveMqttMessage);

		// Inject the listener for "localtest"
		EgressBehavior doTheBusiness = new EgressBehavior();
		runtime.registerListener(doTheBusiness)
				.addSubscription(localTestTopic, doTheBusiness::receiveTestTopic);
	}
}