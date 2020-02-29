package com.xjs.server.leaf.snowflake;

import com.google.common.base.Preconditions;
import com.xjs.server.leaf.common.Result;
import com.xjs.server.leaf.common.Status;
import com.xjs.server.leaf.common.Utils;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

/**
 * @author https://github.com/Meituan-Dianping/Leaf
 * 基于美团的leaf-snowflake：https://github.com/Meituan-Dianping/Leaf
 * 1(符号位) + 41（时间戳） + 6（机器号）+6（待扩展）+10（流水号）
 * 41位时间戳可以用69年
 * 6位机器号：可以部署64个节点
 * 6位保留
 * 10位流水号：毫秒内并发1024
 */
@Slf4j
public class SnowflakeIDGenImpl{

    private static final SecureRandom RANDOM = new SecureRandom();

    /**2019-09-05 10:40*/
    private final long twepoch = 1567651185194L;
    /**序列号位数*/
    private final long sequenceBits = 10L;
    /**保留位数*/
    private final long reserveBits = 6L;
    /**workerId位数*/
    private final long workerIdBits = 6L;

    private final long workerIdShift = sequenceBits+reserveBits;
    private final long timestampLeftShift = sequenceBits+reserveBits+workerIdBits;

    /**最大能够分配的workerId*/
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /**最大能够分配的sequence*/
    private final long maxSequence = -1L ^ (-1L << sequenceBits);

    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    public boolean initFlag;

    public SnowflakeIDGenImpl(String zkAddress, int port) {
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(Utils.getIp(), String.valueOf(port), zkAddress);
        this.initFlag = holder.init();
        if (this.initFlag) {
            this.workerId = holder.getWorkerID();
            log.info("START SUCCESS USE ZK WORKERID-{}", workerId);
        } else {
            Preconditions.checkArgument(initFlag, "Snowflake Id Gen is not init ok");
        }
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
    }

    public boolean init() {
        return this.initFlag;
    }

    public synchronized Result get() {
        //获取当前时间戳
        long timestamp = timeGen();
        //如果当前时间戳小于上次获取id时候的时间戳，说明发生了时钟回退
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            //如果回退时间小于5毫秒，则阻塞offset*2毫秒
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    //重新获取时间戳
                    timestamp = timeGen();
                    //如果还是小于上次获取id时候的时间戳，说明又回退了，不再重试，抛异常！
                    if (timestamp < lastTimestamp) {
                        log.error("重试之后，时钟回退还是太多！异常退出！");
                        return new Result(-1, Status.EXCEPTION);
                    }
                } catch (InterruptedException e) {
                    log.error("wait interrupted");
                    return new Result(-2, Status.EXCEPTION);
                }
            } else {
                log.error("时钟回退太多！异常退出！");
                //如果回退的太多，直接抛异常
                return new Result(-3, Status.EXCEPTION);
            }
        }
        //当前的时间戳和上次获取的相等，说明是同一个毫秒之内发生了并发访问
        if (lastTimestamp == timestamp) {
            //序列号+1
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                //seq为0，当前毫秒内的序列号用尽，阻塞到下一个毫秒，序列号随机开始，不要从0开始，防止大量的0
                sequence = RANDOM.nextInt(10);
                //阻塞到下一个毫秒，返回最新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return new Result(id, Status.SUCCESS);

    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
