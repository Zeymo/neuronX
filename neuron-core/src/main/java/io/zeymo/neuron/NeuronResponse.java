package io.zeymo.neuron;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;

import java.io.IOException;

public class NeuronResponse implements BinaryWritable {
	private final byte[]			body;
	private final ResponseHeader	header;

	public static class ResponseHeader implements BinaryWritable {
		private int				machineId;
		private int				code;
		private int				bodyLength;

		public static final int	LENGTH	= 4 * 3;

		public int getBodyLength() {
			return bodyLength;
		}

		public int getMachineId() {
			return machineId;
		}

		public void setMachineId(int machineId) {
			this.machineId = machineId;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		@Override
		public void readFields(NeuronReader reader) throws IOException {
			this.machineId = reader.readFInt();
			this.code = reader.readFInt();
			this.bodyLength = reader.readFInt();
		}

		@Override
		public void write(NeuronWriter writer) throws IOException {
			writer.writeFInt(machineId);
			writer.writeFInt(code);
			writer.writeFInt(bodyLength);
		}

	}

	public byte[] getBody() {
		return body;
	}

	public ResponseHeader getHeader() {
		return header;
	}

	public NeuronResponse() {
		this(new byte[NeuronConstants.RUNTIME_RPC_MAX_RESPONSE_SIZE]);
	}

	public NeuronResponse(byte[] buffer) {
		this.body = buffer;
		this.header = new ResponseHeader();
	}

	@Override
	public void readFields(NeuronReader reader) throws IOException {
		this.header.readFields(reader);
		reader.readBytes(body, 0, header.bodyLength);
	}

	@Override
	public void write(NeuronWriter writer) throws IOException {
		this.header.write(writer);
		writer.writeBytes(body, 0, header.bodyLength);
	}

	public static void protocolWrite(NeuronWriter writer, int machineId, int code, byte[] body, int bodyLength) throws IOException {
		protocolWriteHeader(writer, machineId, code, bodyLength);
		protocolWriteBody(writer, body, bodyLength);
	}

	public static void protocolWriteHeader(NeuronWriter writer, int machineId, int code, int bodyLength) throws IOException {
		writer.writeFInt(machineId);
		writer.writeFInt(code);
		writer.writeFInt(bodyLength);
	}

	public static void protocolWriteBody(NeuronWriter writer, byte[] body, int bodyLength) throws IOException {
		writer.writeFInt(bodyLength);
		writer.writeBytes(body, 0, bodyLength);
	}

}
