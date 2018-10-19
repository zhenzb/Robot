package action.wx;


import action.common.HttpClient;

public class WXFilter {
	public static void filter(String Content, String FromUserName, String ToUserName) {
		if (Content.equals("会员")) {
			WXSendmsg.sendMsg(1, "您好，您需要什么会员,请回复001，爱奇艺。。。", FromUserName, ToUserName);
		} else if (Content.equals("001")) {
			String phone = HttpClient.sendHttp("http://172.16.40.187/wx/gp");
			if (phone.equals("0")) {
				WXSendmsg.sendMsg(1, "暂时没有空闲，请稍后重试", FromUserName, ToUserName);
			} else {
				new Thread() {
					public void run() {
						WXSendmsg.sendMsg(1, "账号:" + phone + ";请尽快登录。", FromUserName, ToUserName);
						for (int i = 0; 1 < 100; i++) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							String sms = HttpClient.sendHttp("http://172.16.40.187/wx/gsms?phone=" + phone);
							if (sms.equals("")) {
								continue;
							} else {
								WXSendmsg.sendMsg(1, sms, FromUserName, ToUserName);
								i = 100;
								break;
							}
						}
					};
				}.start();
			}
		}else if(Content.equals("002")){
			String filePath = "D:\\picture/cc.jpg";
			String uIn = WXConfig.wxuin;
			String sId = WXConfig.wxsid;
			String sKey = WXConfig.skey;
			String fileName = "cc.jpg";
			//(String filePath,String cookie,String uIn,String sId,String sKey,String fileName,String fromUserName,String toUserName)
			WXSendmsg.sendFiled(filePath,uIn,sId,sKey,FromUserName,ToUserName);
		}
	}
}