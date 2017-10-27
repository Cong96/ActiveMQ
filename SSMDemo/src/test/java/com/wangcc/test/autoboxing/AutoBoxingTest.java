package com.wangcc.test.autoboxing;

import org.junit.Test;

/**
 * @ClassName: AutoBoxingTest
 * @Description: 自动装箱和拆箱是由编译器来完成的，编译器会在编译期根据语法决定是否进行装箱和拆箱动作。
 * @author wangcc
 * @date 2017年10月27日 下午3:01:16 http://wuaner.iteye.com/blog/1668172
 *       http://blog.csdn.net/u013078669/article/details/52766011
 */
public class AutoBoxingTest {
	public static void main(String[] args) {
		AutoBoxingTest.test();
	}

	// @Test
	public static void test() {
		/*
		 * 其实编译器调用的是static Integer valueOf(int i)这个方法,valueOf(int
		 * i)返回一个表示指定int值的Integer对象,那么就变成这样: Integer a=3; => Integer
		 * a=Integer.valueOf(3); 拆箱：跟自动装箱的方向相反，将Integer及Double这样的引用类型的对象重新简化为基本类型的数据。
		 * 如下： int i = new Integer(2);//这是拆箱 编译器内部会调用int intValue()返回该Integer对象的int值
		 */
		Integer integer1 = 100;// 自动装箱，

		Integer integer2 = 100;
		System.out.println("integer1==integer2: " + (integer1 == integer2));// true 自动装箱的两个缓存中的 Integer对象的引用比较
		System.out.println("integer1.equals(integer2): " + (integer1.equals(integer2)));// true
		System.out.println("integer1.compare(integer2): " + integer1.compareTo(integer2));// 0
		Integer integer3 = 200;
		Integer integer4 = 200;
		System.out.println("integer3==integer4: " + (integer3 == integer4));// false 自动装箱的两个new Integer的引用比较
		System.out.println("integer3>integer4: " + (integer3 > integer4)); // false 将两个对象拆箱，再比较大小
		System.out.println("integer3.equals(integer4): " + (integer3.equals(integer4)));// true
		System.out.println("integer3.compare(integer4): " + integer3.compareTo(integer4));// 0
		Integer integer5 = new Integer(100);
		Integer integer6 = new Integer(100);
		System.out.println("integer5==integer6: " + (integer5 == integer6)); // false 两个不同的Integer对象引用的比较
		System.out.println("integer5.equals(integer6): " + (integer5.equals(integer6)));// true
		System.out.println("integer5.compare(integer6): " + integer5.compareTo(integer6));// 0
		int int1 = 100;
		System.out.println("integer1==int1: " + (integer1 == int1));// true Integer缓存对象拆箱后与int比较
		System.out.println("integer1.equals(int1): " + (integer1.equals(int1)));// true
		System.out.println("integer1.compare(int1): " + integer1.compareTo(int1));// 0
		int int2 = 200;
		System.out.println("integer3==int2: " + (integer3 == int2));// true Integer对象拆箱后与int比较
		System.out.println("integer3.equals(int2): " + (integer3.equals(int2)));// true
		System.out.println("integer3.compare(int2): " + integer3.compareTo(int2));// 0

	}

	@Test
	public void qtest() {
		Integer integer1 = 233;
		int i = 233;
		System.out.println("integer1==i:" + (integer1 == i));
		Integer integer2 = 233;
		System.out.println("integer1==integer2:" + (integer1 == integer2));
		System.out.println("integer1.equals(integer2):" + (integer1.equals(integer2)));
	}
}
