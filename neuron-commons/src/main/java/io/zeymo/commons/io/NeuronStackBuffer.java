package io.zeymo.commons.io;

import java.io.IOException;

public class NeuronStackBuffer extends NeuronOutputBuffer {

	private int		depth;
	private int[]	depthArray	= new int[8192];
	private int[]	lengthArray	= new int[8192];
	private int[]	markArray	= new int[8192];

	private int		markCount;

	
	
	
	public NeuronStackBuffer(byte[] buffer) {
		super(buffer);
		this.init(1024);
		this.reset();
	}

	public NeuronStackBuffer(int markCount) {
		super();
		this.init(markCount);
		this.reset();
	}

	public NeuronStackBuffer(int capacity, int markCount) {
		super(capacity);
		this.init(markCount);
		this.reset();
	}

	public void build(NeuronOutputBuffer outputBuffer) throws IOException {
		for (int i = 0; i < this.markCount; ++i) {
			lengthArray[i] = -1;
		}

		final int maxLength = this.getRelativeOffset();
		for (int mark = 0; mark < markCount; ++mark) {
			// write a length mark

			final int depth = depthArray[mark];
			if (depth == -1) {
				break;
			}

			final int position = markArray[mark];
			int length = getLength(mark, maxLength);

			outputBuffer.writeUVInt(length);

			final int nextPosition = mark < markCount - 1 ? markArray[mark + 1] : maxLength;
			outputBuffer.writeBytes(this.buffer, position, nextPosition - position);
		}
	}

	public void finishMark() {
		depth = -1;
		mark();

	}

	private int getLength(int mark, int maxLength) {

		if (lengthArray[mark] == -1) {

			final int currentPosition = markArray[mark];

			if (mark == markCount - 1) {
				final int length = maxLength - currentPosition;
				lengthArray[mark] = length;
				return length;
			}

			final int expectDepth = depthArray[mark] + 1;
			final int nextPosition = markArray[mark + 1];

			int length = nextPosition - currentPosition;
			for (int m = mark + 1; m < markCount; ++m) {
				final int depth = depthArray[m];
				if (depth == expectDepth) {
					int len = getLength(m, maxLength);
					length += len + NeuronReader.measureVarint32(len);
				}
				if (depth < expectDepth) {
					break;
				}
			}

			lengthArray[mark] = length;
			return length;

		}
		return lengthArray[mark];
	}

	public void init(int markCount) {
		this.markArray = new int[markCount];
		this.depthArray = new int[markCount];
		this.lengthArray = new int[markCount];
	}

	public void mark() {
		int position = super.getRelativeOffset();
		markArray[markCount] = position;
		depthArray[markCount] = depth;
		markCount++;
	}

	public void pop() {
		if (depth == 0) {
			throw new IllegalStateException();
		}
		depth--;
	}

	public void popMark() {
		pop();
		mark();
	}

	public void push() {
		depth++;
	}

	public void pushMark() {
		push();
		mark();
	}

	@Override
	public void reset() {
		this.depth = 0;
		this.markCount = 0;
		super.reset();
	}

}
