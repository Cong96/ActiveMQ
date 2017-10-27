title:ActiveMQ与Spring结合使用

date:2017年10月20日11:34:40

---

Spring框架作为现在Java应用中毫无疑问的霸主，自然也提供了对JMS消息服务处理的支持。Spring框架将我们之前单纯使用Java SE API来调用ActiveMQ消息服务器的一些过程给封装起来了，暴露一些接口并预先定义好的一些实现类,让用户能够轻松的进行配置使用。spring-jms

#### 一.Spring环境搭建

创建Maven工程，引入相关pom文件。



```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.wangcc</groupId>
  <artifactId>ActiveMQ</artifactId>
  <packaging>war</packaging>
  <version>0.0.1-SNAPSHOT</version>
  <name>ActiveMQ Maven Webapp</name>
  <url>http://maven.apache.org</url>
    <properties>  
     
        <!-- log4j日志文件管理包版本 -->  
        <slf4j.version>1.7.7</slf4j.version>  
         <log4j.version>1.2.17</log4j.version>  
        <spring.version>4.1.6.RELEASE</spring.version>  

    </properties>  
  <dependencies>
     <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <scope>test</scope>
    </dependency>
     <!-- spring核心包 -->  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-core</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
     <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-jms</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-web</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-oxm</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-tx</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-jdbc</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-webmvc</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-aop</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
  
        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-context-support</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  

        <dependency>  
            <groupId>org.springframework</groupId>  
            <artifactId>spring-test</artifactId>  
            <version>${spring.version}</version>  
        </dependency>  
       <dependency>
        <groupId>org.apache.activemq</groupId>
        <artifactId>activemq-core</artifactId>
        <version>5.7.0</version>
    </dependency>
           <!-- log start -->  
        <dependency>  
            <groupId>log4j</groupId>  
            <artifactId>log4j</artifactId>  
            <version>${log4j.version}</version>  
        </dependency>  
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.16.8</version>
    </dependency>       
          
        <!-- 格式化对象，方便输出日志 -->  
        <dependency>  
            <groupId>com.alibaba</groupId>  
            <artifactId>fastjson</artifactId>  
            <version>1.1.41</version>  
        </dependency>  
  
  
        <dependency>  
            <groupId>org.slf4j</groupId>  
            <artifactId>slf4j-api</artifactId>  
            <version>${slf4j.version}</version>  
        </dependency>  
  
        <dependency>  
            <groupId>org.slf4j</groupId>  
            <artifactId>slf4j-log4j12</artifactId>  
            <version>${slf4j.version}</version>  
        </dependency> 
  </dependencies>
  <build>
    <finalName>ActiveMQ</finalName>
  </build>
</project>

```

之前，我们讲过了JMS编程模型，现在我们就需要将这些配置到Spring中，让Spring来集中管理。

- ConnectionFactory配置

  ConnectionFactory是用于产生到JMS服务器的链接，Spring为我们提供了多个ConnectionFactory，有SingleConnectionFactory和CachingConnectionFactory。SingleConnectionFactory对于建立JMS服务器链接的请求会一直返回同一个链接，并且会忽略Connection的close方法调用。CachingConnectionFactory继承了SingleConnectionFactory，所以它拥有SingleConnectionFactory的所有功能，同时它还新增了缓存功能，它可以缓存Session、MessageProducer和MessageConsumer。我们先使用下SingleConnectionFactory来配置。

  ```java
  <bean id="connectionFactory"
  		class="org.springframework.jms.connection.SingleConnectionFactory">
  		<property name="targetConnectionFactory" ref="amqConnectionFactory"></property>
  		<property name="sessionCacheSize" value="10" />
  ```

  注意这里面有个属性targetConnectionFacotry，因为上述配置的ConnectionFactory只是Spring用于管理ConnectionFactory的，真正产生到JMS服务器链接的ConnectionFactory还得是由JMS  providor提供，并且需要把它注入到Spring提供的ConnectionFactory中。这里很明显我们的JMS providor是ActiveMQ。

  ```java
  	<bean id="amqConnectionFactory" 
  		class="org.apache.activemq.ActiveMQConnectionFactory">
  		<property name="brokerURL">
  			<value>${brokerURL}</value>
  		</property>
  		<property name="userName">
  			<value>${userName}</value>
  		</property>
  		<property name="password">
  			<value>${password}</value>
  		</property>
  		
  		</bean>
  ```

    ActiveMQ为我们提供了一个PooledConnectionFactory，通过往里面注入一个ActiveMQConnectionFactory可以用来将Connection、Session和MessageProducer池化，这样可以大大的减少我们的资源消耗。当使用PooledConnectionFactory时，我们在定义一个ConnectionFactory时应该是如下定义：

  ```java
  bean id="pooledConnectionFactory" class="org.apache.activemq.pool.PooledConnectionFactory">  
      <property name="connectionFactory" ref="amqConnectionFactory"/>  
      <property name="maxConnections" value="10"/>  
  </bean>  
    
  <bean id="connectionFactory" class="org.springframework.jms.connection.SingleConnectionFactory">  
      <property name="targetConnectionFactory" ref="pooledConnectionFactory"/>  
  </bean>  
  ```



