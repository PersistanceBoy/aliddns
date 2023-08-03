
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author liu
 * @time 2021/11/1
 * @description
 */
public class IPv6 {

    static Logger log = LoggerFactory.getLogger(IPv6.class);
    private static final CopyOnWriteArrayList<String> ipv6s = new CopyOnWriteArrayList<>();

    //随机数
    static Random random = new Random();
    static {
        // 创建定时器任务
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> refreshIps(),1,60,TimeUnit.SECONDS);
    }



    private static synchronized void refreshIps() {
        log.info("开始刷新ip");
        try {
            List<String> getIpv6s = getLocalIPv6Address();
            if (getIpv6s.isEmpty()) {
                return;
            }
            List<String> temp=new ArrayList<>();
            List<String> finalTemp = temp;
            getIpv6s.forEach(ip -> {
                try {
                    if (pingTest(ip)) {
                        finalTemp.add(ip);
                    }
                }catch (Exception e){
                    log.error("pingTest 异常",e);
                }
            });
            ipv6s.clear();
            temp= temp.stream().distinct().collect(Collectors.toList());
            ipv6s.addAll(temp);
            if (ipv6s.isEmpty()) {
                ipv6s.addAll(getIpv6s);
            }
        } catch (Exception e) {
            log.error("refreshIps 异常",e);
        }
        log.info("结束刷新ip");
    }

    public static String getNextId(){
        if(ipv6s.isEmpty()){
            refreshIps();
        }
        if(ipv6s.isEmpty()){
            return "";
        }
        int i2 = random.nextInt(1000);
        int flag=i2%ipv6s.size();
        return ipv6s.get(flag);
    }

    public static List<String> getLocalIPv6Address() throws SocketException {
        InetAddress inetAddress = null;
        List<String> ipv6s = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            Enumeration<InetAddress> inetAds = networkInterfaces.nextElement().getInetAddresses();
            while (inetAds.hasMoreElements()) {
                inetAddress = inetAds.nextElement();
                //检查此地址是否是IPv6地址以及是否是保留地址
                if (inetAddress instanceof Inet6Address && !isReservedAddr(inetAddress)) {
                    String ipAddr = inetAddress.getHostAddress();
                    //过滤网卡
                    int index = ipAddr.indexOf('%');
                    if (index > 0) {
                        ipAddr = ipAddr.substring(0, index);
                    }
                    ipv6s.add(ipAddr);
                }
            }
        }
        log.info("ip数据{}",ipv6s);
        return ipv6s;
    }


    private static boolean isReservedAddr(InetAddress inetAddr) {
        if (inetAddr.isAnyLocalAddress() || inetAddr.isLinkLocalAddress() || inetAddr.isLoopbackAddress()) {
            return true;
        }
        return false;
    }

    public static boolean ping(String ipAddress, int pingTimes) {
        BufferedReader in = null;
        // 将要执行的ping命令,此命令是windows格式的命令
        Runtime r = Runtime.getRuntime();
        //String pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;
        String pingCommand = "ping6 " + ipAddress + " -c " + pingTimes;

        try {   // 执行命令并获取输出
            log.info(pingCommand);
            Process p = r.exec(pingCommand);
            if (p == null) {
                return false;
            }
            // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            int connectedCount = 0;
            String line = null;
            while ((line = in.readLine()) != null) {
                connectedCount += getCheckResult(line);
            }   // 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真
            return connectedCount >= pingTimes;
        } catch (Exception ex) {
            ex.printStackTrace();   // 出现异常则返回假
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 用外网测试网站来测试https://ipw.cn/api/ping/ipv6
     */
    private static boolean pingTest(String ipaddr) {
        String pingAddr = String.format("https://ipw.cn/api/ping/ipv6/%s/4/all", ipaddr);
        String reStr=HttpUtil.get(pingAddr);
        JSONObject json = JSONUtil.parseObj(reStr);
        JSONArray jsonArray = json.getJSONArray("pingResultDetail");
        for (Object jsonr : jsonArray) {
            JSONObject jsonResult = (JSONObject) jsonr;
            Boolean result = jsonResult.getBool("result");
            if (result) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        pingTest("sdfs");
    }
    /**
     * 若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
     */
    private static int getCheckResult(String line) {
        log.info("控制台输出的结果为:" + line);
        String[] lines = line.split("=");
        String lessStr = lines[lines.length - 1].split(" ")[0];
        try {

            if (line.contains("Unreachable")) {
                return 0;
            }
            if (line.contains("unreachable")) {
                return 0;
            }
            if (Double.valueOf(lessStr) > 0) {
                return 1;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }


}
