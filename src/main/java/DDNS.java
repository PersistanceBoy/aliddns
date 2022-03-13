import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;


import java.net.SocketException;

import java.util.List;

public class DDNS {
    public static final String host="ljailf520.top";

    public static final String AccessKeyID="LTAI5tPPM4NfTogQjBhPqXL1";

    public static final String AccessKeySecret="FhkD9BjJH91xTgxBksVZLiPEXutHkt";
    /**
     * 获取主域名的所有解析记录列表
     */
    private DescribeSubDomainRecordsResponse describeSubDomainRecords(DescribeSubDomainRecordsRequest request, IAcsClient client) {
        try {
            // 调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            // 发生调用错误，抛出运行时异常
            throw new RuntimeException();
        }
    }

    /**
     * 修改解析记录
     */
    private UpdateDomainRecordResponse updateDomainRecord(UpdateDomainRecordRequest request, IAcsClient client) {
        try {
            //  调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            //  发生调用错误，抛出运行时异常
            throw new RuntimeException();
        }
    }

    /**
     * 修改解析记录
     */
    private AddDomainRecordResponse addDomainRecord(AddDomainRecordRequest request, IAcsClient client) {
        try {
            //  调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            //  发生调用错误，抛出运行时异常
            throw new RuntimeException();
        }
    }



    public static void main(String[] args) throws InterruptedException {
        //  设置鉴权参数，初始化客户端
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",// 地域ID
                AccessKeyID,// 您的AccessKey ID
                AccessKeySecret);// 您的AccessKey Secret
        IAcsClient client = new DefaultAcsClient(profile);

        while (true) {
            try {
//                int i=0;
//                boolean flag=false;
//                while (i<3){
//                    i++;
//                    if(IPv6.ping(host,3)){
//                        flag=true;
//                        break;
//                    }
//                }
//                if(!flag){
//                    LogUtil.logOut("自测地址失败！！！尝试更新ipv6地址");
//                    ddnsOp(client);
//                }
                ddnsOp(client);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.logOut(e);
            }finally {
                Thread.sleep(1000 * 300);
            }
        }
    }

    private static void ddnsOp(IAcsClient client) throws SocketException {
        DDNS ddns = new DDNS();

        //查询指定二级域名的最新解析记录
        DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = new DescribeSubDomainRecordsRequest();
        describeSubDomainRecordsRequest.setSubDomain(host);
        DescribeSubDomainRecordsResponse describeSubDomainRecordsResponse = ddns.describeSubDomainRecords(describeSubDomainRecordsRequest, client);
        LogUtil.log_print("describeSubDomainRecords", describeSubDomainRecordsResponse);

        List<DescribeSubDomainRecordsResponse.Record> domainRecords = describeSubDomainRecordsResponse.getDomainRecords();
        //最新的一条解析记录
        if (domainRecords.size() != 0) {

            DescribeSubDomainRecordsResponse.Record record = domainRecords.get(0);
            //  记录ID
            String recordId = record.getRecordId();
            //  记录值
            String recordsValue = record.getValue();
            //  当前主机公网IP
            String currentHostIP = IPv6.getLocalIPv6Address();
            LogUtil.logOut("-------------------------------当前主机公网IP为：" + currentHostIP + "-------------------------------");
            if (!currentHostIP.equals(recordsValue)) {
                LogUtil.logOut("与记录不一致，尝试修改地址！");
                //  修改解析记录
                UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
                //  主机记录
                updateDomainRecordRequest.setRR("@");
                //  记录ID
                updateDomainRecordRequest.setRecordId(recordId);
                //  将主机记录值改为当前主机IP
                updateDomainRecordRequest.setValue(currentHostIP);
                //  解析记录类型
                updateDomainRecordRequest.setType("AAAA");
                UpdateDomainRecordResponse updateDomainRecordResponse = ddns.updateDomainRecord(updateDomainRecordRequest, client);
                LogUtil.log_print("updateDomainRecord", updateDomainRecordResponse);
            }
        }else{

            //  当前主机公网IP
            String currentHostIP = IPv6.getLocalIPv6Address();
            LogUtil.logOut("-------------------------------当前主机公网IP为：" + currentHostIP + "-------------------------------");
            AddDomainRecordRequest addDomainRecordRequest=new AddDomainRecordRequest();
            addDomainRecordRequest.setDomainName(host);
            addDomainRecordRequest.setRR("@");

//                    //  修改解析记录
//                    UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
//                    //  主机记录
//                    updateDomainRecordRequest.setRR("@");
//                    //  记录ID
//                    updateDomainRecordRequest.setRecordId(recordId);
            //  将主机记录值改为当前主机IP
            addDomainRecordRequest.setValue(currentHostIP);
            //  解析记录类型
            addDomainRecordRequest.setType("AAAA");
            AddDomainRecordResponse updateDomainRecordResponse = ddns.addDomainRecord(addDomainRecordRequest, client);
            LogUtil.log_print("updateDomainRecord", updateDomainRecordResponse);
        }
    }
}