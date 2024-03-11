package cn.lyxlz.service.subdomain;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.thread.lock.LockUtil;
import cn.hutool.core.util.ObjUtil;
import cn.lyxlz.service.subdomain.entity.Options;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

@Data
@Slf4j
public class SubdomainScanner {

    /**
     * 选项
     */
    private Options options;
    /**
     * 字典列表
     */
    private volatile Queue<String> wordList;
    /**
     * 黑名单
     */
    private List<String> blackList;

    public SubdomainScanner(Options options) {
        this.options = options;
        // 读取子域名字典
        FileReader fileReader = FileReader.create(FileUtil.file(options.getDict()));
        List<String> lines = fileReader.readLines();
        wordList = new ConcurrentLinkedQueue<>(lines);
    }

    /**
     * 启动扫描
     */
    public void start() {
        // 扫描任务
        double size = wordList.size();
        CountDownLatch latch = new CountDownLatch(Convert.toInt(size));
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        while (!wordList.isEmpty()) {
            for (int i = 0; i < options.getThreads(); i++) {
                String subName = wordList.poll();
                if (ObjUtil.isNull(subName)) break;
                String domain = subName + "." + options.getDomain();
                executor.execute(new SubdomainScannerTask(domain, latch));
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
    }
}

class SubdomainScannerTask implements Runnable {
    private final String domain;
    private final CountDownLatch latch;

    public SubdomainScannerTask(String domainName, CountDownLatch latch) {
        this.domain = domainName;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            InetAddress address = InetAddress.getByName(domain);
            System.out.println("IP Address of " + domain + ": " + address.getHostAddress());
        } catch (UnknownHostException ignored) {
        } finally {
            latch.countDown();
        }
    }
}
