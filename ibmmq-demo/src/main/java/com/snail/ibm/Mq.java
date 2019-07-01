package com.snail.ibm;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.snail.ibm.model.Data;
import com.snail.ibm.model.DataRoot;
import com.snail.ibm.model.Hb2SwModel;
import com.snail.ibm.model.PackageHead;
import com.snail.ibm.model.PwxkData;

public class Mq {
	static MQQueueManager qMgr;
	static int CCSID = 1381;
	static String queueString = "QRHB2SW";

	public static void connect() throws MQException {
		MQEnvironment.hostname = "10.194.5.80";
		MQEnvironment.channel = "CHANNEL";
		MQEnvironment.port = 1415;
		MQEnvironment.CCSID = CCSID;
		// MQEnvironment.userID = "MUSR_MQADMIN";
		// MQEnvironment.password = "123456";

		qMgr = new MQQueueManager("SWQMGR");
	}

	public static void sendMsg(String msgStr) {
		int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT | MQC.MQOO_INQUIRE | MQC.MQOO_SET_IDENTITY_CONTEXT
				| MQC.MQOO_FAIL_IF_QUIESCING;

		MQQueue queue = null;
		try {
			// 建立Q1通道的连接 QRHB2SW
			queue = qMgr.accessQueue("QRHB2SW", openOptions, null, null, null);
			MQMessage msg = new MQMessage();// 要写入队列的消息
			// msg.format = MQC.MQFMT_STRING;
			msg.characterSet = CCSID;
			msg.encoding = CCSID;

			// msg.correlationId =
			// "REQ\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0".getBytes();
			msg.persistence = MQC.MQPER_PERSISTENCE_AS_Q_DEF;

			// msg.writeObject(msgStr); //将消息写入消息对象中
			msg.write(msgStr.getBytes("GBK"));

			MQPutMessageOptions pmo = new MQPutMessageOptions();
			pmo.options = pmo.options + MQC.MQPMO_NEW_MSG_ID;
			pmo.options = pmo.options + MQC.MQPMO_SYNCPOINT;
			pmo.options = pmo.options + MQC.MQPMO_SET_IDENTITY_CONTEXT;

			// msg.expiry = -1; // 设置消息用不过期
			queue.put(msg, pmo);// 将消息放入队列
			qMgr.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (queue != null) {
				try {
					queue.close();
				} catch (MQException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void receiveMsg() {
		int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT | MQC.MQOO_INQUIRE;
		MQQueue queue = null;
		try {
			queue = qMgr.accessQueue("DZ_QLSW2HB", openOptions, null, null, null);
			System.out.println("该队列当前的深度为:" + queue.getCurrentDepth());
			System.out.println("===========================");
			int depth = queue.getCurrentDepth();
			// 将队列的里的消息读出来
			while (depth-- > 0) {
				MQMessage msg = new MQMessage();// 要读的队列的消息
				MQGetMessageOptions gmo = new MQGetMessageOptions();
				queue.get(msg, gmo);
				int dataLength = msg.getDataLength();
				String readString = msg.readStringOfByteLength(dataLength);
				System.out.println("消息的大小为：" + dataLength);
				String xml2json = StaxonUtils.xml2json(readString);
				System.out.println("消息的内容：\n" + xml2json);
				System.out.println("---------------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (queue != null) {
				try {
					queue.close();
				} catch (MQException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void sendybMsg(String msgStr) {
		MQPutMessageOptions pmo = new MQPutMessageOptions();
		pmo.options = pmo.options + MQC.MQPMO_NEW_MSG_ID;
		pmo.options = pmo.options + MQC.MQPMO_SYNCPOINT;
		pmo.options = pmo.options + MQC.MQPMO_SET_IDENTITY_CONTEXT;
		MQMessage outmsg = new MQMessage();
		outmsg.userId = "";
		outmsg.correlationId = "REQ\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0".getBytes();
		outmsg.persistence = MQC.MQPER_PERSISTENCE_AS_Q_DEF;
		outmsg.expiry = -1; // ?
		// outmsg.format=MQC.MQFMT_STRING;
		// outmsg.characterSet=819;
		// outmsg.encoding=819;
		int openOptions = MQC.MQOO_OUTPUT | MQC.MQOO_SET_IDENTITY_CONTEXT | MQC.MQOO_FAIL_IF_QUIESCING;

		MQQueue queue = null;

		try {
			queue = qMgr.accessQueue("QRHB2SW", openOptions, null, null, null);
			outmsg.write(msgStr.getBytes("GBK"));
			queue.put(outmsg, pmo);
			// msg.writeString(msgStr);
			qMgr.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws MQException {
		connect();
//		initHb2SwXml();
		 sendybMsg(initHb2SwXml()); // DZ_QRHB2SW
		// receiveMsg();/

	}

	private static String initHb2SwXml() {
		Hb2SwModel hb2SwModel = initHb2swData();
		Object json = JSON.toJSON(hb2SwModel);
		String json2xml = StaxonUtils.json2xml(json.toString());
		
//		xmlStr2 = xmlStr2.toUpperCase();
		System.out.println(xmlStr2);
		return xmlStr2;
	}
	static String xmlStr2 = "<?xml version=\"1.0\" encoding=\"GBK\"?>\r\n" + 
			"<Package>\r\n" + 
			"	<PackageHead>\r\n" + 
			"		<SJBBH>1</SJBBH>\r\n" + 
			"		<SJBLX>PWXKZB</SJBLX>\r\n" + 
			"		<DWDM>1</DWDM>\r\n" + 
			"		<DWMC>成都市友伦食品有限公司</DWMC>\r\n" + 
			"		<JLS>1</JLS>\r\n" + 
			"		<SCRQ>20190101</SCRQ>\r\n" + 
			"	</PackageHead>\r\n" + 
			"	<Data>\r\n" + 
			"		<Record>\r\n" + 
			"			<UUID>1</UUID>\r\n" + 
			"			<PWXKZBH>91510124684591103D001P</PWXKZBH>	\r\n" + 
			"			<DWMC>单位名称</DWMC>\r\n" + 
			"			<TYSHXYDM>统一社会信用代码</TYSHXYDM>\r\n" + 
			"			<ZZJGDM>组织机构代码</ZZJGDM>\r\n" + 
			"			<YYZZHM>营业执照号码</YYZZHM>\r\n" + 
			"			<HYLB>1</HYLB>\r\n" + 
			"			<SCJYCSDZ>生产经营场所地址</SCJYCSDZ>\r\n" + 
			"			<SZSFQHDM>510000</SZSFQHDM>\r\n" + 
			"			<SZDSQHDM>510100</SZDSQHDM>\r\n" + 
			"			<SZQXQHDM>510101</SZQXQHDM>\r\n" + 
			"			<JDXZDM>510101</JDXZDM>\r\n" + 
			"			<JSFZR>1</JSFZR>\r\n" + 
			"			<GDDH> </GDDH>\r\n" + 
			"			<YDDH> </YDDH>\r\n" + 
			"			<XKYXQXQ>20190701</XKYXQXQ>\r\n" + 
			"			<XKYXQXZ>20190701</XKYXQXZ>\r\n" + 
			"			<TCRQ>20190701</TCRQ>\r\n" + 
			"			<SFSYZDQY>1</SFSYZDQY>\r\n" + 
			"			<ZYWRWLB>1</ZYWRWLB>\r\n" + 
			"			<HFRQ>20190701</HFRQ>\r\n" + 
			"			<HFHBJGDM>1</HFHBJGDM>\r\n" + 
			"			<HFHBJGMC>1</HFHBJGMC>\r\n" + 
			"			<SJGS>510000</SJGS>\r\n" + 
			"			<DSGS>510100</DSGS>\r\n" + 
			"			<QXGS>510101</QXGS>\r\n" + 
			"			<SQLX>1</SQLX>\r\n" + 
			"		    <BJSJ>20190701</BJSJ>\r\n" + 
			"		</Record>\r\n" + 
			"	</Data>\r\n" + 
			"</Package>	";

	private static Hb2SwModel initHb2swData() {
		Hb2SwModel hb2SwModel = new Hb2SwModel();

		PackageHead packageHead = new PackageHead();
		packageHead.setDWDM("单位名称");
		packageHead.setDWMC("单位代码");
		packageHead.setSCRQ("20190701");
		packageHead.setSJBLX("PWXKZB");
		packageHead.setSJBBH("SJBBH");
		packageHead.setJLS("1");
		hb2SwModel.setPackageHead(packageHead);

		List<Data> datas = new ArrayList<>();

		PwxkData data1 = new PwxkData();
		data1.setDWMC("成都市友伦食品有限公司");
		data1.setTYSHXYDM("91510124684591103D");
		data1.setZZJGDM("/");
		data1.setPWXKZBH("91510124684591103D001P");
		data1.setZYWRWLB("总氮（以N计）,总磷（以P计）");
		data1.setSJGS("510000");
		data1.setDSGS("510100");
		data1.setQXGS("510101");

		data1.setSQLX("");
		datas.add(data1);

		PwxkData data2 = new PwxkData();
		data2.setBJSJ("22");
		datas.add(data2);

		DataRoot dataRoot = new DataRoot();
		dataRoot.setRecord(datas);
		hb2SwModel.setData(dataRoot);
		return hb2SwModel;
	}
}
