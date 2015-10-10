package com.bj58.zptask.dtplat.jobtracker.support.cluster;

import com.bj58.zptask.dtplat.commons.DTaskProvider;
import com.bj58.zptask.dtplat.commons.InjectorHolder;
import com.bj58.zptask.dtplat.core.cluster.Node;
import com.bj58.zptask.dtplat.core.cluster.NodeType;
import com.bj58.zptask.dtplat.core.logger.Logger;
import com.bj58.zptask.dtplat.core.logger.LoggerFactory;
import com.bj58.zptask.dtplat.jobtracker.channel.ChannelWrapper;
import com.bj58.zptask.dtplat.jobtracker.domain.JobTrackerApplication;
import com.bj58.zptask.dtplat.jobtracker.domain.TaskTrackerNode;
import com.bj58.zptask.dtplat.util.ConcurrentHashSet;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task Tracker 管理器 (对 TaskTracker 节点的记录 和 可用线程的记录)
 *
 *
 * @author WuTong
 * @version 1.0
 * @date  2015年9月16日 下午7:30:15
 * @see 
 * @since
 */
public class TaskTrackerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrackerManager.class);
    // 单例
    private final ConcurrentHashMap<String/*nodeGroup*/, Set<TaskTrackerNode>> NODE_MAP = new ConcurrentHashMap<String, Set<TaskTrackerNode>>();
    private JobTrackerApplication application;

    public TaskTrackerManager(JobTrackerApplication application) {
        this.application = application;
    }
    
    /**
     * get all connected node group
     * 
     * @return
     */
    public Set<String> getNodeGroups() {
        return NODE_MAP.keySet();
    }

    /**
     * 添加节点
     *
     * @param node
     */
    public void addNode(Node node) {
        //  channel 可能为 null
        ChannelWrapper channel = application.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());

        if (taskTrackerNodes == null) {
            taskTrackerNodes = new ConcurrentHashSet<TaskTrackerNode>();
            Set<TaskTrackerNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), taskTrackerNodes);
            if (oldSet != null) {
                taskTrackerNodes = oldSet;
            }
        }

        TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getGroup(), node.getThreads(), node.getIdentity(), channel);
        LOGGER.info("Add TaskTracker node:{}", taskTrackerNode);
        taskTrackerNodes.add(taskTrackerNode);

        // create executable queue
        //        application.getExecutableJobQueue().createQueue(node.getGroup());
        //        application.getNodeGroupStore().addNodeGroup(NodeType.TASK_TRACKER, node.getGroup());
        InjectorHolder.getInstance(DTaskProvider.class).addNodeGroup("TASK_TRACKER", node.getGroup());
    }

    /**
     * 删除节点
     *
     * @param node
     */
    public void removeNode(Node node) {
        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());
        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getIdentity());
            taskTrackerNode.setNodeGroup(node.getGroup());
            LOGGER.info("Remove TaskTracker node:{}", taskTrackerNode);
            taskTrackerNodes.remove(taskTrackerNode);
        }
    }

    public TaskTrackerNode getTaskTrackerNode(String nodeGroup, String identity) {
        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);
        if (taskTrackerNodes == null || taskTrackerNodes.size() == 0) {
            return null;
        }

        for (TaskTrackerNode taskTrackerNode : taskTrackerNodes) {
            if (taskTrackerNode.getIdentity().equals(identity)) {
                if (taskTrackerNode.getChannel() == null || taskTrackerNode.getChannel().isClosed()) {
                    // 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
                    ChannelWrapper channel = application.getChannelManager().getChannel(taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                    if (channel != null) {
                        // 更新channel
                        taskTrackerNode.setChannel(channel);
                        LOGGER.info("update node channel , taskTackerNode={}", taskTrackerNode);
                        return taskTrackerNode;
                    }
                } else {
                    // 只有当channel正常的时候才返回
                    return taskTrackerNode;
                }
            }
        }
        return null;
    }

    /**
     * 更新节点的 可用线程数
     *
     * @param nodeGroup
     * @param identity
     * @param availableThreads
     * @param timestamp        时间戳, 只有当 时间戳大于上次更新的时间 才更新可用线程数
     */
    public void updateTaskTrackerAvailableThreads(String nodeGroup, String identity, Integer availableThreads, Long timestamp) {

        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);

        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            for (TaskTrackerNode trackerNode : taskTrackerNodes) {
                if (trackerNode.getIdentity().equals(identity) && trackerNode.getTimestamp() <= timestamp) {
                    trackerNode.setAvailableThread(availableThreads);
                    trackerNode.setTimestamp(timestamp);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("更新节点线程数: {}", trackerNode);
                    }
                }
            }
        }
    }
}
