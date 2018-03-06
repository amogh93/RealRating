package com.nascentech.RealRating;

import com.nascentech.RealRating.amazon.AmazonReviewAnalyzer;

public class Main {

	public static void main(String[] args) {
		if (args.length > 0) {
			new AmazonReviewAnalyzer().analyzeIN(args[0], Integer.valueOf(args[1]));
		} else {
			System.out.println("Taking default url");
			new AmazonReviewAnalyzer().analyzeIN(
					"https://www.amazon.in/OnePlus-Midnight-Black-64GB-memory/dp/B0756ZFXVB/ref=br_asw_pdt-1/260-6170877-4088748?pf_rd_m=A1VBAL9TL5WCBF&pf_rd_s=&pf_rd_r=15X8WE1GE283J831BAFP&pf_rd_t=36701&pf_rd_p=28f0ab62-15c4-49ad-aa05-16c127256d03&pf_rd_i=desktop",
					5);
		}
	}

}
