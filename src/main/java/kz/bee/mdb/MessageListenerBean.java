/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package kz.bee.mdb;

import java.util.Date;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

@MessageDriven(activationConfig = {
	    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
	    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	    @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/TestQueue"),
	    @ActivationConfigProperty(propertyName = "connectionURL", propertyValue = "amqp://guest:guest@localhost/?brokerlist='tcp://localhost:5672?sasl_mechs='PLAIN''"),
	    @ActivationConfigProperty(propertyName = "useLocalTx", propertyValue = "false"),
	    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
	})
	public class MessageListenerBean implements MessageListener
	{
	    private static final Logger _log = Logger.getLogger(MessageListenerBean.class);

	    @Override
	    public void onMessage(Message message)
	    {
	        try
	        {
	                if(message instanceof TextMessage)
	                {
	                    String content = ((TextMessage)message).getText();

	                    _log.info("Received text message with contents: [" + content + "] at " + new Date());
	                }
	            }
	            catch(Exception e)
	            {
	                _log.error(e.getMessage(), e);
	            }

	    }

	}