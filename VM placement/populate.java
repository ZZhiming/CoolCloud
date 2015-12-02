
import ilog.concert.*;
import ilog.cplex.*;
import java.util.*;
public class populate{
	private  int m;
	private  int n;
	private  double[][] lnm;
	private  double[][] gnm;
	private  double[] on;
	public populate(int m, int n){
		this.m=m;
		this.n=n;
		lnm = new double[n+1][m+1];
		gnm = new double[n+1][m+1];
		on = new double[n+1];
	}
	public populate(){}

	public void getResults(int[][] Lnm, int[][] Gnm, int[] On){
		for(int i=1;i<=n;i++){
		  On[i] = (int)on[i];
		  //System.out.println("Variable o" + i +                                       ": Value = " + on[i]);
		  for(int j=1;j<=m;j++){
		    //System.out.println("Variable l" + i + "," + j +                                       ": Value = " + lnm[i][j] );
                    //System.out.println("Variable g" + i + "," + j +                                      ": Value = " + gnm[i][j] );
		    Lnm[i][j] = (int)lnm[i][j];
		    Gnm[i][j] = (int)gnm[i][j];
		   }
		  }
	}
	public void ModelConstructor(int m, int n, int[][] La, double[] Ucpu, double[] Umem, double[] Uhd, double[] Ubw, double Hcpu, double Hmem, double Hhd, double Hbw, int[] Pmn, int Period, int[] MPmn, int[] MTmn, int Pa, int Ps){
		//double[][][] Decision = new double[3][n][m];
		// Create the modeler/solver object
         try{
		//populate DC = new populate(m,n);
		System.out.println("m="+m);
		System.out.println("n="+n);
		System.out.println("Hcpu="+Hcpu);
		System.out.println("Hmem="+Hmem);
		System.out.println("Hhd="+Hhd);
		System.out.println("Hbw="+Hbw);
		System.out.println("Period="+Period);
		System.out.println("Pa="+Pa);
		System.out.println("Ps="+Ps);
		
		for(int i=1;i<=m;i++){
			System.out.println("Ucpu"+i+"="+Ucpu[i]);
			System.out.println("Umem"+i+"="+Umem[i]);
			System.out.println("Uhd"+i+"="+Uhd[i]);
			System.out.println("Ubw"+i+"="+Ubw[i]);
			System.out.println("Pmn"+i+"="+Pmn[i]);
			System.out.println("MPmn"+i+"="+MPmn[i]);
			System.out.println("MTmn"+i+"="+MTmn[i]);
			for(int j=1;j<=n;j++)
				System.out.println("La_"+j+"_"+i+"="+La[j][i]);
				
			
		}
		IloCplex cplex = new IloCplex();
		IloNumVar[][] l = new IloNumVar[n+1][m+1];
		IloNumVar[][] g = new IloNumVar[n+1][m+1];
		IloNumVar[][] o = new IloNumVar[1][n+1];
// 		for(int i=0;i<n;i++){
// 			o[i]=cplex.intVar(0, 1, "o1");
// 		}
			



         populateByNonzero(cplex, l, g, o, m, n, La, Ucpu, Umem, Uhd, Ubw, Hcpu, Hmem, Hhd, Hbw, Pmn, Period, MPmn, MTmn, Pa, Ps);


         // write model to file
         cplex.exportModel("migration.lp");
	Date start = new Date();
         // solve the model and display the solution if one was found
         if ( cplex.solve() ) {
         Date end = new Date();

// 		double[][] dvL = new double[n][m]; //= cplex.getValues(l[0]);
// 		double[][] dvG = new double[n][m];
// 		double[][] dvO = new double[n][m];
		
// 		double[] C = new double[1];
// 		C[0]=cplex.getValue(c[0]);
		for(int i=1;i<=n;i++){
// 		  dvO[0][i] = cplex.getValue(o[0][i]);
// 		  Decision[2][0][i] = cplex.getValue(o[0][i]);
// 		  DC.on[i] = cplex.getValue(o[0][i]);
		  this.on[i] = cplex.getValue(o[0][i]);
			for(int j=1;j<=m;j++){
// 				dvL[i][j]=cplex.getValue(l[i][j]);
// 				dvG[i][j]=cplex.getValue(g[i][j]);
// 				Decision[0][i][j] = cplex.getValue(l[i][j]);
// 				Decision[1][i][j] = cplex.getValue(g[i][j]);
// 				DC.lnm[i][j] = cplex.getValue(l[i][j]);
// 				DC.gnm[i][j] = cplex.getValue(g[i][j]);
				this.lnm[i][j] = cplex.getValue(l[i][j]);
				this.gnm[i][j] = cplex.getValue(g[i][j]);
			}
		} 
            cplex.output().println("Solution status = " + cplex.getStatus());
            cplex.output().println("Solution value  = " + cplex.getObjValue());
	    System.out.println(" Problem tackled in " + (end.getTime()-start.getTime())/1000. + " seconds");
	    //gvm
	    System.out.println();

// 	for(int i=0;i<n;i++)
//         	for (int j = 0; j < m; ++j) {
//                		cplex.output().println("Variable l" + i + "," + j +
//                                       ": Value = " + dvL[i][j] );
//             }
// 	for(int i=0;i<n;i++)
//         	for (int j = 0; j < m; ++j) {
//                		cplex.output().println("Variable g" + i + "," + j +
//                                       ": Value = " + dvG[i][j] );
//             }
//         for(int i=0;i<n;i++)
//                		cplex.output().println("Variable o" + i + 
//                                       ": Value = " + dvO[0][i]);
	

         }
         else 
		System.out.println("No optimal solutions found!");
         cplex.end();

         }
         
         
	catch (IloException e) {
         System.err.println("Concert exception '" + e + "' caught");
      }
	}
	

