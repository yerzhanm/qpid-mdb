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

import java.io.IOException;
import java.lang.reflect.Method;


import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import javax.jms.*;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

@SuppressWarnings("serial")
@WebServlet("/qpidServlet")
public class MessageServlet extends HttpServlet {
   
   @Resource(name="java:/QpidJMSXA")
   private ConnectionFactory _connectionFactory;
   
   @Resource(name="java:jboss/exported/TestQueue")
   private Destination _queue;
   
   private static final String DEFAULT_MESSAGE = "Hello, World!";
   private static final int DEFAULT_COUNT = 1;
   private static final boolean DEFAULT_TOPIC = false;
   private static final boolean DEFAULT_XA = false;
   private static final boolean DEFAULT_TX = false;
   
   private static final Logger _log = Logger.getLogger(MessageServlet.class);
   
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
       doPost(req, resp);
   }
   
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	   InitialContext ctx = null;
       Connection connection = null;
       Session session = null;
       MessageProducer messageProducer = null;
       UserTransaction ut = null;
       boolean useXA = false;
       boolean rollback = false;
       boolean useTX = false;

       try
       {
           String content = (req.getParameter("message") == null) ? DEFAULT_MESSAGE : req.getParameter("message");
           int count = (req.getParameter("count") == null) ? DEFAULT_COUNT : Integer.valueOf(req.getParameter("count"));
           useXA = (req.getParameter("useXA") == null) ? DEFAULT_XA : Boolean.valueOf(req.getParameter("useXA"));
           useTX = (req.getParameter("useTX") == null) ? DEFAULT_TX : Boolean.valueOf(req.getParameter("useTX"));
           ctx = new InitialContext();
           useTX = (req.getParameter("useTX") == null) ? DEFAULT_TOPIC : Boolean.valueOf(req.getParameter("DEFAULT_TX"));

           _log.debug("Environment: ");
           _log.debug("Message content: " + content);
           _log.debug("Message count:" + count);
           _log.debug("Protocol: " + "JMS");
           _log.debug("Destination Type: " + "Queue");
           _log.debug("Using XA: " + useXA);

           resp.getOutputStream().println("Environment: ");
           resp.getOutputStream().println("Message content: " + content);
           resp.getOutputStream().println("Message count:" + count);
           resp.getOutputStream().println("Protocol: " + "JMS");
           resp.getOutputStream().println("Destination Type: " + "Queue");
           resp.getOutputStream().println("Using XA: " + useXA);

           if(useXA)
           {
               ut = (UserTransaction)ctx.lookup("java:comp/UserTransaction");
               ut.begin();
           }

           connection = _connectionFactory.createConnection();
           session = connection.createSession(useTX, Session.AUTO_ACKNOWLEDGE);
           messageProducer =  session.createProducer(_queue);

           for(int i = 0; i < count; i++)
           {
               TextMessage message = session.createTextMessage(content);
               messageProducer.send(message);
           }


           resp.getOutputStream().println("Sent " + count + " messages with content '" + content + "'");
           resp.getOutputStream().flush();

       }
       catch(Exception e)
       {

           if(useXA && ut != null)
           {
               try
               {
                   rollback = true;
                   ut.setRollbackOnly();
               }
               catch(Exception ex)
               {
                   _log.error(ex.getMessage(), ex);
                   throw new ServletException(ex.getMessage(), ex);
               }
           }

           if(useTX)
           {
               try
               {
                   session.rollback();
               }
               catch(Exception ex)
               {
                   _log.error(ex.getMessage(), ex);
                   throw new ServletException(ex.getMessage(), ex);
               }
           }

           _log.error(e.getMessage(), e);
           throw new ServletException(e.getMessage(), e);
       }
       finally
       {
           if(useXA && ut != null)
           {
               try
               {
                   if(rollback)
                   {
                       ut.rollback();
                   }
                   else
                   {
                       ut.commit();
                   }
               }
               catch(Exception e)
               {
                   _log.error(e.getMessage(), e);
                   throw new ServletException(e.getMessage(), e);

               }
           }

           if(useTX && !useXA)
           {
               try
               {

                   if(rollback)
                   {
                       session.rollback();
                   }
                   else
                   {
                       session.commit();
                   }
               }
               catch(Exception e)
               {

                   _log.error(e.getMessage(), e);
                   throw new ServletException(e.getMessage(), e);
               }
           }

           closeResources(session, connection, ctx);
           
       }
   }
   
   public void closeResources(Object...objects)
   {
       try
       {
           for(Object object: objects)
           {
               Method close = object.getClass().getMethod("close", new Class[]{});
               close.invoke(object, new Object[]{});
           }
       }
       catch(Exception ignore)
       {
       }
   }

}
