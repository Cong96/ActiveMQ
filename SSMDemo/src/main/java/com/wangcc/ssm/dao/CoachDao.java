package com.wangcc.ssm.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;

import com.wangcc.ssm.entity.Coach;
import com.wangcc.ssm.mybatis.interceptor.entity.ComplexParamMap;
import com.wangcc.ssm.mybatis.interceptor.entity.Page;

public interface CoachDao {
	public Coach getCoachById(Integer id);

	public Integer insertCoach(Coach coach);

	public List<Coach> querybyPage(Page<?, ?> page);

	public Map<Object, Object> queryMap(ComplexParamMap paramMap);

	@MapKey("ID")
	public Map<Integer, Map<String, Object>> testMapKey();

	public Map<Object, Object> testMap();
}
