package action.wx;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WXSync {
	static Map<String, String> mapMsg = new HashMap<>();
	public static void WXSyncMessage() {
		while (true) {
			String url4 = WXConfig.WXHost+"cgi-bin/mmwebwx-bin/webwxsync?sid=" + WXConfig.wxsid + "&skey=" + WXConfig.skey + "&lang=zh_CN&pass_ticket=" + WXConfig.pass_ticket;
			//System.out.println("循环查询最新消息："+url4);
			HttpPost method4 = new HttpPost(url4);
			method4.setHeader("Accept", "application/json, text/plain, */*");
			method4.setHeader("Content-Type", "application/json;charset=UTF-8");
			System.out.println("循环查看最新消息-----------------------------------");


			JSONObject job = new JSONObject();

			job.put("Uin", WXConfig.wxuin);
			job.put("Sid", WXConfig.wxsid);
			job.put("Skey", WXConfig.skey);
			job.put("DeviceID", "e" + WXConfig.randomNum(15));

			JSONObject webwxsyncJ = new JSONObject();
			webwxsyncJ.put("BaseRequest", job);
			webwxsyncJ.put("SyncKey", WXConfig.SyncKey);
			int now = (int) System.currentTimeMillis();
			webwxsyncJ.put("rr", (~now) + "");

			//System.out.println(webwxsyncJ.toString());
			try {
				StringEntity stringEntity = new StringEntity(webwxsyncJ.toString());
				method4.setEntity(stringEntity);

				HttpResponse response = WXConfig.https.execute(method4);
				HttpEntity entitySort = response.getEntity();
				String html = EntityUtils.toString(entitySort, "utf-8");
				//System.out.println(html);

				JSONObject jooo = JSONObject.parseObject(html);
				WXConfig.SyncKey = jooo.getJSONObject("SyncKey");

				JSONArray AddMsgList = jooo.getJSONArray("AddMsgList");
				for (int i = 0; i < AddMsgList.size(); i++) {
					JSONObject AddMsg = AddMsgList.getJSONObject(i);
					 String Content = AddMsg.getString("Content");
					System.out.println("收到最新消息："+Content);
					String fromUserName = AddMsg.getString("FromUserName");
					String toUserName = AddMsg.getString("ToUserName");
					reMessage(Content,fromUserName,toUserName);

					WXFilter.filter(Content, AddMsg.getString("ToUserName"), AddMsg.getString("FromUserName"));

				}
				Thread.sleep(5000);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static String reMessage(String message,String FromUserName,String ToUserName){
		String messages = "";
		if(null == message || "".equals(message)) {
			messages = mapMsg.get(FromUserName);
			mapMsg.remove(FromUserName);
		}else {
			mapMsg.put(ToUserName, message);
		}
		return messages;
	}
}