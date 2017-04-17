package au.edu.rmit.csit.experiment;

import au.edu.rmit.csit.dataset.Brightkite;
import au.edu.rmit.csit.reverse.precomputation;

public class Rgraphic {
	String color[] = {"red", "blue", "black","green"};
	public void formulate_graphic(int x[], int y[][], String out, String figureadd, String xname, String yname,String legend[],int option){
		String xString = "x<-c(";
		for(int i=0; i<x.length; i++){
			xString += Integer.toString(x[i]);
			if(i<x.length-1)
				xString += ",";
		}
		xString += ")\n";
	//	Brightkite.write(out, xString);
	//	xString ="";
		for(int mn=0; mn<y.length;mn++){
			xString += "m"+Integer.toString(mn+1)+" <- c(";
			for(int pq=0; pq<x.length; pq++){
				xString += y[pq][mn];
				if(pq<7)
					xString += ",";
			}
			xString +=")\n";
	//		Brightkite.write(out, xString);
		}
		xString += "png("+out+", width = 4, height = 4, units = 'in', res = 600)\n";
	//	Brightkite.write(out, xString);
		xString += "plot(x,m1,  type = \"o\", pch=1, lty = 1, col = \"red\", ylab = \""+yname+"\", xlab = \""+xname+"\",ylim=c(0,0.008))\n";
		for(int i = 0; i<y.length; i++){
			xString += "plot(x,m1,  type = \"o\", pch=1, lty = 1, col = \"red\", ylab = \""+yname+"\", xlab = \""+xname+"\",ylim=c(0,0.008))\n";

		}
	//	xString += “legend("topleft", legend=c("IS", "CG", "CGP", "CGMP" ), col=c("red", "blue","black","green"), lty=c(1,2,3,4), pch=c(1,2,3,4), cex=0.6)";

	}
	/*
	 * # the chart for the change of k.
x <- c(5,10,15,20,25)
m1 <- c(0.004486772058823529,0.004440867823529412,0.004769809519607844,0.00633235962745098,0.00470564431372549)
m2 <- c(0.001556749254901961,0.0015823091176470588,0.00194037281372549,0.0020052188137254902,0.002089750137254902)
m3 <- c(0.0013861887352941177,0.001368874156862745,0.0016270425392156864,0.0017455440588235293,0.0014396258823529413)
m4 <- c(0.0011568739411764706,0.0012208265294117648,0.0021663065490196078,0.0013321731568627449,0.0012200026078431371)
# Give the chart file a name.

png("/home/wangsheng/Desktop/RkNNT-shane/graph/experiment/k-LA-LCSS-0-001.png", width = 4, height = 4, units = 'in', res = 600)
plot(x,m1,  type = "o", pch=1, lty = 1, col = "red", ylab = "Running time(s)", xlab = "#Points of Query",ylim=c(0,0.008))
lines(x,m2, type = "o", pch=2, lty = 2, col = "blue")
lines(x,m3, type = "o", pch=3, lty = 3, col = "black")
lines(x,m4, type = "o", pch=4, lty = 4, col = "green")
legend("topleft", legend=c("IS", "CG", "CGP", "CGMP" ), col=c("red", "blue","black","green"), lty=c(1,2,3,4), pch=c(1,2,3,4), cex=0.6)
dev.off()
	 */
}
