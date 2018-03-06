package com.nascentech.RealRating.amazon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import com.nascentech.RealRating.dictionary.WordList;

import org.apache.commons.lang.StringUtils;

public class AmazonReviewAnalyzer {
	private static String product_id, product_review_url, review_text;
	private static final String BASE_URL = "https://www.amazon.in";
	private static String product_name;
	private static final String SORT_BY_RECENT = "&sortBy=recent";
	String[] temp, temp1;
	private Document doc;
	private static boolean isVerifiedPurchase, isProductIssueDetected, isReviewTooShort;
	private static long ratings_total, total_ratings, genuine_reviews, fake_reviews, fake_profiles, genuine_profiles,
			verified_purchases, unverified_purchases, short_reviews;
	private static float rating;
	private List<String> product_issues = new ArrayList<String>();
	private List<String> bad_review = new ArrayList<String>();
	private List<String> helpful_reviews = new ArrayList<>();
	private List<String> too_short_reviews = new ArrayList<>();
	private HashMap<String, String> fake_user_profiles = new HashMap<String, String>();
	private FirefoxDriver firefoxDriver;
	private ArrayList<String> good_words;
	private ArrayList<String> bad_words;
	private ArrayList<String> product_issues_list;

	public AmazonReviewAnalyzer() {
		good_words = WordList.getGoodWordList();
		bad_words = WordList.getBadWordList();
		product_issues_list = WordList.getProductIssueList();
	}

	public void analyzeIN(String url, int pageLimit) {
		temp = url.split("/");
		product_id = temp[5];

		product_name = temp[3];

		try {
			doc = Jsoup.connect(url).get();
			Elements links = doc.select("a#dp-summary-see-all-reviews");

			for (Element link : links) {
				temp1 = link.attr("href").split(product_id);
			}

			product_review_url = BASE_URL + temp1[0] + product_id;

			Elements total_reviews = doc.select("span#acrCustomerReviewText");
			total_ratings = Long.valueOf(total_reviews.text().split(" ")[0].replace(",", ""));

			System.out.println("Total review pages found: " + total_ratings);

			try {
				FirefoxBinary firefoxBinary = new FirefoxBinary();
				firefoxBinary.addCommandLineOptions("--headless");
				System.setProperty("webdriver.gecko.driver", "C:/Users/Amogh/Desktop/geckodriver.exe");
				FirefoxProfile firefoxProfile = new FirefoxProfile();
				firefoxDriver = new FirefoxDriver(firefoxBinary, firefoxProfile);

				for (int i = 1; i <= pageLimit; i++) {
					isReviewTooShort = false;
					System.out.println("Page: " + i);
					navigateByPage(i, firefoxDriver);
				}
			} finally {
				firefoxDriver.quit();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("Writing result to file...");

				File file = new File("C:/users/Amogh/Desktop/" + product_id + ".txt");
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				StringBuilder sb = new StringBuilder();
				sb.append("Product Name: " + product_name);
				sb.append("\nProduct ID: " + product_id);
				sb.append("\n");
				sb.append("Total ratings: " + ratings_total);
				sb.append("\n");
				sb.append("Verified purchases: " + verified_purchases);
				sb.append("\n");
				sb.append("Unverified purchases: " + unverified_purchases);
				sb.append("\n");
				sb.append("Genuine reviews: " + genuine_reviews);
				sb.append("\n");
				sb.append("Reviews rejected due to being too short: " + short_reviews);
				sb.append("\n");
				sb.append("Fake reviews: " + fake_reviews);
				sb.append("\n");
				sb.append("Genuine profiles: " + genuine_profiles);
				sb.append("\n");
				sb.append("Fake profiles: " + fake_profiles + "\n");
				sb.append("--------------------------------------------");
				sb.append("\n");
				sb.append("\nBad reviews\n");
				for (String s : bad_review) {
					sb.append(s);
					sb.append("\n");
					sb.append("----------------------------------------------");
					sb.append("\n");
				}
				sb.append("\n");
				sb.append("Product issues\n");
				for (String s : product_issues) {
					sb.append(s);
					sb.append("\n");
					sb.append("----------------------------------------------");
					sb.append("\n");
				}
				sb.append("\n");
				sb.append("Helpful reviews\n");
				for (String s : helpful_reviews) {
					sb.append(s);
					sb.append("\n");
					sb.append("----------------------------------------------");
					sb.append("\n");
				}
				sb.append("\n");
				sb.append("Rejected reviews\n");
				for (String s : too_short_reviews) {
					sb.append(s);
					sb.append("\n");
					sb.append("----------------------------------------------");
					sb.append("\n");
				}
				sb.append("\n");
				sb.append("Fake profiles\n");
				for (Map.Entry<String, String> entry : fake_user_profiles.entrySet()) {
					sb.append("User: " + entry.getKey());
					sb.append("\n");
					sb.append("Reason: " + entry.getValue());
					sb.append("\n----------------------------------------------");
					sb.append("\n");
				}

				bw.write(sb.toString());
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println(pageLimit + " page(s) analyzed succesfully");
			}
		}
	}

