//package com.bj58.zptask.dtplat.core.cluster;
//
//import java.util.concurrent.Executors;
//
//import com.bj58.zptask.dtplat.core.Application;
//import com.bj58.zptask.dtplat.core.factory.NamedThreadFactory;
//import com.bj58.zptask.dtplat.rpc.HeartBeatMonitor;
//import com.bj58.zptask.dtplat.rpc.RemotingClientDelegate;
//import com.bj58.zptask.dtplat.rpc.netty.NettyClientConfig;
//import com.bj58.zptask.dtplat.rpc.netty.NettyRequestProcessor;
//import com.bj58.zptask.dtplat.util.Constants;
//import com.bj58.zptask.dtplat.util.StringUtils;
//
///**
// * @author Robert HG (254963746@qq.com) on 8/18/14. 抽象 netty 客户端
// */
//public abstract class AbstractClientNode<T extends Node, App extends Application> extends AbstractJobNode<T, App> {
//
//    protected RemotingClientDelegate remotingClient;
//    private HeartBeatMonitor heartBeatMonitor;
//
//    protected void remotingStart() {
//        //        this.remotingClient = new RemotingClientDelegate(new NettyRemotingClient(getNettyClientConfig()), application);
//        //        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient, application);
//
//        remotingClient.start();
//        heartBeatMonitor.start();
//
//        NettyRequestProcessor defaultProcessor = getDefaultProcessor();
//        if (defaultProcessor != null) {
//            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
//            remotingClient.registerDefaultProcessor(defaultProcessor, Executors.newFixedThreadPool(processorSize, new NamedThreadFactory(AbstractClientNode.class.getSimpleName())));
//        }
//        injectRemotingClient();
//    }
//
//    protected void injectRemotingClient() {
//
//    }
//
//    /**
//     * 得到默认的处理器
//     *
//     * @return
//     */
//    protected abstract NettyRequestProcessor getDefaultProcessor();
//
//    protected void remotingStop() {
//        heartBeatMonitor.stop();
//        remotingClient.shutdown();
//    }
//
//    /**
//     * 设置节点组名
//     *
//     * @param nodeGroup
//     */
//    public void setNodeGroup(String nodeGroup) {
//        config.setNodeGroup(nodeGroup);
//    }
//
//    public void setFailStorePath(String failStorePath) {
//        if (StringUtils.isNotEmpty(failStorePath)) {
//            config.setFailStorePath(failStorePath);
//        }
//    }
//
//    public boolean isServerEnable() {
//        return remotingClient.isServerEnable();
//    }
//
//    /**
//     * 这个子类可以覆盖
//     *
//     * @return
//     */
//    protected NettyClientConfig getNettyClientConfig() {
//        NettyClientConfig config = new NettyClientConfig();
//        return config;
//    }
//
//    /**
//     * 设置连接JobTracker的负载均衡算法
//     *
//     * @param loadBalance
//     *            算法 random, consistenthash
//     */
//    public void setLoadBalance(String loadBalance) {
//        config.setParameter("loadbalance", loadBalance);
//    }
//
//}