- 配置生产者

  ​	由于Spring已经把Session会话完全封装起来了，我们就不需要自己配置Session了，我们接下来就可以直接配置生产者了。生产者负责产生消息并发送到JMS服务器，这通常对应的是我们的一个业务逻辑服务实现类。但是我们的服务实现类是怎么进行消息的发送的呢？对于生产者的配置，Spring给我们提供了一个类：JmsTemplate，就像给Hibernate提供HibernateTemplate一样，Spring总会给我们提供XXXTeplate这样的模板工具类。，所以配置生产者其实最核心的就是配置进行消息发送的JmsTemplate。对于消息发送者而言，它在发送消息的时候要知道自己该往哪里发，为此，我们在定义JmsTemplate的时候需要往里面注入一个Spring提供的ConnectionFactory对象。

  ```
  	<!-- jmsTemplate  all system must-->
  	<bean id="jmsTemplate"
  		class="org.springframework.jms.core.JmsTemplate">
  		        <!-- 这个connectionFactory对应的是我们定义的Spring提供的那个ConnectionFactory对象 -->  
  		
  		<property name="connectionFactory">
  			<ref bean="connectionFactory" />
  		</property>
  		<property name="deliveryPersistent">
  			<value>true</value>
  		</property>
  		<property name="messageConverter" ref="objectMessageConverter"></property>
  	</bean>
  	
  ```

  ​

​       在真正利用JmsTemplate进行消息发送的时候，我们需要知道消息发送的目的地，即destination。在Jms中有一个用来表示目的地的Destination接口，它里面没有任何方法定义，只是用来做一个标识而已。当我们在使用JmsTemplate进行消息发送时没有指定destination的时候将使用默认的Destination。默认Destination可以通过在定义jmsTemplate bean对象时通过属性defaultDestination或defaultDestinationName来进行注入，defaultDestinationName对应的就是一个普通字符串。在调用send(MessageCreator messageCreator)方法时将使用这个属性

```
	@Override
	public void send(MessageCreator messageCreator) throws JmsException {
		Destination defaultDestination = getDefaultDestination();
		if (defaultDestination != null) {
			send(defaultDestination, messageCreator);
		}
		else {
			send(getRequiredDefaultDestinationName(), messageCreator);
		}
	}
```



在ActiveMQ中实现了两种类型的Destination，一个是点对点的ActiveMQQueue，另一个就是支持订阅/发布模式的ActiveMQTopic。在定义这两种类型的Destination时我们都可以通过一个name属性来进行构造。

```
	<!-- queue-->
	<bean id="queue"
		class="org.apache.activemq.command.ActiveMQQueue">
		<constructor-arg index="0">
			<value>${um_organ_queue}</value>
		</constructor-arg>
	</bean>
	
	<!-- topic-->
	<bean id="topic"
		class="org.apache.activemq.command.ActiveMQTopic">
		<constructor-arg index="0">
			<value>${app_organ_topic}</value>
		</constructor-arg>
	</bean>
```



我们把生产者发送消息这一个过程封装在我们自己实现的Service中，我们写一个ProducerService，里面有一个向Destination发送纯文本消息的方法sendMessage

