package com.wangcc.ssm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {
	private static ApplicationContext applicationContext; // SpringӦ�������Ļ���
	private static Logger logger = LoggerFactory.getLogger(ApplicationContextAware.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		logger.info("*******************INIT***************************************");
		SpringUtil.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * ��ȡ����
	 * 
	 * @param name
	 * @return Object һ������������ע���bean��ʵ��
	 * @throws BeansException
	 */
	public static Object getBean(String name) {
		return applicationContext.getBean(name);
	}
}
