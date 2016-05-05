package io.zeymo.commons.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineCounter {
	private static long			fileCount	= 0;
	private static int			FLUSH_FLAG	= 1024 * 64;
	private static long			folderCount	= 0;
	private static StringBuffer	statistics	= new StringBuffer();
	private static String		suffixs[];
	private static long			sums		= 0;
	private static String		target;

	private static long countFileTextLines(File file) throws IOException {
		long result = 0;
		if (statistics.length() > FLUSH_FLAG) {
			System.out.print(statistics.toString());
			statistics = new StringBuffer();
		}
		statistics.append("Counting in ").append(file.getAbsolutePath());
		BufferedReader br = new BufferedReader(new FileReader(file));
		while (br.readLine() != null)
			result++;
		br.close();
		statistics.append("   -  ").append(result).append("\n");
		fileCount++;
		return result;
	}

	private static boolean isMatchSuffixs(File file) {
		String fileName = file.getName();
		if (fileName.indexOf(".") != -1) {
			String extName = fileName.substring(fileName.indexOf(".") + 1);
			for (int i = 0; i < suffixs.length; i++) {
				if (suffixs[i].equals(extName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void main(String... args) throws IOException {
		if (args.length == 0) {
			args = new String[] { "/Users/arraynil/workspaces/ejava-tbp/", // 这里是项目的根目录
					"java", "grammar" }; // 这里是统计文件的后缀名
		}

		long startTimes = System.currentTimeMillis();
		if (args.length > 1)
			suffixs = new String[args.length - 1];
		else {
			System.out.println("As that : targetLocation , fileSuffix , fileSuffix . . .");
			return;
		}
		for (int i = 0; i < args.length; i++) {
			if (i == 0) {
				target = args[i];
			} else {
				suffixs[i - 1] = args[i];
			}
		}
		File targetFile = new File(target);
		if (targetFile.exists()) {
			statistic(targetFile);
			System.out.print(statistics.toString());
			System.out.println("All completement. U write [" + sums + "] lines code in [" + fileCount + "] files within [" + folderCount + "] folders. did great job!");
		} else {
			System.out.println("File or Dir not exist : " + target);
		}
		System.out.println("Total times " + (System.currentTimeMillis() - startTimes) + " ms");
	}

	private static void statistic(File file) throws IOException {
		if (file.isDirectory() && !file.getName().startsWith(".")) {
			++folderCount;
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				statistic(files[i]);
			}
		}
		if (file.isFile()) {
			if (isMatchSuffixs(file)) {
				sums += countFileTextLines(file);
			}
		}
	}
}