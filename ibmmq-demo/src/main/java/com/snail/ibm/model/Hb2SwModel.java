package com.snail.ibm.model;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
	 * 对数据进行MQ结构化处理 - 消息回执
	 * 
	 * @param datas
	 * @return
	 */

	public static Object init(MQBackData data) {
		Hb2SwModel hb2SwModel = new Hb2SwModel();
		PackageHead packageHead = new PackageHead();
		packageHead.setSJBBH("15100020190702000005");
		packageHead.setSJBLX("SSDZXX");
		packageHead.setDWDM("四川省生态环境厅");
		packageHead.setDWMC("510000");
		packageHead.setSCRQ(String.valueOf(System.currentTimeMillis() / 1000));
		hb2SwModel.setPackageHead(packageHead);
		JSONObject jsonHb2SwModel = JSONObject.parseObject(JSON.toJSONString(hb2SwModel));
		HashMap<String, String> mQBackData = new HashMap<String, String>(2);
		mQBackData.put("SJBBH", data.getSJBBH());
		mQBackData.put("SJBLX", data.getSJBLX());
		jsonHb2SwModel.put("Data", mQBackData);
		return jsonHb2SwModel;
	}

	public static Hb2SwModel init(List<Data> datas) {
		Hb2SwModel hb2SwModel = new Hb2SwModel();
		PackageHead packageHead = new PackageHead();
		// 不同数据 值不同
		Data data = datas.get(0);
		if (datas.get(0) instanceof PwxkData) {
			PwxkData pwxkData = (PwxkData) datas.get(0);
			packageHead.setSJBLX("PWXKZB");
		}
		// 通用
		packageHead.setSJBBH("15100020190702000005");
		packageHead.setDWDM("四川省生态环境厅");
		packageHead.setDWMC("510000");
		packageHead.setSCRQ(String.valueOf(System.currentTimeMillis() / 1000));
		packageHead.setJLS(String.valueOf(datas.size()));
		hb2SwModel.setPackageHead(packageHead);
		DataRoot dataRoot = new DataRoot();
		dataRoot.setRecord(datas);
		hb2SwModel.setData(dataRoot);
		return hb2SwModel;
	}
}
