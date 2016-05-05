package io.zeymo.cache;

public class DefaultCacheParam implements CacheParam {

	private boolean	evicted;
	private byte[]	evictKey;
	private byte[]	evictValue;
	private int		evictValueLength;

	private byte[]	key;
	private byte[]	value;
	private int		valueLength;

	public DefaultCacheParam(final byte[] key, final byte[] returnValue) {
		this.key = key;
		this.evictKey = new byte[key.length];
		this.value = returnValue;
		this.evictValue = new byte[value.length];
	}

	public DefaultCacheParam(byte[] key, byte[] value, byte[] evictKey, byte[] evictValue) {
		this.key = key;
		this.value = value;
		this.evictKey = evictKey;
		this.evictValue = evictValue;
	}

	// public DefaultCacheParam() {
	// }

	public DefaultCacheParam(final byte[] key, final byte[] returnValue, final int valueLength) {
		this(key, returnValue);
		this.valueLength = valueLength;
	}

	@Override
	public byte[] getEvictKey() {
		return evictKey;
	}

	@Override
	public byte[] getEvictValue() {
		return evictValue;
	}

	@Override
	public int getEvictValueLength() {
		return evictValueLength;
	}

	@Override
	public byte[] getKey() {
		return this.key;
	}

	@Override
	public byte[] getValue() {
		return this.value;
	}

	@Override
	public int getValueLength() {
		return this.valueLength;
	}

	@Override
	public boolean isEvicted() {
		return evicted;
	}

	public void reset() {
		this.evicted = false;
		this.valueLength = -1;
		this.evictValueLength = -1;
	}

	@Override
	public void setEvicted(boolean evicted) {
		this.evicted = evicted;

	}

	public void setEvictKey(byte[] evictKey) {
		this.evictKey = evictKey;
	}

	public void setEvictValue(byte[] evictValue) {
		this.evictValue = evictValue;
	}

	@Override
	public void setEvictValueLength(int lenght) {
		this.evictValueLength = lenght;

	}

	public void setKey(final byte[] key) {
		this.key = key;
	}

	public void setValue(final byte[] value) {
		this.value = value;
	}

	@Override
	public void setValueLength(final int valueLength) {
		this.valueLength = valueLength;
	}

}