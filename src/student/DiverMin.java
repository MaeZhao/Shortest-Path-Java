package student;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import game.FindState;
import game.FleeState;
import game.Node;
import game.NodeStatus;
import game.SewerDiver;

public class DiverMin extends SewerDiver {

	/** Get to the ring in as few steps as possible. Once you get there, <br>
	 * you must return from this function in order to pick<br>
	 * it up. If you continue to move after finding the ring rather <br>
	 * than returning, it will not count.<br>
	 * If you return from this function while not standing on top of the ring, <br>
	 * it will count as a failure.
	 *
	 * There is no limit to how many steps you can take, but you will receive<br>
	 * a score bonus multiplier for finding the ring in fewer steps.
	 *
	 * At every step, you know only your current tile's ID and the ID of all<br>
	 * open neighbor tiles, as well as the distance to the ring at each of <br>
	 * these tiles (ignoring walls and obstacles).
	 *
	 * In order to get information about the current state, use functions<br>
	 * currentLocation(), neighbors(), and distanceToRing() in state.<br>
	 * You know you are standing on the ring when distanceToRing() is 0.
	 *
	 * Use function moveTo(long id) in state to move to a neighboring<br>
	 * tile by its ID. Doing this will change state to reflect your new position.
	 *
	 * A suggested first implementation that will always find the ring, but <br>
	 * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
	 * Some modification is necessary to make the search better, in general. */

	@Override
	public void find(FindState state) {
		// TODO : Find the ring and return.
		// DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
		// Instead, write your method elsewhere, with a good specification,
		// and call it from this one.

		HashSet<Long> visited= new HashSet<Long>(); // keeps track of the visited nodes
		walkSP(state, visited);

	}

	/** Uses the shortest path algorithm to move Min to the ring.<br>
	 * 
	 * @param state   : Min's current state/position
	 * @param visited : a Hashset of visited tiles
	 * @return True if the the ring is found, False if the ring is<br>
	 *         not found */
	private boolean walkSP(FindState state, HashSet<Long> visited) {
		visited.add(state.currentLocation());
		if (state.distanceToRing() == 0) { return true; }

		Long prevPos= state.currentLocation();
		Heap<Long> sortedShorts= sortedShortList(state.neighbors());
		boolean found= false;

		while (!found && sortedShorts.size != 0) {
			Long nextN= sortedShorts.poll();

			if (found == false && !visited.contains(nextN)) {
				state.moveTo(nextN);

				found= walkSP(state, visited);
				if (found == true)
					return true;
				else {
					state.moveTo(prevPos);
				}
			}
		}
		return found;
	}

	/** Sorts the parameter into a min Heap<br>
	 * 
	 * @param neighbors
	 * @return A min Heap of the parameters */
	private Heap<Long> sortedShortList(Collection<NodeStatus> neighbors) {
		Heap<Long> shortToLong= new Heap<>();
		for (NodeStatus n : neighbors) {
			shortToLong.add(n.getId(), n.getDistanceToTarget());
		}
		return shortToLong;
	}

	/** Flee the sewer system before the steps are all used, trying to <br>
	 * collect as many coins as possible along the way. Your solution must ALWAYS <br>
	 * get out before the steps are all used, and this should be prioritized above<br>
	 * collecting coins.
	 *
	 * You now have access to the entire underlying graph, which can be accessed<br>
	 * through FleeState. currentNode() and getExit() will return Node objects<br>
	 * of interest, and getNodes() will return a collection of all nodes on the graph.
	 *
	 * You have to get out of the sewer system in the number of steps given by<br>
	 * getStepsRemaining(); for each move along an edge, this number is <br>
	 * decremented by the weight of the edge taken.
	 *
	 * Use moveTo(n) to move to a node n that is adjacent to the current node.<br>
	 * When n is moved-to, coins on node n are automatically picked up.
	 *
	 * You must return from this function while standing at the exit. Failing <br>
	 * to do so before steps run out or returning from the wrong node will be<br>
	 * considered a failed run.
	 *
	 * Initially, there are enough steps to get from the starting point to the<br>
	 * exit using the shortest path, although this will not collect many coins.<br>
	 * For this reason, a good starting solution is to use the shortest path to<br>
	 * the exit. */
	@Override
	public void flee(FleeState state) {
		// TODO: Get out of the sewer system before the steps are used up.
		// DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
		// with a good specification, and call it from this one.
		HashSet<Node> coins= coins(state);
		coinCollector(state, coins);

	}

	/** Min collects as many coins in its vicinity before exiting<br>
	 * Strategy: - prioritizes 1. closest coins 2. path length <br>
	 * - tries to keep the sum taken steps and return path length <= total steps <br>
	 * - travels to the nearest coin with as few moves as possible <br>
	 * - exit path in as few moves as possible <br>
	 * 
	 * @param state : Min's current state/position
	 * @param coins : A HashSet of all coin nodes */
	private void coinCollector(FleeState state, HashSet<Node> coins) {

		Heap<Node> coinD= shortestCoinDist(state, coins);
		Node exit= state.getExit();

		// Finds the path to the nearest coin and determines whether it can move to it or not
		while (coinD.size != 0) {
			Double distanceN= coinD.peekPriority();
			Node n= coinD.poll();
			List<Node> path= Paths.shortest(state.currentNode(), n);
			path.remove(0);

			int returnLength= Paths.pathSum(Paths.shortest(n, exit));
			// if true, moves the player along corresponding path
			if (distanceN + returnLength <= state.stepsLeft()) {
				moveAlongPath(path, state);
				coins.remove(n);
				coinCollector(state, coins);
			}
		}
		List<Node> returnP= Paths.shortest(state.currentNode(), exit);
		returnP.remove(0);

		moveAlongPath(returnP, state);

	}

	private HashSet<Node> coins(FleeState state) {
		HashSet<Node> coins= new HashSet<>();
		for (Node c : state.allNodes()) {
			int tileWorth= c.getTile().getOriginalCoinValue();
			if (tileWorth != 0) {
				coins.add(c);
			}
		}
		return coins;

	}

	/** Moves Min along a path specified by its parameters
	 * 
	 * @param path  : a List of nodes that Min must move along
	 * @param state : Min's current state/position */
	private void moveAlongPath(List<Node> path, FleeState state) {
		for (Node n : path) {
			state.moveTo(n);
		}
	}

	/** Generates a min Heap of coin nodes organized by its distance from Min
	 * 
	 * @param state : Min's current state/position
	 * @param coins : HashSet of all coins on board
	 * @return min Heap of coin nodes prioritized by distance from Min */
	private Heap<Node> shortestCoinDist(FleeState state, HashSet<Node> coins) { // SHOULD
		// WE BE
		// USING A
		// HASHSET?
		Heap<Node> shortestD= new Heap<Node>();
		for (Node c : coins) {
			int tileD= Paths.pathSum(Paths.shortest(state.currentNode(), c));
			// only coin values are stored:
			if (tileD != 0) {
				shortestD.add(c, tileD);

			}
		}
		return shortestD;
	}

}
