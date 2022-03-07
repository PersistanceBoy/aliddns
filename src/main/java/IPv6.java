

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liu
 * @time 2021/11/1
 * @description
 */
public class IPv6 {
    public static void main(String[] args) throws IOException {
        /*获取本机所有ip地址(包括保留地址，ipv4,ipv6  如果安装了虚拟机会更多其他的地址)
         * try {
            InetAddress ads = null;
            Enumeration<NetworkInterface>   adds = NetworkInterface.getNetworkInterfaces();
            while(adds.hasMoreElements()) {
            Enumeration<InetAddress> inetAds = adds.nextElement().getInetAddresses();
                while(inetAds.hasMoreElements()) {
                    ads = inetAds.nextElement();
                    LogUtil.logOut(ads.getHostAddress());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        //获取可用ipv6地址
//        try {
//            LogUtil.logOut(getLocalIPv6Address());
//        } catch (SocketException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

//       Boolean l= ping("ljailf520.top", 3);
//       LogUtil.logOut(l);

       //getCheckResult("64 bytes from liujia-ThinkPad-T440s (2409:8a55:3039:68c0::960): icmp_seq=1 TTL=64 time=0.080 ms");
       getCheckResult("From 2409:8a55:303a:1270:48b8:44b3:903d:454a icmp_seq=3 Destination unreachable: Address unreachable");


    }
    public static String getLocalIPv6Address() throws SocketException {
        InetAddress inetAddress =null;

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        outer:
        while(networkInterfaces.hasMoreElements()) {
            Enumeration<InetAddress> inetAds = networkInterfaces.nextElement().getInetAddresses();
            while(inetAds.hasMoreElements()) {
                inetAddress = inetAds.nextElement();
                //检查此地址是否是IPv6地址以及是否是保留地址
                if(inetAddress instanceof Inet6Address&& !isReservedAddr(inetAddress)) {
                    break outer;

                }
            }
        }
        String ipAddr = inetAddress.getHostAddress();
        //过滤网卡
        int index = ipAddr.indexOf('%');
        if(index>0) {
            ipAddr = ipAddr.substring(0, index);
        }

        return ipAddr;
    }
    private static boolean isReservedAddr(InetAddress inetAddr) {
        if(inetAddr.isAnyLocalAddress()||inetAddr.isLinkLocalAddress()||inetAddr.isLoopbackAddress())
        {
            return true;
        }
        return false;
    }

    public static boolean ping(String ipAddress, int pingTimes) {
        BufferedReader in = null;
        // 将要执行的ping命令,此命令是windows格式的命令
        Runtime r = Runtime.getRuntime();
        //String pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;
        String pingCommand = "ping6 " + ipAddress +" -c " +pingTimes;

        try {   // 执行命令并获取输出
            LogUtil.logOut(pingCommand);
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
     * 若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
     */
    private static int getCheckResult(String line) {
        LogUtil.logOut("控制台输出的结果为:"+line);
        String[] lines=line.split("=");
        String lessStr=lines[lines.length-1].split(" ")[0];
        try {

            if(line.contains("Unreachable")){
                return 0;
            }
            if(line.contains("unreachable")){
                return 0;
            }
            if(Double.valueOf(lessStr)>0){
                return 1;
            }
        }catch (Exception e){
            return 0;
        }
        return 0;
    }


}
