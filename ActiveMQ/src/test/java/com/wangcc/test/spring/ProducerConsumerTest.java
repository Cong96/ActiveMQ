package com.wangcc.test.spring;

import javax.jms.Destination;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wangcc.activemq.entity.User;
import com.wangcc.activemq.spring.service.MesssageConvertService;
import com.wangcc.activemq.spring.service.ProducerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-jms.xml")
public class ProducerConsumerTest {

	@Autowired
	private ProducerService producerService;
	@Autowired
	@Qualifier("sessionAwareQueue")
	private Destination sessionAwareQueue;

	@Test
	public void testSessionAwareMessageListener() {
		producerService.sendMessage(sessionAwareQueue, "测试SessionAwareMessageListener");
	}

	@Autowired
	@Qualifier("adapterQueue")
	private Destination adapterQueue;

	@Test
	public void testMessageListenerAdapter() {
		producerService.sendMessage(adapterQueue, "测试MessageListenerAdapter");
	}

	@Autowired
	@Qualifier("queue")
	private Destination queue;
	@Autowired
	private MesssageConvertService messsageConvertService;

	@Test
	public void testMessageConverter() {
		User user = new User();
		user.setAge(12);
		user.setEmail("qqqqqqq");
		user.setName("KOBE");
		messsageConvertService.sendMessage(queue, user);
	}
}