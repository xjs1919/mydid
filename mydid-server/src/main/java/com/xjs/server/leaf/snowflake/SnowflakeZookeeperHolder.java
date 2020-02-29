package com.xjs.server.leaf.snowflake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author https://github.com/Meituan-Dianping/Leaf
 */
@Slf4j
public class SnowflakeZookeeperHolder {

    /**根节点*/
    private static final String PREFIX_ZK_PATH = "/snowflake/com.mamcharge.techc";
    /**保存所有数据持久的节点*/
    private static final String PATH_FOREVER = PREFIX_ZK_PATH + "/forever";
    /**workerId本地文件缓存*/
    private static final String PROP_PATH = System.getProperty("java.io.tmpdir") + File.separator  + "com.mamcharge.techc/leafconf/{port}/workerID.properties";

    /**worker自身的节点路径 ip:port-000000001*/
    private String workerZkNodePath;

    /**本服务的ip、port、zk地址*/
    private String ip;
    private String port;
    private String connectionString;

    /**本服务的workerId*/
    private int workerID;
    /**向zk上传的上次更新时间*/
    private long lastUpdateTime;

    public SnowflakeZookeeperHolder(String ip, String port, String connectionString) {
        this.ip = ip;
        this.port = port;
        this.connectionString = connectionString;
    }

