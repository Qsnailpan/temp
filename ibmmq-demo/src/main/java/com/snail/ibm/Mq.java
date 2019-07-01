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
//		connect();
		initHb2SwXml();
//		sendybMsg(initHb2SwXml()); // DZ_QRHB2SW
//		receiveMsg();

	}

	private static String initHb2SwXml() {
//		1.  数据库读取数据 如： 排污许可证推送数据
		List<Data> datas = PwxkData.init();
//		2. 对数据进行MQ结构化处理
		Hb2SwModel hb2SwModel = Hb2SwModel.init(datas);
		Object json = JSON.toJSON(hb2SwModel);
//		3.转MQ 报文结构
		String json2xml = StaxonUtils.json2xml(json.toString());
		System.out.println("转换后数据：-----\n" + json2xml);
		return json2xml;
	}

}
