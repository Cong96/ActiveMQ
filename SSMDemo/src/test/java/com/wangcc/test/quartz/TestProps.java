package com.wangcc.test.quartz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import com.wangcc.ssm.util.ClassLoaderWrapper;

public class TestProps {
	@Test
	public void testProp() throws IOException {
		InputStream in = ClassLoaderWrapper.getInstance().loadResource("quartz.properties");
		Properties prop = new Properties();
		prop.load(in);
		System.out.println(prop.getProperty("jobs.message"));
	}
}
