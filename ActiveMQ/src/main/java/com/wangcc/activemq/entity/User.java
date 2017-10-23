package com.wangcc.activemq.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class User implements Serializable {
	private String name;
	private String email;
	private int age;
}
