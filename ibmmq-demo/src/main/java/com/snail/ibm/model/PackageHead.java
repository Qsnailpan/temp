package com.snail.ibm.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lipan 2019年7月1日
 * @description （ 简单的描述 下：报文头部消息 ）
 *
 */
@Getter
@Setter
public class PackageHead {

	/**
	 * 数据包编号
	 */
	private String SJBBH;
	/**
	 * 数据包类型
	 */
	private String SJBLX;
	/**
	 * 单位代码
	 */
	private String DWDM;
	/**
	 * 单位名称
	 */
	private String DWMC;
	/**
	 * 记录数
	 */
	private String JLS;
	/**
	 * 生成日期年月日时分秒
	 */
	private String SCRQ;

}
