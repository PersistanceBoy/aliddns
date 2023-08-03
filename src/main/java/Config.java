import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class Config {
    static Logger log = LoggerFactory.getLogger(Config.class);
    public static  String host="";

    public static  String AccessKeyID="";

    public static  String AccessKeySecret="";

    public static void initConfig(){
        if(StrUtil.isNotBlank(host) && StrUtil.isNotBlank(AccessKeyID) && StrUtil.isNotBlank(AccessKeySecret)){
            log.info("host:"+host);
            log.info("AccessKeyID:"+AccessKeyID);
            log.info("AccessKeySecret:"+AccessKeySecret);
            return;
        }

        String jarPath = System.getProperty("java.class.path");
        int firstIndex = jarPath.lastIndexOf(System.getProperty("path.separator")) + 1;
        int lastIndex = jarPath.lastIndexOf(File.separator) + 1;
        jarPath = jarPath.substring(firstIndex, lastIndex);

        log.info("jarPath:::"+jarPath);
        File file=new File(jarPath+File.separator+"config.json");

        if(!file.exists()){
            log.info("initConfig config.json文件不存在");
            return;
        }

        JSONObject json= JSONUtil.readJSONObject(file, StandardCharsets.UTF_8);

        String host=json.getStr("host");
        String AccessKeyID=json.getStr("AccessKeyID");
        String AccessKeySecret=json.getStr("AccessKeySecret");
        if(StrUtil.isBlank(host) || StrUtil.isBlank(AccessKeyID) || StrUtil.isBlank(AccessKeySecret)){

            log.info("参数配置出现问题！！！！！！！！");
            throw new RuntimeException("参数配置出现问题！！！！！！！！");
        }
        Config.host=host;
        Config.AccessKeyID=AccessKeyID;
        Config.AccessKeySecret=AccessKeySecret;
    }

    public static void main(String[] args) throws IOException {
        //initConfig();
        String path = Config.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = java.net.URLDecoder.decode(path, "UTF-8");

        log.info("方式1"+path);
        String path1 = System.getProperty("java.class.path");
        int firstIndex = path1.lastIndexOf(System.getProperty("path.separator")) + 1;
        int lastIndex = path1.lastIndexOf(File.separator) + 1;
        path1 = path1.substring(firstIndex, lastIndex);
        log.info("方式1"+path1);

        log.info("方式3"+System.getProperty("user.dir"));
    }


}
