package com.snail.ibm.mq;

import java.util.List;

import com.snail.ibm.model.Data;
import com.snail.ibm.model.PwxkData;

/**
 * @author lipan 2019年7月1日
 * @description （ 简单的描述 下：TODO ）
 *
 */
public class PwxkMq {
	
	StringBuffer sb = new StringBuffer(Integer.MAX_VALUE);
	
	static String MQ_SJBLX = "PWXKZB";

	public PwxkMq(List<Data> datas) {
		Data data = datas.get(0);
		String pwxkMq = "<?xml version=\"1.0\" encoding=\"GBK\"?>\r\n" + 
		"<Package>\r\n" + 
		"	<PackageHead>\r\n" + 
		"		<SJBBH>1</SJBBH>\r\n" + 
		"		<SJBLX>"+MQ_SJBLX+"</SJBLX>\r\n" + 
		"		<DWDM>"+((PwxkData)(data)).getTYSHXYDM()+"</DWDM>\r\n" + 
		"		<DWMC>"+((PwxkData)(data)).getDWMC()+"</DWMC>\r\n" + 
		"		<JLS>"+datas.size()+"</JLS>\r\n" + 
		"		<SCRQ>"+System.currentTimeMillis()/1000+"</SCRQ>\r\n" + 
		"	</PackageHead>\r\n" + 
		"	<Data>\r\n" + 
		
		"    </Data>\r\n" + 
		"</Package>	";

	}
}
