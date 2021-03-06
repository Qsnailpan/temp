package com.snail.ibm.model;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * 初始数据，测试
	 * 
	 * @return
	 */
	public static List<Data> init() {
		List<Data> datas = new ArrayList<>();
		PwxkData data1 = new PwxkData();

		data1.setUUID("2019-07-02-11:11:11:300");
		data1.setPWXKZBH("91510124684591103D001P");
		data1.setDWMC("成都市友伦食品有限公司");
		data1.setTYSHXYDM("91510124684591103D");
		data1.setZZJGDM("/");
		data1.setYYZZHM("9151");
		data1.setHYLB("C3813");
		data1.setSCJYCSDZ("生产经营场所地址");
		data1.setSZSFQHDM("510000");
		data1.setSZDSQHDM("510100");
		data1.setSZQXQHDM("510101");
		data1.setJDXZDM("0101");
		data1.setJSFZR("技术负责人");
		data1.setGDDH("135121212122");
		data1.setYDDH("135121212122");
		data1.setXKYXQXQ("20190701");
		data1.setXKYXQXZ("20190701");
		data1.setTCRQ("20190701");
		data1.setSFSYZDQY("1");
		data1.setZYWRWLB("ZYWRWLB");
		data1.setSJGS("510000");
		data1.setDSGS("510100");
		data1.setQXGS("510101");
		data1.setHFRQ("20190701");
		data1.setHFHBJGDM("1");
		data1.setHFHBJGMC("核发环保机关名称");
		data1.setSQLX("1");
		data1.setBJSJ("20190701");
		datas.add(data1);
		PwxkData data2 = new PwxkData();
		data2.setDWMC("单位名称2");
		datas.add(data2);
		return datas;
	}
}
