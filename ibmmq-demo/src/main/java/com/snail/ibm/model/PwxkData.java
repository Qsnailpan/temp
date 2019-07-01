package com.snail.ibm.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lipan 2019年7月1日
 * @description （ 简单的描述 下：报文数据 ）
 *
 */
@Getter
@Setter
public class PwxkData extends Data {

	/**
	 * 主键
	 */
	private String UUID;
	/**
	 * 排污许可证编号
	 */
	private String PWXKZBH;
	/**
	 * 单位名称
	 */
	private String DWMC;
	/**
	 * 统一社会信用代码
	 */
	private String TYSHXYDM;
	/**
	 * 组织机构代码
	 */
	private String ZZJGDM;
	/**
	 * 营业执照号码
	 */
	private String YYZZHM;
	/**
	 * 行业类别
	 */
	private String HYLB;

	/**
	 * 生产经营场所地址
	 */
	private String SCJYCSDZ;

	/**
	 * 所在省份区划代码
	 */
	private String SZSFQHDM;
	/**
	 * 所在地市区划代码
	 */
	private String SZDSQHDM;
	/**
	 * 所在区县区划代码
	 */
	private String SZQXQHDM;
	/**
	 * 街道乡镇代码
	 */
	private String JDXZDM;
	/**
	 * 技术负责人
	 */
	private String JSFZR;
	/**
	 * 固定电话
	 */
	private String GDDH;
	/**
	 * 移动电话
	 */
	private String YDDH;
	/**
	 * 
	 * 许可有效期限起
	 */
	private String XKYXQXQ;
	/**
	 * 许可有效期限止
	 */
	private String XKYXQXZ;
	/**
	 * 投产日期
	 */
	private String TCRQ;
	/**
	 * 是否属于重点区域
	 */
	private String SFSYZDQY;
	/**
	 * 主要污染物类别
	 */
	private String ZYWRWLB;
	/**
	 * 
	 * 核发日期
	 */
	private String HFRQ;
	/**
	 * 核发环保机关代码
	 */
	private String HFHBJGDM;
	/**
	 * 核发环保机关名称
	 */
	private String HFHBJGMC;
	/**
	 * 数据归属（省）
	 */
	private String SJGS;
	/**
	 * 数据归属（地市）
	 */
	private String DSGS;
	/**
	 * 数据归属（区县）
	 */
	private String QXGS;
	/**
	 * 申请类型
	 */
	private String SQLX;
	/**
	 * 办结时间
	 */
	private String BJSJ;

}
