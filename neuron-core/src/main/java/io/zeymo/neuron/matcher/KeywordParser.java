package io.zeymo.neuron.matcher;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronOutputBuffer;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.NeuronConstants;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <br>
 * 简化范式
 * <ul>
 * <li>QUERY : OR</li>
 * <li>OR : AND [OR AND]*</li>
 * <li>AND : LEFT [AND RIGHT]*</li>
 * <li>QUOTE : (OR) | KEYWORD</li>
 * <li>KEYWORD : STRING</li>
 * </ul>
 */
public class KeywordParser {

	private final static boolean	DEBUG	= false;

	public static interface NodeType {
		public final static int	AND		= 1;
		public final static int	OR		= 2;
		public final static int	KEYWORD	= 4;
	}

	public static class Node implements BinaryWritable {
		int				type;
		String			value;
		ArrayList<Node>	subs;

		protected Node() {
			this.subs = new ArrayList<Node>();
		}

		public Node(int type, String value) {
			this.type = type;
			this.value = value;
			this.subs = new ArrayList<Node>();
		}

		public void addSub(Node sub) {
			subs.add(sub);
		}

		@Override
		public void readFields(NeuronReader reader) throws IOException {
			this.type = reader.readByte();
			this.subs.clear();

			switch (type) {
			case NodeType.KEYWORD:
				this.value = reader.readString(NeuronConstants.STRING_CHARSET);
				break;
			case NodeType.AND:
			case NodeType.OR:
				int count = reader.readUVInt();

				for (int i = 0; i < count; ++i) {
					Node sub = new Node();
					this.subs.add(sub);
					sub.readFields(reader);
				}
				break;
			default:
				throw new IllegalArgumentException("unknown type code : " + this.type);
			}
		}

		@Override
		public void write(NeuronWriter writer) throws IOException {
			writer.writeByte(this.type);
			switch (type) {
			case NodeType.KEYWORD:
				byte[] bytes = value.getBytes(NeuronConstants.STRING_CHARSET);
				writer.writeUVInt(bytes.length);
				writer.writeBytes(bytes);
				return;
			case NodeType.AND:
			case NodeType.OR:
				writer.writeUVInt(this.subs.size());
				for (Node sub : subs) {
					sub.write(writer);
				}
				return;
			default:
				throw new NullPointerException("type is not set.");
			}
		}

		public void toString(StringBuilder sb) {
			switch (type) {
			case NodeType.KEYWORD:
				sb.append(this.value);
				return;
			case NodeType.AND:
				sb.append("[AND](");
				break;
			case NodeType.OR:
				sb.append("[OR](");
				break;
			default:
				throw new IllegalArgumentException("unknown type code : " + this.type);
			}
			
			int len = this.subs.size();
			for (int i = 0; i < len; ++i) {
				this.subs.get(i).toString(sb);
				if (i < len - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
		}
	}

	/**
	 * 根据淘宝搜索引擎的关键字预发构造AST并剪枝 <br>
	 * 
	 * @param query
	 * @return
	 */
	public Node parse(String query) {
		return parseOr(0, query);
	}

	public void translate(String query, NeuronWriter output) throws IOException {
		Node node = parse(query);
		node.write(output);
	}

	public byte[] translate(String query) throws IOException {

		Node node = parse(query);
		NeuronOutputBuffer output = new NeuronOutputBuffer(512);
		node.write(output);

		int length = output.getRelativeOffset();
		byte[] result = new byte[length];
		System.arraycopy(output.getBuffer(), 0, result, 0, length);
		return result;
	}

	private Node parseQuote(int index, String query) {
		// robust
		query = query.trim();
		if (DEBUG) {
			System.out.println(StringUtils.repeat("\t", index) + "QUOTE '" + query + "'");
		}
		if (query.startsWith("(") && query.endsWith(")")) {
			return parseOr(index + 1, query.substring(1, query.length() - 1));
		}

		Node node = new Node(NodeType.KEYWORD, query);
		return node;
	}

	private Node parseAnd(int index, String query) {
		// robust
		query = query.trim();
		if (DEBUG) {
			System.out.println(StringUtils.repeat("\t", index) + "AND '" + query + "'");
		}
		Node node = new Node(NodeType.AND, null);

		int depth = 0;
		int start = 0;

		for (int i = 0; i < query.length(); ++i) {
			char c = query.charAt(i);

			if (c == '(') {
				++depth;
			}

			if (c == ')') {
				--depth;
			}

			if (depth == 0 && c == ' ') {
				int x = i;
				// robust
				while (query.charAt(i + 1) == ' ') {
					++i;
				}

				Node sub = parseQuote(index + 1, query.substring(start, x));
				node.addSub(sub);
				start = i + 1;
			}
		}

		Node sub = parseQuote(index + 1, query.substring(start, query.length()));
		node.addSub(sub);

		if (node.subs.size() == 1) {
			return node.subs.get(0);
		}
		return node;

	}

	private Node parseOr(int index, String query) {
		// robust
		query = query.trim();
		if (DEBUG) {
			System.out.println(StringUtils.repeat("\t", index) + "OR '" + query + "'");
		}

		Node node = new Node(NodeType.OR, null);
		int depth = 0;
		int start = 0;
		boolean broke = false;
		boolean meetO = false;
		boolean meetR = false;

		for (int i = 0; i < query.length(); ++i) {
			char c = query.charAt(i);
			if (c == '(') {
				++depth;
			}
			if (c == ')') {
				--depth;
			}

			if (c == ' ') {
				int x = i;
				// robust
				while (query.charAt(i + 1) == ' ') {
					++i;
				}

				if (broke && depth == 0 && meetO && meetR) {
					Node sub = parseAnd(index + 1, query.substring(start, x - 3));
					node.addSub(sub);

					start = i + 1;
					continue;
				}
				broke = true;
				meetO = false;
				meetR = false;
				continue;

			}

			if (broke && depth == 0 && c == 'O') {
				meetO = true;
				meetR = false;
				continue;
			}

			if (broke && depth == 0 && meetO && c == 'R') {
				meetR = true;
				continue;
			}

			broke = false;
			meetO = false;
			meetR = false;
		}
		Node sub = parseAnd(index + 1, query.substring(start, query.length()));
		node.addSub(sub);

		if (node.subs.size() == 1) {
			return node.subs.get(0);
		}
		return node;
	}

}
