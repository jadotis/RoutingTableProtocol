
/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */
import java.lang.Math;
import java.util.*;

import com.sun.scenario.animation.shared.InfiniteClipEnvelope;

public class Node {

	public static final int INFINITY = 9999;

	int[] lkcost = new int[4]; /*
								 * The link cost between node 0 and other nodes
								 */
	int[][] costs = new int[4][4]; /* Define distance table */
	int nodename; /* Name of this node */

	/* Class constructor */
	public Node() {
	}

	/* students to write the following two routines, and maybe some others */
	void rtinit(int nodename, int[] initial_lkcost) {
		this.nodename = nodename;
		this.lkcost = initial_lkcost;
		// Assign the costs table values of the current node to itself
		initArray();
		this.costs[this.nodename][0] = initial_lkcost[0];
		this.costs[this.nodename][1] = initial_lkcost[1];
		this.costs[this.nodename][2] = initial_lkcost[2];
		this.costs[this.nodename][3] = initial_lkcost[3];
		for (int j = 0; j < this.lkcost.length; j++) {
			if (this.lkcost[j] != INFINITY) {
				if (j == this.nodename) {
					continue;
				} else {
					Packet p = makePacket(j, lkcost); // Sending this or if send
														// lkcosts
					NetworkSimulator.tolayer2(p);
				}
				// Send the packet off to the others.
			}
		}

	}

	void rtupdate(Packet rcvdpkt) {

		if (rcvdpkt.destid != this.nodename) {
			return;
		} else {
			
			for (int i = 0; i < 4; i++) {
				// Updates all the values in the cost vector to what has
				// been sent to us.
				int costValForI = rcvdpkt.mincost[i];
				int myCostForSrcToI = this.costs[rcvdpkt.sourceid][i];
				if(costValForI < myCostForSrcToI){
					this.costs[rcvdpkt.sourceid][i] = costValForI;
					this.costs[i][rcvdpkt.sourceid] = costValForI;
				}
			}

			boolean switchMade = false;
			ArrayList<NodePoison> toPoison = new ArrayList<NodePoison>();
			
			
			
			for (int i = 0; i < 4; i++) // Find the minumum for each path to
										// each node.
			{
				int costToI = this.costs[this.nodename][i]; //get a desired node's current cost
				for (int j = 0; j < 4; j++) {
					if((this.nodename == 3 && i == 0) ||(this.nodename == 0 && i == 3) ){
						continue;
					}
					int costToNextHop = this.costs[this.nodename][j]; //cost to a potential hop
					int theirCostToI = this.costs[j][i]; //potential hop's cost to desired node
					int totalCostWithPotentialHop = costToNextHop + theirCostToI;
					if (totalCostWithPotentialHop < costToI) { //if current desired node's cost exceeds a new cost using a hop, take that route
//						System.out.println("___________________________________________");
//						System.out.println("THIS IS MY NODENAME: " + this.nodename);
//						System.out.println("TO THE DESTINATION: " + i);
//						System.out.println("THIS WAS MY ORIGINAL COST: " + costToI);
//						System.out.println("AND I'M GOING TO HOP VIA THIS NODE: " + j);
//						System.out.println("THIS IS THE TOTAL COST WHICH IS BETTER: " + totalCostWithPotentialHop);
//						System.out.println("___________________________________________");
						this.costs[this.nodename][i] = totalCostWithPotentialHop;
						this.costs[i][this.nodename] = totalCostWithPotentialHop;
						costToI = totalCostWithPotentialHop;
						switchMade = true;
						toPoison.add(new NodePoison(i, j, totalCostWithPotentialHop));

					}
				}
			}
			
			//delete any duplicates in the array
			for (int j = 0; j < toPoison.size(); j++) {
				NodePoison np = toPoison.get(j);
				int destination = np.destination;
				int value = np.value;
				for (int j2 = 0; j2 < toPoison.size(); j2++) {
					if(j2 == j){
						continue;
					}
					NodePoison np2 = toPoison.get(j2);
					int destination2 = np2.destination;
					int value2 = np2.value;
					if(destination == destination2){
						if(value > value2){
							toPoison.remove(np);
						}else{
							toPoison.remove(np2);
						}
					}
				}
			}
			
			if (switchMade) {
				for (int i = 0; i < 4; i++) {
					if((this.nodename == 3 && i == 0) ||(this.nodename == 0 && i == 3) ){
						continue;
					}
					
					if (this.lkcost[i] == INFINITY || this.nodename == i) {
						continue;
					} else {
						int[] arr = Arrays.copyOf(this.costs[this.nodename], this.costs[this.nodename].length);

						for(NodePoison np : toPoison){
							if(np.hop == i){
//								arr[np.destination] = INFINITY;
//								System.out.println("___________________________________________");
//								System.out.println("THIS IS MY NODENAME: " + this.nodename);
//								System.out.println("TO THE DESTINATION: " + np.destination);
//								System.out.println("USING THE HOP: " + np.hop);
//								System.out.println("WITH THE IMPROVED VALUE: "  + np.value);
//								System.out.println("___________________________________________");
									
							}
						}

						Packet p = makePacket(i, arr);
						NetworkSimulator.tolayer2(p);
					}
				}
			}
		}
//		printdt();
	}

