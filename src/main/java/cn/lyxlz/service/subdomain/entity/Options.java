package cn.lyxlz.service.subdomain.entity;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ObjUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Slf4j
public class Options {

    /**
     * 线程
     */
    private Integer threads;
    /**
     * 域
     */
    private String domain;
    /**
     * 字典
     */
    private String dict;
    /**
     * 深度
     */
    private Integer depth;
    /**
     * 帮助
     */
    private Boolean help;
    /**
     * 日志
     */
    private String log_;
    /**
     * DNS服务器
     */
    private String dnsServer;
    /**
     * 通配符域名
     */
    private Boolean wildcardDomain;
    /**
     * AXFC
     */
    private Boolean axfc;
    /**
     * 扫描列表文件名
     */
    private String scanListFN;
    /**
     * 扫描域名列表
     */
    private List<String> scanDomainList;

    public Options(String domain) {
        this.domain = domain;
        dict = "dict/subnames_full.txt";
        threads = 200;
        depth = 1;
        help = false;
        log_ = "./log";
        dnsServer = "114.114.114.114/8.8.8.8";
        wildcardDomain = false;
        axfc = false;
        scanDomainList = null;
        scanListFN = "";
    }

    private Boolean existsDomain() {
        this.scanDomainList = new CopyOnWriteArrayList<>();
        if (ObjUtil.isNotEmpty(this.scanListFN)) {
            File f = FileUtil.file(this.scanListFN);
            FileReader fileReader = new FileReader(f);
            List<String> lines = fileReader.readLines();
            scanDomainList.addAll(lines);
        }

        if (ObjUtil.isNotEmpty(this.scanDomainList.size())) {
            return true;
        }

        if (ObjUtil.isNotEmpty(this.domain)) {
            this.scanDomainList.add(this.domain);
            return true;
        }

        return false;
    }

    /**
     * 验证
     */
    public List<String> validate() {
        if (this.help) {
            // TODO 参数提示
            System.exit(0);
        }

        ArrayList<String> errorList = new ArrayList<>();
        if (!this.existsDomain()) {
            errorList.add("必须指定域名(-d)");
        }
        if (ObjUtil.isEmpty(this.threads)) {
            errorList.add("-t 的参数必须 > 0");
        }
        if (ObjUtil.isEmpty(this.depth)) {
            errorList.add("扫描深度(-depth) 必须 >= 1");
        }
        if (!FileUtil.isExistsAndNotDirectory(Path.of(this.dict), true)) {
            errorList.add("文件(-f) 必须存在");
        }
        if (ObjUtil.isEmpty(this.log_)) {
            String logDir = "log";
            if (!FileUtil.exist(logDir)) {
                FileUtil.mkdir(logDir);
            }
            log.debug("{}/{}.txt", logDir, this.domain);
        }
        if (ObjUtil.isEmpty(this.dnsServer)) {
            //=============================================
            // 114 DNS		114.114.114.114/114.114.115.115
            // 阿里 AliDNS	223.5.5.5/223.6.6.6
            // 百度 BaiduDNS	180.76.76.76
            // DNSPod DNS+	119.29.29.29/182.254.116.116
            // CNNIC SDNS	1.2.4.8/210.2.4.8
            // oneDNS		117.50.11.11/117.50.22.22
            // DNS 派
            // 电信/移动/铁通	101.226.4.6/218.30.118.6
            // DNS 派 联通	123.125.81.6/140.207.198.6
            // Google DNS	8.8.8.8/8.8.4.4
            // IBM Quad9	9.9.9.9
            // OpenDNS		208.67.222.222/208.67.220.220
            // V2EX DNS		199.91.73.222/178.79.131.110
            //=============================================
            this.dnsServer = "8.8.8.8/8.8.4.4";
            log.debug("dns未指定，启动默认dns: {}", this.dnsServer);
        }
        return errorList;
    }

    public void printOptions() {
        System.out.println(this);
    }
}

