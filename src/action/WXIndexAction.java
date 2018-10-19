package action;

import action.common.ResultJSONUtil;
import action.wx.*;
import com.alibaba.fastjson.JSONObject;
import common.PropertiesConf;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import servlet.BaseServlet;

import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName WXIndexAction
 * @Description TODO
 * @Author yanhuo
 * @Date 2018/10/15 14:47
 * @Version 1.0
 **/
@WebServlet(name = "WXIndex", urlPatterns = "/wxindex")
public class WXIndexAction extends BaseServlet{
    //下载登录二维码
    public String wxLogin(HttpServletResponse response) throws IOException {
        //1、初始化页面2、获取uuid绘制登录二维码3、获取二维码
        String appid = Login.index();
        String success = ResultJSONUtil.success(appid);
        return success;
    }

    //循环监听登录状态跳转进首页
    public String checkLonginStatus(){
        Login login= new Login();
        int cf;
        while (true) {
             cf = login.checklogin(WXConfig.appid);
            if (cf == 3) {
                System.out.println("已在手机端确认");
                break;
            }
            if (cf == 1) {
                continue;
            }
            try {
                Thread.sleep(13000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        login.login();
        //1、登录后获取参数，uin、skey、sid、pass_ticket 2、开启微信状态通知
        new WXinit();
        String success = ResultJSONUtil.success(String.valueOf(cf));
        return success;
    }

    /**
     * 获取好友列表
     * @return
     */

    public String getWXContact(){
        new Thread(){
            public void run(){
                //1、检查消息更新 2、获取最新消息 3、发送消息
                WXSync.WXSyncMessage();
            }
        }.start();
        //1、微信初始化，获取初始化信息（账号头像信息，聊天好友，阅读）
        String memberList = WXGetcontact.WXGetcontactList();
        String headImg="";
        if(!"https://wx2.qq.com/".equals(WXConfig.WXHost)) {
            headImg= WXConfig.WXHost.substring(0, 17)+WXConfig.headImgUrl;
        }else {
            headImg= WXConfig.WXHost.substring(0, 18)+WXConfig.headImgUrl;
        }
        JSONObject memberListJson = JSONObject.parseObject(memberList);
        JSONObject user = new JSONObject();
        user.put("headImgUrl",headImg);
        user.put("memberListJson",memberListJson);
        String success = ResultJSONUtil.success(user);
        return success;
    }

    //实时查询消息
    public String getWXMessage(){
        String FromUserName = WXConfig.UserName;
        String newMessage = WXSync.reMessage(null,FromUserName,null);
        return ResultJSONUtil.success(newMessage);
    }

    /**
     * 发送消息
     */
    public String sendMessage( String message, String ToUserName){
        String FromUserName = WXConfig.UserName;
        WXSendmsg.sendMsg(1, message, FromUserName, ToUserName);
        return ResultJSONUtil.success("0");
    }

    //发送图片消息
    public String sendImageMessage(HttpServletRequest request, HttpServletResponse response){
        try {
            String FromUserName = WXConfig.UserName;
            Map params = uploadImage(request,response);
            String imagePath = params.get("imagePath").toString();
            String ToUserName = params.get("ToUserName").toString();
            String uIn = WXConfig.wxuin;
            String sId = WXConfig.wxsid;
            String sKey = WXConfig.skey;
            WXSendmsg.sendFiled(imagePath, uIn, sId, sKey, FromUserName, ToUserName);
           return ResultJSONUtil.success("0");
        }catch (Exception e){
            e.printStackTrace();
            return ResultJSONUtil.success("1");
        }
    }
    public static Map<String,String> uploadImage(HttpServletRequest request,HttpServletResponse response) throws IOException {
        // 针对post请求，设置允许接收中文
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        Map params = new HashMap();
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //3、创建一个文件上传解析器
        ServletFileUpload upload = new ServletFileUpload(factory);
        //4、解决上传文件名的中文乱码
        upload.setHeaderEncoding("utf-8");
        try {
            List<FileItem> fileItems = upload.parseRequest(request);
            for (FileItem item : fileItems) {
                if(item.isFormField()){
                    /*params.put(item.getFieldName(), item.getString("utf-8"));
                    String send_time = (String) params.get("ToUserName");
                    System.out.println(send_time);*/
                    //获取用户具体输入的字符串，
                   String value = item.getString(); // 获取value属性的值
                    if (item.getFieldName().equals("ToUserName")) {
                        params.put("ToUserName",value);
                        System.out.println(value+"yeah");
                    }
                }else {
                    String fieldName2 = item.getName();
                    System.out.println(" filedName2: " + fieldName2);
                    try {
                        InputStream inputStream = item.getInputStream();
                        String filePath = PropertiesConf.WECHAT_IMAGE_LOACH_PATH;
                        String suffx = fieldName2.substring(fieldName2.lastIndexOf(".")).toLowerCase();
                        Random random = new Random();
                        String fileNameNew = random.nextInt(10000) + System.currentTimeMillis() + suffx;
                        uploadFile(inputStream, filePath, fileNameNew);
                        String imagePath = filePath + fileNameNew;
                        params.put("imagePath",imagePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        return params;
    }

    public  static void uploadFile(InputStream file,String filePath,String fileName) throws Exception {
        File targetFile = new File(filePath);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath+fileName);
        byte[] buffer = new byte[1024 * 1024];
        int length;
        while ((length = file.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.flush();
        out.close();
    }
}