	private void navigateByPage(long pageNumber, FirefoxDriver driver) {
		product_review_url = BASE_URL + temp1[0] + product_id + "?pageNumber=" + (pageNumber) + SORT_BY_RECENT;

		System.out.println("Now analyzing: " + product_review_url);

		try {
			doc = Jsoup.connect(product_review_url).get();

			Elements ids = doc.select("div#cm_cr-review_list>div.review");

			for (Element id : ids) {
				System.out.println("Analyzing review ID: " + id.id());

				Elements ratings = doc.select("div#" + id.id());

				if (ratings.text().indexOf("Verified Purchase") != -1) {
					verified_purchases++;
					Pattern p = Pattern.compile("\\d+\\.\\d+");
					Matcher m = p.matcher(ratings.text());
					while (m.find()) {
						if (Float.parseFloat(m.group()) <= 5) {
							rating = Float.parseFloat(m.group());
						}
					}
					System.out.println("Ratings given: " + rating);

					ratings_total += rating;

					review_text = ratings.select("span.review-text").text();
					System.out.println(review_text);

					if (rating >= 4) {
						if (review_text.length() > 25) {
							for (String s : good_words) {
								if (StringUtils.containsIgnoreCase(review_text, s)) {
									helpful_reviews.add(review_text);
									break;
								}
							}
						} else {
							too_short_reviews.add(review_text);
							isReviewTooShort = true;
						}
					} else {
						if (review_text.length() > 25) {
							for (String s : product_issues_list) {
								if (StringUtils.containsIgnoreCase(review_text, s)) {
									product_issues.add(review_text);
									isProductIssueDetected = true;
									break;
								}
							}
							if (!isProductIssueDetected) {
								for (String s : bad_words) {
									if (StringUtils.containsIgnoreCase(review_text, s)) {
										bad_review.add(review_text);
										break;
									}
								}
							}
						} else {
							too_short_reviews.add(review_text);
							isReviewTooShort = true;
						}
					}

					Elements user_profiles = ratings.select("a.author");
					String user_profile_link = BASE_URL + user_profiles.attr("href");
					System.out.println("Analyzing profile: " + user_profile_link);

					if (analyzeUserProfile(user_profile_link, driver)) {
						fake_reviews++;
					} else {
						if (!isReviewTooShort) {
							genuine_reviews++;
						} else {
							short_reviews++;
						}
					}

				} else {
					isVerifiedPurchase = false;
					unverified_purchases++;
					fake_reviews++;
				}
				System.out.println("Verified purchase: " + isVerifiedPurchase);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean analyzeUserProfile(String profile_url, FirefoxDriver driver) {
		boolean isFake = false;
		try {
			driver.get(profile_url);
			Document doc = Jsoup.parse(driver.getPageSource());
			Elements dates = doc.select("span.date");
			String review_dates = dates.text();

			Elements name = doc.select("div.name-container");
			String user_name = name.text();
			System.out.println("Analyzing " + user_name + "'s profile");

			boolean redundantDates = analyzeRedundantDates(review_dates);
			boolean isAnnonymous = analyzeFalseName(user_name);
			if (redundantDates) {
				fake_user_profiles.put(user_name + ";" + profile_url, "Multiple reviews on single date");
			}

			if (isAnnonymous) {
				if (fake_user_profiles.containsKey(user_name + ";" + profile_url)) {
					String text = fake_user_profiles.get(user_name + ";" + profile_url);
					text = text.concat(";Not provided real name in the profile");
					fake_user_profiles.put(user_name + ";" + profile_url, text);
				} else {
					fake_user_profiles.put(user_name + ";" + profile_url, "Not provided real name in the profile");
				}
			}

			if (!redundantDates && !isAnnonymous) {
				genuine_profiles++;
			} else if (redundantDates && isAnnonymous) {
				fake_profiles++;
			} else if (redundantDates || isAnnonymous) {
				isFake = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isFake;
	}

	private boolean analyzeRedundantDates(String date) {
		int redundancyCount = 0;
		String[] dates = date.split(" ");
		for (int i = 0; i < dates.length - 1; i++) {
			if (dates[i].equalsIgnoreCase(dates[i + 1])) {
				redundancyCount++;
			}
		}
		System.out.println("Date redundancy count: " + redundancyCount);
		if (redundancyCount >= 4) {
			return true;
		}
		return false;
	}

	private boolean analyzeFalseName(String name) {
		if (name.equalsIgnoreCase("amazon customer")) {
			return true;
		}
		return false;
	}
}
