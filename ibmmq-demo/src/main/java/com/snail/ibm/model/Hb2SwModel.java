package com.snail.ibm.model;

import java.util.List;

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

	private DataRoot data;

	/**
	 * 对数据进行MQ结构化处理
	 * 
	 * @param datas
	 * @return
	 */
	public static Hb2SwModel init(List<Data> datas) {
		Hb2SwModel hb2SwModel = new Hb2SwModel();
		PackageHead packageHead = new PackageHead();
//		 不同数据 值不同
		Data data = datas.get(0);
		if (datas.get(0) instanceof PwxkData) {
			PwxkData pwxkData = (PwxkData) datas.get(0);
			packageHead.setSJBLX("PWXKZB");
			packageHead.setSJBBH("1");
			packageHead.setDWDM(pwxkData.getTYSHXYDM());
		}
//			 通用
		packageHead.setDWMC(data.getDWMC());
		packageHead.setSCRQ(String.valueOf(System.currentTimeMillis() / 1000));
		packageHead.setJLS(String.valueOf(datas.size()));
		hb2SwModel.setPackageHead(packageHead);
		DataRoot dataRoot = new DataRoot();
		dataRoot.setRecord(datas);
		hb2SwModel.setData(dataRoot);
		return hb2SwModel;
	}
}
