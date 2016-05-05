package io.zeymo.cache.utils;

public class MemorySizeParser {
	private static final class MemorySize {
		private final String	configuredMemorySizeWithoutUnit;
		private final long		multiplicationFactor;

		private MemorySize(final String configuredMemorySizeWithoutUnit, final long multiplicationFactor) {
			this.configuredMemorySizeWithoutUnit = configuredMemorySizeWithoutUnit;
			this.multiplicationFactor = multiplicationFactor;
		}

		public long calculateMemorySizeInBytes() throws IllegalArgumentException {
			try {
				final long memorySizeLong = Long.parseLong(this.configuredMemorySizeWithoutUnit);
				final long result = memorySizeLong * this.multiplicationFactor;
				if (result < 0) {
					throw new IllegalArgumentException("memory size cannot be negative : " + result);
				}
				return result;
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("invalid format for memory size");
			}
		}
	}

	private static final long	BYTE		= 1;
	private static final long	KILOBYTE	= 1024;
	private static final long	MEGABYTE	= 1024 * MemorySizeParser.KILOBYTE;
	private static final long	GIGABYTE	= 1024 * MemorySizeParser.MEGABYTE;

	private static final long	TERABYTE	= 1024 * MemorySizeParser.GIGABYTE;

	public static long parse(final String configuredMemorySize) throws IllegalArgumentException {
		final MemorySize size = MemorySizeParser.parseIncludingUnit(configuredMemorySize);
		return size.calculateMemorySizeInBytes();
	}

	private static MemorySize parseIncludingUnit(String configuredMemorySize) throws IllegalArgumentException {
		if ((configuredMemorySize == null) || "".equals(configuredMemorySize)) {
			return new MemorySize("0", MemorySizeParser.BYTE);
		}
		configuredMemorySize = configuredMemorySize.trim().toLowerCase();

		final char unit = configuredMemorySize.charAt(configuredMemorySize.length() - 1);
		MemorySize memorySize;

		switch (unit) {
		case 'k':
		case 'K':
			memorySize = MemorySizeParser.toMemorySize(configuredMemorySize, MemorySizeParser.KILOBYTE);
			break;
		case 'm':
		case 'M':
			memorySize = MemorySizeParser.toMemorySize(configuredMemorySize, MemorySizeParser.MEGABYTE);
			break;
		case 'g':
		case 'G':
			memorySize = MemorySizeParser.toMemorySize(configuredMemorySize, MemorySizeParser.GIGABYTE);
			break;
		case 't':
		case 'T':
			memorySize = MemorySizeParser.toMemorySize(configuredMemorySize, MemorySizeParser.TERABYTE);
			break;
		default:
			try {
				Integer.parseInt("" + unit);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("invalid format for memory size [" + configuredMemorySize + "]");
			}
			memorySize = new MemorySize(configuredMemorySize, MemorySizeParser.BYTE);
		}

		return memorySize;
	}

	private static MemorySize toMemorySize(final String configuredMemorySize, final long unitMultiplier) {
		if (configuredMemorySize.length() < 2) {
			throw new IllegalArgumentException("invalid format for memory size [" + configuredMemorySize + "]");
		}
		return new MemorySize(configuredMemorySize.substring(0, configuredMemorySize.length() - 1), unitMultiplier);
	}

}