   static void populateByNonzero(IloMPModeler model, IloNumVar[][] l, IloNumVar[][] g, IloNumVar[][] o, int m, int n, int[][] La, double[] Ucpu, double[] Umem, double[] Uhd, double[] Ubw, double Hcpu, double Hmem, double Hhd, double Hbw, int[] Pmn, int Period, int[] MPmn, int[] MTmn, int Pa, int Ps) throws IloException {
	//IloLPMatrix lp = model.addLPMatrix();
	for(int i=1;i<=n;i++){
		o[0][i]=model.intVar(0, 1, "o"+i);
		for(int j=1;j<=m;j++){
			l[i][j]=model.intVar(0, 1, "l"+i+"_"+j);
			g[i][j]=model.intVar(0, 1, "g"+i+"_"+j);
		}
	}
	IloLinearNumExpr VMPower = model.linearNumExpr();
	IloLinearNumExpr MPower = model.linearNumExpr();
	IloLinearNumExpr PMPower = model.linearNumExpr();
	IloNumVar[] c = new IloNumVar[1];
	c[0] = model.intVar(1,1,"c");
 	for(int i=1;i<=n;i++){
 		PMPower.addTerm(Pa-Ps,o[0][i]);
 		for(int j=1;j<=m;j++){
 			VMPower.addTerm(Period*Pmn[j], l[i][j]);
 			MPower.addTerm(MPmn[j]*MTmn[j], g[i][j]);
 			
 		}	
 	}
 	
	model.addMinimize(model.sum(VMPower,MPower,model.sum(model.prod(n*Ps*Period,c[0]),model.prod(Period,PMPower))));
	//IloLinearNumExpr constraint1 = model.linearNumExpr();
	IloRange c1[] = new IloRange[m+1];
	for(int i=1;i<=m;i++){
		IloLinearNumExpr constraint1 = model.linearNumExpr();
		for(int j=1;j<=n;j++)
			constraint1.addTerm(1, l[j][i]);
		c1[i] = model.addEq(constraint1,1,"c1");
	}
	IloRange c2[] = new IloRange[(m+1)*(n+1)];
	for(int i=1;i<=n;i++){
		for(int j=1;j<=m;j++){
			IloLinearNumExpr constraint2 = model.linearNumExpr();
			constraint2.addTerm(1-La[i][j], l[i][j]);
			c2[i*m+j] = model.addEq(model.diff(constraint2,g[i][j]),0,"c2");
		}
	}
	IloRange c3[] = new IloRange[n+1];
	for(int i=1;i<=n;i++){
		IloLinearNumExpr constraint3 = model.linearNumExpr();
		for(int j=1;j<=m;j++)
			constraint3.addTerm(Ucpu[j],l[i][j]);
		c3[i] = model.addLe(constraint3,Hcpu,"c3");
	}
	IloRange c4[] = new IloRange[n+1];
	for(int i=1;i<=n;i++){
		IloLinearNumExpr constraint4 = model.linearNumExpr();
		for(int j=1;j<=m;j++)
			constraint4.addTerm(Umem[j],l[i][j]);
		c4[i] = model.addLe(constraint4,Hmem,"c4");
	}
	IloRange c5[] = new IloRange[n+1];
	for(int i=1;i<=n;i++){
		IloLinearNumExpr constraint5 = model.linearNumExpr();
		for(int j=1;j<=m;j++)
			constraint5.addTerm(Uhd[j],l[i][j]);
		c5[i] = model.addLe(constraint5,Hhd,"c5");
	}
	IloRange c6[] = new IloRange[n+1];
	for(int i=1;i<=n;i++){
		IloLinearNumExpr constraint6 = model.linearNumExpr();
		for(int j=1;j<=m;j++)
			constraint6.addTerm(Ubw[j],l[i][j]);
		c6[i] = model.addLe(constraint6,Hbw,"c6");
	}
	IloRange c7[] = new IloRange[n+1];
	for(int i=1;i<=n;i++){
		IloLinearNumExpr constraint7 = model.linearNumExpr();
		for(int j=1;j<=m;j++)
			constraint7.addTerm(1, l[i][j]);
		c7[i] = model.addGe(model.diff(constraint7,o[0][i]),0,"c7");
	}
	IloRange c8[] = new IloRange[(m+1)*(n+1)];
	for(int i=1;i<=n;i++){
		for(int j=1;j<=m;j++){
			IloLinearNumExpr constraint8 = model.linearNumExpr();
			constraint8.addTerm(1, l[i][j]);
			c8[i*m+j] = model.addLe(model.diff(constraint8,o[0][i]),0,"c8");
		}
	}

   }
}