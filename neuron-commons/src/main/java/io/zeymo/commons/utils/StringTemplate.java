package io.zeymo.commons.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class StringTemplate {
	
	public static class KeySegment {
		public boolean	fixed	= false;
		public String	segment;

		public KeySegment() {

		}

		public KeySegment(boolean isFixed, String segment) {
			this.fixed = isFixed;
			this.segment = segment;
		}

	}

	private ArrayList<KeySegment>	segmentList;

	public StringTemplate() {
		this.segmentList = new ArrayList<KeySegment>();
	}

	public StringTemplate(String expression) {
		this.init(expression);
	}

	public ArrayList<KeySegment> getSegmentList() {
		return segmentList;
	}

	private void init(String expression) {
		int len = expression.length();
		boolean quote = false;

		char[] buffer = new char[len];
		int bufferIndex = 0;

		ArrayList<KeySegment> segments = new ArrayList<KeySegment>();

		for (int i = 0; i < len; ++i) {
			char c = expression.charAt(i);
			switch (c) {
			case '\\':
				if (quote) {
					buffer[bufferIndex++] = c;
					quote = false;
				} else {
					quote = true;
				}
				break;
			case '$':
				if (quote) {
					buffer[bufferIndex++] = c;
					quote = false;
					continue;
				}
				if (i + 3 < len) {
					int right = expression.indexOf('}', i + 2);
					if ('{' == expression.charAt(i + 1) && right > 1 && (right - i) > 2) {

						if (bufferIndex > 0) {
							// push
							String segment = new String(buffer, 0, bufferIndex);
							segments.add(new KeySegment(true, segment));
							bufferIndex = 0;
						}

						// 变量内容 i+2 ---> right
						{
							String segment = expression.substring(i + 2, right);
							segments.add(new KeySegment(false, segment));
							i = right;
						}
					} else { // ${here}
						buffer[bufferIndex++] = c;
					}
				} else { // i + 3 < len
					buffer[bufferIndex++] = c;
				}
				break;
			default:
				buffer[bufferIndex++] = c;
			}// switch

		}// for
		if (bufferIndex > 0) {
			// push
			String segment = new String(buffer, 0, bufferIndex);
			segments.add(new KeySegment(true, segment));
			bufferIndex = 0;
		}

		this.segmentList = segments;
	}

	public String make(HashMap<String, Object> context) {
		StringBuilder sb = new StringBuilder();
		for (KeySegment segment : this.segmentList) {
			if (segment.fixed) {
				sb.append(segment.segment);
			} else {
				sb.append(context.get(segment.segment));
			}
		}
		return sb.toString();
	}

	public void setSegmentList(ArrayList<KeySegment> segmentList) {
		this.segmentList = segmentList;
	}

	@Override
	public String toString() {
		return GsonUtils.toString(this);
	}

}