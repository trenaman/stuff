package com.fusesource.amqtx;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Hello world!
 *
 */
public class Consumer 
{
    public static void main( String[] args ) throws Exception
    {
        ConnectionFactory cf = new ActiveMQConnectionFactory("scott", "tiger", "tcp://localhost:61616");
        
        Connection jms = cf.createConnection(); 
        jms.start();
        
        Session s = jms.createSession(true, Session.AUTO_ACKNOWLEDGE);
        Destination foo = s.createQueue("foo");
        MessageConsumer c = s.createConsumer(foo);
        
        int i = 0;
        boolean done = false;
        while (!done) { 
          TextMessage msg = (TextMessage) c.receive(1000);
          if (msg == null) { 
            done = true ;
          } else { 
            System.out.println(msg.getText());
          }
        } 
        
        s.close();
        jms.stop();  
        jms.close();
 
    }
}
