package com.chbrown13.pull_rec;

import com.jcabi.github.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.json.JsonObject;

/**
 * PullRecommender is the main class for this project and handles interactions with Github repositories.
 */
public class PullRecommender {

	private Repo repo;
	private int recs;
	private Set<String> prs;
	private static int open = 0;
	private static int pulls;

	public PullRecommender(Repo repo) {
		this.repo = repo;
		Utils.setProjectName(repo.coordinates().repo());
		Utils.setProjectOwner(repo.coordinates().user());
		prs = new HashSet<String>();
	}

	/**
	 * Gets the number of recommendations made
	 *
	 * @return   Count of recommendations
     */
	public int getRecommendationCount() {
		return this.recs;
	}

	public int getPRCount() {
		return this.prs.size();
	}
	
	/**
	 * Post message recommending tool to Github on pull request fixing error.
	 *
	 * @param comment   Comment with recommendation
	 * @param pull	 	Pull request to comment on
	 * @param error     Error fixed in pull request
	 */
	private void makeRecommendation(Tool tool, Pull.Smart pull, Error error, String hash, int line, Set<Error> errors) {
		String comment = error.generateComment(tool, errors, hash);
		System.out.println(comment);
		recs += 1;
		prs.add(error.getKey());
	}

	/**
	 * Analyze code of files in pull request and compare to master branch.
	 *
	 * @param pull   Current pull request
	 * @return       Number of recommendations made
	 */
	private void analyze(Pull.Smart pull) {
		Tool tool = new ErrorProne();
		System.out.println("Analyzing PR #" + Integer.toString(pull.number()) + "...");
		try {
			String author = pull.json().getJsonObject("user").getString("login");
			String pullHash = pull.json().getJsonObject("head").getString("sha");
			String baseHash = pull.json().getJsonObject("base").getString("sha");
			Set<Error> baseErrors = Utils.checkout(baseHash, author, tool, true);
			Set<Error> pullErrors = Utils.checkout(pullHash, author, tool, false);
			if(baseErrors != null && pullErrors != null) {
				Set<Error> fixed = new HashSet<Error>();				
				fixed.addAll(baseErrors);				
				//System.out.println("LET'S GO!!!!!!!!!!!!!");
				fixed.removeAll(pullErrors);
				for (Error e: fixed) {
					//System.out.println(e.getKey() + " " + pullErrors.contains(e));
					if (Utils.isFix(e)) {
						makeRecommendation(tool, pull, e, pullHash, Utils.getFix(), baseErrors);
					}
				}

			}
			Utils.cleanup();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Searches for new pull requests opened for a Github repository every 15 minutes.
	 *
	 * @return   List of new pull requests
	 */
	private ArrayList<Pull.Smart> getPullRequests(int num) {
		System.out.println("Getting new pull requests...");
		ArrayList<Pull.Smart> requests = new ArrayList<Pull.Smart>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("state", "all");
		Iterator<Pull> pullit = this.repo.pulls().iterate(params).iterator();
		int i = 0;
		while (pullit.hasNext()) {
			if (i >= num) {
				break;
			}
			Pull.Smart pull = new Pull.Smart(pullit.next());
			try{
				if (pull.state().equals("open")) {
					open += 1;
				}
			} catch (Exception e) {
				//nada
			}
			requests.add(pull);
			i++;
			pulls++;
		}
		return requests;
	}

	public static void main(String[] args) {
		String[] acct = Utils.getCredentials(".github.creds");
        RtGithub github = new RtGithub(acct[0], acct[1]);
        Repo repo = github.repos().get(new Coordinates.Simple(args[0], args[1]));
		PullRecommender recommender = new PullRecommender(repo);
		ArrayList<Pull.Smart> requests = recommender.getPullRequests(Integer.parseInt(args[2]));
		if (requests != null && !requests.isEmpty()) {
			for (Pull.Smart pull: requests) {
				recommender.analyze(pull);
			}
		}
		System.out.println("{num} recommendations made on {prs} PRs, {open} which were open out of {pulls} total."
			.replace("{num}", Integer.toString(recommender.getRecommendationCount()))
			.replace("{prs}", Integer.toString(recommender.getPRCount()))
			.replace("{open}", Integer.toString(open))
			.replace("{pulls}", Integer.toString(pulls)));
	}
}


