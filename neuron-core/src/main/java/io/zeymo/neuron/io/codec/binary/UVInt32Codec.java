package io.zeymo.neuron.io.codec.binary;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.io.codec.IntDecoder;

import java.io.IOException;

public class UVInt32Codec implements BinaryCodec, IntDecoder {

	public static void encode(int value, NeuronWriter writer) throws IOException {
		writer.writeUVInt(value);
	}

	private final int			fieldIndex;

	private final UnionBuffer unionCache;

	public UVInt32Codec(UnionBuffer unionCache, int fieldIndex) {
		this.unionCache = unionCache;
		this.fieldIndex = fieldIndex;
	}

	@Override
	public int decode(byte[] buffer, int offset) throws IOException {
		int start = offset;

		byte tmp = buffer[offset++];
		int result = tmp;
		if (tmp < 0) {
			result = tmp & 0x7f;
			if ((tmp = buffer[offset++]) >= 0) {
				result |= tmp << 7;
			} else {
				result |= (tmp & 0x7f) << 7;
				if ((tmp = buffer[offset++]) >= 0) {
					result |= tmp << 14;
				} else {
					result |= (tmp & 0x7f) << 14;
					if ((tmp = buffer[offset++]) >= 0) {
						result |= tmp << 21;
					} else {
						result |= (tmp & 0x7f) << 21;
						result |= (tmp = buffer[offset++]) << 28;
						if (tmp < 0) {
							// 丢弃高位的5个字节，譬如遇到一个坑爹的writeVarint64(xx)
							for (int i = 0; i < 5; i++) {
								if (buffer[offset++] >= 0) {
									break;
								}
							}
							// 蛋疼
							throw new IOException("Varint损坏");
						}
					}
				}
			}
		}
		unionCache.setInt(fieldIndex, result);
		return offset - start;
	}

	@Override
	public void encode(NeuronInputBuffer inputBuffer, NeuronWriter writer) throws IOException {
		int bits = inputBuffer.readUVInt();
		writer.writeUVInt(bits);
	}

	@Override
	public int getValue() {
		return unionCache.getInt(fieldIndex);
	}

	@Override
	public Object debug(byte[] buffer) {
		int value = unionCache.getInt(fieldIndex);
		return value;
	}

}
