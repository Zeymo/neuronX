package io.zeymo.neuron.debug;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronOutputBuffer;
import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyUtils;
import io.zeymo.neuron.*;
import io.zeymo.neuron.domain.Node;
import io.zeymo.neuron.domain.Sector;
import io.zeymo.neuron.domain.SectorIterator;
import io.zeymo.neuron.domain.Varchar;
import io.zeymo.neuron.io.BufferParam;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.runtime.NeuronCodecRuntime;
import io.zeymo.neuron.runtime.NeuronConfiguration;
import io.zeymo.neuron.schema.SectorLayout;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugUtils {

	public static String toString(Class<?> clazz) {
		ProtectionDomain pd = clazz.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		return clazz.getName() + " @ {" + cs.getLocation() + "}";
	}

	public static void printMessage(String message) {
		String repeat = StringUtils.repeat("=", message.length() + 5);
		System.out.println(repeat);
		System.out.print(" >> ");
		System.out.println(message);
		System.out.println(repeat);
	}

	public static void checkNotNull(Object notNull, String message) {
		if (notNull == null) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void mkdir(String name) {
		File dir = new File(name);
		if (dir.exists()) {
			return;
		}
		dir.mkdirs();
	}

	public static String timeToString(long time) {
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd,HH:mm:ss:S");
		return format.format(date);
	}

	public static void clearDirectory(String dir) {
		File file = new File(dir);
		clearDirectory(file);
	}

	@NeuronAnnotations.NotGCFree
	public static void parseResponse(NeuronResponseGroup responseGroup, BinaryWritable result) throws IOException {

		if (responseGroup.getGroupCount() == 0) {
			throw new RuntimeException("empty result in responseGroup");
		}

		NeuronResponse response = new NeuronResponse();
		responseGroup.get(0, response);
		NeuronByteArrayReader reader = new NeuronByteArrayReader();

		if (response.getHeader().getCode() == NeuronResponseCode.OK) {
			reader.init(response.getBody(), 0, response.getHeader().getBodyLength());
			result.readFields(reader);
		} else {
			String message = new String(response.getBody(), 0, response.getHeader().getBodyLength(), NeuronConstants.STRING_CHARSET);
			throw new RuntimeException("ERROR/TIMEOUT: " + message);
		}
	}

	public static void clearDirectory(File dir) {

		if (!dir.exists()) {
			return;
		}
		if (!dir.isDirectory()) {
			return;
		}

		File[] subFiles = dir.listFiles();
		for (File subFile : subFiles) {
			if (subFile.isDirectory()) {
				clearDirectory(subFile);
			}
			subFile.delete();
		}
	}

	public static void persist(byte[] buffer, int offset, int length, String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(fileName));
		fos.write(buffer, offset, length);
		fos.close();
	}

	public static int readFully(String fileName, byte[] buffer) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		int sum = 0;
		int read = 0;
		while ((read = IOUtils.read(fis, buffer)) > 0) {
			sum += read;
		}
		return sum;
	}

	public static String toString(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		return new String(baos.toString());
	}

	public static String toString(BufferParam param, NeuronCodecRuntime codecRuntime, NeuronConfiguration configuration) throws IOException {

		codecRuntime.loadNode(param);
		return toString(codecRuntime, configuration);
	}

	public static String toSimpleString(NeuronCodecRuntime codecRuntime, NeuronConfiguration configuration) throws IOException {

		final Node node = codecRuntime.getNode();
		final StringBuilder sb = new StringBuilder();

		sb.append("[node id:" + node.getNodeId());
		sb.append(" offset:" + node.getOffset());
		sb.append(" version:" + node.getVersion());
		sb.append(" length:" + node.getLength());
		sb.append(" status:" + node.getStatus());
		sb.append(" sector_count:" + node.getSectorCount()).append("]\n");

		SectorLayout[] sectorLayouts = configuration.getNodeLayout().getSectorLayoutArray();

		for (SectorLayout layout : sectorLayouts) {
			if (layout == null) {
				continue;
			}

			Sector sector = node.getSector(layout.getIndex());

			sb.append("[sector");
			sb.append(" index:" + sector.getTypeIndex());
			sb.append(" count:" + sector.getCount());
			sb.append(" offset:" + sector.getDataOffset());
			sb.append(" length:" + sector.getDataLength());
			sb.append("]\n");

		}
		return sb.toString();
	}

	public static String toString(NeuronCodecRuntime codecRuntime, NeuronConfiguration configuration) throws IOException {

		final Node node = codecRuntime.getNode();
		final StringBuilder sb = new StringBuilder();

		sb.append("[node id:" + node.getNodeId());
		sb.append(" offset:" + node.getOffset());
		sb.append(" version:" + node.getVersion());
		sb.append(" length:" + node.getLength());
		sb.append(" status:" + node.getStatus());
		sb.append(" sector_count:" + node.getSectorCount()).append("]\n");

		SectorLayout[] sectorLayouts = configuration.getNodeLayout().getSectorLayoutArray();

		for (SectorLayout layout : sectorLayouts) {

			if (layout == null) {
				continue;
			}

			Sector sector = node.getSector(layout.getIndex());
			int sectorIndex = sector.getTypeIndex();
			SectorIterator iterator = codecRuntime.getIterator(sectorIndex);

			sb.append("[sector");
			sb.append(" index:" + sector.getTypeIndex());
			sb.append(" count:" + sector.getCount());
			sb.append(" offset:" + sector.getDataOffset());
			sb.append(" length:" + sector.getDataLength());
			sb.append("]\n");

			while (iterator.hasNext()) {
				iterator.next();
				sb.append("\t");
				sb.append(toString(iterator));
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static String toString(SectorIterator iterator) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[ row " + iterator.getRowNumber());
		for (BinaryCodec codec : iterator.getCodecArray()) {
			Object obj = codec.debug(iterator.getBuffer());
			sb.append(", " + obj);
		}
		sb.append(" ]");
		return sb.toString();
	}

	public static Object[] toObjectArray(BufferParam param, SectorIterator iterator) {
		BinaryCodec[] codecArray = iterator.getCodecArray();

		Object[] result = new Object[codecArray.length];

		int i = 0;
		for (BinaryCodec codec : codecArray) {
			Object obj = codec.debug(param.getBuffer());
			result[i++] = obj;
		}

		return result;
	}

	public static JsonProperties createSchemaProperties(File path) throws Exception {
		JsonProperties property = JsonPropertyUtils.loadProperty(path);
		return property;
	}

	public static JsonProperties createSchemaProperties() throws Exception {
		return createSchemaProperties(new File("./serverRoot/conf/schema.json"));
	}

	public static NeuronConfiguration createConfiguration() throws Exception {
		return createConfiguration(new File("./serverRoot/conf/schema.json"));
	}

	public static NeuronConfiguration createConfiguration(File path) throws Exception {
		JsonProperties schemaProperties = createSchemaProperties(path);
		System.out.println(schemaProperties.toPrettyString());
		NeuronConfiguration configuration = new NeuronConfiguration(schemaProperties);
		return configuration;
	}

	public static String toString(int[] array, int offset, int count) {
		StringBuilder sb = new StringBuilder();
		sb.append("int[");
		sb.append(count);
		sb.append("]:[");
		int end = offset + count - 1;
		for (int i = offset; i < end; ++i) {
			sb.append(array[offset + i]);
			sb.append(',');
		}
		if (count > 0) {
			sb.append(array[offset + count]);
		}
		sb.append(']');
		return sb.toString();
	}

	public static String toString(long[] array, int offset, int count) {
		StringBuilder sb = new StringBuilder();
		sb.append("long[");
		sb.append(count);
		sb.append("]:[");
		int end = offset + count - 1;
		for (int i = offset; i < end; ++i) {
			sb.append(array[offset + i]);
			sb.append(',');
		}
		if (count > 0) {
			sb.append(array[offset + count]);
		}
		sb.append(']');
		return sb.toString();
	}

	public final static char[]	BINARY_CHARS	= new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toString(byte[] array, int offset, int count) {
		StringBuilder sb = new StringBuilder();
		sb.append("byte[");
		sb.append(count);
		sb.append("]:[");
		int end = offset + count - 1;
		for (int i = offset; i < end; ++i) {
			int v = array[offset + i] & 0xFF;
			sb.append(BINARY_CHARS[v >> 4]);
			sb.append(BINARY_CHARS[v & 0xF]);
			sb.append(',');
		}
		if (count > 0) {
			sb.append(array[offset + count]);
		}
		sb.append(']');
		return sb.toString();
	}

	@NeuronAnnotations.NotGCFree
	public static NeuronRequest newRequest(String taskName, BinaryWritable param, long execTimeoutMs) throws IOException {
		NeuronOutputBuffer requestBuffer = new NeuronOutputBuffer();

		param.write(requestBuffer);

		NeuronRequest request = new NeuronRequest(new Varchar(taskName), requestBuffer.getBuffer());
		request.setParamLength(requestBuffer.getRelativeOffset());
		request.setTimeoutMs(execTimeoutMs);

		return request;
	}
}
