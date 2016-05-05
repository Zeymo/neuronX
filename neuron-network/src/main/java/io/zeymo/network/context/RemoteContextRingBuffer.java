package io.zeymo.network.context;

import io.netty.buffer.ByteBuf;
import io.zeymo.network.api.RequestParam;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created By Zeymo at 14-9-26 17:58
 */
public class RemoteContextRingBuffer {

	public static final int		NULL_SESSION_ID	= -1;

	private static final int	MAX_CAPACITY	= 128;

	private final static int	FREE			= 0;

	private RemoteContext[]		ringBuffer;

	private AtomicLong[]		sids;

	// capacity must be power of two
	public RemoteContextRingBuffer(int capacity) {

		if (capacity > MAX_CAPACITY) {
			capacity = MAX_CAPACITY;
		}

		ringBuffer = new RemoteContext[capacity];
		for (int i = 0; i < ringBuffer.length; i++) {
			ringBuffer[i] = new RemoteContext();
		}
		sids = new AtomicLong[capacity];
		for (int j = 0; j < sids.length; j++) {
			sids[j] = new AtomicLong();
		}
	}

	//
	// public ByteBuffer getResponse(int sid) {
	// return ringBuffer[sid].getResponse();
	// }
	//
	// public ByteBuffer[] getResponses(int sid) {
	// return ringBuffer[sid].getResponses();
	// }

	public void complete(int sid, long sessionTick, int index, ByteBuf value) {
		// 这里校验不抛异常，因为有可能因为下游代码在业务超时后，主动调用过releaseSession()
		if (sessionTick == sids[sid].get()) {
			ringBuffer[sid].complete(sid, sessionTick, index, value);
		}
	}

	public int profile(RequestParam param, long timeout) {
		long newTick = System.currentTimeMillis() + timeout;
		int sid = alloc(newTick);
		ringBuffer[sid].init(1, param);
		return sid;
	}

	public int profile(ArrayList<RequestParam> params, long timeout) {
		int latch = params.size();
		long newTick = System.currentTimeMillis() + timeout;
		int sid = alloc(newTick);
		if (latch != 0) {
			ringBuffer[sid].init(latch, params);
		}
		return sid;
	}

	public long getTick(int sessionId) {
		return sids[sessionId].get();
	}

	private int alloc(long newTick) {
		long tick = System.currentTimeMillis();
		int i = 0;
		for (;;) {
			long prevTick = sids[i].get();

			if (prevTick < tick) {
				// fd: 找到一个可用的tick后，用乐观锁的方式占有这个sid
				if (sids[i].compareAndSet(prevTick, newTick)) {
					return i;
				}
			}
			i++;
			if (i == sids.length) {
				i = 0;
				// fd:每一圈更新一次时间戳，防止出现大量空等待
				tick = System.currentTimeMillis();
			}
		}
	}

	public boolean await(int sid) throws InterruptedException {
		long tick = sids[sid].get();
		long timeout = tick - System.currentTimeMillis();

		if (timeout <= 0) {
			// fd: 此种情况包含session已经被释放，或者过期
			return false;
		}
		return ringBuffer[sid].await(timeout);
	}

	public boolean isDone(int sid) {
		return ringBuffer[sid].isDone();
	}

	public void release(int sid, long sessionTick) {
		// fd: release session 需要校验被释放的tick是否正确
		sids[sid].compareAndSet(sessionTick, FREE);
	}
}
