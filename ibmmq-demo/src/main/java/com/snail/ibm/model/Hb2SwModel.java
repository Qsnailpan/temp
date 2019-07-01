package com.snail.ibm.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lipan 2019年7月1日
 * @description （ 简单的描述 下：环保到税务 - 对应的报文结构 ）
 *
 */
@Getter
@Setter
public class Hb2SwModel {

	private PackageHead packageHead;

	private  DataRoot data;
}