```java
package com.wangcc.activemq.spring.service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

/**
 * @ClassName: ProducerService
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author wangcc
 * @date 2017年10月17日 下午6:24:03
 *       MessageListenerAdapter除了会自动的把一个普通Java类当做MessageListener来处理接收到的消息之外，其另外一个主要的功能是可以自动的发送返回消息。
 *       当我们用于处理接收到的消息的方法的返回值不为空的时候，Spring会自动将它封装为一个JMS
 *       Message，然后自动进行回复。那么这个时候这个回复消息将发送到哪里呢？这主要有两种方式可以指定。
 *       第一，可以通过发送的Message的setJMSReplyTo方法指定该消息对应的回复消息的目的地。这里我们把我们的生产者发送消息的代码做一下修改，在发送消息之前先指定该消息对应的回复目的地为一个叫responseQueue的队列目的地
 */
@Service
public class ProducerService {
	@Autowired
	private JmsTemplate jmsTemplate;
	// @Autowired
	// @Qualifier("responseQueue")
	//
	// private Destination responseDestination;

	public void sendMessage(Destination destination, final String message) {
		System.out.println("---------------生产者发送消息-----------------");
		System.out.println("---------------生产者发了一个消息：" + message);
		/*
		 * 
		 * A调用B 比如 A 让 B 做事，根据粒度不同，可以理解成 A 函数调用 B 函数，或者 A 类使用 B 类，或者 A 组件使用 B 组件等等。反正就是
		 * 当 B 做这件事情的时候，自身的需要的信息不够，而 A 又有。就需要 A 从外面传进来，或者 B 做着做着再向外面申请。对于 B
		 * 来说，一种被动得到信息，一种是主动去得到信息，有人给这两种方式术语，叫信息的 push，和信息的 pull。
		 * 
		 * 
		 * A 叫 B 做事。 A 调用 B，A 需要向 B 传参数
		 * 
		 * 
		 * 
		 * 在这里插一句唠唠回调函数callback 回调函数在框架中的运用非常的常见 虽然很常见，但是想真正理解回调函数却没有想象中的那么简单容易
		 * 
		 * 
		 * 在这里MessageCreator对象中的createMessage就是回调方法
		 * createMessage这个方法中的Session类型的参数需要JmsTemplate对象的send方法给出
		 * 而send方法的执行又需要MessageCreator对象的传入
		 * 
		 * 对这个场景来说，本来是MessageCreator用调用jmsTemplate的send方法， A调用B
		 * 但是现在send方法是不完整的，需要传入MessageCreator对象才能够执行
		 * 
		 * A调用B中的send方法，send方法得到A所需要的Session参数，将参数给到B，然后B执行回调函数createMessage 然后完成流程
		 * 
		 */
		jmsTemplate.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {

				TextMessage textMessage = session.createTextMessage(message);
				return textMessage;
			}
		});
	}
}

```

我们在之前说过了Spring把Session会话完全封装在Spring中了，然后我们发现Session在上述类中作为回调方法的参数出现。

我们来分析一下这段代码：

```
	jmsTemplate.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {

				TextMessage textMessage = session.createTextMessage(message);
				return textMessage;
			}
		});
```

我们发现	public void send(final Destination destination, final MessageCreator messageCreator) throws JmsException 方法的第二个参数是使用匿名类注入的，看到匿名类，我们很容易就想到了callback方法了，事实这就是一个回调方法。查阅send方法，我们发现里面依然是回调方法，为什么是回调方法呢，因为执行我们的方法所需要的参数Session得你Spring给我呀,然后才能调用我们自己的方法进行相关处理。

```
@Override
	public void send(final Destination destination, final MessageCreator messageCreator) throws JmsException {
		execute(new SessionCallback<Object>() {
			@Override
			public Object doInJms(Session session) throws JMSException {
				doSend(session, destination, messageCreator);
				return null;
			}
		}, false);
	}
```

关于生产者的配置，先介绍到这里。



