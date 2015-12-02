package cloud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Heuristic {
	//ArrayList<Integer> gVal = new ArrayList<Integer>();
	//ArrayList<Integer> kVal = new ArrayList<Integer>();
	//ArrayList<Integer> cgVal = new ArrayList<Integer>();
	//ArrayList<Integer> oVal = new ArrayList<Integer>();
	double[] list = new double [3];
	ArrayList<Double> totalCPU = new ArrayList<Double>();
	ArrayList<Double> totalMEM = new ArrayList<Double>();
	ArrayList<Double> totalNET = new ArrayList<Double>();
	ArrayList<Double> Pactive = new ArrayList<Double>();
	ArrayList<Double> Psleep = new ArrayList<Double>();
	ArrayList<Double> Hcpu = new ArrayList<Double>();
	ArrayList<Double> Hmem = new ArrayList<Double>();
	ArrayList<Double> Hnet = new ArrayList<Double>();
	ArrayList<Double> Hdisk = new ArrayList<Double>();
	ArrayList<Double> Pm = new ArrayList<Double>();
	ArrayList<Double> Pmigrate = new ArrayList<Double>();
	ArrayList<Double> T = new ArrayList<Double>();
	ArrayList<Integer> lmn = new ArrayList<Integer>();
	ArrayList<Double> Ucpu = new ArrayList<Double>();
	ArrayList<Double> Umem = new ArrayList<Double>();
	ArrayList<Double> Unet = new ArrayList<Double>();
	ArrayList<Double> Udisk = new ArrayList<Double>();
	ArrayList<Integer> kmn = new ArrayList<Integer>();
	ArrayList<Integer> om = new ArrayList<Integer>();
	ArrayList<Integer> gmn = new ArrayList<Integer>();
	ArrayList<Double> migEnergy = new ArrayList<Double>();
	ArrayList<Double> tmpMigEnergy = new ArrayList<Double>();
	ArrayList<Double> VMEnergy = new ArrayList<Double>();
	ArrayList<Double> tmpVMEnergy = new ArrayList<Double>();
	ArrayList<Double> PMEnergy = new ArrayList<Double>();
	ArrayList<Double> tmpPMEnergy = new ArrayList<Double>();
	double totEnergy, tmpTotEnergy;
	Heuristic(double[] list, ArrayList<Double> Pactive, ArrayList<Double> Psleep, ArrayList<Double> Hcpu, ArrayList<Double> Hmem, ArrayList<Double> Hnet, ArrayList<Double> Hdisk, ArrayList<Double> Pm, ArrayList<Double> Pmigrate, ArrayList<Double> T, ArrayList<Integer> lmn, ArrayList<Double> Ucpu, ArrayList<Double> Umem, ArrayList<Double> Unet, ArrayList<Double> Udisk){
		this.Hcpu = Hcpu;
		this.Hdisk = Hdisk;
		this.Hmem = Hmem;
		this.Hnet = Hnet;
		this.list = list;
		this.lmn = lmn;
		this.Pactive = Pactive;
		this.Pm = Pm;
		this.Pmigrate = Pmigrate;
		this.Psleep = Psleep;
		this.T = T;
		this.Ucpu = Ucpu;
		this.Udisk = Udisk;
		this.Umem = Umem;
		this.Unet = Unet;
		//this.kmn = lmn;
		for (int i=0; i<(int)list[0]; i++)
			for (int j=0; j<(int)list[1]; j++)
				kmn.add(lmn.get(i*(int)list[1]+j));
		for (int i=0; i<(int)list[0]; i++){
			double tempCPU = 0.0, tempMEM = 0.0, tempNET = 0.0;
			for (int j=0; j<(int)list[1]; j++)
				if (kmn.get(i*(int)list[1]+j) == 1){
					tempCPU += Ucpu.get(i*(int)list[1]+j);
					tempMEM += Umem.get(i*(int)list[1]+j);
					tempNET += Unet.get(i*(int)list[1]+j);
					//disk[i] += Udisk.get(i*(int)list[1]+j);
				}
			totalCPU.add(i, tempCPU);
			totalMEM.add(i, tempMEM);
			totalNET.add(i, tempNET);
		}
		System.out.println(totalCPU);
		System.out.println(totalMEM);
		System.out.println(totalNET);
	}
	int[] getUtiSumRanking(){
		double[] utiSumToSort = new double [(int)list[0]];
		double[] utiSummation = new double [(int)list[0]];
		int[] utiRanking = new int [(int)list[0]];
		for (int i=0; i<(int)list[0]; i++)
			if (om.get(i)==1){
				utiSummation[i] = totalCPU.get(i) + totalMEM.get(i) + totalNET.get(i);
				utiSumToSort[i] = totalCPU.get(i) + totalMEM.get(i) + totalNET.get(i);
			}
		Arrays.sort(utiSumToSort);
		for (int i=0; i<(int)list[0]; i++)
			for (int j=0; j<(int)list[0]; j++)
				if (utiSummation[j] == utiSumToSort[i])
					utiRanking[i] = j;
		return utiRanking;
	}
	boolean processOfUniting(int[] utiRanking, int fromIndex, int toIndex){
		
	}
	void consolidation(){
		int[] utiRanking = new int [(int)list[0]];
		utiRanking = getUtiSumRanking();
		boolean nextConsolidation = true;
		while (nextConsolidation == true){
			nextConsolidation = false;
			for (int i=0; i<(int)list[0]-1; i++){
				if (om.get(utiRanking[i]) == 1){
					for (int j=i+1; j<(int)list[0]; j++){
						nextConsolidation = processOfUniting(utiRanking, i, j);						//call the function to do consolidation, which return a value indicating whether consolidation can be performed
						if (nextConsolidation == true){						//if succeed, perform consolidation and set nextConsolidation = 1 and break the loop
							boolean consolidatedSuccessfully;
							consolidatedSuccessfully = processOfUniting(utiRanking, i, j);
							if(consolidatedSuccessfully != true){
								System.out.println("Consolidation Procedure didn't Perform as Expected! Program Stops!");
								System.exit(0);
							}
							break;
						}
						//else set nextConsolidation = 0
					}
				}
				if (nextConsolidation == true){//if nextConsolidation = 1, resort the array and break the loop
					utiRanking = getUtiSumRanking();
					break;
				}
			}
		}
	}
	double[] uCPU(){
		double[] cpu = new double [(int)list[0]];
		//double[] mem = new double [(int)list[0]];
		//double[] net = new double [(int)list[0]];
		//double[] disk= new double [(int)list[0]];
		//System.out.print("The Utilization of CPU on PMs are ");
		for (int i=0; i<(int)list[0]; i++){
			cpu[i] = totalCPU.get(i);
			//cpu = 0; mem = 0; net = 0; disk = 0;
/*			for (int j=0; j<(int)list[1]; j++)
				if (kmn.get(i*(int)list[1]+j) == 1){
					cpu[i] += Ucpu.get(i*(int)list[1]+j);
					//mem[i] += Umem.get(i*(int)list[1]+j);
					//net[i] += Unet.get(i*(int)list[1]+j);
					//disk[i] += Udisk.get(i*(int)list[1]+j);
				}*/
			//System.out.print(cpu[i]+", ");
		}
		//System.out.println("respectively.");
		return cpu;
	}
	double[] uMEM(){
		double[] mem = new double [(int)list[0]];
		//double[] mem = new double [(int)list[0]];
		//double[] net = new double [(int)list[0]];
		//double[] disk= new double [(int)list[0]];
		//System.out.print("The Utilization of MEM on PMs are ");
		for (int i=0; i<(int)list[0]; i++){
			mem[i] = totalMEM.get(i);
			//cpu = 0; mem = 0; net = 0; disk = 0;
/*			for (int j=0; j<(int)list[1]; j++)
				if (kmn.get(i*(int)list[1]+j) == 1){
					//cpu[i] += Ucpu.get(i*(int)list[1]+j);
					mem[i] += Umem.get(i*(int)list[1]+j);
					//net[i] += Unet.get(i*(int)list[1]+j);
					//disk[i] += Udisk.get(i*(int)list[1]+j);
				}*/
			//System.out.print(mem[i]+", ");
		}
		//System.out.println("respectively.");
		return mem;
	}
	double[] uNET(){
		//double[] cpu = new double [(int)list[0]];
		//double[] mem = new double [(int)list[0]];
		double[] net = new double [(int)list[0]];
		//double[] disk= new double [(int)list[0]];
		//System.out.print("The Utilization of NET on PMs are ");
		for (int i=0; i<(int)list[0]; i++){
			net[i] = totalNET.get(i);
			//cpu = 0; mem = 0; net = 0; disk = 0;
/*			for (int j=0; j<(int)list[1]; j++)
				if (kmn.get(i*(int)list[1]+j) == 1){
					net[i] += Unet.get(i*(int)list[1]+j);
					//mem[i] += Umem.get(i*(int)list[1]+j);
					//net[i] += Unet.get(i*(int)list[1]+j);
					//disk[i] += Udisk.get(i*(int)list[1]+j);
				}*/
			//System.out.print(net[i]+", ");
		}
		//System.out.println("respectively.");
		return net;
	}
	void replicateArrayList(List<Double> des, List<Double> src){
		if (des.isEmpty() == false || src.isEmpty() == true){
			System.out.println("Destination is not Empty or Source is Empty. Program Stops");
			System.exit(0);
		}
		else
			for (int i=0; i<src.size(); i++)
				des.add(src.get(i));
	}
	int recursiveCal(String utiType, double tempSCPU, double tempSMEM, double tempSNET, double tempDCPU, double tempDMEM, double tempDNET, int SPM, int DPM, List<Double> candidateVMs, int moveVM, int index, List<Integer> finalVMs){
		int reachBottom = 0;
		if (utiType == "CPU"){
			//System.out.println("The source and destination PMs are "+SPM+" and "+DPM+", respectively.");
			for (int i=index; i>=0; i--){
				int swapedVMs = -1;
				for (int j=0; j<(int)list[1]; j++)
					if (kmn.get(DPM*(int)list[1]+j) == 1 && Ucpu.get(DPM*(int)list[1]+j) == candidateVMs.get(i)){
						swapedVMs = j;
						break;
				}
				if(swapedVMs < 0){
					System.out.println("1. Initial Value Error. Program Stops.");
					System.exit(0);
				}
				finalVMs.add(swapedVMs);
				System.out.println(swapedVMs+" is added.");
				if (tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs) > tempSCPU && i > 0){
					System.out.println(finalVMs.get(finalVMs.size()-1)+" is gonna be removed.");
					finalVMs.remove(finalVMs.size()-1);
					//System.out.println(finalVMs);
				}
				else if (tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs) <= Hcpu.get(SPM) && tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs) <= Hmem.get(SPM) && tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs) <= Hnet.get(SPM) && tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs) <= Hcpu.get(DPM) && tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs) <= Hmem.get(DPM) && tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs) <= Hnet.get(DPM)){
					System.out.println("The VMs to be swaped:"+finalVMs);
					break;
					
				}
				else if (i == 0){
					System.out.println(finalVMs.get(finalVMs.size()-1)+" is removed.");
					finalVMs.remove(finalVMs.size()-1);
					reachBottom = 1;
					
				}
				else{
					List<Double> newCandidateVMs = new ArrayList<Double>();
/*					newCandidateVMs.ensureCapacity(candidateVMs.size());
					Collections.copy(newCandidateVMs, candidateVMs);
					newCandidateVMs.remove(i);
					for (int m=0; m<)*/
					replicateArrayList(newCandidateVMs, candidateVMs);
					newCandidateVMs.remove(i);
					System.out.println(finalVMs);
					System.out.println((tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs))+", "+(tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs))+", "+(tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs))+", "+(tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs))+", "+(tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs))+", "+(tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs)));
					reachBottom = recursiveCal("CPU", tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs), tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs), tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs), tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs), tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs), tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs), SPM, DPM, newCandidateVMs, moveVM, newCandidateVMs.size()-1, finalVMs);
					if (reachBottom == 1){
						finalVMs.remove(finalVMs.size()-1);
						//System.out.println("Reaching Up...");
					}
				}
			}
		}
		else if (utiType == "MEM"){
			//reachBottom = 0;
			for (int i=index; i>=0; i--){
				int swapedVMs = -1;
				for (int j=0; j<(int)list[1]; j++)
					if (kmn.get(DPM*(int)list[1]+j) == 1 && Umem.get(DPM*(int)list[1]+j) == candidateVMs.get(i)){
						swapedVMs = j;
						break;
				}
				if(swapedVMs < 0){
					System.out.println("1. Initial Value Error. Program Stops.");
					System.exit(0);
				}
				finalVMs.add(swapedVMs);			
				if (tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs) > tempSMEM && i > 0)
					finalVMs.remove(finalVMs.size()-1);
				else if (tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs) <= Hcpu.get(SPM) && tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs) <= Hmem.get(SPM) && tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs) <= Hnet.get(SPM) && tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs) <= Hcpu.get(DPM) && tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs) <= Hmem.get(DPM) && tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs) <= Hnet.get(DPM))
					break;
				else if (i == 0){
					finalVMs.remove(finalVMs.size()-1);
					reachBottom = 1;
				}
				else{
					List<Double> newCandidateVMs = new ArrayList<Double>();
					replicateArrayList(newCandidateVMs, candidateVMs);
					newCandidateVMs.remove(i);
					reachBottom = recursiveCal("MEM", tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs), tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs), tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs), tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs), tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs), tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs), SPM, DPM, newCandidateVMs, moveVM, newCandidateVMs.size()-1, finalVMs);
					if (reachBottom == 1)
						finalVMs.remove(finalVMs.size()-1);
				}
			}
		}
		
		else if (utiType == "NET"){
			//reachBottom = 0;
			for (int i=index; i>=0; i--){
				int swapedVMs = -1;
				for (int j=0; j<(int)list[1]; j++)
					if (kmn.get(DPM*(int)list[1]+j) == 1 && Unet.get(DPM*(int)list[1]+j) == candidateVMs.get(i)){
						swapedVMs = j;
						break;
				}
				if(swapedVMs < 0){
					System.out.println("1. Initial Value Error. Program Stops.");
					System.exit(0);
				}
				finalVMs.add(swapedVMs);			
				if (tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs) > tempSNET && i > 0)
					finalVMs.remove(finalVMs.size()-1);
				else if (tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs) <= Hcpu.get(SPM) && tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs) <= Hmem.get(SPM) && tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs) <= Hnet.get(SPM) && tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs) <= Hcpu.get(DPM) && tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs) <= Hmem.get(DPM) && tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs) <= Hnet.get(DPM))
					break;
				else if (i == 0){
					finalVMs.remove(finalVMs.size()-1);
					reachBottom = 1;
				}
				else{
					List<Double> newCandidateVMs = new ArrayList<Double>();
					replicateArrayList(newCandidateVMs, candidateVMs);
					newCandidateVMs.remove(i);
					//int reachBottom;
					reachBottom = recursiveCal("NET", tempSCPU - Ucpu.get(SPM*(int)list[1]+moveVM) + Ucpu.get(SPM*(int)list[1]+swapedVMs), tempSMEM - Umem.get(SPM*(int)list[1]+moveVM) + Umem.get(SPM*(int)list[1]+swapedVMs), tempSNET - Unet.get(SPM*(int)list[1]+moveVM) + Unet.get(SPM*(int)list[1]+swapedVMs), tempDCPU + Ucpu.get(DPM*(int)list[1]+moveVM) - Ucpu.get(DPM*(int)list[1]+swapedVMs), tempDMEM + Umem.get(DPM*(int)list[1]+moveVM) - Umem.get(DPM*(int)list[1]+swapedVMs), tempDNET + Unet.get(DPM*(int)list[1]+moveVM) - Unet.get(DPM*(int)list[1]+swapedVMs), SPM, DPM, newCandidateVMs, moveVM, newCandidateVMs.size()-1, finalVMs);
					if (reachBottom == 1)
						finalVMs.remove(finalVMs.size()-1);
				}
			}
		}
		else{
			System.out.print("1. Unexpected Utilization Type. Porgram Stops.");
			System.exit(0);
		}
		return reachBottom;
	}
	int swapVMs(String utiType, int pMNum){
		int ConstrainViolated = 1;
		if (utiType == "CPU"){
			//int migrationInd;
			List<Double> sortList = new ArrayList<Double>();
			for(int i=0; i<(int)list[1]; i++)
				if(kmn.get(pMNum*(int)list[1]+i) == 1)
					sortList.add(Ucpu.get(pMNum*(int)list[1]+i));
			Collections.sort(sortList);
			int moveVM = -1;
			//double[] sortUArray = new double [(int)list[0]*3];
			//for (int i=0; i<(int)list[0]; i++)
			//sortCPU = uCPU();
			//Arrays.sort(sortCPU);
			double[] cpu = new double [(int)list[0]];
			double[] mem = new double [(int)list[0]];
			double[] net = new double [(int)list[0]];
			//double[] disk= new double [(int)list[0]];
			//int[] kmn = new int [(int)list[0]];
			//kmn = lmn;
			cpu = uCPU();
			mem = uMEM();
			net = uNET();
			//disk = uDISK();
			double[] uArray = new double [(int)list[0]*3];
			System.arraycopy(cpu, 0, uArray, 0, cpu.length);
			System.arraycopy(mem, 0, uArray, cpu.length, mem.length);
			System.arraycopy(net, 0, uArray, cpu.length+mem.length, net.length);
			//for(double [] temp: uArray);
				//System.out.println(Arrays.toString(uArray));
			Arrays.sort(uArray);
			//Arrays.sort(cpu);
			//Arrays.sort(mem);
			//Arrays.sort(net);
			for(int h=sortList.size()-1; h>=0; h--){
				for(int k=0; k<(int)list[1]; k++)
					if(kmn.get(pMNum*(int)list[1]+k) == 1 && sortList.get(h) == Ucpu.get(pMNum*(int)list[1]+k))
						moveVM = k;
				if(moveVM < 0){
					System.out.println("Initial Value Error. Program Stops.");
					System.exit(0);
				}
				//int moveToPM;
				for(int i=0; i<uArray.length; i++){
					//moveToPM = totalCPU.indexOf(uArray[i]);
					if(totalCPU.indexOf(uArray[i]) >= 0 && totalCPU.indexOf(uArray[i]) != pMNum){
						List<Double> candidateVMs = new ArrayList<Double>();
						for(int j=0; j<(int)list[1]; j++)
							if(kmn.get(totalCPU.indexOf(uArray[i])*(int)list[1]+j) == 1)
								candidateVMs.add(Ucpu.get(totalCPU.indexOf(uArray[i])*(int)list[1]+j));
						Collections.sort(candidateVMs);
						double tempSCPU = totalCPU.get(pMNum), tempSMEM = totalMEM.get(pMNum), tempSNET = totalNET.get(pMNum), tempDCPU = totalCPU.get(totalCPU.indexOf(uArray[i])), tempDMEM = totalMEM.get(totalCPU.indexOf(uArray[i])), tempDNET = totalNET.get(totalCPU.indexOf(uArray[i]));
						List<Integer> finalVMs = new ArrayList<Integer>();
						System.out.println("Swapping VM "+moveVM+" on PM "+pMNum+".");
						System.out.println("The target PM is "+totalCPU.indexOf(uArray[i])+".");
						System.out.println("Candidate VMs are "+candidateVMs+".");
						recursiveCal("CPU", tempSCPU, tempSMEM, tempSNET, tempDCPU, tempDMEM, tempDNET, pMNum, totalCPU.indexOf(uArray[i]), candidateVMs, moveVM, candidateVMs.size()-1, finalVMs);
						if (finalVMs.size() > 0){
							int targetPM = totalCPU.indexOf(uArray[i]);
							kmn.set(pMNum*(int)list[1]+moveVM, 0);
							kmn.set(totalCPU.indexOf(uArray[i])*(int)list[1]+moveVM, 1);
							totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
							totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
							totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
							totalCPU.set(targetPM, totalCPU.get(targetPM)+Ucpu.get(targetPM*(int)list[1]+moveVM));
							//System.out.println(totalMEM.get(totalCPU.indexOf(uArray[i]))+", "+Umem.get(totalCPU.indexOf(uArray[i])*(int)list[1]+moveVM));
							totalMEM.set(targetPM, totalMEM.get(targetPM)+Umem.get(targetPM*(int)list[1]+moveVM));
							totalNET.set(targetPM, totalNET.get(targetPM)+Unet.get(targetPM*(int)list[1]+moveVM));
							//System.out.println("VM "+moveVM+" is moved to PM "+totalCPU.indexOf(uArray[i])+" from "+pMNum+" to reduce CPU utilization.");
							for (int l=0; l<finalVMs.size(); l++){
								kmn.set(targetPM*(int)list[1]+finalVMs.get(l), 0);
								kmn.set(pMNum*(int)list[1]+finalVMs.get(l), 1);
								totalCPU.set(targetPM, totalCPU.get(targetPM)-Ucpu.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalMEM.set(targetPM, totalMEM.get(targetPM)-Umem.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalNET.set(targetPM, totalNET.get(targetPM)-Unet.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalCPU.set(pMNum, totalCPU.get(pMNum)+Ucpu.get(pMNum*(int)list[1]+finalVMs.get(l)));
								totalMEM.set(pMNum, totalMEM.get(pMNum)+Umem.get(pMNum*(int)list[1]+finalVMs.get(l)));
								totalNET.set(pMNum, totalNET.get(pMNum)+Unet.get(pMNum*(int)list[1]+finalVMs.get(l)));
							}
							break;
						}
/*					if(moveToPM != pMNum && totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM) <= Hcpu.get(moveToPM) && totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM) <= Hmem.get(moveToPM) && totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM) <= Hnet.get(moveToPM)){
									
						kmn.set(pMNum*(int)list[1]+moveVM, 0);
						kmn.set(moveToPM*(int)list[1]+moveVM, 1);
						totalCPU.set(moveToPM, totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM));
						totalMEM.set(moveToPM, totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM));
						totalNET.set(moveToPM, totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM));
						totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
						totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
						totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
						System.out.println("VM "+moveVM+" is moved to PM "+moveToPM+" from "+pMNum+" to reduce CPU utilization.");
						System.out.println(totalCPU);
						sortCPU = uCPU();
						Arrays.sort(sortCPU);
						break;
						}*/
					}
/*					else if(totalMEM.indexOf(uArray[i]) >= 0 && totalMEM.indexOf(uArray[i]) != pMNum){
						
					}
					else if(totalNET.indexOf(uArray[i]) >= 0 && totalNET.indexOf(uArray[i]) != pMNum)*/
						
					
				}
				if(totalCPU.get(pMNum) <= Hcpu.get(pMNum)){
					ConstrainViolated = 0;
					break;
				}
			}
			//if(totalCPU.get(pMNum) > Hcpu.get(pMNum)){
				
				//vioIndicator = 1;
			//}
		}
		else if (utiType == "MEM"){
			List<Double> sortList = new ArrayList<Double>();
			for(int i=0; i<(int)list[1]; i++)
				if(kmn.get(pMNum*(int)list[1]+i) == 1)
					sortList.add(Umem.get(pMNum*(int)list[1]+i));
			Collections.sort(sortList);
			int moveVM = -1;
			double[] cpu = new double [(int)list[0]];
			double[] mem = new double [(int)list[0]];
			double[] net = new double [(int)list[0]];
			cpu = uCPU();
			mem = uMEM();
			net = uNET();
			double[] uArray = new double [(int)list[0]*3];
			System.arraycopy(cpu, 0, uArray, 0, cpu.length);
			System.arraycopy(mem, 0, uArray, cpu.length, mem.length);
			System.arraycopy(net, 0, uArray, cpu.length+mem.length, net.length);
			Arrays.sort(uArray);
			for(int h=sortList.size()-1; h>=0; h--){
				for(int k=0; k<(int)list[1]; k++)
					if(kmn.get(pMNum*(int)list[1]+k) == 1 && sortList.get(h) == Umem.get(pMNum*(int)list[1]+k))
						moveVM = k;
				if(moveVM < 0){
					System.out.println("Initial Value Error. Program Stops.");
					System.exit(0);
				}
				for(int i=0; i<uArray.length; i++){
					if(totalMEM.indexOf(uArray[i]) >= 0 && totalMEM.indexOf(uArray[i]) != pMNum){
						List<Double> candidateVMs = new ArrayList<Double>();
						for(int j=0; j<(int)list[1]; j++)
							if(kmn.get(totalMEM.indexOf(uArray[i])*(int)list[1]+j) == 1)
								candidateVMs.add(Umem.get(totalMEM.indexOf(uArray[i])*(int)list[1]+j));
						Collections.sort(candidateVMs);
						double tempSCPU = totalCPU.get(pMNum), tempSMEM = totalMEM.get(pMNum), tempSNET = totalNET.get(pMNum), tempDCPU = totalCPU.get(totalMEM.indexOf(uArray[i])), tempDMEM = totalMEM.get(totalMEM.indexOf(uArray[i])), tempDNET = totalNET.get(totalMEM.indexOf(uArray[i]));
						List<Integer> finalVMs = new ArrayList<Integer>();
						recursiveCal("MEM", tempSCPU, tempSMEM, tempSNET, tempDCPU, tempDMEM, tempDNET, pMNum, totalMEM.indexOf(uArray[i]), candidateVMs, moveVM, candidateVMs.size()-1, finalVMs);
						if (finalVMs.size() > 0){
							int targetPM = totalMEM.indexOf(uArray[i]);
							kmn.set(pMNum*(int)list[1]+moveVM, 0);
							kmn.set(totalMEM.indexOf(uArray[i])*(int)list[1]+moveVM, 1);
							totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
							totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
							totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
							totalCPU.set(targetPM, totalCPU.get(targetPM)+Ucpu.get(targetPM*(int)list[1]+moveVM));
							totalMEM.set(targetPM, totalMEM.get(targetPM)+Umem.get(targetPM*(int)list[1]+moveVM));
							totalNET.set(targetPM, totalNET.get(targetPM)+Unet.get(targetPM*(int)list[1]+moveVM));
							for (int l=0; l<finalVMs.size(); l++){
								kmn.set(targetPM*(int)list[1]+finalVMs.get(l), 0);
								kmn.set(pMNum*(int)list[1]+finalVMs.get(l), 1);
								totalCPU.set(targetPM, totalCPU.get(targetPM)-Ucpu.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalMEM.set(targetPM, totalMEM.get(targetPM)-Umem.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalNET.set(targetPM, totalNET.get(targetPM)-Unet.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalCPU.set(pMNum, totalCPU.get(pMNum)+Ucpu.get(pMNum*(int)list[1]+finalVMs.get(l)));
								totalMEM.set(pMNum, totalMEM.get(pMNum)+Umem.get(pMNum*(int)list[1]+finalVMs.get(l)));
								totalNET.set(pMNum, totalNET.get(pMNum)+Unet.get(pMNum*(int)list[1]+finalVMs.get(l)));
							}
							break;
						}
					}
				}
				if(totalMEM.get(pMNum) <= Hmem.get(pMNum)){
					ConstrainViolated = 0;
					break;
				}
			}
		}
		else if (utiType == "NET"){
			List<Double> sortList = new ArrayList<Double>();
			for(int i=0; i<(int)list[1]; i++)
				if(kmn.get(pMNum*(int)list[1]+i) == 1)
					sortList.add(Unet.get(pMNum*(int)list[1]+i));
			Collections.sort(sortList);
			int moveVM = -1;
			double[] cpu = new double [(int)list[0]];
			double[] mem = new double [(int)list[0]];
			double[] net = new double [(int)list[0]];
			cpu = uCPU();
			mem = uMEM();
			net = uNET();
			double[] uArray = new double [(int)list[0]*3];
			System.arraycopy(cpu, 0, uArray, 0, cpu.length);
			System.arraycopy(mem, 0, uArray, cpu.length, mem.length);
			System.arraycopy(net, 0, uArray, cpu.length+mem.length, net.length);
			Arrays.sort(uArray);
			for(int h=sortList.size()-1; h>=0; h--){
				for(int k=0; k<(int)list[1]; k++)
					if(kmn.get(pMNum*(int)list[1]+k) == 1 && sortList.get(h) == Unet.get(pMNum*(int)list[1]+k))
						moveVM = k;
				if(moveVM < 0){
					System.out.println("Initial Value Error. Program Stops.");
					System.exit(0);
				}
				for(int i=0; i<uArray.length; i++){
					if(totalNET.indexOf(uArray[i]) >= 0 && totalNET.indexOf(uArray[i]) != pMNum){
						List<Double> candidateVMs = new ArrayList<Double>();
						for(int j=0; j<(int)list[1]; j++)
							if(kmn.get(totalNET.indexOf(uArray[i])*(int)list[1]+j) == 1)
								candidateVMs.add(Unet.get(totalNET.indexOf(uArray[i])*(int)list[1]+j));
						Collections.sort(candidateVMs);
						double tempSCPU = totalCPU.get(pMNum), tempSMEM = totalMEM.get(pMNum), tempSNET = totalNET.get(pMNum), tempDCPU = totalCPU.get(totalNET.indexOf(uArray[i])), tempDMEM = totalMEM.get(totalNET.indexOf(uArray[i])), tempDNET = totalNET.get(totalNET.indexOf(uArray[i]));
						List<Integer> finalVMs = new ArrayList<Integer>();
						recursiveCal("NET", tempSCPU, tempSMEM, tempSNET, tempDCPU, tempDMEM, tempDNET, pMNum, totalNET.indexOf(uArray[i]), candidateVMs, moveVM, candidateVMs.size()-1, finalVMs);
						if (finalVMs.size() > 0){
							int targetPM = totalNET.indexOf(uArray[i]);
							kmn.set(pMNum*(int)list[1]+moveVM, 0);
							kmn.set(targetPM*(int)list[1]+moveVM, 1);
							totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
							totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
							totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
							totalCPU.set(targetPM, totalCPU.get(targetPM)+Ucpu.get(targetPM*(int)list[1]+moveVM));
							totalMEM.set(targetPM, totalMEM.get(targetPM)+Umem.get(targetPM*(int)list[1]+moveVM));
							totalNET.set(targetPM, totalNET.get(targetPM)+Unet.get(targetPM*(int)list[1]+moveVM));
							for (int l=0; l<finalVMs.size(); l++){
								kmn.set(totalNET.indexOf(uArray[i])*(int)list[1]+finalVMs.get(l), 0);
								kmn.set(pMNum*(int)list[1]+finalVMs.get(l), 1);
								totalCPU.set(targetPM, totalCPU.get(targetPM)-Ucpu.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalMEM.set(targetPM, totalMEM.get(targetPM)-Umem.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalNET.set(targetPM, totalNET.get(targetPM)-Unet.get(targetPM*(int)list[1]+finalVMs.get(l)));
								totalCPU.set(pMNum, totalCPU.get(pMNum)+Ucpu.get(pMNum*(int)list[1]+finalVMs.get(l)));
								totalMEM.set(pMNum, totalMEM.get(pMNum)+Umem.get(pMNum*(int)list[1]+finalVMs.get(l)));
								totalNET.set(pMNum, totalNET.get(pMNum)+Unet.get(pMNum*(int)list[1]+finalVMs.get(l)));
							}
							break;
						}
					}
				}
				if(totalNET.get(pMNum) <= Hnet.get(pMNum)){
					ConstrainViolated = 0;
					break;
				}
			}
		}
			
		else{
			System.out.print("Unexpected Utilization Type. Porgram Stops.");
			System.exit(0);
		}
		return ConstrainViolated;
	}
	int utiRedistributor(String utiType, int pMNum) {
		int vioIndicator = 0;
		if (utiType == "CPU"){
			//int migrationInd;
			List<Double> sortList = new ArrayList<Double>();
			for(int i=0; i<(int)list[1]; i++)
				if(kmn.get(pMNum*(int)list[1]+i) == 1)
					sortList.add(Ucpu.get(pMNum*(int)list[1]+i));
			Collections.sort(sortList);
			int moveVM = -1;
			double[] sortCPU = new double [(int)list[0]];
			//System.arraycopy(cpu, 0, sortCPU, 0, cpu.length);
			sortCPU = uCPU();
			Arrays.sort(sortCPU);
			//do{
				//migrationInd = 0;
			for(int h=sortList.size()-1; h>=0; h--){
				for(int k=0; k<(int)list[1]; k++)
					if(kmn.get(pMNum*(int)list[1]+k) == 1 && sortList.get(h) == Ucpu.get(pMNum*(int)list[1]+k))
						moveVM = k;
				if(moveVM < 0){
					System.out.println("Initial Value Error. Program Stops.");
					System.exit(0);
				}
				int moveToPM;
				for(int i=0; i<(int)list[0]; i++){
					moveToPM = totalCPU.indexOf(sortCPU[i]);
					//System.out.println();
					if(moveToPM != pMNum && totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM) <= Hcpu.get(moveToPM) && totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM) <= Hmem.get(moveToPM) && totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM) <= Hnet.get(moveToPM)){
						kmn.set(pMNum*(int)list[1]+moveVM, 0);
						kmn.set(moveToPM*(int)list[1]+moveVM, 1);
						totalCPU.set(moveToPM, totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM));
						totalMEM.set(moveToPM, totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM));
						totalNET.set(moveToPM, totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM));
						totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
						totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
						totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
						System.out.println("VM "+moveVM+" is moved to PM "+moveToPM+" from "+pMNum+" to reduce CPU utilization.");
						System.out.println(totalCPU);
						sortCPU = uCPU();
						Arrays.sort(sortCPU);
						break;
					}
				}
				if(totalCPU.get(pMNum) <= Hcpu.get(pMNum))
					break;
			}
			if(totalCPU.get(pMNum) > Hcpu.get(pMNum)){
				for(int i=0; i<(int)list[0]; i++){
					System.out.println("PM "+i);
					for(int j=0; j<(int)list[1]; j++)
						if(kmn.get(i*(int)list[1]+j) == 1)
							System.out.println(Ucpu.get(i*(int)list[1]+j)+", "+Umem.get(i*(int)list[1]+j)+", "+Unet.get(i*(int)list[1]+j));
				}
				vioIndicator = swapVMs("CPU", pMNum);
				//vioIndicator = 1;
			}
		}
		else if (utiType == "MEM"){
			//int migrationInd;
			List<Double> sortList = new ArrayList<Double>();
			for(int i=0; i<(int)list[1]; i++)
				if(kmn.get(pMNum*(int)list[1]+i) == 1)
					sortList.add(Umem.get(pMNum*(int)list[1]+i));
			Collections.sort(sortList);
			int moveVM = -1;
			double[] sortMEM = new double [(int)list[0]];
			sortMEM = uMEM();
			Arrays.sort(sortMEM);
			//do{
				//migrationInd = 0;
			for(int h=sortList.size()-1; h>=0; h--){
				for(int k=0; k<(int)list[1]; k++)
					if(kmn.get(pMNum*(int)list[1]+k) == 1 && sortList.get(h) == Umem.get(pMNum*(int)list[1]+k))
						moveVM = k;
				if(moveVM < 0){
					System.out.println("Initial Value Error. Program Stops.");
					System.exit(0);
				}
				int moveToPM;
				for(int i=0; i<(int)list[0]; i++){
					moveToPM = totalMEM.indexOf(sortMEM[i]);
					if(moveToPM != pMNum && totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM) <= Hcpu.get(moveToPM) && totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM) <= Hmem.get(moveToPM) && totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM) <= Hnet.get(moveToPM)){
						kmn.set(pMNum*(int)list[1]+moveVM, 0);
						kmn.set(moveToPM*(int)list[1]+moveVM, 1);
						totalCPU.set(moveToPM, totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM));
						totalMEM.set(moveToPM, totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM));
						totalNET.set(moveToPM, totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM));
						totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
						totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
						totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
						System.out.println("VM "+moveVM+" is moved to PM "+moveToPM+" from "+pMNum+" to reduce MEM utilization.");
						sortMEM = uMEM();
						Arrays.sort(sortMEM);
						break;
					}
				}
				if(totalMEM.get(pMNum) <= Hmem.get(pMNum))
					break;
			}
			if(totalMEM.get(pMNum) > Hmem.get(pMNum)){
				vioIndicator = swapVMs("MEM", pMNum);
			}
		}
		else if (utiType == "NET"){
			List<Double> sortList = new ArrayList<Double>();
			for(int i=0; i<(int)list[1]; i++)
				if(kmn.get(pMNum*(int)list[1]+i) == 1)
					sortList.add(Unet.get(pMNum*(int)list[1]+i));
			Collections.sort(sortList);
			int moveVM = -1;
			double[] sortNET = new double [(int)list[0]];
			sortNET = uNET();
			Arrays.sort(sortNET);
			//do{
				//migrationInd = 0;
			for(int h=sortList.size()-1; h>=0; h--){
				for(int k=0; k<(int)list[1]; k++)
					if(kmn.get(pMNum*(int)list[1]+k) == 1 && sortList.get(h) == Unet.get(pMNum*(int)list[1]+k))
						moveVM = k;
				if(moveVM < 0){
					System.out.println("Initial Value Error. Program Stops.");
					System.exit(0);
				}
				int moveToPM;
				for(int i=0; i<(int)list[0]; i++){
					moveToPM = totalNET.indexOf(sortNET[i]);
					if(moveToPM != pMNum && totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM) <= Hcpu.get(moveToPM) && totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM) <= Hmem.get(moveToPM) && totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM) <= Hnet.get(moveToPM)){
						kmn.set(pMNum*(int)list[1]+moveVM, 0);
						kmn.set(moveToPM*(int)list[1]+moveVM, 1);
						totalCPU.set(moveToPM, totalCPU.get(moveToPM)+Ucpu.get(moveToPM*(int)list[1]+moveVM));
						totalMEM.set(moveToPM, totalMEM.get(moveToPM)+Umem.get(moveToPM*(int)list[1]+moveVM));
						totalNET.set(moveToPM, totalNET.get(moveToPM)+Unet.get(moveToPM*(int)list[1]+moveVM));
						totalCPU.set(pMNum, totalCPU.get(pMNum)-Ucpu.get(pMNum*(int)list[1]+moveVM));
						totalMEM.set(pMNum, totalMEM.get(pMNum)-Umem.get(pMNum*(int)list[1]+moveVM));
						totalNET.set(pMNum, totalNET.get(pMNum)-Unet.get(pMNum*(int)list[1]+moveVM));
						System.out.println("VM "+moveVM+" is moved to PM "+moveToPM+" from "+pMNum+" to reduce NET utilization.");
						sortNET = uNET();
						Arrays.sort(sortNET);
						break;
					}
				}
				if(totalNET.get(pMNum) <= Hnet.get(pMNum))
					break;
			}
			if(totalNET.get(pMNum) > Hnet.get(pMNum)){
				vioIndicator = swapVMs("NET", pMNum);
			}
		}
		else{
			System.out.print("Unexpected Utilization Type. Porgram Stops.");
			System.exit(0);
		}
			
		return vioIndicator;
	}
	ArrayList<Integer> approach(){
		double[] cpu = new double [(int)list[0]];
		double[] mem = new double [(int)list[0]];
		double[] net = new double [(int)list[0]];
		//double[] disk= new double [(int)list[0]];
		//int[] kmn = new int [(int)list[0]];
		//kmn = lmn;
		cpu = uCPU();
		mem = uMEM();
		net = uNET();
		//disk = uDISK();
		double[] uArray = new double [(int)list[0]*3];
		System.arraycopy(cpu, 0, uArray, 0, cpu.length);
		System.arraycopy(mem, 0, uArray, cpu.length, mem.length);
		System.arraycopy(net, 0, uArray, cpu.length+mem.length, net.length);
		//for(double [] temp: uArray);
			//System.out.println(Arrays.toString(uArray));
		Arrays.sort(uArray);
		Arrays.sort(cpu);
		Arrays.sort(mem);
		Arrays.sort(net);
		int uArrayIndex = uArray.length-1;
		int cViolation = 0, mViolation = 0, nViolation = 0;
		
		while (uArrayIndex >= 0 && cViolation == 0 && mViolation == 0 && nViolation == 0){
			if(totalCPU.indexOf(uArray[uArrayIndex]) >= 0 && uArray[uArrayIndex] > Hcpu.get(totalCPU.indexOf(uArray[uArrayIndex]))){
			//if(Arrays.binarySearch(cpu, uArray[uArrayIndex]) >= 0 && uArray[uArrayIndex] > Hcpu.get(Arrays.binarySearch(cpu, uArray[uArrayIndex]))){
				cViolation = utiRedistributor("CPU", totalCPU.indexOf(uArray[uArrayIndex]));
				if (cViolation == 0) 
					uArrayIndex = uArray.length-1;
				else 
					break;
			}
			else if(totalMEM.indexOf(uArray[uArrayIndex]) >= 0 && uArray[uArrayIndex] > Hmem.get(totalMEM.indexOf(uArray[uArrayIndex]))){
				mViolation = utiRedistributor("MEM", totalMEM.indexOf(uArray[uArrayIndex]));
				if (mViolation == 0)
					uArrayIndex = uArray.length-1;
				else
					break;
			}
			else if(totalNET.indexOf(uArray[uArrayIndex]) >= 0 && uArray[uArrayIndex] > Hnet.get(totalNET.indexOf(uArray[uArrayIndex]))){
				nViolation = utiRedistributor("NET", totalNET.indexOf(uArray[uArrayIndex]));
				if (nViolation == 0)
					uArrayIndex = uArray.length-1;
				else
					break;
			}
			else if(totalCPU.indexOf(uArray[uArrayIndex]) < 0 && totalMEM.indexOf(uArray[uArrayIndex]) < 0 && totalNET.indexOf(uArray[uArrayIndex]) < 0){
				System.out.println("No Matches. Program Stops.");
				System.out.println(Arrays.toString(uArray));
				System.out.println(Arrays.toString(cpu));
				System.out.println(Arrays.toString(mem));
				System.out.println(Arrays.toString(net));
				System.out.println(totalCPU);
				System.out.println(totalMEM);
				System.out.println(totalNET);
				System.out.println("uArrayIndex is "+uArrayIndex+", uArray[uArrayIndex] is "+uArray[uArrayIndex]);
				System.out.println(totalCPU.indexOf(uArray[uArrayIndex])+","+totalMEM.indexOf(uArray[uArrayIndex])+","+totalNET.indexOf(uArray[uArrayIndex])+","+totalNET.indexOf(0.29));
				System.exit(0);
			}
			else
				uArrayIndex--;
			cpu = uCPU();
			mem = uMEM();
			net = uNET();
			System.arraycopy(cpu, 0, uArray, 0, cpu.length);
			System.arraycopy(mem, 0, uArray, cpu.length, mem.length);
			System.arraycopy(net, 0, uArray, cpu.length+mem.length, net.length);
			Arrays.sort(uArray);
		}
		if (cViolation != 0){
			System.out.println("CPU Utilization Constraint cannot be Satisfied. Program Stops");
			System.out.println(totalCPU);
			System.out.println(totalMEM);
			System.out.println(totalNET);
			System.out.println(kmn);
			System.out.println(lmn);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Ucpu.get(i*(int)list[1]+j)+", "+Umem.get(i*(int)list[1]+j)+", "+Unet.get(i*(int)list[1]+j));
			}
			System.exit(0);
		}
		else if (mViolation != 0){
			System.out.println("MEM Utilization Constraint cannot be Satisfied. Program Stops");
			System.out.println(totalMEM);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Umem.get(i*(int)list[1]+j));
			}
			System.exit(0);
		}
		else if (nViolation != 0){
			System.out.println("NET Utilization Constraint cannot be Satisfied. Program Stops");
			System.out.println(totalNET);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Unet.get(i*(int)list[1]+j));
			}
			System.exit(0);
		}
		else{
			System.out.println("No Utilization Constraints Violated. Program Proceeds to the Energy-saving Process.");
			System.out.println(totalCPU);
			System.out.println(totalMEM);
			System.out.println(totalNET);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Ucpu.get(i*(int)list[1]+j)+", "+Umem.get(i*(int)list[1]+j)+", "+Unet.get(i*(int)list[1]+j));
			}
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.print(j+", ");
				System.out.println("");
			}
			for (int i=0; i<(int)list[0]; i++)
				for (int j=0; j<(int)list[1]; j++)
					if (lmn.get(i*(int)list[1]+j) == 0 && kmn.get(i*(int)list[1]+j) == 1)
						gmn.add(i*(int)list[1]+j, 1);
					else
						gmn.add(i*(int)list[1]+j, 0);
			for (int i=0; i<(int)list[0]; i++){
				om.add(i, 0);
				for (int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1){
						om.set(i, 1);
						break;
					}
			}
			//double energy=0.0;
			for (int i=0; i<(int)list[0]; i++){
				PMEnergy.add(0.0);
				if (om.get(i)==1){
					totEnergy+=Pactive.get(i)*list[2];
					PMEnergy.set(i, Pactive.get(i)*list[2]);
				}
				else{
					totEnergy+=Psleep.get(i)*list[2];
					PMEnergy.set(i, Psleep.get(i)*list[2]);
				}
			}
			System.out.println(totEnergy);
			for (int i=0; i<(int)list[0]; i++){
				VMEnergy.add(0.0);
				for (int j=0; j<(int)list[1]; j++)
					if (kmn.get(i*(int)list[1]+j)==1){
						totEnergy+=Pm.get(i*(int)list[1]+j)*list[2];
						VMEnergy.set(i, VMEnergy.get(i)+Pm.get(i*(int)list[1]+j)*list[2]);
						PMEnergy.set(i, PMEnergy.get(i)+Pm.get(i*(int)list[1]+j)*list[2]);
					}
			}
			System.out.println(totEnergy);
			for (int i=0; i<(int)list[0]; i++){
				migEnergy.add(0.0);
				for (int j=0; j<(int)list[1]; j++)
					if (gmn.get(i*(int)list[1]+j)==1){
						totEnergy+=Pmigrate.get(i*(int)list[1]+j)*T.get(i*(int)list[1]+j);
						migEnergy.set(i, migEnergy.get(i)+Pmigrate.get(i*(int)list[1]+j)*T.get(i*(int)list[1]+j));
						PMEnergy.set(i, PMEnergy.get(i)+Pmigrate.get(i*(int)list[1]+j)*T.get(i*(int)list[1]+j));
					}
			}
			System.out.println("Before Energy-saving porcedure, the energy consumption is "+totEnergy+" J.");

		}
	//Energy-saving Procedure Goes from here.
		consolidation();
	return kmn;
	}
}
