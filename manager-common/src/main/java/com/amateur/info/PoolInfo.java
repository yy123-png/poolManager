package com.amateur.info;

import lombok.Data;

/**
 * 线程池信息
 *
 * @author sun
 */
@Data
public class PoolInfo {

    /**
     * 线程池Bean名称
     */
    private String poolBeanName;

    private int corePoolSize;

    private int maximumPoolSize;

    private String workQueueFullyClassName;

    private long keepAliveTime;

    private String threadFactoryFullyClassName;

}
