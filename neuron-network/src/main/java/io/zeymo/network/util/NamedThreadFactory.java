package io.zeymo.network.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private final AtomicInteger	mThreadNum	= new AtomicInteger(0);

	private final String		mPrefix;

	private final boolean		mDaemon;

	private final ThreadGroup	mGroup;

	public NamedThreadFactory(String prefix) {
		this(prefix, false);
	}

	public NamedThreadFactory(String prefix, boolean daemon) {
		mPrefix = prefix + "-thread-";
		mDaemon = daemon;
		SecurityManager s = System.getSecurityManager();
		mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable runnable) {
		String name = mPrefix + mThreadNum.getAndIncrement();
		Thread ret = new Thread(mGroup, runnable, name, 0);
		ret.setDaemon(mDaemon);
		return ret;
	}

	public ThreadGroup getThreadGroup() {
		return mGroup;
	}
}