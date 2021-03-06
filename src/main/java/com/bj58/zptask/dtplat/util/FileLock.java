package com.bj58.zptask.dtplat.util;

import com.bj58.zptask.dtplat.core.logger.Logger;
import com.bj58.zptask.dtplat.core.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件锁
 * 这个锁很赞    不知道filechannel 还有tryLock方法
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public class FileLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLock.class);
    private File file;

    private FileChannel channel = null;
    private java.nio.channels.FileLock lock = null;

    public FileLock(String filename) {
        this(new File(filename));
    }

    public FileLock(File file) {
        this.file = file;
        FileUtils.createFileIfNotExist(file);
    }

    /**
     * 获得锁
     */
    public boolean tryLock() {
        try {
            Path path = Paths.get(file.getPath());
            if (channel != null && channel.isOpen()) {
                return false;
            }
            channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ);
            lock = channel.tryLock();
            if (lock != null) {
                return true;
            }
        } catch (IOException e) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e1) {
                    LOGGER.error("file channel close failed.", e1);
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void release() {
        try {
            if (lock != null) {
                lock.release();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                try {
                    channel.close(); // also releases the lock
                } catch (IOException e) {
                    LOGGER.error("file channel close failed.", e);
                }
            }
        }
    }
}
