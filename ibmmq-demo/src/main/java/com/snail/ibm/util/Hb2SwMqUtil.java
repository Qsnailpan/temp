package com.snail.ibm.util;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.snail.ibm.model.Data;
import com.snail.ibm.model.Hb2SwModel;
import com.snail.ibm.model.MQBackData;
import com.snail.ibm.model.PwxkData;

public class Hb2SwMqUtil {
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

	/**
	 * 消费复核提请数据 入库
	 * 
	 * @param queueStr
	 * @return
	 * @throws MQException
	 */
	public static String receiveMsg(String queueStr) throws MQException {
		if (qMgr == null) {
			connect();
		}
		int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT | MQC.MQOO_INQUIRE;
		MQQueue queue = null;
		try { // DZ_QLSW2HB /
			queue = qMgr.accessQueue("QLSW2HB", openOptions, null, null, null);
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
				// System.out.println("消息的内容：\n" + readString);
				readString = readString.substring(readString.indexOf("<Package>"));
				System.out.println("截取消息的内容：\n" + readString);
				// 消息处理
				receiveFhtqMsg(readString);
				// 消息回执
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (queue != null) {
				try {
					queue.close();
				} catch (MQException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	/**
	 * 消费处理Mq ---> 复核提请数据
	 * 
	 * @param readString
	 */
	private static void receiveFhtqMsg(String fhtqStr) {
		System.out.println("---------------------------");
		String xml2json = StaxonUtils.xml2json(fhtqStr);
		System.out.println("xml --> json：\n" + xml2json);
		// 获取 数据包信息
		JSONObject parseObject = JSONObject.parseObject(fhtqStr);
		JSONObject rootJson = parseObject.getJSONObject("Package");
		JSONObject rootPackageHead = rootJson.getJSONObject("PackageHead");
		// 数据包编号、数据包类型
		String sjbbh = (String) rootPackageHead.get("SJBBH");
		String sjblx = (String) rootPackageHead.get("SJBLX");

		System.out.println("获取数据包编号、数据包类型：" + sjbbh + "," + sjblx);
		// 获取数据
		JSONObject jsonData = rootJson.getJSONObject("Data");
		JSONArray jsonRecordArray = jsonData.getJSONArray("Record");
		System.out.println("获取数据：" + jsonRecordArray);
		// 入库  TODO

		// 消息回执
		Object mQBackDataJson = Hb2SwModel.init(new MQBackData(sjblx, sjbbh));
		System.out.println(mQBackDataJson);
	}

	public static void sendybMsg(String msgStr) throws MQException {
		if (qMgr == null) {
			connect();
		}
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
			System.out.println("发送成功！");
		} catch (Exception e) {
			System.out.println("发送失败！");
			e.printStackTrace();
		}

	}

	public static void sendybMsg(List<Data> datas) throws MQException {
		// 2. 对数据进行MQ结构化处理
		Hb2SwModel hb2SwModel = Hb2SwModel.init(datas);
		Object json = JSON.toJSON(hb2SwModel);
		// 3.转MQ 报文结构
		String json2xml = StaxonUtils.json2xmlPay(json.toString());
		// System.out.println("转换前报文数据：-----\n" + json2xml);
		// 键第一个字母转大写
		// <packageHead></packageHead> --> <PackageHead></PackageHead>
		for (int i = 97; i <= 122; i++) {
			char c = (char) i;
			char C = (char) (i - 32);
			json2xml = json2xml.replaceAll("<" + c, "<" + C);
			json2xml = json2xml.replaceAll("</" + c, "</" + C);
		}
		System.out.println("转换后报文数据：-----\n" + json2xml);
		sendybMsg(json2xml);
	}

	public static void main(String[] args) throws MQException {
		// initHb2SwXml();
		// sendybMsg(initHb2SwXml()); // DZ_QRHB2SW
		// receiveMsg("");
		JSONObject parseObject = JSONObject.parseObject(fhtqStr);
		JSONObject rootJson = parseObject.getJSONObject("Package");
		JSONObject rootPackageHead = rootJson.getJSONObject("PackageHead");
		String sjbbh = (String) rootPackageHead.get("SJBBH");
		String sjblx = (String) rootPackageHead.get("SJBLX");
		System.out.println(sjbbh + "," + sjblx);
		JSONObject jsonData = rootJson.getJSONObject("Data");
		JSONArray jsonRecordArray = jsonData.getJSONArray("Record");
		System.out.println(jsonRecordArray);

		Object mQBackDataJson = Hb2SwModel.init(new MQBackData(sjblx, sjbbh));
		System.out.println(mQBackDataJson);
	}

	private static String initHb2SwXml() {
		// 1. 数据库读取数据 如： 排污许可证推送数据
		List<Data> datas = PwxkData.init();
		// 2. 对数据进行MQ结构化处理
		Hb2SwModel hb2SwModel = Hb2SwModel.init(datas);
		Object json = JSON.toJSON(hb2SwModel);
		// 3.转MQ 报文结构
		String json2xml = StaxonUtils.json2xmlPay(json.toString());
		System.out.println("转换前报文数据：-----\n" + json2xml);
		// 键第一个字母转大写
		// <packageHead></packageHead> --> <PackageHead></PackageHead>
		for (int i = 97; i <= 122; i++) {
			char c = (char) i;
			char C = (char) (i - 32);
			json2xml = json2xml.replaceAll("<" + c, "<" + C);
			json2xml = json2xml.replaceAll("</" + c, "</" + C);
		}
		System.out.println("转换后报文数据：-----\n" + json2xml);
		return json2xml;
	}

	/**
	 * 测试使用： MQ 消费复核提请示例数据
	 */
	static String fhtqStr = "{\r\n" + "  \"Package\": {\r\n" + "    \"PackageHead\": {\r\n"
			+ "      \"SJBBH\": \"15100020190702000939\",\r\n" + "      \"SJBLX\": \"FHTQXX\",\r\n"
			+ "      \"DWDM\": \"15100000000\",\r\n" + "      \"DWMC\": \"国家税务总局四川省税务局\",\r\n"
			+ "      \"JLS\": \"4\",\r\n" + "      \"SCRQ\": \"20190702155733\"\r\n" + "    },\r\n"
			+ "    \"Data\": {\r\n" + "      \"Record\": [\r\n" + "        {\r\n" + "          \"-index\": \"1\",\r\n"
			+ "          \"UUID\": \"6E1F0EEDD28E875259E69B2F856BC491\",\r\n"
			+ "          \"FHTQWSWYXH\": \"6E1F0EEDD28E875259E69B2F856BC491\",\r\n"
			+ "          \"WSZGZH\": \"彭税二税环核函〔2019〕1号\",\r\n" + "          \"JSFHHBJGBM\": \"510182000\",\r\n"
			+ "          \"NSRMC\": \"四川亚东水泥有限公司\",\r\n" + "          \"TYSHXYDM\": \"9151010076537555XN\",\r\n"
			+ "          \"HYLB\": \"3011\",\r\n" + "          \"FHSY\": \"04\",\r\n"
			+ "          \"YJ\": \"《中华人民共和国环境保护税法》第二十条及《中华人民共和国环境保护税法实施条例》第二十二条\",\r\n"
			+ "          \"XYFHDJTSX\": \"污染物种类、排放标准采用、浓度及排放量\",\r\n" + "          \"CZYCQXMS\": \"同行业纳税人环保税税负比\",\r\n"
			+ "          \"FHSQSJDQ\": \"20181001\",\r\n" + "          \"FHSQSJDZ\": \"20181231\",\r\n"
			+ "          \"WSZZJG\": \"15101826500\",\r\n" + "          \"WSZZRQ\": \"20190328\",\r\n"
			+ "          \"WSJSJGHB\": \"510182000\",\r\n" + "          \"WSCDJGSW\": \"15101826500\"\r\n"
			+ "        },\r\n" + "        {\r\n" + "          \"-index\": \"2\",\r\n"
			+ "          \"UUID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "          \"FHTQWSWYXH\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "          \"WSZGZH\": \"双税九税环核函〔2019〕1号\",\r\n" + "          \"JSFHHBJGBM\": \"510122000\",\r\n"
			+ "          \"NSRMC\": \"成都明语添祥包装有限公司\",\r\n" + "          \"TYSHXYDM\": \"91510122069763620Q\",\r\n"
			+ "          \"HYLB\": \"2926\",\r\n" + "          \"FHSY\": \"01\",\r\n"
			+ "          \"YJ\": \"《中华人民共和国环境保护税法》第二十条及《中华人民共和国环境保护税法实施条例》第二十二条\",\r\n"
			+ "          \"XYFHDJTSX\": \"请复核该企业2018年4季度环保申报数据是否正确。\",\r\n"
			+ "          \"CZYCQXMS\": \"环保设备投入时间为2017年底，但排放量却相差大。\",\r\n" + "          \"FHSQSJDQ\": \"20181001\",\r\n"
			+ "          \"FHSQSJDZ\": \"20181231\",\r\n" + "          \"WSZZJG\": \"15101220600\",\r\n"
			+ "          \"WSZZRQ\": \"20190402\",\r\n" + "          \"WSJSJGHB\": \"510122000\",\r\n"
			+ "          \"WSCDJGSW\": \"15101220600\",\r\n" + "          \"SWFHTQZBXX\": {\r\n"
			+ "            \"SWFHTQZB\": [\r\n" + "              {\r\n"
			+ "                \"UUID\": \"EA47BE42A51C34FB78A8351F6CBD1761\",\r\n"
			+ "                \"ID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "                \"SSYF\": \"201810\",\r\n" + "                \"WRWDMHB\": \"A21026\",\r\n"
			+ "                \"WRWMC\": \"二氧化硫（气）\",\r\n" + "                \"PFKBH\": \"FQ-01\",\r\n"
			+ "                \"PFKMC\": \"锅炉\",\r\n" + "                \"FSQPFL\": \"609000\",\r\n"
			+ "                \"SFAQNSSB\": \"Y\",\r\n" + "                \"SBWRWPFL\": \"2.9597\",\r\n"
			+ "                \"SCNDZ\": \"4.86\",\r\n" + "                \"ZXBZMC\": \"GB13271-2014\",\r\n"
			+ "                \"BZNDZ\": \"200\",\r\n" + "                \"GTFWLB\": \"101211101\"\r\n"
			+ "              },\r\n" + "              {\r\n"
			+ "                \"UUID\": \"631A49D64CC530CBAEB066B1807351CB\",\r\n"
			+ "                \"ID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "                \"SSYF\": \"201810\",\r\n" + "                \"WRWDMHB\": \"A21002\",\r\n"
			+ "                \"WRWMC\": \"氮氧化物（气）\",\r\n" + "                \"PFKBH\": \"FQ-01\",\r\n"
			+ "                \"PFKMC\": \"锅炉\",\r\n" + "                \"FSQPFL\": \"609000\",\r\n"
			+ "                \"SFAQNSSB\": \"Y\",\r\n" + "                \"SBWRWPFL\": \"9.8049\",\r\n"
			+ "                \"SCNDZ\": \"16.1\",\r\n" + "                \"ZXBZMC\": \"GB13271-2014\",\r\n"
			+ "                \"BZNDZ\": \"200\",\r\n" + "                \"GTFWLB\": \"101211102\"\r\n"
			+ "              },\r\n" + "              {\r\n"
			+ "                \"UUID\": \"9E1A16228C99CCEB0E61A38B4B481E91\",\r\n"
			+ "                \"ID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "                \"SSYF\": \"201811\",\r\n" + "                \"WRWDMHB\": \"A21026\",\r\n"
			+ "                \"WRWMC\": \"二氧化硫（气）\",\r\n" + "                \"PFKBH\": \"FQ-01\",\r\n"
			+ "                \"PFKMC\": \"锅炉\",\r\n" + "                \"FSQPFL\": \"633400\",\r\n"
			+ "                \"SFAQNSSB\": \"Y\",\r\n" + "                \"SBWRWPFL\": \"3.0783\",\r\n"
			+ "                \"SCNDZ\": \"4.86\",\r\n" + "                \"ZXBZMC\": \"GB13271-2014\",\r\n"
			+ "                \"BZNDZ\": \"200\",\r\n" + "                \"GTFWLB\": \"101211101\"\r\n"
			+ "              },\r\n" + "              {\r\n"
			+ "                \"UUID\": \"8E25C159BBDF17CE11812B40B23BAC94\",\r\n"
			+ "                \"ID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "                \"SSYF\": \"201811\",\r\n" + "                \"WRWDMHB\": \"A21002\",\r\n"
			+ "                \"WRWMC\": \"氮氧化物（气）\",\r\n" + "                \"PFKBH\": \"FQ-01\",\r\n"
			+ "                \"PFKMC\": \"锅炉\",\r\n" + "                \"FSQPFL\": \"633400\",\r\n"
			+ "                \"SFAQNSSB\": \"Y\",\r\n" + "                \"SBWRWPFL\": \"10.1977\",\r\n"
			+ "                \"SCNDZ\": \"16.1\",\r\n" + "                \"ZXBZMC\": \"GB13271-2014\",\r\n"
			+ "                \"BZNDZ\": \"200\",\r\n" + "                \"GTFWLB\": \"101211102\"\r\n"
			+ "              },\r\n" + "              {\r\n"
			+ "                \"UUID\": \"7430BE1C9A94AC1695405F2DAB81ACC1\",\r\n"
			+ "                \"ID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "                \"SSYF\": \"201812\",\r\n" + "                \"WRWDMHB\": \"A21026\",\r\n"
			+ "                \"WRWMC\": \"二氧化硫（气）\",\r\n" + "                \"PFKBH\": \"FQ-01\",\r\n"
			+ "                \"PFKMC\": \"锅炉\",\r\n" + "                \"FSQPFL\": \"584600\",\r\n"
			+ "                \"SFAQNSSB\": \"Y\",\r\n" + "                \"SBWRWPFL\": \"2.8411\",\r\n"
			+ "                \"SCNDZ\": \"4.86\",\r\n" + "                \"ZXBZMC\": \"GB13271-2014\",\r\n"
			+ "                \"BZNDZ\": \"200\",\r\n" + "                \"GTFWLB\": \"101211101\"\r\n"
			+ "              },\r\n" + "              {\r\n"
			+ "                \"UUID\": \"B45232D9AAF086C28ECAFA907641F5D4\",\r\n"
			+ "                \"ID\": \"7B4E21C8CDF0897C602B0B45B1186376\",\r\n"
			+ "                \"SSYF\": \"201812\",\r\n" + "                \"WRWDMHB\": \"A21002\",\r\n"
			+ "                \"WRWMC\": \"氮氧化物（气）\",\r\n" + "                \"PFKBH\": \"FQ-01\",\r\n"
			+ "                \"PFKMC\": \"锅炉\",\r\n" + "                \"FSQPFL\": \"584600\",\r\n"
			+ "                \"SFAQNSSB\": \"Y\",\r\n" + "                \"SBWRWPFL\": \"9.412\",\r\n"
			+ "                \"SCNDZ\": \"16.1\",\r\n" + "                \"ZXBZMC\": \"GB13271-2014\",\r\n"
			+ "                \"BZNDZ\": \"200\",\r\n" + "                \"GTFWLB\": \"101211102\"\r\n"
			+ "              }\r\n" + "            ]\r\n" + "          }\r\n" + "        },\r\n" + "        {\r\n"
			+ "          \"-index\": \"3\",\r\n" + "          \"UUID\": \"3e85695b98b74ed79b70ce6113c15db2\",\r\n"
			+ "          \"FHTQWSWYXH\": \"3e85695b98b74ed79b70ce6113c15db2\",\r\n"
			+ "          \"WSZGZH\": \"米税垭税环核函〔2019〕2号\",\r\n" + "          \"JSFHHBJGBM\": \"510421000\",\r\n"
			+ "          \"NSRMC\": \"攀枝花兴辰钒钛有限公司\",\r\n" + "          \"TYSHXYDM\": \"9151042172744223XA\",\r\n"
			+ "          \"HYLB\": \"3219\",\r\n" + "          \"FHSY\": \"04\",\r\n"
			+ "          \"YJ\": \"《中华人民共和国环境保护税法》第二十条及《中华人民共和国环境保护税法实施条例》第二十二条\r\n" + "\",\r\n"
			+ "          \"XYFHDJTSX\": \"1.米环罚【2018】016号处罚文书中该纳税人2018年12月违反水污染防治管理制度，被处罚96万元，请提供该纳税人2018年12月违反规定排放污染物的种类、数量和浓度值。\r\n"
			+ "2.请提供2019年1月至3月该纳税人各类大气污染物、水污染物的种类、数量和浓度值。\",\r\n"
			+ "          \"CZYCQXMS\": \"申报的污染物浓度值与米环罚【2018】016号处罚文书中的违法事实不一致\",\r\n"
			+ "          \"FHSQSJDQ\": \"20181201\",\r\n" + "          \"FHSQSJDZ\": \"20190331\",\r\n"
			+ "          \"WSZZJG\": \"15104211000\",\r\n" + "          \"WSZZRQ\": \"20190508\",\r\n"
			+ "          \"WSJSJGHB\": \"510421000\",\r\n" + "          \"WSCDJGSW\": \"15104211000\"\r\n"
			+ "        },\r\n" + "        {\r\n" + "          \"-index\": \"4\",\r\n"
			+ "          \"UUID\": \"66FC50E6A07C20148002B757AB792A73\",\r\n"
			+ "          \"FHTQWSWYXH\": \"66FC50E6A07C20148002B757AB792A73\",\r\n"
			+ "          \"WSZGZH\": \"江油税彰税环核函〔2019〕1号\",\r\n" + "          \"JSFHHBJGBM\": \"510781000\",\r\n"
			+ "          \"NSRMC\": \"江油中科成污水净化有限公司\",\r\n" + "          \"TYSHXYDM\": \"91510781746921607A\",\r\n"
			+ "          \"HYLB\": \"7721\",\r\n" + "          \"FHSY\": \"01\",\r\n"
			+ "          \"YJ\": \"《中华人民共和国环境保护税法》第二十条及《中华人民共和国环境保护税法实施条例》第二十二条\r\n" + "\",\r\n"
			+ "          \"XYFHDJTSX\": \"下发的自动检测数据显示：江油中科成污水净化有限公司2019年3月化学需氧量(COD)当月实测最高排放浓度为74.605mg/L，实际浓度为23.42mg/L，下发数据有误。\",\r\n"
			+ "          \"CZYCQXMS\": \"下发的自动检测数据和实际数据不符：经核实，2019年3月8日17时40分，自动检测设备售后人员进行高浓度标准溶液设备测试，导致江油中科成污水净化有限公司上传至环保局平台的平均数据为74.605mg/L，浓度超标；2019年3月8日18时40分回复正常值28.76mg/L。\",\r\n"
			+ "          \"FHSQSJDQ\": \"20190301\",\r\n" + "          \"FHSQSJDZ\": \"20190331\",\r\n"
			+ "          \"WSZZJG\": \"15107812700\",\r\n" + "          \"WSZZRQ\": \"20190514\",\r\n"
			+ "          \"WSJSJGHB\": \"510781000\",\r\n" + "          \"WSCDJGSW\": \"15107812700\"\r\n"
			+ "        }\r\n" + "      ]\r\n" + "    }\r\n" + "  }\r\n" + "}";
}
