package db;

import java.util.ArrayList;
import java.util.List;

public class DBNode {
	private final int id;
	private final String name;
	private List<DBOutcome> outcomes = null;
	private List<DBNode> parents = null;
	public List<Double> probabilities = null;

	public DBNode(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<DBOutcome> getOutcomes() {
		return outcomes;
	}

	public String[] getOutcomesArray() {
		final String[] result = new String[outcomes.size()];
		for (int i = 0; i < outcomes.size(); i++) {
			result[i] = outcomes.get(i).getName();
		}
		return result;
	}

	public List<DBNode> getParents() {
		return parents;
	}

	public String[] getParentsArray() {
		if (parents == null || parents.size() == 0) {
			return new String[]{};
		}
		final String[] result = new String[parents.size()];
		for (int i = 0; i < parents.size(); i++) {
			result[i] = parents.get(i).getName();
		}
		return result;
	}

	public List<Double> getProbabilities() {
		return probabilities;
	}

	public double[] getProbabilitiesArray() {
		return toPrimitive(probabilities);
	}

	public void addOutcome(DBOutcome outcome) {
		if (outcomes == null)
			outcomes = new ArrayList<DBOutcome>();
		this.outcomes.add(outcome);
	}

	public void addParent(DBNode parent) {
		if (parents == null)
			parents = new ArrayList<DBNode>();
		this.parents.add(parent);
	}

	public void addProbability(double probability) {
		if (probabilities == null)
			probabilities = new ArrayList<Double>();
		this.probabilities.add(probability);
	}

	private static double[] toPrimitive(List<Double> array) {
		if (array == null || array.size() == 0) {
			return new double[]{};
		}
		final double[] result = new double[array.size()];
		for (int i = 0; i < array.size(); i++) {
			result[i] = array.get(i).doubleValue();
		}
		return result;
	}
}