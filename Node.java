/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */
public class Node {

    public static final int INFINITY = 9999;

    int[] lkcost = new int[4];		/*The link cost between node 0 and other nodes*/
    int[][] costs = new int[4][4];  		/*Define distance table*/
    int nodename;               /*Name of this node*/

    /* Class constructor */
    public Node() { }

    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost)
	{
		this.nodename = nodename;
		this.lkcost = initial_lkcost;
		//Assign the costs table values of the current node to itself
		initArray();
		this.costs[this.nodename][0] = initial_lkcost[0];
		this.costs[this.nodename][1] = initial_lkcost[1];
		this.costs[this.nodename][2] = initial_lkcost[2];
		this.costs[this.nodename][3] = initial_lkcost[3];
        for(int j = 0; j < this.lkcost.length ; j++)
        {
            if (this.lkcost[j] != INFINITY)
            {
                if(j == this.nodename)
                {
                    continue;
                }
                else
                {
                    Packet p = makePacket(j, costs[this.nodename]);
                    NetworkSimulator.tolayer2(p);
                }
                    //Send the packet off to the others.
            }
        }


	}

    void rtupdate(Packet rcvdpkt)
	{

        if(rcvdpkt.destid != this.nodename)
		{
			return;
		}
		else
		{
			//Takes the values from the packet and assigns them to
			//the vector table of the current node.


			//If we have shorter minimum value than we send that information
			//back to the source destination so that we can update its information
			boolean shouldSendPkt = false;
			costs[rcvdpkt.sourceid][rcvdpkt.destid] = rcvdpkt.mincost[rcvdpkt.destid];
			costs[rcvdpkt.destid][rcvdpkt.sourceid] = rcvdpkt.mincost[rcvdpkt.destid];
			int additiveCost = costs[rcvdpkt.sourceid][rcvdpkt.destid];
			for(int i = 0; i < lkcost.length; i++)
			{
				int addToAll = additiveCost + rcvdpkt.mincost[i];
				if(addToAll < costs[rcvdpkt.destid][i])
				{
					costs[i][rcvdpkt.destid] = addToAll;
					costs[rcvdpkt.destid][i] = addToAll;
					shouldSendPkt = true;
				}
			}
			if(shouldSendPkt)
			{
				for(int j = 0; j < this.lkcost.length ; j++)
				{
                    if (this.lkcost[j] != INFINITY)
                    {
                        Packet p = makePacket(rcvdpkt.sourceid, costs[this.nodename]);
                        NetworkSimulator.tolayer2(p);
                        //Send the packet off to the others.
                    }
                }
			}


		}
	}


    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {  }


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {

	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }

    //Makes a packet from the current node to be sent
	//to some other node specified by destid
    Packet makePacket(int destid, int[] mincosts)
	{
		Packet p = new Packet(this.nodename, destid, mincosts);
		return p;
	}

	void initArray()
	{
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				this.costs[i][j] = INFINITY;
			}
		}
	}

}