- ### 配置消费者

  生产者往指定目的地Destination发送消息后，接下来就是消费者对指定目的地的消息进行消费了。那么消费者是如何知道有生产者发送消息到指定目的地Destination了呢？在之间的纯Java代码（不借助框架）中，我们实现了异步传输（使用监听器 onmessage()）和同步传输（While循环轮询）。那么在Spring这么厉害的框架里自然是选择了监听器方式，这也是调用函数的异步调用的实现。Spring为我们封装了消息监听容器MessageListenerContainer，它负责接收信息，并把接收到的信息分发给真正的MessageListener进行处理。每个消费者对应每个目的地都需要有对应的MessageListenerContainer。对于消息监听容器而言，除了要知道监听哪个目的地之外，还需要知道到哪里去监听，也就是说它还需要知道去监听哪个JMS服务器，这是通过在配置MessageConnectionFactory的时候往里面注入一个ConnectionFactory来实现的。所以我们在配置一个MessageListenerContainer的时候有三个属性必须指定，一个是表示从哪里监听的ConnectionFactory；一个是表示监听什么的Destination；一个是接收到消息以后进行消息处理的MessageListener。Spring一共为我们提供了两种类型的MessageListenerContainer，SimpleMessageListenerContainer和DefaultMessageListenerContainer。

  SimpleMessageListenerContainer会在一开始的时候就创建一个会话session和消费者Consumer，并且会使用标准的JMS MessageConsumer.setMessageListener()方法注册监听器让JMS提供者调用监听器的回调函数。它不会动态的适应运行时需要和参与外部的事务管理。兼容性方面，它非常接近于独立的JMS规范，但一般不兼容Java EE的JMS限制。

  大多数情况下我们还是使用的DefaultMessageListenerContainer，跟SimpleMessageListenerContainer相比，DefaultMessageListenerContainer会动态的适应运行时需要，并且能够参与外部的事务管理。它很好的平衡了对JMS提供者要求低、先进功能如事务参与和兼容Java EE环境。

```
	<bean id="queueReceiverListenerContainer"
		class="org.springframework.jms.listener.DefaultMessageListenerContainer">
		<property name="connectionFactory" ref="connectionFactory" />
		<property name="destination" ref="queue" />
		<property name="messageListener" ref="queueReceiver" />
	
	</bean>		
```



- **定义处理消息的MessageListener

  ​       要定义处理消息的MessageListener我们只需要实现JMS规范中的MessageListener接口就可以了。MessageListener接口中只有一个方法onMessage方法，当接收到消息的时候会自动调用该方法。

```
package com.wangcc.activemq.spring.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueMesssageListener implements MessageListener {
	private static Logger logger = LoggerFactory.getLogger(QueueMesssageListener.class);

	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage textMsg = (TextMessage) message;
			System.out.println("接收到一个纯文本消息。");
			logger.info("接收到一个纯文本消息。");
			try {
				System.out.println("消息内容是：" + textMsg.getText());
				logger.info("消息内容是:{}", textMsg.getText());
			} catch (JMSException e) {
				logger.error("MessageListener Error:{}", e);

			}
		}
		if (message instanceof ObjectMessage) {
			ObjectMessage objmessage = (ObjectMessage) message;
			try {
				Object obj = objmessage.getObject();
				System.out.println("消息内容是：" + obj.toString());

				logger.info("ObjectMessage:{}", obj);
			} catch (JMSException e) {
				logger.error("TopicMessageListener Error:{}", e);
			}
		}
	}

}
```

在Spring中注册这个Bean

```
	<bean id="queueReceiver" 
		class="com.wangcc.activemq.spring.listener.QueueMesssageListener">
	</bean>
```

配置完这些属性之后，我们就可以进行测试了。

```
package com.wangcc.test.spring;

import javax.jms.Destination;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wangcc.activemq.spring.service.ProducerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-jms.xml")
public class ProducerTest {
	@Autowired
	private ProducerService producerService;
	@Autowired
	@Qualifier("queue")
	private Destination destination;

	@Test
	public void testSend() {
		for (int i = 0; i < 2; i++) {
			producerService.sendMessage(destination, "你好，生产者！这是消息：" + (i + 1));
		}
	}
}
```



### 二.消息监听器

消息服务器，当然最重要的就是消息了,当生产者将消息发出之后，消费者是如何知道有生产者发送消息到指定目的地Destination的解决方法就是使用消息监听器，注册到MQ服务器上(伟大的回调方法)。

那么在Spring中，到底给我们提供了哪些消息监听器呢，我们来看看。

- ### MessageListener

  ### 	

  这个监听器我们之前已经使用了，使用这个监听器，我们只需要实现其onMessage方法，这是JMS规范提供给我们的原始接口。

