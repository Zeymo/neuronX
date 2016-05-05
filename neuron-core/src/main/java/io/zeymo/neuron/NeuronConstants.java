package io.zeymo.neuron;

import java.nio.charset.Charset;

public interface NeuronConstants {
	// ID:LONG - STATUS:INT - VERSION:INT - DATA
	// public final static int NODE_STATUS_OFFSET = 8;

	public final static int		COLLECTOR_MAX_RECORD_COUNT					= 100;
	public final static boolean	DEBUG										= false;
	public final static boolean	DEBUG_IO									= DEBUG;
	public final static int		MATCHER_BM_MAX_PATTERN_LENGTH				= 128;

	/**
	 * IC/SELL允许发布的商品标题最大长度为60字符(中文30个)，Neuron采用Byte[]+KMP/BM，128长度的byte[]妥妥的
	 */
	public final static int		MATCHER_KMP_MAX_PATTERN_LENGTH				= 128;

	public final static int		NEURON_NETWORK_CONPONENT_ID					= 0;
	public final static long	NEURON_NETWORK_POOLING_TIMEOUT_MS			= 1000;
	public final static int		NO_OP_INDEX									= -1;

	public final static int		RANK_ITEM_MAX_ARRAY_LENGTH					= 32;

	/**
	 * 集群内并发远程请求最大并发度
	 */
	public final static int		RUNTIME_RPC_MAX_BATCH_SIZE					= 4;

	/**
	 * 返回值中最大有多少个不同的BinaryWritable
	 */
	public final static int		RUNTIME_RPC_MAX_RESPONSE_GROUP_SUB_COUNT	= 32;
	public final static int		RUNTIME_RPC_MAX_RESPONSE_GROUP_CAPACITY		= 1 * 1024 * 256;

	public final static int		RUNTIME_RPC_MAX_DISPATCH_PLAN_SIZE			= 256;

	/**
	 * 在集群中，本地请求任务中node数量，应当略大于 RUNTIME_RPC_MAX_DISPATCH_PLAN_SIZE
	 */
	public final static int		RUNTIME_RPC_MAX_EVENT_NODE_LIST_SIZE		= 256;

	public final static int		RUNTIME_MAX_BUFFER_SIZE						= 512 * 1024;

	/**
	 * 每个节点的最大数据量，超过这个量级通通干掉
	 */
	public final static int		RUNTIME_MAX_CLUSTER_SIZE					= 16;
	public final static int		RUNTIME_MAX_REDUCE_INPUT_COUNT				= 64;

	/**
	 * 一次请求中最大的node数量
	 */
	public final static int		RUNTIME_MAX_NODE_QUERY_BATCH				= 1024;
	public final static int		RUNTIME_MAX_FIELD_COUNT						= 32;
	public final static int		RUNTIME_MAX_FIELD_SIZE						= 300;						// 发布的Feed内容最大长度140中文字符

	public final static int		RUNTIME_MAX_REQUEST_BATCH_SIZE				= 1024 * 2 * 256;
	public final static int		RUNTIME_RPC_MAX_REQUEST_SIZE				= 1024 * 1 * 256;
	public final static int		RUNTIME_RPC_MAX_RESPONSE_SIZE				= 1024 * 1 * 256;
	/**
	 * 批量调用集群机器的返回信息最大值
	 */
	public final static int		RUNTIME_RPC_BATCH_RESPONSE_SIZE				= 1024 * 1 * 256;

	/**
	 * 最大行宽度
	 */
	public final static int		RUNTIME_MAX_ROW_SIZE						= 1024;

	public final static int		RUNTIME_MAX_SECTOR_COUNT					= 8;
	public final static int		RUNTIME_MAX_SECTOR_ROW_COUNT				= 1024;

	public final static int		RUNTIME_MAX_SUMMARY_COUNT					= 8;

	/**
	 * 最大taskName长度, 形如 "neuron.getNodes".length()
	 */
	public final static int		RUNTIME_MAX_TASK_NAME_LENGTH				= 128;

	/**
	 * Search输出的最大内容缓存长度（各个召回字段长度总和*召回数量）
	 */
	public final static int		SEARCH_CONTENT_BUFFER_LENGTH				= RUNTIME_MAX_BUFFER_SIZE;

	public final static int		SEARCH_MAX_COLLECTOR_SIZE					= 128;

	public final static int		SEARCH_MAX_MATCHER_ARRAY_LENGTH				= 16;

	/**
	 * 一次传入引擎的最大NODE_ID数量
	 */
	public final static int		SEARCH_MAX_NODE_ARRAY_LENGTH				= 256;

	public final static int		SEARCH_MAX_OP_ARRAY_LENGTH					= 512;

	public final static int		SIZE_OF_LONG								= 8;

	public final static int		STAT_GROUP_ARRAY_MAX_LENGTH					= 32;

	public final static int		STAT_ITEM_ARRAY_MAX_LENGTH					= 32;

	// public final static String STORAGE_APPEND_DIRNAME = "append";
	// public final static String STORAGE_DATA_FILENAME = "neuron.data";
	//
	// public final static String STORAGE_DUMP_DIRNAME = "dump";
	//
	// public final static String STORAGE_INDEX_FILENAME = "neuron.index";
	//
	// public final static long STORAGE_MAX_FILESIZE = Integer.MAX_VALUE;
	//
	// public final static long STORAGE_MAX_FILESIZE_THREHOLD =
	// STORAGE_MAX_FILESIZE - RUNTIME_MAX_BUFFER_SIZE;
	//
	// public final static int STORAGE_SEQUENCIAL_INPUT_BLOCK_SIZE = 1024 * 8;

	/**
	 * 双字节汉字存储，比UTF-8效率略高，另外GBK采用0x7F一类的前缀，编码层的equals判断也比较好实现
	 */
	public final static Charset	STRING_CHARSET								= Charset.forName("GBK");

	public final static int		UNION_CACHE_UNIT_SIZE						= 2;

}
