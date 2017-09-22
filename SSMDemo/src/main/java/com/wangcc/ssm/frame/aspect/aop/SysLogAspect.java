package com.wangcc.ssm.frame.aspect.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.wangcc.ssm.frame.aspect.annotation.MyLog;
import com.wangcc.ssm.frame.aspect.service.SysLogService;
import com.wangcc.ssm.frame.entity.SysLog;
import com.wangcc.ssm.util.ClassLoaderWrapper;

//@Aspect
@Component
public class SysLogAspect {
	private static Logger logger = LoggerFactory.getLogger(SysLogAspect.class);
	@Autowired
	private SysLogService sysLogService;
	private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

	/*
	 * 1��execution(): ���ʽ���塣
	 * 
	 * 2����һ��*�ţ���ʾ�������ͣ�*�ű�ʾ���е����͡�
	 * 
	 * 3����������ʾ��Ҫ���صİ������������������ʾ��ǰ���͵�ǰ���������Ӱ���com.sample.service.impl�����������������ķ�����
	 * 
	 * 4���ڶ���*�ţ���ʾ������*�ű�ʾ���е��ࡣ
	 * 
	 * 5��*(..):�������Ǻű�ʾ��������*�ű�ʾ���еķ������������������ʾ�����Ĳ�������������ʾ�κβ�����
	 */
	@Pointcut("execution  (* com.wangcc.ssm.service.*.*(..)) ")
	public void serviceAspect() {
	}

	@Before("serviceAspect()")
	public void doBefore(JoinPoint joinPoint) {
		logger.info("==========ִ��serviceǰ��֪ͨ===============");
		if (logger.isInfoEnabled()) {
			logger.info("before " + joinPoint);
		}
	}

