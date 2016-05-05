package io.zeymo.neuron.schema;

import io.zeymo.commons.utils.ResourceUtils;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.io.codec.ObjectCodec;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SchemaUtils {
	private final static HashMap<String, Class<? extends BinaryCodec>> binaryCodecNameMap;
	private final static HashMap<String, Class<? extends ObjectCodec>>	objectCodecNameMap;
	static {

		// binaryCodecNameMap
		{
			binaryCodecNameMap = new HashMap<String, Class<? extends BinaryCodec>>();
			String config = ResourceUtils.getResourceAsString("com/taobao/neuron/io/codec/binarycodec-alias.config", BinaryCodec.class);

			Scanner scanner = new Scanner(config);
			String line = null;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine().trim();
				if (line.startsWith("#") || line.length() == 0) {
					continue;
				}

				String[] splits = line.split("=");
				String name = splits[0].trim();
				String clazzName = splits[1].trim();

				try {
					Class<BinaryCodec> clazz = getClazz(clazzName);
					binaryCodecNameMap.put(name, clazz);
				} catch (ClassNotFoundException e) {
					scanner.close();
					throw new RuntimeException("FATAL : class not found for binary-codec-alias : " + line);
				}
			}
			scanner.close();
		}

		{
			objectCodecNameMap = new HashMap<String, Class<? extends ObjectCodec>>();
			String config = ResourceUtils.getResourceAsString("com/taobao/neuron/io/codec/objectcodec-alias.config", ObjectCodec.class);

			Scanner scanner = new Scanner(config);
			String line = null;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine().trim();
				if (line.startsWith("#") || line.length() == 0) {
					continue;
				}

				String[] splits = line.split("=");
				String name = splits[0].trim();
				String clazzName = splits[1].trim();

				try {
					Class<ObjectCodec> clazz = getClazz(clazzName);
					objectCodecNameMap.put(name, clazz);
				} catch (ClassNotFoundException e) {
					scanner.close();
					throw new RuntimeException("FATAL : class not found for object-codec-alias : " + line);
				}
			}
			scanner.close();
		}
	}

	public static BinaryCodec createBinaryCodec(UnionBuffer unionBuffer, String alias, int typeIndex) {
		Class<? extends BinaryCodec> clazz = binaryCodecNameMap.get(alias);
		try {
			Constructor<? extends BinaryCodec> constructor = clazz.getConstructor(UnionBuffer.class, int.class);
			return constructor.newInstance(unionBuffer, typeIndex);
		} catch (Exception e) {
			throw new RuntimeException("FATAL : cannot find proper constructor for binary codec type : " + alias);
		}
	}

	// public static BinaryCodec[] createBinaryCodecArray(UnionBuffer unionBuffer) throws Exception {
	// BinaryCodec[] result = new BinaryCodec[NeuronConstants.RUNTIME_MAX_FIELD_COUNT];
	//
	// String[] typeArray = fieldLayout.getTypeArray();
	// for (int typeIndex = 0; typeIndex < typeArray.length; ++typeIndex) {
	// String type = typeArray[typeIndex];
	// if (type == null) {
	// continue;
	// }
	// result[typeIndex] = SchemaUtils.createBinaryCodec(unionBuffer, type, typeIndex);
	// }
	//
	// return result;
	// }

	// public static AllocPolicy[] createAllocPolicyArray(NodeLayout nodeLayout, AllocPolicyLayout allocPolicyLayout) {
	// AllocPolicy[] result = new AllocPolicy[nodeLayout.getSectorLayoutArray().length];
	//
	// for (SectorLayout sectorLayout : nodeLayout.getSectorLayoutArray()) {
	// if(sectorLayout == null){
	// continue;
	// }
	// int sectorIndex = sectorLayout.getIndex();
	//
	// String sectorName = sectorLayout.getName();
	//
	// AllocPolicyItemLayout itemLayout = null;
	// for (AllocPolicyItemLayout item : allocPolicyLayout.getLayoutItems()) {
	// if (item.getSectorName().equals(sectorName)) {
	// itemLayout = item;
	// }
	// }
	//
	// AllocPolicy policy = null;
	// if (itemLayout == null) {
	// policy = new FixedAllocPolicy();
	// } else {
	// policy = (AllocPolicy) JsonPropertyUtils.newInstance(itemLayout.getClass(), itemLayout.getProperties());
	// }
	//
	// result[sectorIndex] = policy;
	// }
	//
	// return result;
	//
	// }

	// public static int[] createFieldIndexArray( List<ColumnLayout> fieldReferenceList) {
	// int[] indexArray = new int[fieldReferenceList.size()];
	// for (int i = 0; i < fieldReferenceList.size(); ++i) {
	// ColumnLayout sectorFieldLayout = fieldReferenceList.get(i);
	// indexArray[i] = fieldLayout.getIndex(sectorFieldLayout.getField());
	// }
	//
	// return indexArray;
	// }

	// public static int[] createFieldIndexArray(FieldLayout fieldLayout, SectorLayout sectorLayout) {
	// List<ColumnLayout> fieldReferenceList = sectorLayout.getColumnList();
	// return createFieldIndexArray(fieldLayout, fieldReferenceList);
	// }

	// public static int[] createFieldIndexArray(FieldLayout fieldLayout, SummaryLayout sectorLayout) {
	// List<ColumnLayout> fieldReferenceList = sectorLayout.getColumnList();
	// return createFieldIndexArray(fieldLayout, fieldReferenceList);
	// }

	public static ObjectCodec createObjectCodec(ColumnLayout layout) {
		return createObjectCodec(layout.getType());
	}

	public static ObjectCodec createObjectCodec(String alias) {
		Class<? extends ObjectCodec> clazz = objectCodecNameMap.get(alias);
		try {
			Constructor<? extends ObjectCodec> constructor = clazz.getConstructor();
			return constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("unable to initialize object-codec by default constructor : " + alias, e);
		}
	}

	// public static ObjectCodec[] createObjectCodecArray(FieldLayout fieldLayout) {
	// ObjectCodec[] result = new ObjectCodec[NeuronConstants.RUNTIME_MAX_FIELD_COUNT];
	//
	// String[] typeArray = fieldLayout.getTypeArray();
	// for (int typeIndex = 0; typeIndex < typeArray.length; ++typeIndex) {
	// String type = typeArray[typeIndex];
	// if (type == null) {
	// continue;
	// }
	// result[typeIndex] = SchemaUtils.createObjectCodec(type);
	// }
	//
	// return result;
	// }

	public static BinaryCodec[] createSectorBinaryCodecArray(UnionBuffer unionBuffer, SectorLayout sectorLayout) {
		// iterator's col codec
		ArrayList<ColumnLayout> fieldReferenceList = sectorLayout.getColumnList();

		BinaryCodec[] sectorDecoderArray = new BinaryCodec[fieldReferenceList.size()];

		for (int index = 0; index < fieldReferenceList.size(); ++index) {
			ColumnLayout columnLayout = fieldReferenceList.get(index);
			String type = columnLayout.getType();
			int typeIndex = columnLayout.getSequence();

			sectorDecoderArray[index] = createBinaryCodec(unionBuffer, type, typeIndex);
		}

		return sectorDecoderArray;
	}

	public static ObjectCodec[] createSectorObjectCodecArray(SectorLayout sectorLayout) {
		// iterator's col codec
		ArrayList<ColumnLayout> fieldReferenceList = sectorLayout.getColumnList();
		ObjectCodec[] sectorDecoderArray = new ObjectCodec[fieldReferenceList.size()];
		
		for (int index = 0; index < fieldReferenceList.size(); ++index) {
			ColumnLayout columnLayout = fieldReferenceList.get(index);
			sectorDecoderArray[index] = createObjectCodec(columnLayout);
		}
		return sectorDecoderArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClazz(String clazzName) throws ClassNotFoundException {
		return (Class<T>) Class.forName(clazzName);
	}
}
