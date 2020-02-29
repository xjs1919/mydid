package com.xjs.sdk.mydid.util;

import java.util.concurrent.*;

public class ThreadPoolUtil {
	
	private final ScheduledThreadPoolExecutor executor;

	private static ThreadPoolUtil instance = new ThreadPoolUtil();

	private ThreadPoolUtil() {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory(){
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "ID-SDK-THREAD");
				thread.setDaemon(true);
				return thread;
			}
		});
		this.executor = executor;
	}

	public static ThreadPoolUtil getInstance() {
		return instance;
	}

	public static <T> Future<T> execute(final Callable<T> runnable) {
		return getInstance().executor.submit(runnable);
	}

	public static Future<?> execute(final Runnable runnable) {
		return getInstance().executor.submit(runnable);
	}

	public static ScheduledFuture scheduleWithFixedDelay(final Runnable runnable, final long initDelaySeconds, final long delaySeconds){
		return getInstance().executor.scheduleWithFixedDelay(runnable, initDelaySeconds, delaySeconds, TimeUnit.SECONDS);
	}
}