- ### SessionAwareMessageListener

SessionAwareMessageListener是Spring为我们提供的，它不是标准的JMS MessageListener。MessageListener的设计只是纯粹用来接收消息的，假如我们在使用MessageListener处理接收到的消息时我们需要发送一个消息通知对方我们已经收到这个消息了，那么这个时候我们就需要在代码里面去重新获取一个Connection或Session。SessionAwareMessageListener的设计就是为了方便我们在接收到消息后发送一个回复的消息，它同样为我们提供了一个处理接收到的消息的onMessage方法，但是这个方法可以同时接收两个参数，一个是表示当前接收到的消息Message，另一个就是可以用来发送消息的Session对象。（我们要知道为什么Spring给我们提供了一个这样的消息监听器，要理解why）

我们实现一下这个接口，添加一个属性Destination,这样我们好在Spring配置文件中指定发送确认信息的目的地。

```java
package com.wangcc.activemq.spring.listener;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.listener.SessionAwareMessageListener;

/**
 * @ClassName: QueueSessionAwareMessageListener
 * @Description:
 * @author wangcc
 * @date 2017年10月17日 下午5:19:20
 *       SessionAwareMessageListener是Spring为我们提供的，它不是标准的JMS
 *       MessageListener。MessageListener的设计只是纯粹用来接收消息的，假如我们在使用MessageListener处理接收到的消息时我们需要发送一个消息通知对方我们已经收到这个消息了，那么这个时候我们就需要在代码里面去重新获取一个Connection或Session。SessionAwareMessageListener的设计就是为了方便我们在接收到消息后发送一个回复的消息，它同样为我们提供了一个处理接收到的消息的onMessage方法，但是这个方法可以同时接收两个参数，一个是表示当前接收到的消息Message，另一个就是可以用来发送消息的Session对象。
 */
public class QueueSessionAwareMessageListener implements SessionAwareMessageListener<TextMessage> {
	private Destination destination;

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public void onMessage(TextMessage message, Session session) throws JMSException {
		System.out.println("收到一条消息");
		System.out.println("消息内容是：" + message.getText());
		MessageProducer producer = session.createProducer(destination);
		Message textMessage = session.createTextMessage("ConsumerSessionAwareMessageListener。。。");
		producer.send(textMessage);

	}

}
```

配置文件

```
	 <bean id="sessionAwareQueue" class="org.apache.activemq.command.ActiveMQQueue">  
        <constructor-arg>  
            <value>sessionAwareQueue</value>  
        </constructor-arg>  
    </bean>  
	 <bean id="sessionAwareListenerContainer"  
        class="org.springframework.jms.listener.DefaultMessageListenerContainer">  
        <property name="connectionFactory" ref="connectionFactory" />  
        <property name="destination" ref="sessionAwareQueue" />  
        <property name="messageListener" ref="queueSessionAwareMessageListener" />  
        </bean>
        <bean id="queueSessionAwareMessageListener" class="com.wangcc.activemq.spring.listener.QueueSessionAwareMessageListener">
                <property name="destination" ref="queue"/>  
         </bean>
```

然后，我们进行测试。

```
	@Autowired
	@Qualifier("sessionAwareQueue")
	private Destination sessionAwareQueue;

	@Test
	public void testSessionAwareMessageListener() {
		producerService.sendMessage(sessionAwareQueue, "测试SessionAwareMessageListener");
	}

```

我们往sessionAwareQueue发送了一条纯文本消息之后，消息会被QueueSessionAwareMessageListener的onMessage方法进行处理，在onMessage方法中QueueSessionAwareMessageListener就是简单的把接收到的纯文本信息的内容打印出来了，之后再往queueDestination发送了一个纯文本消息



- ### MessageListenerAdapter



MessageListenerAdapter类实现了MessageListener接口和SessionAwareMessageListener接口，它的主要作用是将接收到的消息进行类型转换，然后通过反射的形式把它交给一个普通的Java类进行处理。

​       MessageListenerAdapter会把接收到的消息做如下转换：

​       TextMessage转换为String对象；

​       BytesMessage转换为byte数组；

​       MapMessage转换为Map对象；

​       ObjectMessage转换为对应的Serializable对象。

