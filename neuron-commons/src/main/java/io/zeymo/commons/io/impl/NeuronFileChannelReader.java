package io.zeymo.commons.io.impl;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NeuronFileChannelReader extends NeuronBaseReader {

	private FileChannel	channel;
	private ByteBuffer	buffer;
	private long		lastFillPosition;

	public NeuronFileChannelReader(FileChannel channel, int bufferSize) {
		this.buffer = ByteBuffer.allocate(bufferSize);
		this.reuse(channel);
	}

	public NeuronFileChannelReader(int bufferSize) {
		this.buffer = ByteBuffer.allocate(bufferSize);
	}

	public void reuse(FileChannel channel) {
		this.channel = channel;

		this.buffer.clear();
		this.buffer.limit(0);
	}

	public void position(long position) throws IOException {
		/*
		 * Constraints:
		 *	+--------------------------------------------------------+
		 *	| BUFFER	----[0]---	---[POS]---	--[Y]--	--[Limit]--- |
		 *	|　　　　　　　　　　|　　　　　　　|　　　　　　|　　　　　　|　　　　|
		 *	| CHANNEL	---[POS]-	---[MIN]---	--[X]--	---[MAX]---- |
		 *	+--------------------------------------------------------+
		 *	
		 *	MIN = POS + BUFFER.POS
		 *	MAX = POS + BUFFER.LIMIT
		 *	X = CHANNEL.NEW_POS
		 *
		 *	IF
		 *		X in [MIN,MAX]
		 *	THEN
		 *		Y = X - CHANNEL.POS
		 */

		long channelPositon = lastFillPosition;
		long minPosition = channelPositon;
		long maxPosition = channelPositon + this.buffer.limit();

		if (position < minPosition || position > maxPosition) {
			channel.position(position);
			// limit to 0, lazy refill, until call read()
			this.buffer.limit(0);
		} else {
			buffer.position((int) (position - channelPositon));
		}
	}

	@Override
	public byte readRawByte() throws IOException {
		int remaining = this.buffer.remaining();
		if (remaining == 0) {
			if (!this.refill()) {
				throw new EOFException();
			}
		}
		return this.buffer.get();
	}

	@Override
	public void readRawBytes(byte[] dst, int offset, int length) throws IOException {
		while (length > 0) {
			int remaining = this.buffer.remaining();
			for (; remaining > 0; remaining--, offset++) {
				dst[offset] = this.buffer.get();
				--length;
				if (length == 0) {
					return;
				}
			}
			if (!this.refill()) {
				throw new EOFException();
			}
		}
	}

	private boolean refill() throws IOException {
		// remaining not enough for filling dst[]
		this.buffer.clear();
		this.lastFillPosition = this.channel.position();

		int bytesRead = this.channel.read(buffer);
		this.buffer.flip();

		if (bytesRead <= 0) {
			return false;
		}
		return true;
	}

	@Override
	public void readRawBytes(ByteBuffer byteBuffer, int length) throws IOException {

		while (length > 0) {
			int remaining = this.buffer.remaining();
			for (; remaining > 0; remaining--) {
				byteBuffer.put(this.buffer.get());
				--length;
				if (length == 0) {
					return;
				}
			}
			if (!this.refill()) {
				throw new EOFException();
			}
		}
	}

	public boolean isEOF() throws IOException {
		return getRelativeOffset() >= this.channel.size();
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public long getRelativeOffset() {
		return lastFillPosition + buffer.position();
	}

	@Override
	public void skip(int bytes) throws IOException {
		long pos = channel.position();
		pos += bytes;
		channel.position(pos);
	}

}
