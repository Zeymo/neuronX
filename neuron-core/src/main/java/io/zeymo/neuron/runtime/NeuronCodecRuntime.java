package io.zeymo.neuron.runtime;

import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.domain.Node;
import io.zeymo.neuron.domain.SectorIterator;
import io.zeymo.neuron.io.BufferParam;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.schema.NodeLayout;
import io.zeymo.neuron.schema.SchemaUtils;
import io.zeymo.neuron.schema.SectorLayout;

import java.io.IOException;

/**
 * 每个线程分配一个Runtime实例
 */
public class NeuronCodecRuntime {
	// private final byte[] buffer;
	// private final ByteBuffer byteBuffer;

	public byte[] getCurrentBuffer() {
		return this.node.getBuffer();
	}

	public int getVersion() {
		// TODO!
		return 0;
	}

	public NodeLayout getNodeLayout() {
		return nodeLayout;
	}

	// private final BinaryCodec[] codecArray;
	private final Node node;

	private final SectorIterator[]		sectorIteratorArray;

	private final NeuronByteArrayReader sharedDataReader;
	private final UnionBuffer unionBuffer;

	private final NodeLayout			nodeLayout;

	public int getSectorIndex(String name) {
		SectorLayout sectorLayout = nodeLayout.getSectorLayout(name);
		return sectorLayout.getIndex();
	}

	public NeuronCodecRuntime(NeuronConfiguration configuration) {
		// initialize decoders & UnionBuffer
		this.unionBuffer = new UnionBuffer(configuration.getNodeLayout());
		this.sectorIteratorArray = new SectorIterator[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];
		// this.buffer = new byte[NeuronConstants.RUNTIME_MAX_BUFFER_SIZE];
		// this.byteBuffer = ByteBuffer.wrap(buffer);

		// FieldLayout fieldLayout = configuration.getFieldLayout();
		this.nodeLayout = configuration.getNodeLayout();

		try {
			// this.codecArray = SchemaUtils.createBinaryCodecArray(fieldLayout, this.unionBuffer);

			// initialize sectorIterators
			SectorLayout[] sectorLayoutArray = nodeLayout.getSectorLayoutArray();

			// each iterator
			for (int typeIndex = 0; typeIndex < sectorLayoutArray.length; ++typeIndex) {
				SectorLayout layout = sectorLayoutArray[typeIndex];
				if (layout == null) {
					continue;
				}
				// iterator's col codec
				SectorIterator iterator = new SectorIterator();

				BinaryCodec[] sectorDecoderArray = SchemaUtils.createSectorBinaryCodecArray(unionBuffer, layout);

				iterator.setDecoderArray(sectorDecoderArray);
				this.sectorIteratorArray[typeIndex] = iterator;
			}

		} catch (Exception e) {
			throw new IllegalArgumentException("unable to create NeuronCodecRuntime", e);
		}
		this.node = new Node();
		this.sharedDataReader = new NeuronByteArrayReader();

	}

	public BinaryCodec[] getCodecArray(int sectorIndex) {
		return this.sectorIteratorArray[sectorIndex].getCodecArray();
	}

	public BinaryCodec[] getCodecArray(String sectorName) {
		int sectorIndex = this.getSectorIndex(sectorName);
		return this.getCodecArray(sectorIndex);
	}

	// public BinaryCodec[] getCodecArray() {
	// return codecArray;
	// }

	// public byte[] getBuffer() {
	// return buffer;
	// }

	public SectorIterator getIterator(int sectorIndex) {
		SectorIterator iterator = sectorIteratorArray[sectorIndex];
		iterator.reset(node.getBuffer(), node.getSector(sectorIndex));
		return iterator;
	}

	public Node getNode() {
		return node;
	}

	public long getNodeId() {
		return this.node.getNodeId();
	}

	public UnionBuffer getUnionBuffer() {
		return unionBuffer;
	}

	public Node loadNode(BufferParam param) throws IOException {

		// ByteArrayInputStream bais = new ByteArrayInputStream(buffer, offset,
		// length);

		// try {
		// int len = Snappy.uncompress(buffer, offset, length, this.buffer, 0);
		// node.reset(sharedDataReader, this.buffer, 0, len);
		//
		// } catch (SnappyException e) {
		// throw new IllegalArgumentException("unable to decode");
		// }
		// GZIPInputStream gis = new GZIPInputStream(bais);
		// int len = 0;
		// int off = 0;
		// while (len != -1) {
		// len = gis.read(this.buffer, off,this.buffer.length-off);
		// off += len;
		// }
		// gis.close();

		// System.arraycopy(buffer, offset, this.buffer, 0, length);
		node.reset(sharedDataReader, param.getBuffer(), 0, param.getLength());
		return node;
	}

	public Node loadNode(byte[] buffer, int offset, int length) throws IOException {
		node.reset(sharedDataReader, buffer, offset, length);
		return node;
	}

	// public void loadNode(FileChannel channel, int offset, int length) throws
	// IOException {
	// channel.position(offset);
	// this.byteBuffer.clear();
	// channel.read(this.byteBuffer);
	// node.reset(sharedDataReader, this.buffer, 0, length);
	// }

}
