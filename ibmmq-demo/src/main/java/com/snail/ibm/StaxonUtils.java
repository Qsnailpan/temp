package com.snail.ibm;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.snail.ibm.model.Data;
import com.snail.ibm.model.DataRoot;
import com.snail.ibm.model.Hb2SwModel;
import com.snail.ibm.model.PackageHead;
import com.snail.ibm.model.PwxkData;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;

/**
 * json <==> xml 互转工具
 * 
 * @author lipan
 *
 */
public class StaxonUtils {

	/**
	 * @Description: json string convert to xml string
	 */
	public static String json2xml(String json) {
		StringReader input = new StringReader(json);
		StringWriter output = new StringWriter();
		JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).repairingNamespaces(false).build();
		try {
			XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
			XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
			writer = new PrettyXMLEventWriter(writer);
			writer.add(reader);
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output.toString();
	}

	/**
	 * @Description: json string convert to xml string ewidepay ues only
	 */
	public static String json2xmlPay(String json) {
		StringReader input = new StringReader(json);
		StringWriter output = new StringWriter();
		JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).repairingNamespaces(false).build();
		try {
			XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
			XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
			writer = new PrettyXMLEventWriter(writer);
			writer.add(reader);
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (output.toString().length() >= 38) {// remove <?xml version="1.0" encoding="UTF-8"?>
			return "<?xml version=\"1.0\" encoding=\"GBK\"?>\n" + "<Package> \n" + output.toString().substring(39)
					+ "</Package>";
		}
		return output.toString();
	}

	/**
	 * @Description: xml string convert to json string
	 */
	public static String xml2json(String xml) {
		StringReader input = new StringReader(xml);
		StringWriter output = new StringWriter();
		JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
		try {
			XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
			XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);
			writer.add(reader);
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output.toString();
	}

	/**
	 * @Description: 去掉转换xml之后的换行和空格
	 */
	public static String json2xmlReplaceBlank(String json) {
		String str = StaxonUtils.json2xml(json);
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	public static void main(String[] args) {
		// def_test();
		Hb2SwModel hb2SwModel = initHb2swData();
		Object json = JSON.toJSON(hb2SwModel);
		String json2xml = StaxonUtils.json2xmlPay(json.toString());
		System.out.println(json2xml);
	}

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

//		 其他值。。。
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

	private static void def_test() {
		JSONObject json = new JSONObject();
		json.put("name", "jack");
		json.put("age", 25);
		String xmlstr = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><FromUserName><![CDATA[fromUser]]></FromUserName><CreateTime>1348831860</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[thisisatest]]></Content><MsgId>1234567890123456</MsgId></xml>";
		System.out.println("JSON-->XML:");
		System.out.println("JSON:" + json.toString());
		System.out.println("---------------------------------------------------------------");
		System.out.println("普通转XML带格式：\n" + StaxonUtils.json2xml(json.toString()));
		System.out.println("---------------------------------------------------------------");
		System.out.println("转XML去掉头部、前后补充<XML>：\n" + StaxonUtils.json2xmlPay(json.toString()));
		System.out.println("---------------------------------------------------------------");
		System.out.println("普通转XML去掉空格换行：\n" + StaxonUtils.json2xmlReplaceBlank(json.toString()));
		System.out.println("---------------------------------------------------------------");
		System.out.println("XML转JSON：\n" + StaxonUtils.xml2json(xmlstr));
	}

}
