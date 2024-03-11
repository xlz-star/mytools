package cn.lyxlz;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.lyxlz.service.subdomain.SubdomainScanner;
import cn.lyxlz.service.subdomain.entity.Options;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.net.UnknownHostException;


@Slf4j
public class App {
    public static void main(String[] args) throws UnknownHostException, TextParseException {
        TimeInterval timer = DateUtil.timer();
        Options options = new Options("lyxlz.cn");
        SubdomainScanner scanner = new SubdomainScanner(options);
        scanner.start();
        long runTime = timer.intervalSecond();
        System.out.println("执行结束, 花费时间: " + runTime + "s");
    }
}
