package io.zeymo.neuron.matcher;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.commons.io.impl.NeuronByteArrayWriter;
import io.zeymo.neuron.NeuronAnnotations;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.io.UnionBuffer;

import java.io.IOException;

public class KeywordMatcher implements BinaryWritable {

	private final byte[]				opArray;
	private int							opLength;
	private byte[]						rawBuffer;
	private int							textOffset;
	private int							textLength;
	private final NeuronByteArrayReader opReader;

	public KeywordMatcher() {
		this.opArray = new byte[NeuronConstants.SEARCH_MAX_OP_ARRAY_LENGTH];
		this.opReader = new NeuronByteArrayReader(opArray);
	}

	public void reset() {
		this.opLength = 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			opReader.init(opArray);
			KeywordParser.Node node = new KeywordParser.Node();
			node.readFields(opReader);
			node.toString(sb);
		} catch (IOException e) {
			sb.append("[ERROR]");
		}
		return sb.toString();
	}

	public boolean match(byte[] rawBuffer, int fieldIndex, UnionBuffer unionBuffer) throws IOException {
		this.rawBuffer = rawBuffer;
		this.textOffset = unionBuffer.getInt(fieldIndex, 0);
		this.textLength = unionBuffer.getInt(fieldIndex, 1);
		opReader.init(opArray);

		return match(opReader, false);
	}

	/**
	 * pattern@opArray
	 * text@rawBuffer
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public boolean match(NeuronByteArrayReader reader, boolean skip) throws IOException {

		if (opLength == 0) {
			return false;
		}

		int code = reader.readByte();
		switch (code) {
		case KeywordParser.NodeType.KEYWORD:

			int patternLength = reader.readUVInt();
			int patternOffset = reader.getPosition();
			reader.skip(patternLength);

			if (textLength == 0) {
				return false;
			}

			if (patternLength == 0) {
				return true;
			}

			boolean exclude = false;
			byte first = opArray[patternOffset];
			if (first == '-') {
				exclude = true;
				first = opArray[++patternOffset];
				patternLength -= 1;
			}
			int max = textOffset + (textLength - patternLength);

			for (int i = textOffset; i <= max; i++) {
				if (rawBuffer[i] != first) {
					while (++i <= max && rawBuffer[i] != first)
						;
				}

				if (i <= max) {
					int j = i + 1;
					int end = j + patternLength - 1;
					for (int k = patternOffset + 1; j < end && rawBuffer[j] == opArray[k]; j++, k++)
						;
					if (j == end) {
						if (exclude) {
							// System.out.println("FALSE: -" + new
							// String(opArray, patternOffset, patternLength,
							// NeuronConstants.STRING_CHARSET));
							return false;
						} else {
							// System.out.println("TRUE: " + new String(opArray,
							// patternOffset, patternLength,
							// NeuronConstants.STRING_CHARSET));
							return true;
						}

					}
				}
			}
			if (exclude) {
				// System.out.println("TRUE: -" + new String(opArray,
				// patternOffset, patternLength,
				// NeuronConstants.STRING_CHARSET));
				return true;
			} else {
				// System.out.println("FALSE: " + new String(opArray,
				// patternOffset, patternLength,
				// NeuronConstants.STRING_CHARSET));
				return false;
			}
		case KeywordParser.NodeType.AND:
			int andSize = reader.readUVInt();
			boolean andResult = true;

			while (andSize-- > 0) {
				if (!match(reader, skip)) {
					andResult = false;
					break;
				}
			}

			while (andSize-- > 0) {
				match(reader, true);
			}
			return andResult;
		case KeywordParser.NodeType.OR:
			int orSize = reader.readUVInt();
			boolean orResult = false;

			while (orSize-- > 0) {
				if (match(reader, skip)) {
					orResult = true;
					break;
				}
			}

			while (orSize-- > 0) {
				match(reader, true);
			}
			return orResult;
		}
		return false;

	}

	@Override
	public void readFields(NeuronReader reader) throws IOException {
		this.opLength = reader.readUVInt();
		reader.readBytes(opArray, 0, opLength);
	}

	@NeuronAnnotations.NotGCFree
	public static void assign(String query, KeywordParser parser, KeywordMatcher matcher) throws IOException {
		KeywordParser.Node node = parser.parse(query);
		NeuronByteArrayWriter writer = new NeuronByteArrayWriter(matcher.opArray);
		node.write(writer);
		matcher.opLength = writer.getPosition();
	}

	@Override
	public void write(NeuronWriter writer) throws IOException {
		writer.writeUVInt(opLength);
		writer.writeBytes(opArray, 0, opLength);
	}

}
