import cn.hutool.core.util.StrUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.SocketException;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DDNS {

    static Logger log = LoggerFactory.getLogger(DDNS.class);
    /**
     * 获取主域名的所有解析记录列表
     */
    private DescribeSubDomainRecordsResponse describeSubDomainRecords(DescribeSubDomainRecordsRequest request, IAcsClient client) {
        try {
            // 调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            log.error("获取主域名的所有解析记录列表异常！！",e);
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
            log.error("修改解析记录异常！！",e);
            throw new RuntimeException();
        }
    }

    /**
     * 新增解析记录
     */
    private AddDomainRecordResponse addDomainRecord(AddDomainRecordRequest request, IAcsClient client) {
        try {
            //  调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            log.error("新增解析记录异常！！",e);
            //  发生调用错误，抛出运行时异常
            throw new RuntimeException();
        }
    }


    public static void main(String[] args) {
        try {
            Config.initConfig();
        }catch (Exception e){
            log.error("初始化配置文件失败！！",e);
        }

        //  设置鉴权参数，初始化客户端
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",// 地域ID
                Config.AccessKeyID,// 您的AccessKey ID
                Config.AccessKeySecret);// 您的AccessKey Secret
        IAcsClient client = new DefaultAcsClient(profile);

        // 创建定时器任务
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            try {
                ddnsOp(client);
            } catch (Exception e) {
                log.error("初始化配置文件失败！！",e);
            }
        }, 1, 300, TimeUnit.SECONDS);
    }

    private static void ddnsOp(IAcsClient client) {
        DDNS ddns = new DDNS();

        //查询指定二级域名的最新解析记录
        DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = new DescribeSubDomainRecordsRequest();
        describeSubDomainRecordsRequest.setSubDomain(Config.host);
        DescribeSubDomainRecordsResponse describeSubDomainRecordsResponse = ddns.describeSubDomainRecords(describeSubDomainRecordsRequest, client);
        log.info("describeSubDomainRecords:{}", describeSubDomainRecordsResponse);

        List<DescribeSubDomainRecordsResponse.Record> domainRecords = describeSubDomainRecordsResponse.getDomainRecords();
        //最新的一条解析记录
        if (domainRecords.size() != 0) {

            DescribeSubDomainRecordsResponse.Record record = domainRecords.get(0);
            //  记录ID
            String recordId = record.getRecordId();
            //  记录值
            String recordsValue = record.getValue();
            //  当前主机公网IP
            String currentHostIP = IPv6.getNextId();

            if (StrUtil.isBlank(currentHostIP)) {
                log.info("----------无法获取到主机ip-----------");
                return;
            }
            log.info("-------------------------------当前主机公网IP为：" + currentHostIP + "-------------------------------");
            if (!currentHostIP.equals(recordsValue)) {
                log.info("-------------------------------当前主机公网IP不一致，开始修改-------------------------------");
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
                log.info("修改后的ip信息:{}", updateDomainRecordResponse);
                log.info("-------------------------------修改结束-------------------------------");
            }else {
                log.info("-------------------------------当前主机公网IP一致，不需要修改-------------------------------");
            }

        } else {
            log.info("DDNS无该域名解析信息");
            //  当前主机公网IP
            String currentHostIP = IPv6.getNextId();
            //  当前主机公网IP
            if (StrUtil.isBlank(currentHostIP)) {
                log.info("----------无法获取到主机ip-----------");
                return;
            }
            log.info("-------------------------------当前主机公网IP为：" + currentHostIP + "-------------------------------");
            AddDomainRecordRequest addDomainRecordRequest = new AddDomainRecordRequest();
            addDomainRecordRequest.setDomainName(Config.host);
            addDomainRecordRequest.setRR("@");
            //  将主机记录值改为当前主机IP
            addDomainRecordRequest.setValue(currentHostIP);
            //  解析记录类型
            addDomainRecordRequest.setType("AAAA");
            AddDomainRecordResponse updateDomainRecordResponse = ddns.addDomainRecord(addDomainRecordRequest, client);
            log.info("addDomainRecord 新增ip信息", updateDomainRecordResponse);
        }
    }
}