​       既然前面说了MessageListenerAdapter会把接收到的消息做一个类型转换，然后利用反射把它交给真正的目标处理器——一个普通的Java类进行处理（如果真正的目标处理器是一个MessageListener或者是一个SessionAwareMessageListener，那么Spring将直接使用接收到的Message对象作为参数调用它们的onMessage方法，而不会再利用反射去进行调用），那么我们在定义一个MessageListenerAdapter的时候就需要为它指定这样一个目标类。这个目标类我们可以通过MessageListenerAdapter的构造方法参数指定也可以通过它的delegate属性来指定。





配置文件

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"  
    xmlns:jms="http://www.springframework.org/schema/jms"  
    xsi:schemaLocation="http://www.springframework.org/schema/beans  
     http://www.springframework.org/schema/beans/spring-beans-3.0.xsd  
     http://www.springframework.org/schema/context  
     http://www.springframework.org/schema/context/spring-context-3.0.xsd  
    http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd  
    http://www.springframework.org/schema/jms http://www.springframework.org/schema/jms/spring-jms-3.0.xsd">  
    <context:component-scan base-package="com.wangcc.activemq.spring.service" />  

	<!--该配置使配置文件可以使用外部的properties文件 -->
	<bean id="propertyConfigurer"
		class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
		<property name="locations">
			<list>
				<value>classpath:jms.properties</value>
			</list>
		</property>
	</bean>
	<!-- JMS连接工厂 all system must
	
	-->
 <!-- 真正可以产生Connection的ConnectionFactory，由对应的 JMS服务厂商提供-->  
	
	<bean id="amqConnectionFactory" 
		class="org.apache.activemq.ActiveMQConnectionFactory">
		<property name="brokerURL">
			<value>${brokerURL}</value>
		</property>
		<property name="userName">
			<value>${userName}</value>
		</property>
		<property name="password">
			<value>${password}</value>
		</property>
		
		</bean>
		<!-- 
		 ConnectionFactory是用于产生到JMS服务器的链接的，Spring为我们提供了多个ConnectionFactory，有SingleConnectionFactory和CachingConnectionFactory。SingleConnectionFactory对于建立JMS服务器链接的请求会一直返回同一个链接，并且会忽略Connection的close方法调用。CachingConnectionFactory继承了SingleConnectionFactory，所以它拥有SingleConnectionFactory的所有功能，同时它还新增了缓存功能，它可以缓存Session、MessageProducer和MessageConsumer。
		
		 -->
	<bean id="connectionFactory"
		class="org.springframework.jms.connection.SingleConnectionFactory">
		<!-- class="org.apache.activemq.ActiveMQConnectionFactory"> -->
		<property name="targetConnectionFactory" ref="amqConnectionFactory"></property>
		<property name="sessionCacheSize" value="10" />
		
	</bean>
	    <!-- Spring提供的JMS工具类，它可以进行消息发送、接收等 -->  
	
	<!-- jmsTemplate  all system must-->
	<bean id="jmsTemplate"
		class="org.springframework.jms.core.JmsTemplate">
		        <!-- 这个connectionFactory对应的是我们定义的Spring提供的那个ConnectionFactory对象 -->  
		
		<property name="connectionFactory">
			<ref bean="connectionFactory" />
		</property>
		<property name="deliveryPersistent">
			<value>true</value>
		</property>
		<property name="messageConverter" ref="objectMessageConverter"></property>
	</bean>
	
	<bean id="objectMessageConverter" class="com.wangcc.activemq.converter.ObjectMessageConverter"></bean>
	
	
	
	
	<!-- queue-->
	<bean id="queue"
		class="org.apache.activemq.command.ActiveMQQueue">
		<constructor-arg index="0">
			<value>${um_organ_queue}</value>
		</constructor-arg>
	</bean>
	
	<!-- topic-->
	<bean id="topic"
		class="org.apache.activemq.command.ActiveMQTopic">
		<constructor-arg index="0">
			<value>${app_organ_topic}</value>
		</constructor-arg>
	</bean>
<!-- 所以我们在配置一个MessageListenerContainer的时候有三个属性必须指定，一个是表示从哪里监听的ConnectionFactory；一个是表示监听什么的Destination；一个是接收到消息以后进行消息处理的MessageListener。

