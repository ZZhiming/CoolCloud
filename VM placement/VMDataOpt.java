

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

// the OptVM class reads performance data from each VM (1 to 20), and the ILP model is use to get the optimal VM placement
// this class will also print the CPU,mem, network utilization to file named VMdata.txt 

public class VMDataOpt {

   private static class TrustAllTrustManager implements
         javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      @Override
      public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType)
            throws java.security.cert.CertificateException {
         return;
      }

      @Override
      public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType)
            throws java.security.cert.CertificateException {
         return;
      }
   }
   
	
   
	/************************************************************************************/
	//variables, notations for ILP optimization
   
	public static Map<Integer, String> vm= new HashMap<Integer,String>();
	public static Map<Integer, String> host=new HashMap<Integer, String>();
	//public static int m2=1;
	public static double [] tu=new double [25];
	public static int [][] tmn= new int [5][25];
	
	public static int M=4; // total number of PM or hosts
	public static int N=20; // total number of vms.
	
	
    public static int period=600;
	// period in seconds, interval between two migration decision calculations
	
	public static int Pactive = 60;
	// when physical machine is in active state, base power consumption in watts
	
	public static int Psleep = 10;
	//when physical machine in sleep state
	
	public static int [] Pm = new int[25];
	//power consumption for each virtual machine
	
	public static int [] Pmigrate= new int[25];
	//power consumption for migrating each virtual machine
	
	public static int [] T = new int [25];
	//time consumption for each vm migration
	
	public static int [][] lmn= new int[5][25];
	//VM initial placement lm[M][N]  VM n on PM m;
	
	public static double [] Ucpu =new double[25];
	// host cpu utilization from 
	
	public static double [] Umem = new double[25];
	// memory utilization
	
	public static double [] Unet=new double[25];
	//network bandwidth utilization
	
	public static double [] Udisk = new double[25];
	//hard disk utilization
    
	public static double Hcpu =0.75;
	public static double Hmem=0.75;
	public static double Hnet =0.75;
	public static double Hdisk=0.75;
	
	
	// end of notations for ILP optimization
   /*************************************************************************/

   private static final ManagedObjectReference SVC_INST_REF =
         new ManagedObjectReference();
   private static final String SVC_INST_NAME = "ServiceInstance";

   private static ManagedObjectReference propCollectorRef;
   private static VimService vimService;
   private static VimPortType vimPort;
   private static ServiceContent serviceContent;
   private static Boolean isConnected = false;

   public  static File file;
   public static FileWriter filewriter;
   public static BufferedWriter bufferedwriter;
   public static int c=1; // global counter in display value
   public static int m=1; // global counter in display() and collectruntime()
   
   private static String url;
   private static String userName;
   private static String password;
   private static boolean help = false;
   private static ManagedObjectReference perfManager;
   private static String virtualmachinename;

   private static void trustAllHttpsCertificates() throws Exception {
      // Create a trust manager that does not validate certificate chains:
      javax.net.ssl.TrustManager[] trustAllCerts =
            new javax.net.ssl.TrustManager[1];
      javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
      trustAllCerts[0] = tm;
      javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
      javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
      sslsc.setSessionTimeout(0);
      sc.init(null, trustAllCerts, null);
      javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
            .getSocketFactory());
   }

   // get common parameters
   private static void getConnectionParameters(String[] args)
         throws IllegalArgumentException {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--help")) {
            help = true;
            break;
         } else if (param.equalsIgnoreCase("--url") && !val.startsWith("--")
               && !val.isEmpty()) {
            url = val;
         } else if (param.equalsIgnoreCase("--username")
               && !val.startsWith("--") && !val.isEmpty()) {
            userName = val;
         } else if (param.equalsIgnoreCase("--password")
               && !val.startsWith("--") && !val.isEmpty()) {
            password = val;
         }
         val = "";
         ai += 2;
      }
      if (url == null || userName == null || password == null) {
         throw new IllegalArgumentException(
               "Expected --url, --username, --password arguments.");
      }
   }

   // get input parameters if vcenter and vm names are typed in by user
   private static void getInputParameters(String[] args) {
      int ai = 0;
      String param = "";
      String val = "";
      while (ai < args.length) {
         param = args[ai].trim();
         if (ai + 1 < args.length) {
            val = args[ai + 1].trim();
         }
         if (param.equalsIgnoreCase("--vmname") && !val.startsWith("--")
               && !val.isEmpty()) {
            virtualmachinename = val;
         }
         val = "";
         ai += 2;
      }
      if (virtualmachinename == null) {
         throw new IllegalArgumentException("Expected --vmname argument.");
      }
   }

   
   private static void inputs(){
	   url= "https://10.24.102.170/sdk";
	   userName ="zhang\\administrator";
	   password= "Yang@110"; 
   }
  
   // setup connection with servers
   private static void connect() throws Exception {

      HostnameVerifier hv = new HostnameVerifier() {
         @Override
         public boolean verify(String urlHostName, SSLSession session) {
            return true;
         }
      };
      trustAllHttpsCertificates();
      HttpsURLConnection.setDefaultHostnameVerifier(hv);

      SVC_INST_REF.setType(SVC_INST_NAME);
      SVC_INST_REF.setValue(SVC_INST_NAME);

      vimService = new VimService();
      vimPort = vimService.getVimPort();
      Map<String, Object> ctxt =
            ((BindingProvider) vimPort).getRequestContext();

      ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
      ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

      serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
      vimPort.login(serviceContent.getSessionManager(), userName, password,
            null);
      isConnected = true;

      propCollectorRef = serviceContent.getPropertyCollector();
      perfManager = serviceContent.getPerfManager();
   }

  
   private static void disconnect() throws Exception {
      if (isConnected) {
         vimPort.logout(serviceContent.getSessionManager());
      }
      isConnected = false;
   }

  
         //get the properties of servers and vms
   private static List<ObjectContent> retrievePropertiesAllObjects(
         List<PropertyFilterSpec> listpfs) throws Exception {

      RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

      List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

      try {
         RetrieveResult rslts =
               vimPort.retrievePropertiesEx(propCollectorRef, listpfs,
                     propObjectRetrieveOpts);
         if (rslts != null && rslts.getObjects() != null
               && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
         }
         String token = null;
         if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
         }
         while (token != null && !token.isEmpty()) {
            rslts =
                  vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null) {
               token = rslts.getToken();
               if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                  listobjcontent.addAll(rslts.getObjects());
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         System.out.println(" : Failed Getting Contents");
         e.printStackTrace();
      }

      return listobjcontent;
   }

   private static void state(){
		// Virtual machines are labeled from 1 to 20; Hosts are labeled from 1 to 4;
	    // initial placement state
    lmn[1][1]=1; 
	lmn[1][2]=1;
	lmn[1][8]=1;
	lmn[1][15]=1;
	lmn[1][14]=1;
	lmn[1][16]=1;
	
	lmn[2][5]=1;
	lmn[2][9]=1;
	lmn[2][17]=1;
	
	lmn[3][3]=1;
	lmn[3][4]=1;
	lmn[3][6]=1;
	lmn[3][7]=1;
	lmn[3][11]=1;

	lmn[4][10]=1;
	lmn[4][18]=1;
	lmn[4][19]=1;
	lmn[4][20]=1;
	lmn[4][12]=1;
	lmn[4][13]=1;
	   
	// for now, i keep the Udisk same, since all vms share iscsi datastore, for now no datastore migration
   Udisk[1]=0.07;
   Udisk[2]=0.11;
   Udisk[2]=0.08;
   Udisk[3]=0.12;
   Udisk[4]=0.14;
   Udisk[5]=0.09;
   Udisk[6]=0.1;
   Udisk[7]=0.13;
   Udisk[8]=0.1;
   Udisk[9]=0.07;
   Udisk[10]=0.09;
   Udisk[11]=0.14;
   Udisk[12]=0.11;
   Udisk[13]=0.1;
   Udisk[14]=0.1;
   Udisk[15]=0.1;
   Udisk[16]=0.12;
   Udisk[17]=0.12;
   Udisk[18]=0.14;
   Udisk[19]=0.12;
   Udisk[20]=0.11;
   
   }
   
   private static void powerTwrite(){
	   try{
		   for(int ik=1; ik<21;ik++){
	
	           bufferedwriter.write("Ucpu["+ik+"]=" + Ucpu[ik]+"\n");
	           bufferedwriter.newLine();
	           bufferedwriter.flush();}
		   bufferedwriter.newLine();
	       bufferedwriter.flush();   
		 
	       for(int ik=1; ik<21;ik++){
	    		
	           bufferedwriter.write("Umem["+ik+"]=" + Umem[ik]+"\n");
	           bufferedwriter.newLine();
	           bufferedwriter.flush();}
		   bufferedwriter.newLine();
	       bufferedwriter.flush();  
	       
	       for(int ik=1; ik<21;ik++){
	    		
	           bufferedwriter.write("Unet["+ik+"]=" + Unet[ik]+"\n");
	           bufferedwriter.newLine();
	           bufferedwriter.flush();}
		   bufferedwriter.newLine();
	       bufferedwriter.flush();  
		   
	   for(int ik=1; ik<21;ik++){
		    
           Pm[ik]=(int) (Ucpu[ik]*100+7);	 
  	 //System.out.println("Pm["+ik+"] = "+(int)Pm[ik]);}
           bufferedwriter.write("Pm["+ik+"]=" + Pm[ik]+"\n");
           bufferedwriter.newLine();
           bufferedwriter.flush();}
	   bufferedwriter.newLine();
       bufferedwriter.flush();
	   
	   for(int ik=1;ik<21;ik++){
           Pmigrate[ik]=(int) (Ucpu[ik]*100/1.8+3);
           bufferedwriter.write("Pmigrate["+ik+"]=" + Pmigrate[ik]+"\n");
           bufferedwriter.newLine();
           bufferedwriter.flush();
	   }
	   
	   bufferedwriter.newLine();
       bufferedwriter.flush();
	   
	   for(int ik=1;ik<21;ik++){
		   T[ik]=(int)(Umem[ik]*100+5);
           bufferedwriter.write("T["+ik+"]=" + T[ik]+"\n");
           bufferedwriter.newLine();
           bufferedwriter.flush();
	   }
	   
	   
	   }catch(Exception e){
		   
	   }
	   
   }
   
   
   public static void optimize(){
	    populate Modeler = new populate(N, M); 
		//gvm cpu
		Modeler.ModelConstructor(N, M, lmn, Ucpu, Umem, Udisk, Unet, Hcpu, Hmem, Hdisk, Hnet, Pm, period, Pmigrate, T, Pactive, Psleep);
		int[][] Lmn = new int[M+1][N+1];
		int[][] Gmn = new int[M+1][N+1];
		int[] Om = new int[M+1];
		Modeler.getResults(Lmn,Gmn,Om);
		
		System.out.println("traverse Gmn:");
		for(int [] item: Gmn){
			System.out.println(Arrays.toString(item));
		}
		  
		// this small section calculates the standard deviation of cpu utilization to compare with VMware DRS
		double [] cpuo=new double[5];
		for(int i=1;i<5;i++)
		  for(int j=1;j<21;j++)
		     { if (lmn[i][j]!=0)
		    	    cpuo[i]=cpuo[i]+Ucpu[j];}
		
		System.out.println(cpuo[1]+" "+cpuo[2]+" "+cpuo[3]+" "+cpuo[4]);
		System.out.println("Standard deviation is:"+sd(cpuo[1],cpuo[2],cpuo[3],cpuo[4]));
		
		System.out.println("traverse Lmn:");
		for(int [] item: Lmn){
			System.out.println(Arrays.toString(item));
		}
		
		System.out.println("traverse lmn:");
		for(int [] item: lmn){
			System.out.println(Arrays.toString(item));
		}
		
		for(int i=1; i<=M; i++)
			for(int j=1; j<=N; j++)
				System.out.println("Variable l" + i + "_" + j + ": Value = " + Lmn[i][j]);
		for(int i=1; i<=M; i++)
			for(int j=1; j<=N; j++)
				System.out.println("Variable g" + i + "_" + j + ": Value = " + Gmn[i][j]);
		for(int i=1; i<=M; i++)
			System.out.println("Variable o" + i + ": Value = " + Om[i]);
		
   }
   
	// calculate the standard deviation across the four physical servers
	public static double sd(double s1, double s2, double s3, double s4){
		double sd, av ;
		av = (s1+s2+s3+s4)/4;
		s1=Math.abs(av-s1);
		s2=Math.abs(av-s2);
		s3=Math.abs(av-s3);
		s4=Math.abs(av-s4);
		av=(s1*s1+s2*s2+s3*s3+s4*s4)/4;
		sd=Math.sqrt(av);
	
		return sd;
	}
   
        // dispay performance collected of each vm to logcat and print to VMdata.txt
   private static void displayValues(List<PerfEntityMetricBase> values,
         Map<Integer, PerfCounterInfo> counters) {
            
	        double li;
      for (int i = 0; i < values.size(); ++i) {
         List<PerfMetricSeries> listpems =
               ((PerfEntityMetric) values.get(i)).getValue();
         List<PerfSampleInfo> listinfo =
               ((PerfEntityMetric) values.get(i)).getSampleInfo();

         System.out.println("Sample time range: "
               + listinfo.get(0).getTimestamp().toString() + " - "
               + listinfo.get(listinfo.size() - 1).getTimestamp().toString());
         for (int vi = 0; vi < listpems.size(); ++vi) {
            PerfCounterInfo pci =
                  counters.get(new Integer(listpems.get(vi).getId()
                        .getCounterId()));
            if (pci != null) {
               System.out.println(pci.getNameInfo().getSummary());
            }
            if (listpems.get(vi) instanceof PerfMetricIntSeries) {
               PerfMetricIntSeries val = (PerfMetricIntSeries) listpems.get(vi);
               List<Long> lislon = val.getValue();
             //  for (Long k : lislon) {
                  //System.out.print(k + " ");
               if(vi==0){  
               System.out.print((double)lislon.get(9)/100);
             //  }
                  
                  li=(double)lislon.get(9)/100;
                  
                  String s =lislon.get(9).toString();
           	  
              //}
           	   try{
           	  switch(m){
           	  case 1: Ucpu[c]=li/100;
           	           break;
           	  case 2: Umem[c]=li/100;
           	           break;
           	  case 3: Unet[c]=li;
           	           break;
           	  default: break;         
           	  }
           	          	  
             //b bufferedwriter.write(li+"\n");
              //b bufferedwriter.newLine();
              c++;
              if(c==21)
            	{//b bufferedwriter.newLine();
            	 c=1;
            	 }
              //b bufferedwriter.flush();
           	   }
           	   catch(Exception e){
           	   
              }
            
               System.out.println();
               }
            }
         }
      }
   }

  
   // initialize performance counters, hashmap used and use key to specify which counter you need to use
   private static List<PerfCounterInfo> getPerfCounters() {
      List<PerfCounterInfo> pciArr = null;

      try {
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("perfCounter");
         propertySpec.setType("PerformanceManager");
         List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
         propertySpecs.add(propertySpec);

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(perfManager);
         List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
         objectSpecs.add(objectSpec);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);

         List<PropertyFilterSpec> propertyFilterSpecs =
               new ArrayList<PropertyFilterSpec>();
         propertyFilterSpecs.add(propertyFilterSpec);

         List<PropertyFilterSpec> listpfs =
               new ArrayList<PropertyFilterSpec>(1);
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont =
               retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     List<PerfCounterInfo> pcinfolist =
                           ((ArrayOfPerfCounterInfo) dp.getVal())
                                 .getPerfCounterInfo();
                     pciArr = pcinfolist;
                  }
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return pciArr;
   }

   private static TraversalSpec getVMTraversalSpec() {
      // Create a traversal spec that starts from the 'root' objects
      // and traverses the inventory tree to get to the VirtualMachines.
      // Build the traversal specs bottoms up

      // Traversal to get to the VM in a VApp
      TraversalSpec vAppToVM = new TraversalSpec();
      vAppToVM.setName("vAppToVM");
      vAppToVM.setType("VirtualApp");
      vAppToVM.setPath("vm");

      // Traversal spec for VApp to VApp
      TraversalSpec vAppToVApp = new TraversalSpec();
      vAppToVApp.setName("vAppToVApp");
      vAppToVApp.setType("VirtualApp");
      vAppToVApp.setPath("resourcePool");
      // SelectionSpec for VApp to VApp recursion
      SelectionSpec vAppRecursion = new SelectionSpec();
      vAppRecursion.setName("vAppToVApp");
      // SelectionSpec to get to a VM in the VApp
      SelectionSpec vmInVApp = new SelectionSpec();
      vmInVApp.setName("vAppToVM");
      // SelectionSpec for both VApp to VApp and VApp to VM
      List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
      vAppToVMSS.add(vAppRecursion);
      vAppToVMSS.add(vmInVApp);
      vAppToVApp.getSelectSet().addAll(vAppToVMSS);

      // This SelectionSpec is used for recursion for Folder recursion
      SelectionSpec sSpec = new SelectionSpec();
      sSpec.setName("VisitFolders");

      // Traversal to get to the vmFolder from DataCenter
      TraversalSpec dataCenterToVMFolder = new TraversalSpec();
      dataCenterToVMFolder.setName("DataCenterToVMFolder");
      dataCenterToVMFolder.setType("Datacenter");
      dataCenterToVMFolder.setPath("vmFolder");
      dataCenterToVMFolder.setSkip(false);
      dataCenterToVMFolder.getSelectSet().add(sSpec);

      // TraversalSpec to get to the DataCenter from rootFolder
      TraversalSpec traversalSpec = new TraversalSpec();
      traversalSpec.setName("VisitFolders");
      traversalSpec.setType("Folder");
      traversalSpec.setPath("childEntity");
      traversalSpec.setSkip(false);
      List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
      sSpecArr.add(sSpec);
      sSpecArr.add(dataCenterToVMFolder);
      sSpecArr.add(vAppToVM);
      sSpecArr.add(vAppToVApp);
      traversalSpec.getSelectSet().addAll(sSpecArr);
      return traversalSpec;
   }


   private static ManagedObjectReference getVmByVMname(String vmName) {
      ManagedObjectReference retVal = null;
      ManagedObjectReference rootFolder = serviceContent.getRootFolder();
      try {
         TraversalSpec tSpec = getVMTraversalSpec();
         // Create Property Spec
         PropertySpec propertySpec = new PropertySpec();
         propertySpec.setAll(Boolean.FALSE);
         propertySpec.getPathSet().add("name");
         propertySpec.setType("VirtualMachine");

         // Now create Object Spec
         ObjectSpec objectSpec = new ObjectSpec();
         objectSpec.setObj(rootFolder);
         objectSpec.setSkip(Boolean.TRUE);
         objectSpec.getSelectSet().add(tSpec);

         // Create PropertyFilterSpec using the PropertySpec and ObjectPec
         // created above.
         PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
         propertyFilterSpec.getPropSet().add(propertySpec);
         propertyFilterSpec.getObjectSet().add(objectSpec);

         List<PropertyFilterSpec> listpfs =
               new ArrayList<PropertyFilterSpec>(1);
         listpfs.add(propertyFilterSpec);
         List<ObjectContent> listobjcont =
               retrievePropertiesAllObjects(listpfs);

         if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
               ManagedObjectReference mr = oc.getObj();
               String vmnm = null;
               List<DynamicProperty> dps = oc.getPropSet();
               if (dps != null) {
                  for (DynamicProperty dp : dps) {
                     vmnm = (String) dp.getVal();
                  }
               }
               if (vmnm != null && vmnm.equals(vmName)) {
                  retVal = mr;
                  break;
               }
            }
         }
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return retVal;
   }

              
   // this method is the core method that collects realtime performance data of each vm, network bandwidth is set to 1000kbps
   private static void collectRuntime() throws Exception {
	   
	   file = new File("C:/Users/isu/Desktop/VMdata.txt");
	    filewriter = new FileWriter(file);
	      bufferedwriter = new BufferedWriter(filewriter);
	       String [] event =new String[4];
	       event[1]="cpu";
	       event[2]="mem";
	       event[3]="net";
	   // for(int i=0; i<21; i++)
	       //  "zzm"+i
	   // p[1]=cpu, p[2]=mem, 
      //ManagedObjectReference vmmor = getVmByVMname(virtualmachinename);
          for( m=1;m<4; m++ ) {
        	      
	      for (int v=1; v<21;v++){
	   ManagedObjectReference vmmor = getVmByVMname("zzm"+v);
       //b bufferedwriter.write("U"+event[m]+"["+v+"]=");
      if (vmmor != null) {
         List<PerfCounterInfo> cInfo = getPerfCounters();
         List<PerfCounterInfo> vmCpuCounters = new ArrayList<PerfCounterInfo>();
         for (int i = 0; i < cInfo.size(); ++i) {
            if (event[m].equalsIgnoreCase(cInfo.get(i).getGroupInfo().getKey())) {
               vmCpuCounters.add(cInfo.get(i));
            }
         }
         Map<Integer, PerfCounterInfo> counters =
               new HashMap<Integer, PerfCounterInfo>();
       
         // list all the available performance events, now i am only using the percent of usage, event #1
         // when using event #2 , a long list of performances, i=3
         /*  while (true) {
            int i = 0;
            for (Iterator<PerfCounterInfo> it = vmCpuCounters.iterator();
                  .hasNext();) {
               PerfCounterInfo pcInfo = it.next();
               System.out.println(++i + " - "
                     + pcInfo.getNameInfo().getSummary());
            }
            System.out.println("Please select a counter from"
                  + " the above list" + "\nEnter 0 to end: ");
            BufferedReader reader =
                  new BufferedReader(new InputStreamReader(System.in));
            i = Integer.parseInt(reader.readLine());
            if (i > vmCpuCounters.size()) {
               System.out.println("*** Value out of range!");
            } else {
               --i;
               if (i < 0) {
                  return;
               }
               PerfCounterInfo pcInfo = vmCpuCounters.get(2);
               counters.put(new Integer(pcInfo.getKey()), pcInfo);
               break;
            }
         }*/
         
         PerfCounterInfo pcInfo = vmCpuCounters.get(1);
         counters.put(new Integer(pcInfo.getKey()), pcInfo);
         List<PerfMetricId> listpermeid =
               vimPort.queryAvailablePerfMetric(perfManager, vmmor, null, null,
                     new Integer(20));
         List<PerfMetricId> mMetrics = new ArrayList<PerfMetricId>();
         if (listpermeid != null) {
            for (int index = 0; index < listpermeid.size(); ++index) {
               if (counters.containsKey(new Integer(listpermeid.get(index)
                     .getCounterId()))) {
                  mMetrics.add(listpermeid.get(index));
               }
            }
         }
         monitorPerformance(perfManager, vmmor, mMetrics, counters);
      } else {
         System.out.println("Virtual Machine " + virtualmachinename
               + " not found");
      }
     
      }
          }
   }


   private static void monitorPerformance(ManagedObjectReference pmRef,
         ManagedObjectReference vmRef, List<PerfMetricId> mMetrics,
         Map<Integer, PerfCounterInfo> counters) throws Exception {
      PerfQuerySpec qSpec = new PerfQuerySpec();
      qSpec.setEntity(vmRef);
      qSpec.setMaxSample(new Integer(10));
      qSpec.getMetricId().addAll(mMetrics);
      qSpec.setIntervalId(new Integer(20));

      List<PerfQuerySpec> qSpecs = new ArrayList<PerfQuerySpec>();
      qSpecs.add(qSpec);
     // while (true) {
         List<PerfEntityMetricBase> listpemb = vimPort.queryPerf(pmRef, qSpecs);
         List<PerfEntityMetricBase> pValues = listpemb;
         if (pValues != null) {
            displayValues(pValues, counters);
         }
         System.out.println("Sleeping 50 milli-seconds...");
         Thread.sleep(50);
         //Thread.sleep(20 * 1000);
     // }
   }
   

   private static void printSoapFaultException(SOAPFaultException sfe) {
      System.out.println("SOAP Fault -");
      if (sfe.getFault().hasDetail()) {
         System.out.println(sfe.getFault().getDetail().getFirstChild()
               .getLocalName());
      }
      if (sfe.getFault().getFaultString() != null) {
         System.out.println("\n Message: " + sfe.getFault().getFaultString());
      }
   }

 
   public static void main(String[] args) {

      try {
         //getConnectionParameters(args);
         //getInputParameters(args);
    	 inputs();
         connect();
         state();
         collectRuntime();
         powerTwrite();
         optimize();
         
      } catch (IllegalArgumentException e) {
         System.out.println(e.getMessage());
       
      } catch (SOAPFaultException sfe) {
         printSoapFaultException(sfe);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            disconnect(); 
            
         } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
         } catch (Exception e) {
            System.out.println("Failed to disconnect - " + e.getMessage());
            e.printStackTrace();
         }
      }
   }
}
