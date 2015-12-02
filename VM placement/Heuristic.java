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
	//ArrayList<Double> tmpMigEnergy = new ArrayList<Double>();
	ArrayList<Double> VMEnergy = new ArrayList<Double>();
	//ArrayList<Double> tmpVMEnergy = new ArrayList<Double>();
	ArrayList<Double> PMEnergy = new ArrayList<Double>();
	//ArrayList<Double> tmpPMEnergy = new ArrayList<Double>();
	double totEnergy;//, tmpTotEnergy;
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
	boolean VMigration(String resourceType, ArrayList<Integer> placement, int toPM, int[] utiRanking, int fromPM){
		boolean validMigration = false;
		if (resourceType == "CPU"){
			int VMNumber = 0;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1)
					VMNumber++;
			double[] sortedVMUti = new double [VMNumber];
			int[] sortedVM = new int [VMNumber];
			int tmp = VMNumber-1 ;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1){
					sortedVMUti[tmp] = Ucpu.get(toPM*(int)list[1]+i);
					sortedVM[tmp] = i;
					tmp--;
				}
			getRanking(sortedVMUti, sortedVM);
			for (int i=VMNumber-1; i>0; i--){
				if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
					break;
				for (int j=0; j<utiRanking.length; j++)
					if (utiRanking[j] != toPM && utiRanking[j] != fromPM && om.get(utiRanking[j]) != 0){
						if (totalCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hcpu.get(utiRanking[j]) && totalMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hmem.get(utiRanking[j]) && totalNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hnet.get(utiRanking[j])){
							placement.set(toPM*(int)list[1]+sortedVM[i], 0);
							placement.set(utiRanking[j]*(int)list[1]+sortedVM[i], 1);
							totalCPU.set(toPM, totalCPU.get(toPM) - Ucpu.get(toPM*(int)list[1]+sortedVM[i]));
							totalCPU.set(utiRanking[j], totalCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totalMEM.set(toPM, totalMEM.get(toPM) - Umem.get(toPM*(int)list[1]+sortedVM[i]));
							totalMEM.set(utiRanking[j], totalMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totalNET.set(toPM, totalNET.get(toPM) - Unet.get(toPM*(int)list[1]+sortedVM[i]));
							totalNET.set(utiRanking[j], totalNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							migEnergy.set(toPM, migEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							PMEnergy.set(toPM, PMEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							totEnergy -= Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]);
							migEnergy.set(utiRanking[j], migEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							PMEnergy.set(utiRanking[j], PMEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totEnergy += Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]);
							VMEnergy.set(toPM, VMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							PMEnergy.set(toPM, PMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							totEnergy -= Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2];
							VMEnergy.set(utiRanking[j], VMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							PMEnergy.set(utiRanking[j], PMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							totEnergy += Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2];
							
							if (PMEnergy.get(toPM) == Pactive.get(toPM)*list[2]){
								PMEnergy.set(toPM, Psleep.get(toPM)*list[2]);
								totEnergy = totEnergy - Pactive.get(toPM)*list[2] + Psleep.get(toPM)*list[2];
							}
							break;
						}
					}
			}
			if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
				validMigration = true;
		}
		else if (resourceType == "MEM"){
			int VMNumber = 0;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1)
					VMNumber++;
			double[] sortedVMUti = new double [VMNumber];
			int[] sortedVM = new int [VMNumber];
			int tmp = VMNumber-1;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1){
					sortedVMUti[tmp] = Umem.get(toPM*(int)list[1]+i);
					sortedVM[tmp] = i;
					tmp--;
				}
			getRanking(sortedVMUti, sortedVM);
			for (int i=VMNumber-1; i>0; i--){
				if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
					break;
				for (int j=0; j<utiRanking.length; j++)
					if (utiRanking[j] != toPM && utiRanking[j] != fromPM && om.get(utiRanking[j]) != 0){
						if (totalCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hcpu.get(utiRanking[j]) && totalMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hmem.get(utiRanking[j]) && totalNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hnet.get(utiRanking[j])){
							placement.set(toPM*(int)list[1]+sortedVM[i], 0);
							placement.set(utiRanking[j]*(int)list[1]+sortedVM[i], 1);
							totalCPU.set(toPM, totalCPU.get(toPM) - Ucpu.get(toPM*(int)list[1]+sortedVM[i]));
							totalCPU.set(utiRanking[j], totalCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totalMEM.set(toPM, totalMEM.get(toPM) - Umem.get(toPM*(int)list[1]+sortedVM[i]));
							totalMEM.set(utiRanking[j], totalMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totalNET.set(toPM, totalNET.get(toPM) - Unet.get(toPM*(int)list[1]+sortedVM[i]));
							totalNET.set(utiRanking[j], totalNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							migEnergy.set(toPM, migEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							PMEnergy.set(toPM, PMEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							totEnergy -= Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]);
							migEnergy.set(utiRanking[j], migEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							PMEnergy.set(utiRanking[j], PMEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totEnergy += Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]);
							VMEnergy.set(toPM, VMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							PMEnergy.set(toPM, PMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							totEnergy -= Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2];
							VMEnergy.set(utiRanking[j], VMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							PMEnergy.set(utiRanking[j], PMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							totEnergy += Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2];
							//System.out.println("1. "+PMEnergy);
							//System.out.println("2. "+VMEnergy);
							//System.out.println("3. "+totEnergy);
							if (PMEnergy.get(toPM) == Pactive.get(toPM)*list[2]){
								PMEnergy.set(toPM, Psleep.get(toPM)*list[2]);
								totEnergy = totEnergy - Pactive.get(toPM)*list[2] + Psleep.get(toPM)*list[2];
							}
							break;
						}
					}
			}
			if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
				validMigration = true;
		}
		else if (resourceType == "NET"){
			int VMNumber = 0;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1)
					VMNumber++;
			double[] sortedVMUti = new double [VMNumber];
			int[] sortedVM = new int [VMNumber];
			int tmp = VMNumber-1;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1){
					sortedVMUti[tmp] = Unet.get(toPM*(int)list[1]+i);
					sortedVM[tmp] = i;
					tmp--;
				}
			getRanking(sortedVMUti, sortedVM);
			for (int i=VMNumber-1; i>0; i--){
				if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
					break;
				for (int j=0; j<utiRanking.length; j++)
					if (utiRanking[j] != toPM && utiRanking[j] != fromPM && om.get(utiRanking[j]) != 0){
						if (totalCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hcpu.get(utiRanking[j]) && totalMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hmem.get(utiRanking[j]) && totalNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hnet.get(utiRanking[j])){
							placement.set(toPM*(int)list[1]+sortedVM[i], 0);
							placement.set(utiRanking[j]*(int)list[1]+sortedVM[i], 1);
							totalCPU.set(toPM, totalCPU.get(toPM) - Ucpu.get(toPM*(int)list[1]+sortedVM[i]));
							totalCPU.set(utiRanking[j], totalCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totalMEM.set(toPM, totalMEM.get(toPM) - Umem.get(toPM*(int)list[1]+sortedVM[i]));
							totalMEM.set(utiRanking[j], totalMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totalNET.set(toPM, totalNET.get(toPM) - Unet.get(toPM*(int)list[1]+sortedVM[i]));
							totalNET.set(utiRanking[j], totalNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							migEnergy.set(toPM, migEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							PMEnergy.set(toPM, PMEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							totEnergy -= Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]);
							migEnergy.set(utiRanking[j], migEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							PMEnergy.set(utiRanking[j], PMEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							totEnergy += Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]);
							VMEnergy.set(toPM, VMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							PMEnergy.set(toPM, PMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							totEnergy -= Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2];
							VMEnergy.set(utiRanking[j], VMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							PMEnergy.set(utiRanking[j], PMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							totEnergy += Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2];
							//System.out.println("4. "+totEnergy);
							if (PMEnergy.get(toPM) == Pactive.get(toPM)*list[2]){
								PMEnergy.set(toPM, Psleep.get(toPM)*list[2]);
								totEnergy = totEnergy - Pactive.get(toPM)*list[2] + Psleep.get(toPM)*list[2];
							}
							break;
						}
					}
			}
			if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
				validMigration = true;
		}
		else{
			System.out.println("Unexpected Resource Type. Program Stops.");
			System.exit(0);
		}
		return validMigration;
	}
	boolean processOfUniting(int[] utiRanking, int fromIndex, int toIndex, int fromPM, int toPM){
/*		ArrayList<Double> tmpTotCPU = new ArrayList<Double>();
		ArrayList<Double> tmpTotMEM = new ArrayList<Double>();
		ArrayList<Double> tmpTotNET = new ArrayList<Double>();
		ArrayList<Double> tmpMigEnergy = new ArrayList<Double>();
		ArrayList<Double> tmpVMEnergy = new ArrayList<Double>();
		ArrayList<Double> tmpPMEnergy = new ArrayList<Double>();
		double tmpTotEnergy = totEnergy;
		for (int i=0; i<(int)list[0]; i++){
			tmpTotCPU.add(totalCPU.get(i));
			tmpTotMEM.add(totalMEM.get(i));
			tmpTotNET.add(totalNET.get(i));
			tmpMigEnergy.add(migEnergy.get(i));
			tmpVMEnergy.add(VMEnergy.get(i));
			tmpPMEnergy.add(PMEnergy.get(i));
		}*/
		//for (int i; i<(int)list[0]; i++)
		boolean valid = false;
		for (int j=0; j<(int)list[1]; j++)
			if (kmn.get(fromPM*(int)list[1]+j) == 1){
				kmn.set(fromPM*(int)list[1]+j, 0);
				kmn.set(toPM*(int)list[1]+j, 1);
				totalCPU.set(fromPM, totalCPU.get(fromPM) - Ucpu.get(fromPM*(int)list[1]+j));
				totalCPU.set(toPM, totalCPU.get(toPM) + Ucpu.get(toPM*(int)list[1]+j));
				totalMEM.set(fromPM, totalMEM.get(fromPM) - Umem.get(fromPM*(int)list[1]+j));
				totalMEM.set(toPM, totalMEM.get(toPM) + Umem.get(toPM*(int)list[1]+j));
				totalNET.set(fromPM, totalNET.get(fromPM) - Unet.get(fromPM*(int)list[1]+j));
				totalNET.set(toPM, totalNET.get(toPM) + Unet.get(toPM*(int)list[1]+j));
				migEnergy.set(fromPM, migEnergy.get(fromPM) - Pmigrate.get(fromPM*(int)list[1]+j)*T.get(fromPM*(int)list[1]+j));
				PMEnergy.set(fromPM, PMEnergy.get(fromPM) - Pmigrate.get(fromPM*(int)list[1]+j)*T.get(fromPM*(int)list[1]+j));
				totEnergy -= Pmigrate.get(fromPM*(int)list[1]+j)*T.get(fromPM*(int)list[1]+j);
				migEnergy.set(toPM, migEnergy.get(toPM) + Pmigrate.get(toPM*(int)list[1]+j)*T.get(toPM*(int)list[1]+j));
				PMEnergy.set(toPM, PMEnergy.get(toPM) + Pmigrate.get(toPM*(int)list[1]+j)*T.get(toPM*(int)list[1]+j));
				totEnergy += Pmigrate.get(toPM*(int)list[1]+j)*T.get(toPM*(int)list[1]+j);
				VMEnergy.set(fromPM, VMEnergy.get(fromPM) - Pm.get(fromPM*(int)list[1]+j)*list[2]);
				PMEnergy.set(fromPM, PMEnergy.get(fromPM) - Pm.get(fromPM*(int)list[1]+j)*list[2]);
				totEnergy -= Pm.get(fromPM*(int)list[1]+j)*list[2];
				VMEnergy.set(toPM, VMEnergy.get(toPM) + Pm.get(toPM*(int)list[1]+j)*list[2]);
				PMEnergy.set(toPM, PMEnergy.get(toPM) + Pm.get(toPM*(int)list[1]+j)*list[2]);
				totEnergy += Pm.get(toPM*(int)list[1]+j)*list[2];
				if (PMEnergy.get(fromPM) == Pactive.get(fromPM)*list[2]){
					PMEnergy.set(fromPM, Psleep.get(fromPM)*list[2]);
					totEnergy = totEnergy - Pactive.get(fromPM)*list[2] + Psleep.get(fromPM)*list[2];
				}
			}
		if (PMEnergy.get(fromPM) != Psleep.get(fromPM)*list[2]){
			System.out.println("0. Migration Error! Program Stops!");
			System.out.println(PMEnergy.get(fromPM)+", "+Psleep.get(fromPM)*list[2]);
			System.exit(0);
		}
		if (totalCPU.get(toPM) <= Hcpu.get(toPM) && totalMEM.get(toPM) <= Hmem.get(toPM) && totalNET.get(toPM) <= Hnet.get(toPM))
			valid = true;
		else{
			double[] resourceToSort = {totalCPU.get(toPM), totalMEM.get(toPM), totalNET.get(toPM)};
			Arrays.sort(resourceToSort);
			char[] uti = new char [resourceToSort.length];
			for (int k=resourceToSort.length-1; k>=0; k--){
				if (resourceToSort[k] == totalCPU.get(toPM))
					uti[k] = 'C';
				else if (resourceToSort[k] == totalMEM.get(toPM))
					uti[k] = 'M';
				else if (resourceToSort[k] == totalNET.get(toPM))
					uti[k] = 'N';
				else{
					System.out.println("0. Unexpected Comparison Result. Program Stops.");
					System.exit(0);
				}
			}
			for (int k=resourceToSort.length-1; k>=0; k--){	
				if (uti[k] == 'C'){
/*					int MEMIndex = Arrays.binarySearch(resourceToSort, totalMEM.get(toPM));
					int NETIndex = Arrays.binarySearch(resourceToSort, totalNET.get(toPM));*/
					if (totalCPU.get(toPM) > Hcpu.get(toPM)){
						valid = VMigration("CPU", kmn, toPM, utiRanking, fromPM);
					}
/*					if (valid == false)
						break;*/
/*					resourceToSort[k] = totalCPU.get(toPM);
					resourceToSort[MEMIndex] = totalMEM.get(toPM);
					resourceToSort[NETIndex] = totalNET.get(toPM);*/
				}
				else if (uti[k] == 'M'){
/*					int CPUIndex = Arrays.binarySearch(resourceToSort, totalCPU.get(toPM));
					int NETIndex = Arrays.binarySearch(resourceToSort, totalNET.get(toPM));*/
					if (totalMEM.get(toPM) > Hmem.get(toPM)){
						valid = VMigration("MEM", kmn, toPM, utiRanking, fromPM);
						//System.out.println("4. "+totEnergy);
					}
/*					if (valid == false)
						break;*/
/*					resourceToSort[k] = totalMEM.get(toPM);
					resourceToSort[CPUIndex] = totalCPU.get(toPM);
					resourceToSort[NETIndex] = totalNET.get(toPM);*/
				}
				else if (uti[k] == 'N'){
/*					int CPUIndex = Arrays.binarySearch(resourceToSort, totalCPU.get(toPM));
					int MEMIndex = Arrays.binarySearch(resourceToSort, totalMEM.get(toPM));*/
					if (totalNET.get(toPM) > Hnet.get(toPM)){
						valid = VMigration("NET", kmn, toPM, utiRanking, fromPM);
					}
/*					if (valid == false)
						break;*/
/*					resourceToSort[k] = totalNET.get(toPM);
					resourceToSort[MEMIndex] = totalMEM.get(toPM);
					resourceToSort[CPUIndex] = totalCPU.get(toPM);*/
				}
				else{
					System.out.println("Unexpected Comparison Result. Program Stops.");
					System.exit(0);
				}
				
			}
			
			
		}
		// try to not violate resource utilization constraints
		// if total energy consumption with migration is less than staying at the physical machine, return true
		// else try to find a solution with less energy consumption
		// if found, return true
		// else return false
		return valid;
	}
	void getRanking(double[] sortedVMUti, int[] sortedVM){
		double[] clonedArray = new double [sortedVMUti.length];
		int[] clonedIndex = new int [sortedVM.length];
		for (int i=0; i<clonedArray.length; i++){
			clonedArray[i] = sortedVMUti[i];
			clonedIndex[i] = sortedVM[i];
		}
		Arrays.sort(clonedArray);
		for (int i=0; i<clonedArray.length; i++)
			for (int j=0; j<clonedArray.length; j++)
				if (clonedArray[i] == sortedVMUti[j])
					sortedVM[i] = clonedIndex[j];
	}
	boolean VMigration(String resourceType, ArrayList<Integer> placement, int toPM, int[] utiRanking, int fromPM, ArrayList<Double> tmpTotCPU, ArrayList<Double> tmpTotMEM, ArrayList<Double> tmpTotNET, ArrayList<Double> tmpMigEnergy, ArrayList<Double> tmpPMEnergy, ArrayList<Double> tmpVMEnergy, double tmpTotEnergy){
		boolean validMigration = false;
		if (resourceType == "CPU"){
			int VMNumber = 0;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1)
					VMNumber++;
			double[] sortedVMUti = new double [VMNumber];
			int[] sortedVM = new int [VMNumber];
			int tmp = VMNumber-1 ;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1){
					sortedVMUti[tmp] = Ucpu.get(toPM*(int)list[1]+i);
					sortedVM[tmp] = i;
					tmp--;
				}
			getRanking(sortedVMUti, sortedVM);
			for (int i=VMNumber-1; i>0; i--){
				if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
					break;
				for (int j=0; j<utiRanking.length; j++)
					if (utiRanking[j] != toPM && utiRanking[j] != fromPM && om.get(utiRanking[j]) != 0){
						if (tmpTotCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hcpu.get(utiRanking[j]) && tmpTotMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hmem.get(utiRanking[j]) && tmpTotNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hnet.get(utiRanking[j])){
							placement.set(toPM*(int)list[1]+sortedVM[i], 0);
							placement.set(utiRanking[j]*(int)list[1]+sortedVM[i], 1);
							tmpTotCPU.set(toPM, tmpTotCPU.get(toPM) - Ucpu.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotCPU.set(utiRanking[j], tmpTotCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotMEM.set(toPM, tmpTotMEM.get(toPM) - Umem.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotMEM.set(utiRanking[j], tmpTotMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotNET.set(toPM, tmpTotNET.get(toPM) - Unet.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotNET.set(utiRanking[j], tmpTotNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpMigEnergy.set(toPM, tmpMigEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotEnergy -= Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]);
							tmpMigEnergy.set(utiRanking[j], tmpMigEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpPMEnergy.set(utiRanking[j], tmpPMEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotEnergy += Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]);
							tmpVMEnergy.set(toPM, tmpVMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							tmpTotEnergy -= Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2];
							tmpVMEnergy.set(utiRanking[j], tmpVMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							tmpPMEnergy.set(utiRanking[j], tmpPMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							tmpTotEnergy += Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2];
							
							if (tmpPMEnergy.get(toPM) == Pactive.get(toPM)*list[2]){
								tmpPMEnergy.set(toPM, Psleep.get(toPM)*list[2]);
								tmpTotEnergy = tmpTotEnergy - Pactive.get(toPM)*list[2] + Psleep.get(toPM)*list[2];
							}
							break;
						}
					}
			}
			if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
				validMigration = true;
		}
		else if (resourceType == "MEM"){
			int VMNumber = 0;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1)
					VMNumber++;
			double[] sortedVMUti = new double [VMNumber];
			int[] sortedVM = new int [VMNumber];
			int tmp = VMNumber-1;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1){
					sortedVMUti[tmp] = Umem.get(toPM*(int)list[1]+i);
					sortedVM[tmp] = i;
					tmp--;
				}
			getRanking(sortedVMUti, sortedVM);
			for (int i=VMNumber-1; i>0; i--){
				if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
					break;
				for (int j=0; j<utiRanking.length; j++)
					if (utiRanking[j] != toPM && utiRanking[j] != fromPM && om.get(utiRanking[j]) != 0){
						if (tmpTotCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hcpu.get(utiRanking[j]) && tmpTotMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hmem.get(utiRanking[j]) && tmpTotNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hnet.get(utiRanking[j])){
							placement.set(toPM*(int)list[1]+sortedVM[i], 0);
							placement.set(utiRanking[j]*(int)list[1]+sortedVM[i], 1);
							tmpTotCPU.set(toPM, tmpTotCPU.get(toPM) - Ucpu.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotCPU.set(utiRanking[j], tmpTotCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotMEM.set(toPM, tmpTotMEM.get(toPM) - Umem.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotMEM.set(utiRanking[j], tmpTotMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotNET.set(toPM, tmpTotNET.get(toPM) - Unet.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotNET.set(utiRanking[j], tmpTotNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpMigEnergy.set(toPM, tmpMigEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotEnergy -= Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]);
							tmpMigEnergy.set(utiRanking[j], tmpMigEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpPMEnergy.set(utiRanking[j], tmpPMEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotEnergy += Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]);
							tmpVMEnergy.set(toPM, tmpVMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							tmpTotEnergy -= Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2];
							tmpVMEnergy.set(utiRanking[j], tmpVMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							tmpPMEnergy.set(utiRanking[j], tmpPMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							tmpTotEnergy += Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2];
							//System.out.println("1. "+tmpPMEnergy);
							//System.out.println("2. "+tmpVMEnergy);
							//System.out.println("3. "+tmpTotEnergy);
							if (tmpPMEnergy.get(toPM) == Pactive.get(toPM)*list[2]){
								tmpPMEnergy.set(toPM, Psleep.get(toPM)*list[2]);
								tmpTotEnergy = tmpTotEnergy - Pactive.get(toPM)*list[2] + Psleep.get(toPM)*list[2];
							}
							break;
						}
					}
			}
			if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
				validMigration = true;
		}
		else if (resourceType == "NET"){
			int VMNumber = 0;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1)
					VMNumber++;
			double[] sortedVMUti = new double [VMNumber];
			int[] sortedVM = new int [VMNumber];
			int tmp = VMNumber-1;
			for (int i=0; i<(int)list[1]; i++)
				if (placement.get(toPM*(int)list[1]+i) == 1){
					sortedVMUti[tmp] = Unet.get(toPM*(int)list[1]+i);
					sortedVM[tmp] = i;
					tmp--;
				}
			getRanking(sortedVMUti, sortedVM);
			for (int i=VMNumber-1; i>0; i--){
				if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
					break;
				for (int j=0; j<utiRanking.length; j++)
					if (utiRanking[j] != toPM && utiRanking[j] != fromPM && om.get(utiRanking[j]) != 0){
						if (tmpTotCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hcpu.get(utiRanking[j]) && tmpTotMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hmem.get(utiRanking[j]) && tmpTotNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]) <= Hnet.get(utiRanking[j])){
							placement.set(toPM*(int)list[1]+sortedVM[i], 0);
							placement.set(utiRanking[j]*(int)list[1]+sortedVM[i], 1);
							tmpTotCPU.set(toPM, tmpTotCPU.get(toPM) - Ucpu.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotCPU.set(utiRanking[j], tmpTotCPU.get(utiRanking[j]) + Ucpu.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotMEM.set(toPM, tmpTotMEM.get(toPM) - Umem.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotMEM.set(utiRanking[j], tmpTotMEM.get(utiRanking[j]) + Umem.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotNET.set(toPM, tmpTotNET.get(toPM) - Unet.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotNET.set(utiRanking[j], tmpTotNET.get(utiRanking[j]) + Unet.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpMigEnergy.set(toPM, tmpMigEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) - Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]));
							tmpTotEnergy -= Pmigrate.get(toPM*(int)list[1]+sortedVM[i])*T.get(toPM*(int)list[1]+sortedVM[i]);
							tmpMigEnergy.set(utiRanking[j], tmpMigEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpPMEnergy.set(utiRanking[j], tmpPMEnergy.get(utiRanking[j]) + Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]));
							tmpTotEnergy += Pmigrate.get(utiRanking[j]*(int)list[1]+sortedVM[i])*T.get(utiRanking[j]*(int)list[1]+sortedVM[i]);
							tmpVMEnergy.set(toPM, tmpVMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) - Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2]);
							tmpTotEnergy -= Pm.get(toPM*(int)list[1]+sortedVM[i])*list[2];
							tmpVMEnergy.set(utiRanking[j], tmpVMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							tmpPMEnergy.set(utiRanking[j], tmpPMEnergy.get(utiRanking[j]) + Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2]);
							tmpTotEnergy += Pm.get(utiRanking[j]*(int)list[1]+sortedVM[i])*list[2];
							//System.out.println("4. "+totEnergy);
							if (tmpPMEnergy.get(toPM) == Pactive.get(toPM)*list[2]){
								tmpPMEnergy.set(toPM, Psleep.get(toPM)*list[2]);
								tmpTotEnergy = tmpTotEnergy - Pactive.get(toPM)*list[2] + Psleep.get(toPM)*list[2];
							}
							break;
						}
					}
			}
			if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
				validMigration = true;
		}
		else{
			System.out.println("Unexpected Resource Type. Program Stops.");
			System.exit(0);
		}
		return validMigration;
	}
	boolean processOfUniting(int[] utiRanking, int fromIndex, int toIndex, int fromPM, int toPM, ArrayList<Integer> tmpkmn){
		ArrayList<Double> tmpTotCPU = new ArrayList<Double>();
		ArrayList<Double> tmpTotMEM = new ArrayList<Double>();
		ArrayList<Double> tmpTotNET = new ArrayList<Double>();
		ArrayList<Double> tmpMigEnergy = new ArrayList<Double>();
		ArrayList<Double> tmpVMEnergy = new ArrayList<Double>();
		ArrayList<Double> tmpPMEnergy = new ArrayList<Double>();
		double tmpTotEnergy = totEnergy;
		boolean valid = false;
		for (int i=0; i<(int)list[0]; i++){
			tmpTotCPU.add(totalCPU.get(i));
			tmpTotMEM.add(totalMEM.get(i));
			tmpTotNET.add(totalNET.get(i));
			tmpMigEnergy.add(migEnergy.get(i));
			tmpVMEnergy.add(VMEnergy.get(i));
			tmpPMEnergy.add(PMEnergy.get(i));
		}
		//for (int i; i<(int)list[0]; i++)
		for (int j=0; j<(int)list[1]; j++)
			if (tmpkmn.get(fromPM*(int)list[1]+j) == 1){
				tmpkmn.set(fromPM*(int)list[1]+j, 0);
				tmpkmn.set(toPM*(int)list[1]+j, 1);
				tmpTotCPU.set(fromPM, tmpTotCPU.get(fromPM) - Ucpu.get(fromPM*(int)list[1]+j));
				tmpTotCPU.set(toPM, tmpTotCPU.get(toPM) + Ucpu.get(toPM*(int)list[1]+j));
				tmpTotMEM.set(fromPM, tmpTotMEM.get(fromPM) - Umem.get(fromPM*(int)list[1]+j));
				tmpTotMEM.set(toPM, tmpTotMEM.get(toPM) + Umem.get(toPM*(int)list[1]+j));
				tmpTotNET.set(fromPM, tmpTotNET.get(fromPM) - Unet.get(fromPM*(int)list[1]+j));
				tmpTotNET.set(toPM, tmpTotNET.get(toPM) + Unet.get(toPM*(int)list[1]+j));
				tmpMigEnergy.set(fromPM, tmpMigEnergy.get(fromPM) - Pmigrate.get(fromPM*(int)list[1]+j)*T.get(fromPM*(int)list[1]+j));
				tmpPMEnergy.set(fromPM, tmpPMEnergy.get(fromPM) - Pmigrate.get(fromPM*(int)list[1]+j)*T.get(fromPM*(int)list[1]+j));
				tmpTotEnergy -= Pmigrate.get(fromPM*(int)list[1]+j)*T.get(fromPM*(int)list[1]+j);
				tmpMigEnergy.set(toPM, tmpMigEnergy.get(toPM) + Pmigrate.get(toPM*(int)list[1]+j)*T.get(toPM*(int)list[1]+j));
				tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) + Pmigrate.get(toPM*(int)list[1]+j)*T.get(toPM*(int)list[1]+j));
				tmpTotEnergy += Pmigrate.get(toPM*(int)list[1]+j)*T.get(toPM*(int)list[1]+j);
				tmpVMEnergy.set(fromPM, tmpVMEnergy.get(fromPM) - Pm.get(fromPM*(int)list[1]+j)*list[2]);
				tmpPMEnergy.set(fromPM, tmpPMEnergy.get(fromPM) - Pm.get(fromPM*(int)list[1]+j)*list[2]);
				tmpTotEnergy -= Pm.get(fromPM*(int)list[1]+j)*list[2];
				tmpVMEnergy.set(toPM, tmpVMEnergy.get(toPM) + Pm.get(toPM*(int)list[1]+j)*list[2]);
				tmpPMEnergy.set(toPM, tmpPMEnergy.get(toPM) + Pm.get(toPM*(int)list[1]+j)*list[2]);
				tmpTotEnergy += Pm.get(toPM*(int)list[1]+j)*list[2];
				if (tmpPMEnergy.get(fromPM) == Pactive.get(fromPM)*list[2]){
					tmpPMEnergy.set(fromPM, Psleep.get(fromPM)*list[2]);
					tmpTotEnergy = tmpTotEnergy - Pactive.get(fromPM)*list[2] + Psleep.get(fromPM)*list[2];
				}
				//System.out.println(tmpPMEnergy.get(fromPM));
			}
		if (tmpPMEnergy.get(fromPM) != Psleep.get(fromPM)*list[2]){
			System.out.println("Migration Error! Program Stops!");
			System.out.println(tmpPMEnergy.get(fromPM)+", "+Psleep.get(fromPM)*list[2]);
			for (int i=0; i<(int)list[1]; i++)
				if (tmpkmn.get(fromPM*(int)list[1]+i) == 1)
					System.out.println("VM "+i+" is still on PM "+fromPM+".");
			System.exit(0);
		}
		if (tmpTotCPU.get(toPM) <= Hcpu.get(toPM) && tmpTotMEM.get(toPM) <= Hmem.get(toPM) && tmpTotNET.get(toPM) <= Hnet.get(toPM))
			valid = true;
		else{
			double[] resourceToSort = {tmpTotCPU.get(toPM), tmpTotMEM.get(toPM), tmpTotNET.get(toPM)};
			Arrays.sort(resourceToSort);
			char[] uti = new char [resourceToSort.length];
			for (int k=resourceToSort.length-1; k>=0; k--){
				if (resourceToSort[k] == tmpTotCPU.get(toPM))
					uti[k] = 'C';
				else if (resourceToSort[k] == tmpTotMEM.get(toPM))
					uti[k] = 'M';
				else if (resourceToSort[k] == tmpTotNET.get(toPM))
					uti[k] = 'N';
				else{
					System.out.println("0. Unexpected Comparison Result. Program Stops.");
					System.exit(0);
				}
			}
			for (int k=resourceToSort.length-1; k>=0; k--){	
				if (uti[k] == 'C'){
/*					int MEMIndex = Arrays.binarySearch(resourceToSort, tmpTotMEM.get(toPM));
					int NETIndex = Arrays.binarySearch(resourceToSort, tmpTotNET.get(toPM));*/
					if (tmpTotCPU.get(toPM) > Hcpu.get(toPM)){
						valid = VMigration("CPU", tmpkmn, toPM, utiRanking, fromPM, tmpTotCPU, tmpTotMEM, tmpTotNET, tmpMigEnergy, tmpPMEnergy, tmpVMEnergy, tmpTotEnergy);
					}
/*					if (valid == false)
						break;
*/					if	(tmpTotEnergy > totEnergy)
						valid = false;
/*					resourceToSort[k] = tmpTotCPU.get(toPM);
					resourceToSort[MEMIndex] = tmpTotMEM.get(toPM);
					resourceToSort[NETIndex] = tmpTotNET.get(toPM);*/
				}
				else if (uti[k] == 'M'){
/*					int CPUIndex = Arrays.binarySearch(resourceToSort, tmpTotCPU.get(toPM));
					int NETIndex = Arrays.binarySearch(resourceToSort, tmpTotNET.get(toPM));*/
					if (tmpTotMEM.get(toPM) > Hmem.get(toPM)){
						valid = VMigration("MEM", tmpkmn, toPM, utiRanking, fromPM, tmpTotCPU, tmpTotMEM, tmpTotNET, tmpMigEnergy, tmpPMEnergy, tmpVMEnergy, tmpTotEnergy);
					}
/*					if (valid == false)
						break;*/
					if	(tmpTotEnergy > totEnergy)
						valid = false;
/*					resourceToSort[k] = tmpTotMEM.get(toPM);
					resourceToSort[CPUIndex] = tmpTotCPU.get(toPM);
					resourceToSort[NETIndex] = tmpTotNET.get(toPM);*/
				}
				else if (uti[k] == 'N'){
/*					int CPUIndex = Arrays.binarySearch(resourceToSort, tmpTotCPU.get(toPM));
					int MEMIndex = Arrays.binarySearch(resourceToSort, tmpTotMEM.get(toPM));*/
					if (tmpTotNET.get(toPM) > Hnet.get(toPM)){
						valid = VMigration("NET", tmpkmn, toPM, utiRanking, fromPM, tmpTotCPU, tmpTotMEM, tmpTotNET, tmpMigEnergy, tmpPMEnergy, tmpVMEnergy, tmpTotEnergy);
					}
/*					if (valid == false)
						break;*/
					if	(tmpTotEnergy > totEnergy)
						valid = false;
/*					resourceToSort[k] = tmpTotNET.get(toPM);
					resourceToSort[MEMIndex] = tmpTotMEM.get(toPM);
					resourceToSort[CPUIndex] = tmpTotCPU.get(toPM);*/
				}
				else{
					System.out.println("0. Unexpected Comparison Result. Program Stops.");
/*					for (double y:resourceToSort)
						System.out.println(y);
					System.out.println(tmpTotCPU.get(toPM)+tmpTotMEM.get(toPM)+tmpTotNET.get(toPM));*/
					System.exit(0);
				}
				
			}
		}
		
			
		// try to not violate resource utilization constraints
		// if total energy consumption with migration is less than staying at the physical machine, return true
		// else try to find a solution with less energy consumption
		// if found, return true
		// else return false
		return valid;
	}
	void consolidation(){
		int[] utiRanking = new int [(int)list[0]];
		utiRanking = getUtiSumRanking();
		boolean nextConsolidation = true;
		while (nextConsolidation == true){
			nextConsolidation = false;
			
			for (int i=0; i<(int)list[0]-1; i++){
				if (om.get(utiRanking[i]) == 1){
					for (int j=0; j<(int)list[0]; j++){
						if (om.get(utiRanking[j]) == 1 && i != j){
							ArrayList<Integer> tmpkmn = new ArrayList<Integer>();
							for (int g=0; g<(int)list[0]; g++)
								for (int h=0; h<(int)list[1]; h++)
									tmpkmn.add(kmn.get(g*(int)list[1]+h));
							nextConsolidation = processOfUniting(utiRanking, i, j, utiRanking[i], utiRanking[j], tmpkmn);						//call the function to do consolidation, which return a value indicating whether consolidation can be performed
							if (nextConsolidation == true){						//if succeed, perform consolidation and set nextConsolidation = 1 and break the loop
								boolean consolidatedSuccessfully;
								//System.out.println("Print-out test.");
								consolidatedSuccessfully = processOfUniting(utiRanking, i, j, utiRanking[i], utiRanking[j]);
								om.set(utiRanking[i], 0);
/*								for(int z=0; z<(int)list[0]; z++){
									System.out.println("PM "+z);
									for(int a=0; a<(int)list[1]; a++)
										if(kmn.get(z*(int)list[1]+a) == 1)
											System.out.print(a+", ");
									System.out.println("");
								}
								for (int b:utiRanking)
									System.out.println(b);
								System.out.println(om);
								System.out.println(totalCPU);
								System.out.println(totalMEM);
								System.out.println(totalNET);*/
								for (int y=0; y<(int)list[1]; y++)
									if (kmn.get(utiRanking[i]*(int)list[1]+y) == 1)
										System.out.println("Decision Variables Conflict! Program Stops!");
								if (consolidatedSuccessfully != true){
									System.out.println("Consolidation Procedure didn't Perform as Expected! Program Stops!");
									System.exit(0);
								}
								break;
							}
							//else set nextConsolidation = 0
						}
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
		final long startTime = System.currentTimeMillis();
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
			System.out.println("CPU Utilization Constraint cannot be Satisfied.");
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
			//System.exit(0);
		}
		else if (mViolation != 0){
			System.out.println("MEM Utilization Constraint cannot be Satisfied.");
			System.out.println(totalMEM);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Umem.get(i*(int)list[1]+j));
			}
			//System.exit(0);
		}
		else if (nViolation != 0){
			System.out.println("NET Utilization Constraint cannot be Satisfied.");
			System.out.println(totalNET);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Unet.get(i*(int)list[1]+j));
			}
			//System.exit(0);
		}
		else{
			System.out.println("No Utilization Constraints Violated. Program Proceeds to the Energy-saving Process.");
		}
		final long endTime = System.currentTimeMillis();
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
						System.out.println(Pm.get(i*(int)list[1]+j)+", "+list[2]);
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
			System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();
			System.out.println("Before the energy-saving porcedure, the energy consumption is "+totEnergy+" J.");

		//}
		System.out.println("The solution before the energy-saving procedure:");
		System.out.println(kmn);
		for(int i=0; i<(int)list[0]; i++){
			System.out.println("PM "+i);
			for(int j=0; j<(int)list[1]; j++)
				if(kmn.get(i*(int)list[1]+j) == 1)
					System.out.print(j+", ");
			System.out.println("");
		}
		System.out.println(totalCPU);
		System.out.println(totalMEM);
		System.out.println(totalNET);
	//Energy-saving Procedure Goes from here.
		consolidation();
		System.out.println();
		System.out.println("After the energy-saving porcedure, the energy consumption is "+totEnergy+" J.");
		
/*		System.out.println("The initial placement:");
		for(int i=0; i<(int)list[0]; i++){
			System.out.println("PM "+i);
			for(int j=0; j<(int)list[1]; j++)
				if(lmn.get(i*(int)list[1]+j) == 1)
					System.out.print(j+", ");
			System.out.println("");
		}*/
		//System.out.println(lmn);
		System.out.println("The solution after the energy-saving procedure:");
		System.out.println(kmn);
		for(int i=0; i<(int)list[0]; i++){
			System.out.println("PM "+i);
			for(int j=0; j<(int)list[1]; j++)
				if(kmn.get(i*(int)list[1]+j) == 1)
					System.out.print(j+", ");
			System.out.println("");
		}
		System.out.println(totalCPU);
		System.out.println(totalMEM);
		System.out.println(totalNET);
/*		double energy = 0;
		for (int i=0; i<(int)list[0]; i++){
			if (om.get(i)==1){
				energy+=Pactive.get(i)*list[2];
			}
			else{
				energy+=Psleep.get(i)*list[2];
			}
		}
		for (int i=0; i<(int)list[0]; i++){
			for (int j=0; j<(int)list[1]; j++)
				if (kmn.get(i*(int)list[1]+j)==1){
					energy+=Pm.get(i*(int)list[1]+j)*list[2];
				}
		}*/
		
/*		for (int i=0; i<(int)list[0]; i++){
			for (int j=0; j<(int)list[1]; j++)
				if (gmn.get(i*(int)list[1]+j)==1){
					energy+=Pmigrate.get(i*(int)list[1]+j)*T.get(i*(int)list[1]+j);
				}
		}
*///		System.out.println(energy);
		System.out.println();
		System.out.println("The initial placement:");
		System.out.println(lmn);
		for(int i=0; i<(int)list[0]; i++){
			System.out.println("PM "+i);
			for(int j=0; j<(int)list[1]; j++)
				if(lmn.get(i*(int)list[1]+j) == 1)
					System.out.print(j+", ");
			System.out.println("");
		}
		
		ArrayList<Integer> PMmode = new ArrayList<Integer>();
		for (int i=0; i<(int)list[0]; i++){
			PMmode.add(i, 0);
			for (int j=0; j<(int)list[1]; j++)
				if(lmn.get(i*(int)list[1]+j) == 1){
					PMmode.set(i, 1);
					break;
				}
		}
		double energy = 0;
		for (int i=0; i<(int)list[0]; i++){
			if (PMmode.get(i)==1){
				energy+=Pactive.get(i)*list[2];
			}
			else{
				energy+=Psleep.get(i)*list[2];
			}
		}
		for (int i=0; i<(int)list[0]; i++){
			for (int j=0; j<(int)list[1]; j++)
				if (lmn.get(i*(int)list[1]+j)==1){
					energy+=Pm.get(i*(int)list[1]+j)*list[2];
				}
		}
		ArrayList<Double> PMCPU = new ArrayList<Double>();
		ArrayList<Double> PMMEM = new ArrayList<Double>();
		ArrayList<Double> PMNET = new ArrayList<Double>();
		for (int i=0; i<(int)list[0]; i++){
			double tempCPU = 0.0, tempMEM = 0.0, tempNET = 0.0;
			for (int j=0; j<(int)list[1]; j++)
				if (lmn.get(i*(int)list[1]+j) == 1){
					tempCPU += Ucpu.get(i*(int)list[1]+j);
					tempMEM += Umem.get(i*(int)list[1]+j);
					tempNET += Unet.get(i*(int)list[1]+j);
					//disk[i] += Udisk.get(i*(int)list[1]+j);
				}
			PMCPU.add(i, tempCPU);
			PMMEM.add(i, tempMEM);
			PMNET.add(i, tempNET);
		}
		System.out.println("After the energy-saving porcedure, the energy consumption is "+energy+" J.");
		System.out.println(PMCPU);
		System.out.println(PMMEM);
		System.out.println(PMNET);
		System.out.println();
		System.out.println("Total execution time: " + (endTime - startTime) + " milliseconds");
		if (cViolation != 0){
			System.out.println("CPU Utilization Constraint cannot be Satisfied.");
			/*System.out.println(totalCPU);
			System.out.println(totalMEM);
			System.out.println(totalNET);
			System.out.println(kmn);
			System.out.println(lmn);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Ucpu.get(i*(int)list[1]+j)+", "+Umem.get(i*(int)list[1]+j)+", "+Unet.get(i*(int)list[1]+j));
			}*/
		}
		if (mViolation != 0){
			System.out.println("MEM Utilization Constraint cannot be Satisfied.");
			/*System.out.println(totalMEM);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Umem.get(i*(int)list[1]+j));
			}*/
			//System.exit(0);
		}
		if (nViolation != 0){
			System.out.println("NET Utilization Constraint cannot be Satisfied.");
			/*System.out.println(totalNET);
			for(int i=0; i<(int)list[0]; i++){
				System.out.println("PM "+i);
				for(int j=0; j<(int)list[1]; j++)
					if(kmn.get(i*(int)list[1]+j) == 1)
						System.out.println(Unet.get(i*(int)list[1]+j));
			}*/
			//System.exit(0);
		}
	return kmn;
	}
}
