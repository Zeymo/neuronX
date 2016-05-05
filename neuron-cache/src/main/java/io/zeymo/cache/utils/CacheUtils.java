package io.zeymo.cache.utils;

import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.impl.NeuronByteArrayReader;

import java.nio.ByteBuffer;

/**
 * Created By Zeymo at 14-8-14 22:35
 */
public class CacheUtils {

	public static String toString(final byte[] array, final String prefix) {
		final StringBuffer sb = new StringBuffer();
		sb.append(prefix + " [");
		for (final byte b : array) {
			sb.append(b + ",");
		}
		final String str = sb.substring(0, sb.length() - 1);
		return str + "]";
	}

	public static String toString(final ByteBuffer buffer, final int blockSize, final int[] array, final int keyLength, final int valueLength, final String prefix) {

		final int position = buffer.position();
		final int limit = buffer.limit();

		final StringBuffer sb = new StringBuffer();
		final byte[] key = new byte[keyLength];
		final byte[] value = new byte[valueLength];
		final NeuronByteArrayReader keyReader = new NeuronByteArrayReader(key, 0, key.length);
		final NeuronByteArrayReader valueReader = new NeuronByteArrayReader(value, 0, value.length);
		try {
			sb.append(prefix + " [");
			for (int i = 0; i < array.length; i++) {
				NeuronReader.reuse(keyReader, key, 0, keyLength);
				NeuronReader.reuse(valueReader, value, 0, valueLength);
				if ((i & 1) == 0) {
					sb.append("(");
				}
				sb.append(array[i]);
				if ((i & 1) == 0) {
					sb.append(",");
				}
				if ((i & 1) != 0) {
					sb.append(")");
				}
				if ((i & 1) != 0) {
					final int index = ((i - 1) >> 1);
					sb.append("~" + index + "|");
					buffer.position(index * blockSize);
					buffer.limit((index * blockSize) + blockSize);
					if (buffer.hasRemaining()) {
						buffer.get(key, 0, keyLength);
						final long k = keyReader.readFLong();
						sb.append(k + ":");
						buffer.get(value, 0, valueLength);
                        //for test,just support int and long type
                        if(valueLength == 4){
                            final long v = valueReader.readFInt();
                            sb.append(v);
                        }else if(valueLength == 8){
                            final long v = valueReader.readFLong();
                            sb.append(v);
                        }else{

                        }

					}
				}
				if ((i & 1) != 0) {
					sb.append(",");
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			buffer.position(position);
			buffer.limit(limit);
		}
		final String str = sb.substring(0, sb.length() - 1);
		return str + "]";

	}

	// private static final sun.misc.Unsafe UNSAFE;
	//
	// static
	// {
	// sun.misc.Unsafe unsafe;
	// try{
	// Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
	// unsafeField.setAccessible( true );
	// unsafe = (sun.misc.Unsafe) unsafeField.get(null);
	// }catch ( Exception e ){
	// unsafe = null;
	// }
	// UNSAFE = unsafe;
	// }
	//
	// public static sun.misc.Unsafe instence(){
	// return UNSAFE;
	// }
}
