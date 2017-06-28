import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class JMOAnalyzer 
{
	static FileReader jmoExtractBase=null;
	static FileReader jmoT2File=null;
	
	static Logger logger = Logger.getRootLogger();
	static String baseFolder="D:\\JPMC-JMO\\";
	static String baseReportsFolder=baseFolder+"Reports\\";
	static Map<String, List<String>> predecessorMap = new LinkedHashMap<String, List<String>>(); 
	// This structure contains the job and its predecessors
	
	static String jobPredDefString="DEFINE JOBPRED ID=";
	static String jobsetPredDefString="DEFINE JOBSETPRED ID=";
	static String triggerDefString="DEFINE TRIGGER ID=";
	static String jobDefString="DEFINE JOB ID=";
	static String jobsetDefString="DEFINE JOBSET ID=";
	
	
	public static void main(String[] args) throws IOException 
	{
		Date now = new Date();
		String log4jLocation = "resources/log4j.properties";
		PropertyConfigurator.configure(log4jLocation);
		logger=Logger.getLogger("JMOAnalyzer.JMOExtractMain");
		System.out.println("Enter the full path to the JMO Extract");
		//String jmoExtractFile=System.console().readLine();
		logger.debug("Reading JMO Extract : "+args[0]);
		JMOAnalyzer jmoAnalyzer = new JMOAnalyzer();
		jmoAnalyzer.readJMOExtract(args[0]);
	}
	
	public void splitJMOFile(String jmoExtract)
	{
		
	}
	public void readJMOExtract(String jmoExtract) throws IOException
	{
		List<String> jobPredecessorList = new ArrayList<String>();
		List<String> jobsetPredecessorList=new ArrayList<String>();
		String currentJob=null;
		String predecessorJobset=null;
		String predecessorJob=null;
		String predecessorJobNumber=null;
		String predecessorTrigger=null;
		String predecessorTriggerType=null;
		
		String currentJobset=null;
		String jspredecessorJobset=null;
		String jspredecessorJob=null;
		String jspredecessorJobNumber=null;
		String jspredecessorTrigger=null;
		String jspredecessorTriggerType=null;
		
		logger=Logger.getLogger("JMOAnalyzer.readJMOExtract");
		jmoExtractBase = new FileReader(jmoExtract);
		BufferedReader baseFileReader = new BufferedReader(jmoExtractBase);
		String currentFileContent=null;
		while((currentFileContent=baseFileReader.readLine())!=null)
		{
			String currentLine=currentFileContent.trim();
			if(currentLine.contains(jobPredDefString))
			{
				logger.debug("Job Predecessor found");
				logger.debug(currentLine);
				int startIndex=jobPredDefString.length();
				int endIndex=0;
				if(currentLine.contains("PJOB") && (currentLine.contains("PSET")))
				{
					logger.info("Job has another job as a predecessor");
					endIndex=currentLine.indexOf("PJOB");
					currentJob=currentLine.substring(startIndex, endIndex);
					
					startIndex=currentLine.indexOf("PJOB=");
					endIndex=currentLine.indexOf("PSET");
					predecessorJob=currentLine.substring(startIndex+"PJOB=".length(),endIndex).trim();
					
					startIndex=currentLine.indexOf("PSET=");
					endIndex=currentLine.indexOf("PJNO");
					predecessorJobset = currentLine.substring(startIndex+"PSET=".length(),endIndex);
					startIndex=currentLine.indexOf("PJNO=");
					predecessorJobNumber=currentLine.substring(startIndex+"PJNO=".length(),startIndex+"PJNO=".length()+4);
					logger.info("Current Job: "+currentJob);
					logger.info("Predecessor Job : "+predecessorJob);
					logger.info("Predecessor Jobset: "+predecessorJobset);
					logger.info("Predecessor Job Number: "+predecessorJobNumber);
					if(predecessorMap.containsKey(currentJob))
					{
						jobPredecessorList=predecessorMap.get(currentJob);
						jobPredecessorList.add(predecessorJob+"^"+predecessorJobset+"^"+predecessorJobNumber);
						logger.debug(jobPredecessorList);
						logger.debug(predecessorMap);
					}
					else
					{
						jobPredecessorList=new ArrayList<String>();
						jobPredecessorList.add(predecessorJob+"^"+predecessorJobset+"^"+predecessorJobNumber);
						predecessorMap.put(currentJob, jobPredecessorList);
						logger.debug(jobPredecessorList);
						logger.debug(predecessorMap);
					}
				}
				if((currentLine.contains("WORKDAY") && (currentLine.contains("TRID"))))
				{
					logger.info("Job has a trigger as a predecessor");
					logger.info("Parsing Trigger details...");
					endIndex=currentLine.indexOf("WORKDAY");
					currentJob=currentLine.substring(startIndex,endIndex).trim();
					startIndex=currentLine.indexOf("TREV")+"TREV=".length();
					endIndex=currentLine.indexOf("TRID");
					predecessorTriggerType=currentLine.substring(startIndex,endIndex).trim();
					startIndex=currentLine.indexOf("TRID")+"TRID=".length();
					predecessorTrigger=currentLine.substring(startIndex);
					logger.info("Current Job: "+currentJob);
					logger.info("Predecessor Trigger : "+predecessorTrigger);
					logger.info("Predecessor Trigger Type: "+predecessorTriggerType);
					if(predecessorMap.containsKey(currentJob))
					{
						jobPredecessorList=predecessorMap.get(currentJob);
						jobPredecessorList.add(predecessorTrigger+"^"+predecessorTriggerType);
						logger.debug(jobPredecessorList);
						logger.debug(predecessorMap);
					}
					else
					{
						jobPredecessorList=new ArrayList<String>();
						jobPredecessorList.add(predecessorTrigger+"^"+predecessorTriggerType);
						predecessorMap.put(currentJob, jobPredecessorList);
						logger.debug(jobPredecessorList);
						logger.debug(predecessorMap);
					}
					
				}
				if((!currentFileContent.trim().contains("PJNO") && (currentFileContent.trim().contains("PSET"))))
				{
					logger.info("Job has a Jobset as a predecessor");
					logger.debug("Parse jobset info...");
					startIndex=currentLine.indexOf("PSET")+"PSET=".length();
					endIndex=currentLine.indexOf("WORKDAY");
					predecessorJobset=currentLine.substring(startIndex,endIndex).trim();
					logger.info("Current Job: "+currentJob);
					logger.info("Predecessor Jobset: "+predecessorJobset);
					if(predecessorMap.containsKey(currentJob))
					{
						jobPredecessorList=predecessorMap.get(currentJob);
						jobPredecessorList.add(predecessorJobset);
						logger.debug(jobPredecessorList);
						logger.debug(predecessorMap);
					}
					else
					{
						jobPredecessorList=new ArrayList<String>();
						jobPredecessorList.add(predecessorJobset);
						predecessorMap.put(currentJob, jobPredecessorList);
						logger.debug(jobPredecessorList);
						logger.debug(predecessorMap);
					}
				}
				
			}
			if(currentLine.contains(jobsetPredDefString))
			{
				
				
				logger.info("Parsing Jobset Predecessors");
				int jsstartIndex=0;
				int jsendIndex=0;
				jsstartIndex=currentLine.indexOf(jobsetPredDefString)+jobsetPredDefString.length();
				if(currentLine.contains("PJOB") && (currentLine.contains("PSET")))
				{
					logger.info("Jobset has another job as a predecessor");
					jsendIndex=currentLine.indexOf("PJOB");
					currentJobset=currentLine.substring(jsstartIndex, jsendIndex);
					
					jsstartIndex=currentLine.indexOf("PJOB=");
					jsendIndex=currentLine.indexOf("PSET");
					jspredecessorJob=currentLine.substring(jsstartIndex+"PJOB=".length(),jsendIndex).trim();
					
					jsstartIndex=currentLine.indexOf("PSET=");
					jsendIndex=currentLine.indexOf("PJNO");
					jspredecessorJobset = currentLine.substring(jsstartIndex+"PSET=".length(),jsendIndex);
					jsstartIndex=currentLine.indexOf("PJNO=");
					jspredecessorJobNumber=currentLine.substring(jsstartIndex+"PJNO=".length(),jsstartIndex+"PJNO=".length()+4);
					logger.info("Current Job: "+currentJobset);
					logger.info("Predecessor Job : "+jspredecessorJob);
					logger.info("Predecessor Jobset: "+jspredecessorJobset);
					logger.info("Predecessor Job Number: "+jspredecessorJobNumber);
					if(predecessorMap.containsKey(currentJobset))
					{
						jobsetPredecessorList=predecessorMap.get(currentJobset);
						jobsetPredecessorList.add(jspredecessorJob+"^"+jspredecessorJobset+"^"+jspredecessorJobNumber);
						logger.debug(jobsetPredecessorList);
						logger.debug(predecessorMap);
					}
					else
					{
						jobsetPredecessorList=new ArrayList<String>();
						jobsetPredecessorList.add(jspredecessorJob+"^"+jspredecessorJobset+"^"+jspredecessorJobNumber);
						predecessorMap.put(currentJobset, jobsetPredecessorList);
						logger.debug(jobsetPredecessorList);
						logger.debug(predecessorMap);
					}
				}
				if((currentLine.contains("WORKDAY") && (currentLine.contains("TRID"))))
				{
					logger.info("Job has a trigger as a predecessor");
					logger.info("Parsing Trigger details...");
					jsendIndex=currentLine.indexOf("WORKDAY");
					currentJobset=currentLine.substring(jsstartIndex,jsendIndex).trim();
					jsstartIndex=currentLine.indexOf("TREV")+"TREV=".length();
					jsendIndex=currentLine.indexOf("TRID");
					jspredecessorTriggerType=currentLine.substring(jsstartIndex,jsendIndex).trim();
					jsstartIndex=currentLine.indexOf("TRID")+"TRID=".length();
					jspredecessorTrigger=currentLine.substring(jsstartIndex);
					logger.info("Current Job: "+currentJobset);
					logger.info("Predecessor Trigger : "+jspredecessorTrigger);
					logger.info("Predecessor Trigger Type: "+jspredecessorTriggerType);
					if(predecessorMap.containsKey(currentJobset))
					{
						jobsetPredecessorList=predecessorMap.get(currentJobset);
						jobsetPredecessorList.add(predecessorTrigger+"^"+predecessorTriggerType);
						logger.debug(jobsetPredecessorList);
						logger.debug(predecessorMap);
					}
					else
					{
						jobsetPredecessorList=new ArrayList<String>();
						jobsetPredecessorList.add(predecessorTrigger+"^"+predecessorTriggerType);
						predecessorMap.put(currentJobset, jobsetPredecessorList);
						logger.debug(jobsetPredecessorList);
						logger.debug(predecessorMap);
					}
					
				}
				if((!currentFileContent.trim().contains("PJNO") && (currentFileContent.trim().contains("PSET"))))
				{
					logger.info("Job has a Jobset as a predecessor");
					logger.debug("Parse jobset info...");
					jsstartIndex=currentLine.indexOf("PSET")+"PSET=".length();
					jsendIndex=currentLine.indexOf("WORKDAY");
					predecessorJobset=currentLine.substring(jsstartIndex,jsendIndex).trim();
					logger.info("Current Job: "+currentJobset);
					logger.info("Predecessor Jobset: "+predecessorJobset);
					if(predecessorMap.containsKey(currentJobset))
					{
						jobsetPredecessorList=predecessorMap.get(currentJobset);
						jobsetPredecessorList.add(predecessorJobset);
						logger.debug(jobsetPredecessorList);
						logger.debug(predecessorMap);
					}
					else
					{
						jobsetPredecessorList=new ArrayList<String>();
						jobsetPredecessorList.add(predecessorJobset);
						predecessorMap.put(currentJobset, jobsetPredecessorList);
						logger.debug(jobsetPredecessorList);
						logger.debug(predecessorMap);
					}
				}
			}
			
			
			
		}
	}

}