Spring一共为我们提供了两种类型的MessageListenerContainer，SimpleMessageListenerContainer和DefaultMessageListenerContainer。
SimpleMessageListenerContainer会在一开始的时候就创建一个会话session和消费者Consumer，并且会使用标准的JMS MessageConsumer.setMessageListener()方法注册监听器让JMS提供者调用监听器的回调函数。它不会动态的适应运行时需要和参与外部的事务管理。兼容性方面，它非常接近于独立的JMS规范，但一般不兼容Java EE的JMS限制。
大多数情况下我们还是使用的DefaultMessageListenerContainer，跟SimpleMessageListenerContainer相比，DefaultMessageListenerContainer会动态的适应运行时需要，并且能够参与外部的事务管理。它很好的平衡了对JMS提供者要求低、先进功能如事务参与和兼容Java EE环境。
 -->	
 <!-- 
 setClientID call not supported on proxy for shared Connection. Set the 'clientId' property on the SingleConnectionFactory instead.
  	所以在这个监听器容器中不能使用connectionFactory(CachingConnectionFactory)
  	这里注意下connectionFactory，用的是activemq提供的connectionFactory，是因为spring提供的connectionFactory没有clientId属性；autoStartup是定义是否加载spring配置时自动启动监听；destination指具体需要监听的topic名字，messageListener指具体的监听处理程序，要实现MessageListener接口。
  -->
	<bean id="topicReceiverListenerContainer"
		class="org.springframework.jms.listener.DefaultMessageListenerContainer">
		<property name="subscriptionDurable">
			<value>true</value>
		</property>
		<property name="durableSubscriptionName">
			<value>124</value>
		</property>
		<property name="clientId">
			<value>124</value>
		</property> 
		<property name="connectionFactory" ref="amqConnectionFactory" />
		<property name="destination" ref="topic" />
		<property name="messageListener" ref="topicReceiver" />
	
	</bean>
	<!--  
	<bean id="topicReceiverListener"
		class="org.springframework.jms.listener.DefaultMessageListenerContainer">
		
		<property name="connectionFactory" ref="connectionFactory" />
		<property name="destination" ref="topic" />
		<property name="messageListener" ref="topicReceiver" />
		<property name="autoStartup" value="false" />
		<property name="concurrentConsumers" value="2" />
		<property name="maxConcurrentConsumers" value="30" />
	</bean>
	-->
	<bean id="queueReceiverListenerContainer"
		class="org.springframework.jms.listener.DefaultMessageListenerContainer">
		<property name="connectionFactory" ref="connectionFactory" />
		<property name="destination" ref="queue" />
		<property name="messageListener" ref="queueReceiver" />
	
	</bean>
	<!--    在Spring整合JMS的应用中我们在定义消息监听器的时候一共可以定义三种类型的消息监听器，分别是MessageListener、SessionAwareMessageListener和MessageListenerAdapter -->
	<bean id="queueReceiver" 
		class="com.wangcc.activemq.spring.listener.QueueMesssageListener">
	</bean>
	<bean id="topicReceiver" 
		class="com.wangcc.activemq.spring.listener.TopicMessageListener">
	</bean>
	 <bean id="sessionAwareQueue" class="org.apache.activemq.command.ActiveMQQueue">  
        <constructor-arg>  
            <value>sessionAwareQueue</value>  
        </constructor-arg>  
    </bean>  
	 <bean id="sessionAwareListenerContainer"  
        class="org.springframework.jms.listener.DefaultMessageListenerContainer">  
        <property name="connectionFactory" ref="connectionFactory" />  
        <property name="destination" ref="sessionAwareQueue" />  
        <property name="messageListener" ref="queueSessionAwareMessageListener" />  
        </bean>
        <bean id="queueSessionAwareMessageListener" class="com.wangcc.activemq.spring.listener.QueueSessionAwareMessageListener">
                <property name="destination" ref="queue"/>  
         </bean>
         
         
         <!--  MessageListenerAdapter类实现了MessageListener接口和SessionAwareMessageListener接口，它的主要作用是将接收到的消息进行类型转换，然后通过反射的形式把它交给一个普通的Java类进行处理。
       MessageListenerAdapter会把接收到的消息做如下转换：
       TextMessage转换为String对象；
       BytesMessage转换为byte数组；
       MapMessage转换为Map对象；
       ObjectMessage转换为对应的Serializable对象。
       既然前面说了MessageListenerAdapter会把接收到的消息做一个类型转换，然后利用反射把它交给真正的目标处理器——一个普通的Java类进行处理（如果真正的目标处理器是一个MessageListener或者是一个SessionAwareMessageListener，那么Spring将直接使用接收到的Message对象作为参数调用它们的onMessage方法，而不会再利用反射去进行调用），那么我们在定义一个MessageListenerAdapter的时候就需要为它指定这样一个目标类。
       这个目标类我们可以通过MessageListenerAdapter的构造方法参数指定,也可以用delegate属性指定
       
       前面说了如果我们指定的这个目标处理器是一个MessageListener或者是一个SessionAwareMessageListener的时候Spring将直接利用接收到的Message对象作为方法参数调用它们的onMessage方法。但是如果指定的目标处理器是一个普通的Java类时Spring将利用Message进行了类型转换之后的对象作为参数通过反射去调用真正的目标处理器的处理方法，那么Spring是如何知道该调用哪个方法呢？这是通过MessageListenerAdapter的defaultListenerMethod属性来决定的，当我们没有指定该属性时，Spring会默认调用目标处理器的handleMessage方法。
        -->
        <!-- 用于测试消息监听适配器的队列目的地 -->  
    <bean id="adapterQueue" class="org.apache.activemq.command.ActiveMQQueue">  
        <constructor-arg>  
            <value>adapterQueue</value>  
        </constructor-arg>  
    </bean>  
         <bean id="messageListenerAdapter" class="org.springframework.jms.listener.adapter.MessageListenerAdapter">  
        <property name="delegate">  
            <bean class="com.wangcc.activemq.spring.listener.QueueAdapterMessageListener"/>  
        </property>  
                <property name="defaultListenerMethod" value="receiveMessage"/>  
        		<property name="defaultResponseDestination" ref="defaultResponseQueue"></property>
    </bean>  
    
    
    <!-- 消息监听适配器对应的监听容器 -->  
    <bean id="messageListenerAdapterContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">  
        <property name="connectionFactory" ref="connectionFactory"/>  
        <property name="destination" ref="adapterQueue"/>  
        <!-- 使用MessageListenerAdapter来作为消息监听器 -->  
        <property name="messageListener" ref="messageListenerAdapter"/>
         </bean>  


    <!--  第二，通过MessageListenerAdapter的defaultResponseDestination属性来指定。这里我们也来做一个测试，首先维持生产者发送消息的代码不变，即发送消息前不通过Message的setJMSReplyTo方法指定消息的回复目的地；接着我们在定义MessageListenerAdapter的时候通过其defaultResponseDestination属性指定其默认的回复目的地是“defaultResponseQueue”，并定义defaultResponseQueue对应的消息监听器和消息监听容器。 -->
        <bean id="defaultResponseQueue" class="org.apache.activemq.command.ActiveMQQueue">  
        <constructor-arg>  
            <value>defaultResponseQueue</value>  
        </constructor-arg>  
    </bean>  
    <bean id="defaultResponseQueueListener" class="com.wangcc.activemq.spring.listener.DefaultResponseMessageListener">
    </bean>
    <bean id="defaultResponseQueueMessageListenerContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">  
    <property name="connectionFactory" ref="connectionFactory"/>  
    <property name="destination" ref="defaultResponseQueue"/>  
    <property name="messageListener" ref="defaultResponseQueueListener"/>  
</bean>  
    
    
    <bean id="responseQueue" class="org.apache.activemq.command.ActiveMQQueue">
      <constructor-arg>  
        <value>responseQueue</value>  
    </constructor-arg>  
    </bean>
    <!-- responseQueue对应的监听容器 -->  
<bean id="responseQueueMessageListenerContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">  
    <property name="connectionFactory" ref="connectionFactory"/>  
    <property name="destination" ref="responseQueue"/>  
    <property name="messageListener" ref="responseQueueListener"/>  
</bean>  
<!-- responseQueue对应的监听器 -->  
<bean id="responseQueueListener" class="com.wangcc.activemq.spring.listener.ResponseQueueListener"/>  


</beans>

```