    public boolean init() {
        try {
            CuratorFramework curator = createWithOptions(connectionString, new RetryUntilElapsed(1000, 4), 10000, 6000);
            curator.start();
            Stat stat = curator.checkExists().forPath(PATH_FOREVER);
            if (stat == null) {
                //不存在根节点,机器第一次启动,创建 forever/ip:port-000000000,并上传数据
                workerZkNodePath = createNode(curator);
                //worker id 默认是0
                updateLocalWorkerID(0);
                //定时上报本机时间给forever节点
                doService(curator);
                return true;
            } else {
                //ip:port->00001
                Map<String, Integer> nodeMap = Maps.newHashMap();
                //ip:port->(ipport-000001)
                Map<String, String> realNode = Maps.newHashMap();
                //存在根节点,先检查是否有属于自己的根节点
                List<String> keys = curator.getChildren().forPath(PATH_FOREVER);
                for (String key : keys) {
                    String[] nodeKey = key.split("-");
                    realNode.put(nodeKey[0], key);
                    nodeMap.put(nodeKey[0], Integer.parseInt(nodeKey[1]));
                }
                Integer workerid = nodeMap.get(ip + ":" + port);
                if (workerid != null) {
                    //有自己的节点,zk_AddressNode=ip:port
                    workerZkNodePath = PATH_FOREVER + "/" + realNode.get(ip + ":" + port);
                    //启动worder时使用会使用
                    if (!checkInitTimeStamp(curator, workerZkNodePath)){
                        throw new CheckLastTimeException("init timestamp check error,forever node timestamp gt this node time");
                    }
                    updateLocalWorkerID(workerid);
                    doService(curator);
                    log.info("[Old NODE]find forever node have this endpoint ip-{} port-{} workid-{} childnode and start SUCCESS", ip, port, workerid);
                } else {
                    //表示新启动的节点,创建持久节点 ,不用check时间
                    String newNode = createNode(curator);
                    workerZkNodePath = newNode;
                    String[] nodeKey = newNode.split("-");
                    int workerID = Integer.parseInt(nodeKey[1]);
                    updateLocalWorkerID(workerID);
                    doService(curator);
                    log.info("[New NODE]can not find node on forever node that endpoint ip-{} port-{} workid-{},create own node on forever node and start SUCCESS ", ip, port, workerID);
                }
            }
        } catch (Exception e) {
            log.error("Start node ERROR {}", e);
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(new File(PROP_PATH.replace("{port}", port + ""))));
                int workerID = Integer.valueOf(properties.getProperty("workerID"));
                log.warn("START FAILED ,use local node file properties workerID-{}", workerID);
                this.workerID = workerID;
            } catch (Exception e1) {
                log.error("Read file error ", e1);
                return false;
            }
        }
        return true;
    }

    private void doService(CuratorFramework curator) {
         //snowflake_forever/ip:port-000000001
        ScheduledUploadData(curator, workerZkNodePath);
    }

    private void ScheduledUploadData(final CuratorFramework curator, final String zk_AddressNode) {
        //每3s上报数据
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "schedule-upload-time");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateNewData(curator, zk_AddressNode);
            }
        }, 1L, 3L, TimeUnit.SECONDS);

    }

    private boolean checkInitTimeStamp(CuratorFramework curator, String zk_AddressNode) throws Exception {
        byte[] bytes = curator.getData().forPath(zk_AddressNode);
        Endpoint endPoint = deBuildData(new String(bytes));
        //当前时间不能小于最后一次上报的时间
        return endPoint.getTimestamp() <= System.currentTimeMillis();
    }

    private void updateNewData(CuratorFramework curator, String path) {
        try {
            if (System.currentTimeMillis() < lastUpdateTime) {
                return;
            }
            curator.setData().forPath(path, buildData().getBytes());
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            log.info("update init data error path is {} error is {}", path, e);
        }
    }

    /**
     * 创建持久顺序节点 ,并把节点数据放入 value
     *
     * @param curator
     * @return
     * @throws Exception
     */
    private String createNode(CuratorFramework curator) throws Exception {
        try {
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(PATH_FOREVER + "/" + (ip + ":" + port) + "-", buildData().getBytes());
        } catch (Exception e) {
            log.error("create node error msg {} ", e.getMessage());
            throw e;
        }
    }

    /**
     * 构建需要上传的数据
     *
     * @return
     */
    private String buildData() throws JsonProcessingException {
        Endpoint endpoint = new Endpoint(ip, port, System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(endpoint);
        return json;
    }

    private Endpoint deBuildData(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Endpoint endpoint = mapper.readValue(json, Endpoint.class);
        return endpoint;
    }

    /**
     * 在节点文件系统上缓存一个workid值,zk失效,机器重启时保证能够正常启动
     *
     * @param workerID
     */
    private void updateLocalWorkerID(int workerID) {
        this.workerID = workerID;
        File LeafconfFile = new File(PROP_PATH.replace("{port}", port));
        boolean exists = LeafconfFile.exists();
        log.info("file exists status is {}", exists);
        if (exists) {
            try {
                FileUtils.writeStringToFile(LeafconfFile, "workerID=" + workerID, StandardCharsets.UTF_8, false);
                log.info("update file cache workerID is {}", workerID);
            } catch (IOException e) {
                log.error("update file cache error ", e);
            }
        } else {
            //不存在文件,父目录页肯定不存在
            try {
                boolean mkdirs = LeafconfFile.getParentFile().mkdirs();
                log.info("init local file cache create parent dis status is {}, worker id is {}", mkdirs, workerID);
                if (mkdirs) {
                    if (LeafconfFile.createNewFile()) {
                        FileUtils.writeStringToFile(LeafconfFile, "workerID=" + workerID, StandardCharsets.UTF_8,false);
                        log.info("local file cache workerID is {}", workerID);
                    }
                } else {
                    log.warn("create parent dir error===");
                }
            } catch (IOException e) {
                log.warn("craete workerID conf file error", e);
            }
        }
    }

    private CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder().connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

    /**
     * 上报数据结构
     */
    static class Endpoint {
        private String ip;
        private String port;
        private long timestamp;

        public Endpoint() {
        }

        public Endpoint(String ip, String port, long timestamp) {
            this.ip = ip;
            this.port = port;
            this.timestamp = timestamp;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public String getWorkerZkNodePath() {
        return workerZkNodePath;
    }

    public void setWorkerZkNodePath(String workerZkNodePath) {
        this.workerZkNodePath = workerZkNodePath;
    }

    public int getWorkerID(){
        return this.workerID;
    }
}
