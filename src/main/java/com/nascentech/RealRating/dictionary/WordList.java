package com.nascentech.RealRating.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class WordList {
	private static ArrayList<String> good_words = new ArrayList<String>();
	private static ArrayList<String> bad_words = new ArrayList<String>();
	private static ArrayList<String> product_issues = new ArrayList<String>();

	public static ArrayList<String> getGoodWordList() {
		try {
			File file = new File("C:/Users/Amogh/Desktop/good_words.txt");

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				good_words.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return good_words;
	}

	public static ArrayList<String> getBadWordList() {
		try {
			File file = new File("C:/Users/Amogh/Desktop/bad_words.txt");

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				bad_words.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bad_words;
	}

	public static ArrayList<String> getProductIssueList() {
		try {
			File file = new File("C:/Users/Amogh/Desktop/product_issues.txt");

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				product_issues.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return product_issues;
	}
}