	/*
	 * called when cost from the node to linkid changes from current value to
	 * newcost
	 */
	void linkhandler(int linkid, int newcost) {
		this.lkcost[linkid] = newcost;
		this.costs[this.nodename] = this.lkcost; //We add the updated lkcosts to our costs array.
		boolean switchMade = false;
		ArrayList<NodePoison> toPoison = new ArrayList<NodePoison>();
		
		
		
		for (int i = 0; i < 4; i++) // Find the minumum for each path to
									// each node.
		{
			int costToI = this.costs[this.nodename][i]; //get a desired node's current cost
			for (int j = 0; j < 4; j++) {
				if((this.nodename == 3 && i == 0) ||(this.nodename == 0 && i == 3) ){
					continue;
				}
				int costToNextHop = this.costs[this.nodename][j]; //cost to a potential hop
				int theirCostToI = this.costs[j][i]; //potential hop's cost to desired node
				int totalCostWithPotentialHop = costToNextHop + theirCostToI;
				if (totalCostWithPotentialHop < costToI) { //if current desired node's cost exceeds a new cost using a hop, take that route
//					System.out.println("___________________________________________");
//					System.out.println("THIS IS MY NODENAME: " + this.nodename);
//					System.out.println("TO THE DESTINATION: " + i);
//					System.out.println("THIS WAS MY ORIGINAL COST: " + costToI);
//					System.out.println("AND I'M GOING TO HOP VIA THIS NODE: " + j);
//					System.out.println("THIS IS THE TOTAL COST WHICH IS BETTER: " + totalCostWithPotentialHop);
//					System.out.println("___________________________________________");
					this.costs[this.nodename][i] = totalCostWithPotentialHop;
					this.costs[i][this.nodename] = totalCostWithPotentialHop;
					costToI = totalCostWithPotentialHop;
					switchMade = true;
					toPoison.add(new NodePoison(i, j, totalCostWithPotentialHop));

				}
			}
		}
		
		//delete any duplicates in the array
		for (int j = 0; j < toPoison.size(); j++) {
			NodePoison np = toPoison.get(j);
			int destination = np.destination;
			int value = np.value;
			for (int j2 = 0; j2 < toPoison.size(); j2++) {
				if(j2 == j){
					continue;
				}
				NodePoison np2 = toPoison.get(j2);
				int destination2 = np2.destination;
				int value2 = np2.value;
				if(destination == destination2){
					if(value > value2){
						toPoison.remove(np);
					}else{
						toPoison.remove(np2);
					}
				}
			}
		}
		
		if (switchMade) {
			for (int i = 0; i < 4; i++) {
				if((this.nodename == 3 && i == 0) ||(this.nodename == 0 && i == 3) ){
					continue;
				}
				
				if (this.lkcost[i] == INFINITY || this.nodename == i) {
					continue;
				} else {
					int[] arr = Arrays.copyOf(this.costs[this.nodename], this.costs[this.nodename].length);

					for(NodePoison np : toPoison){
						if(np.hop == i){
							arr[np.destination] = INFINITY;
//							System.out.println("___________________________________________");
//							System.out.println("THIS IS MY NODENAME: " + this.nodename);
//							System.out.println("TO THE DESTINATION: " + np.destination);
//							System.out.println("USING THE HOP: " + np.hop);
//							System.out.println("WITH THE IMPROVED VALUE: "  + np.value);
//							System.out.println("___________________________________________");
//								
						}
					}

					Packet p = makePacket(i, arr);
					NetworkSimulator.tolayer2(p);
				}
			}
		}
		printdt();
	}

	/* Prints the current costs to reaching other nodes in the network */
	void printdt() {
		switch (nodename) {
		case 0:
			System.out.printf("                via     \n");
			System.out.printf("   D0 |    1     2 \n");
			System.out.printf("  ----|-----------------\n");
			System.out.printf("     1|  %3d   %3d \n", costs[1][1], costs[1][2]);
			System.out.printf("dest 2|  %3d   %3d \n", costs[2][1], costs[2][2]);
			System.out.printf("     3|  %3d   %3d \n", costs[3][1], costs[3][2]);
			break;
		case 1:
			System.out.printf("                via     \n");
			System.out.printf("   D1 |    0     2    3 \n");
			System.out.printf("  ----|-----------------\n");
			System.out.printf("     0|  %3d   %3d   %3d\n", costs[0][0], costs[0][2], costs[0][3]);
			System.out.printf("dest 2|  %3d   %3d   %3d\n", costs[2][0], costs[2][2], costs[2][3]);
			System.out.printf("     3|  %3d   %3d   %3d\n", costs[3][0], costs[3][2], costs[3][3]);
			break;
		case 2:
			System.out.printf("                via     \n");
			System.out.printf("   D2 |    0     1    3 \n");
			System.out.printf("  ----|-----------------\n");
			System.out.printf("     0|  %3d   %3d   %3d\n", costs[0][0], costs[0][1], costs[0][3]);
			System.out.printf("dest 1|  %3d   %3d   %3d\n", costs[1][0], costs[1][1], costs[1][3]);
			System.out.printf("     3|  %3d   %3d   %3d\n", costs[3][0], costs[3][1], costs[3][3]);
			break;
		case 3:
			System.out.printf("                via     \n");
			System.out.printf("   D3 |    1     2 \n");
			System.out.printf("  ----|-----------------\n");
			System.out.printf("     0|  %3d   %3d\n", costs[0][1], costs[0][2]);
			System.out.printf("dest 1|  %3d   %3d\n", costs[1][1], costs[1][2]);
			System.out.printf("     2|  %3d   %3d\n", costs[2][1], costs[2][2]);
			break;
		}
	}

	// Makes a packet from the current node to be sent
	// to some other node specified by destid
	Packet makePacket(int destid, int[] mincosts) {
		Packet p = new Packet(this.nodename, destid, mincosts);
		return p;
	}
	
	void initArray() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				this.costs[i][j] = INFINITY;
			}
		}
	}

}

class NodePoison{
	int destination;
	int hop;
	int value;
	public NodePoison(int d, int h, int v){
		this.destination = d;
		this.hop = h;
		this.value = v;
	}
}
