package io.zeymo.network.context;

import io.netty.buffer.ByteBuf;
import io.zeymo.network.api.RequestParam;

import java.util.ArrayList;

/**
 * Created By Zeymo at 14-9-26 17:58
 */
public class RemoteContext {

	private final ArrayList<RequestParam> paramWrap;
	private int								latch;
	private ArrayList<RequestParam>			params;
	private boolean							done;

	public RemoteContext() {
		this.paramWrap = new ArrayList<RequestParam>(1);
	}

	/**
	 * 所有的等待、complete方法都是同步调用<br/>
	 * 因此只需要在方法体前部检查done标记或者latch==0即可判断是否还需继续等待
	 * 
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean await(long timeout) throws InterruptedException {
		if (this.latch == 0) {
			this.done = true;
			return true;
		}

		if (this.done) {
			return this.latch == 0;
		}

		wait(timeout);
		this.done = true;
		return this.latch == 0;
	}

	/**
	 * @see this.await()
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean await() throws InterruptedException {
		if (this.latch == 0) {
			this.done = true;
			return true;
		}

		if (this.done) {
			return this.latch == 0;
		}

		wait();
		this.done = true;
		return this.latch == 0;
	}

	public synchronized void complete(int sessionId, long sessionTick, int index, ByteBuf value) {
		if (done) {
			return;
		}
		RequestParam param = params.get(index);
		// 【NETTY坑】: 不仅有readerIndex()，还有arrayOffset()
		param.complete(sessionId, value.array(), value.arrayOffset() + value.readerIndex(), value.readableBytes());

		if (--latch == 0) {
			done = true;
			notifyAll();
		}

	}

	public boolean isDone() {
		return done;
	}

	public void init(int latch, RequestParam param) {
		this.latch = latch;
		this.paramWrap.clear();
		this.paramWrap.add(param);
		this.params = paramWrap;
		this.done = false;
	}

	public void init(int latch, ArrayList<RequestParam> params) {
		this.latch = latch;
		this.params = params;
		this.done = false;
	}

	// public RemotingParam getResponse() {
	// return this.responseWrap.getServer(0).getResponse();
	// }
	//
	// public ByteBuffer[] getResponses() {
	// return this.responses;
	// }

}
