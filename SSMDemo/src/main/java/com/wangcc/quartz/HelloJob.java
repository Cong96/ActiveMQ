package com.wangcc.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @ClassName: HelloJob
 * @Description: http://blog.csdn.net/tanyongbing1988/article/details/45689987
 * @author wangcc
 * @date 2017年10月23日 下午3:26:57
 * 
 */
@Service
@PropertySource(value = "classpath:quartz.properties")

public class HelloJob {

	public HelloJob() {
		System.out.println("HelloJob创建成功");
	}

	// @Scheduled(cron = "0/1 * * * * ? ")
	@Scheduled(cron = "${jobs.message}") // 每隔1秒隔行一次
	public void run() {
		System.out.println("Hello MyJob  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date()));
	}

	/*
	 * 
	 * 要注意的是，要使用
	 * 
	 * @Bean public static PropertySourcesPlaceholderConfigurer
	 * propertyConfigInDev() { return new PropertySourcesPlaceholderConfigurer(); }
	 * 
	 * 才能让spring正确解析出${} 中的值 http://blog.csdn.net/itchiang/article/details/51144218
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