	@Around("serviceAspect()")
	public void around(JoinPoint joinPoint) {
		logger.info("==========��ʼִ��service����֪ͨ===============");
		long start = System.currentTimeMillis();
		try {
			((ProceedingJoinPoint) joinPoint).proceed();
			long end = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info("around " + joinPoint + "\tUse time : " + (end - start) + " ms!");
			}
			System.out.println("==========����ִ��controller����֪ͨ===============");
		} catch (Throwable e) {
			long end = System.currentTimeMillis();
			if (logger.isInfoEnabled()) {
				logger.info("around " + joinPoint + "\tUse time : " + (end - start) + " ms with exception : "
						+ e.getMessage());
			}
		}
	}

	@After("serviceAspect()")
	public void after(JoinPoint joinPoint) {

		// ͨ��RequestContextHolder����ȡrequest����ô��ȡ����
		// http://blog.csdn.net/zzy7075/article/details/53559902
		// // HttpServletRequest request =
		// RequestContextHolder.getRequestAttributes().getRequest();
		// RequestAttributes requestAttributes =
		// RequestContextHolder.currentRequestAttributes();
		//// RequestContextHolder.getRequestAttributes();
		// HttpServletRequest request =
		// ((ServletRequestAttributes)requestAttributes).getRequest();
		// //��session�����ȡ��Ӧ��ֵ
		// HttpSession session = request.getSession();
		// String str = (String)
		// requestAttributes.getAttribute("name",RequestAttributes.SCOPE_SESSION);
		// AOPͨ����̬����,��ʵ��Ҫִ�еķ����������Ϣ��װ��JoinPoint������
		// ͨ��InvocationHandler�е�invoke(Object proxy, Method method, Object[] args)
		// ��InvocationHandler��������ʵ��ҵ�����ͨ�����ʵ�ʶ������ǾͿ��Եõ�target�����Ϣ
		// method������˵�����Եõ�Method��ض���

		// ʹ��loadClass�Ƚϸɾ���֪ʶ�õ�Class����û�н��г�ʼ����������Class.forName()��
		try {
			// argsҲ����˵ ���һ������ Object[] args
			String methodName = joinPoint.getSignature().getName();
			String targetName = joinPoint.getTarget().getClass().getName();
			Object[] arguments = joinPoint.getArgs();
			Class clazz = classLoaderWrapper.loadClass(targetName);
			Method[] methods = clazz.getMethods();
			String operationType = "";
			String operationName = "";
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					// ���ﻹû�п��ǲ������Ͳ�ͬ�����ǲ���������ͬ�����
					Class[] clazzs = method.getParameterTypes();
					if (clazzs.length == arguments.length) {
						operationType = method.getAnnotation(MyLog.class).operationType();
						operationName = method.getAnnotation(MyLog.class).operationName();
						break;
					}
				}
			}
			SysLog log = new SysLog();
			String ip = "";
			log.setId(UUID.randomUUID().toString());
			log.setDescription(operationName);
			log.setMethod((joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()")
					+ "." + operationType);
			log.setLogType(operationType);
			log.setRequestIp(ip);
			log.setExceptionCode(null);
			log.setExceptionDetail(null);
			log.setParams(null);
			// ��Ŀ��Ӧ����ֻ��IP��¼
			log.setCreateBy("");
			log.setCreateDate(new Date());
			// �������ݿ�
			sysLogService.insertLog(log);
			System.out.println("=====service����֪ͨ����=====");
		} catch (Exception e) {

			logger.error("==����֪ͨ�쳣==");
			logger.error("�쳣��Ϣ:{}", e);
		}

	}

	// ���ú��÷���֪ͨ,ʹ���ڷ���aspect()��ע��������
	@AfterReturning("serviceAspect()")
	public void afterReturn(JoinPoint joinPoint) {
		logger.info("=====ִ��service���÷���֪ͨ=====");
		if (logger.isInfoEnabled()) {
			logger.info("afterReturn " + joinPoint);
		}
	}

	/**
	 * �쳣֪ͨ �������ؼ�¼�쳣��־
	 * 
	 * @param joinPoint
	 * @param e
	 */
	@AfterThrowing(pointcut = "serviceAspect()", throwing = "e")
	public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
		/*
		 * HttpServletRequest request = ((ServletRequestAttributes)
		 * RequestContextHolder.getRequestAttributes()).getRequest(); HttpSession
		 * session = request.getSession(); //��ȡsession�е��û� User user = (User)
		 * session.getAttribute(WebConstants.CURRENT_USER); //��ȡ����ip String ip =
		 * request.getRemoteAddr();
		 */
		// ��ȡ�û����󷽷��Ĳ��������л�ΪJSON��ʽ�ַ���

		// User user = new User();
		// user.setId(1);
		// user.setName("����");
		String ip = "127.0.0.1";

		String params = "";
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
			for (int i = 0; i < joinPoint.getArgs().length; i++) {
				params += JSON.toJSONString(joinPoint.getArgs()[i]) + ";";
			}
		}
		try {

			String targetName = joinPoint.getTarget().getClass().getName();
			String methodName = joinPoint.getSignature().getName();
			Object[] arguments = joinPoint.getArgs();
			Class targetClass = Class.forName(targetName);
			Method[] methods = targetClass.getMethods();
			String operationType = "";
			String operationName = "";
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					Class[] clazzs = method.getParameterTypes();
					if (clazzs.length == arguments.length) {
						operationType = method.getAnnotation(MyLog.class).operationType();
						operationName = method.getAnnotation(MyLog.class).operationName();
						break;
					}
				}
			}
			/* ========����̨���========= */
			System.out.println("=====�쳣֪ͨ��ʼ=====");
			System.out.println("�쳣����:" + e.getClass().getName());
			System.out.println("�쳣��Ϣ:" + e.getMessage());
			System.out.println("�쳣����:"
					+ (joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()")
					+ "." + operationType);
			System.out.println("��������:" + operationName);
			System.out.println("������:" + "");
			System.out.println("����IP:" + ip);
			System.out.println("�������:" + params);
			/* ==========���ݿ���־========= */
			SysLog log = new SysLog();
			log.setId(UUID.randomUUID().toString());
			log.setDescription(operationName);
			log.setExceptionCode(e.getClass().getName());
			log.setLogType(operationType);

			log.setExceptionDetail(e.getMessage());
			log.setMethod(
					(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()"));
			log.setParams(params);
			log.setCreateBy("");
			log.setCreateDate(new Date());
			log.setRequestIp(ip);
			// �������ݿ�
			sysLogService.insertLog(log);
			System.out.println("=====�쳣֪ͨ����=====");
		} catch (Exception ex) {
			// ��¼�����쳣��־
			logger.error("==�쳣֪ͨ�쳣==");
			logger.error("�쳣��Ϣ:{}", ex);
		}
		/* ==========��¼�����쳣��־========== */
		logger.error("�쳣����:{}�쳣����:{}�쳣��Ϣ:{}����:{}",
				joinPoint.getTarget().getClass().getName() + joinPoint.getSignature().getName(), e.getClass().getName(),
				e.getMessage(), params);

	}
}
