package com.wangcc.test.mybatis;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.wangcc.ssm.dao.CoachDao;
import com.wangcc.ssm.entity.Coach;
import com.wangcc.ssm.mybatis.interceptor.entity.ComplexParamMap;
import com.wangcc.ssm.mybatis.interceptor.entity.Page;
import com.wangcc.ssm.mybatis.interceptor.entity.ParamMap;
import com.wangcc.ssm.util.MybatisUtil;
import com.wangcc.ssm.util.ReflectUtil;

public class MybatisTest {
	private static Logger logger = LoggerFactory.getLogger(MybatisTest.class);

	@Test
	public void testReflect() throws Exception {
		Son son = new Son();
		son.setName("dagasdg");
		son.setAge(13);
		Address address = new Address();
		address.setLocation("la");
		son.setAddress(address);
		String name = ReflectUtil.getFieldValue(son, "name");
		System.out.println("name:" + name);
		String location = ReflectUtil.getFieldValue(son, "address,location");
		System.out.println("location:" + location);
		ReflectUtil.setFieldValue(son, "name", "kobe");
		ReflectUtil.setFieldValue(son, "address,location", "LOS");
		System.out.println(son);
		System.out.println(son.getName());
	}

	@Test
	public void testproperty() throws Exception {
		Son son = new Son();
		son.setName("dagasdg");
		son.setAge(13);
		Address address = new Address();
		address.setLocation("la");
		son.setAddress(address);
		Class<?> clazz = son.getClass();
		PropertyDescriptor pd = new PropertyDescriptor("address", clazz);
		Method readMethod = pd.getReadMethod(); // getName()
		Object name = readMethod.invoke(son);
		System.out.println(name);
	}

	@Test
	public void test() {
		SqlSession session = null;
		System.out.println("Dd");
		try {

			session = MybatisUtil.getCurrentSession();
			CoachDao mapper = session.getMapper(CoachDao.class);
			// Coach coach = mapper.getCoachById(1);
			// System.out.println(coach);
			ComplexParamMap<Coach> cmap = new ComplexParamMap<Coach>();
			Coach coach = new Coach();
			coach.setName("jackson");
			ParamMap paramMap = new ParamMap("id", "name");
			cmap.setParamMap(paramMap);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "jackson");
			cmap.setParams(params);
			cmap.setObj(coach);
			Map<Object, Object> map = mapper.queryMap(cmap);
			System.out.println(JSON.toJSONString(map));
			Page<Coach, ?> page = new Page<>();
			page.setSelf(coach);
			page.setPageSize(4);
			page.setPageNo(2);
			List<Coach> list = mapper.querybyPage(page);
			System.out.println(JSON.toJSONString(list));

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	@Test
	public void testMap() {
		SqlSession session = null;
		System.out.println("Dd");
		try {

			session = MybatisUtil.getCurrentSession();
			CoachDao mapper = session.getMapper(CoachDao.class);
			Map<Object, Object> map = mapper.testMap();
			System.out.println(JSON.toJSONString(map));

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("testMap Error:{}", e);
		} finally {
			session.close();
		}
	}

	@Test
	public void testMapKey() {
		SqlSession session = null;
		System.out.println("Dd");
		try {

			session = MybatisUtil.getCurrentSession();
			CoachDao mapper = session.getMapper(CoachDao.class);
			Map<Integer, Map<String, Object>> map = mapper.testMapKey();
			System.out.println(JSON.toJSONString(map));

		} catch (Exception e) {
			// TODO: handle exception
			logger.error("testMap Error:{}", e);
		} finally {
			session.close();
		}
	}
}
