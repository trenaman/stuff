package com.fusesource.amqtx;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Hello world!
 *
 */
public class Producer 
{
    public static void main( String[] args ) throws Exception
    {
        ConnectionFactory cf = new ActiveMQConnectionFactory("scott", "tiger", "tcp://localhost:61616");
        
        Connection jms = cf.createConnection(); 
        jms.start();
        
        Session s = jms.createSession(true, Session.AUTO_ACKNOWLEDGE);
        Destination foo = s.createQueue("foo");
        MessageProducer p = s.createProducer(foo);
        
        for (int i = 0; i < 7; i++) { 
          TextMessage msg = s.createTextMessage("" + i);
          p.send(msg);
        }
       // s.commit();
        
        s.close();
        jms.stop();  
        jms.close();
 
    }
}
