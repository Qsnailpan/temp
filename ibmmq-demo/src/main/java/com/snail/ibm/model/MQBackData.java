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
public class MQBackData extends Data {

	private String SJBLX;
	private String SJBBH;

	
	public MQBackData() {
		
	}
	public MQBackData(String sJBLX, String sJBBH) {
		SJBLX = sJBLX;
		SJBBH = sJBBH;
	}
}
