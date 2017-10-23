package com.wangcc.test.quartz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) // 表示继承了SpringJUnit4ClassRunner类
@ContextConfiguration(locations = { "classpath:mybatis-spring.xml" })
public class TestQuartz {
	@Test
	public void testquartz() throws InterruptedException {
		Thread.sleep(100000L);

		System.out.println("Test quartz");
	}

}
