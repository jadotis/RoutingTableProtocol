
/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */
import java.lang.Math;
import java.util.*;

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
			boolean shouldUpdateAll = false;
			// If we have all INFINITY values for the sourceID we should update
			// all.
			// For the intial setup of the cost array
			for (int i = 0; i < 4; i++) {
				if (this.costs[rcvdpkt.sourceid][i] != INFINITY) {
					break;
				} else if (i == 3 && this.costs[rcvdpkt.sourceid][i] == INFINITY) {
					shouldUpdateAll = true;
				}
			}
			if (shouldUpdateAll) {
				for (int i = 0; i < 4; i++) {
					// Updates all the values in the cost vector to what has
					// been sent to us.
					this.costs[rcvdpkt.sourceid][i] = rcvdpkt.mincost[i];
					if(i == this.nodename){
						int currCostToSrc = this.costs[this.nodename][rcvdpkt.sourceid];
						int srcCostToMe = rcvdpkt.mincost[i];
						if(srcCostToMe < currCostToSrc){
							this.costs[this.nodename][rcvdpkt.sourceid] = srcCostToMe;
						}
					}
				}

				// TO do poison reverse we should make a packet here
				// The values in the packet at the value of source ID should be
				// infinity
				for (int i = 0; i < 4; i++) {
					int currCostForI = this.costs[rcvdpkt.sourceid][i];
					int newCost = rcvdpkt.mincost[i];
					if (newCost < currCostForI) {
						this.costs[rcvdpkt.sourceid][i] = newCost;
						this.costs[i][rcvdpkt.sourceid] = newCost;
					}
				}
				boolean switchMade = false;
				for (int i = 0; i < 4; i++) // Find the minumum for each path to
											// each node.
				{
					int costToI = this.costs[this.nodename][i];
					for (int j = 0; j < 4; j++) {
						int costToNextHop = this.costs[this.nodename][j];
						int theirCostToI = this.costs[j][i];
						int totalCostWithPotentialHop = costToNextHop + theirCostToI;
						if (totalCostWithPotentialHop < costToI) {
							this.costs[this.nodename][i] = totalCostWithPotentialHop;
							costToI = totalCostWithPotentialHop;
							switchMade = true;
						}

					}
				}
				if (switchMade) {
					for (int i = 0; i < 4; i++) {
						if (this.lkcost[i] == INFINITY || this.nodename == i) {
							continue;
						} else {
							int[] arr = this.costs[this.nodename];
//							arr[i] = INFINITY;
							Packet p = makePacket(i, arr);
							NetworkSimulator.tolayer2(p);
						}
					}
				}
			}
		}
	}

	/*
	 * called when cost from the node to linkid changes from current value to
	 * newcost
	 */
	void linkhandler(int linkid, int newcost) {
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
