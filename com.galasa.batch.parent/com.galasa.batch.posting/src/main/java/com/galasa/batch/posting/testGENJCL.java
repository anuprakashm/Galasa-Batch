package com.galasa.batch.posting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobname;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandException;

@Test
public class testGENJCL {
	@ZosImage(imageTag="ZDNT")
	public IZosImage zosImageA;

	@ZosTSOCommand(imageTag="ZDNT")
	public IZosTSOCommand tsoCommand;
	
	@ZosBatch(imageTag="ZDNT")
	public IZosBatch zosBatch;
	
    @ZosBatchJobname(imageTag="ZDNT")
    public IZosBatchJobname zosBatchJobname;

    @BundleResources
    public IBundleResources resources;
    
    @Logger
    public Log              logger;	
	
	@Test
	public void testTSO() throws ZosTSOCommandException, ZosBatchException, TestBundleResourceException, IOException {
		String tsoCommandString = "SUBMIT 'ADCDMST.SAMPLE.JCL(GENJCL)'";
		String tsoResponse = tsoCommand.issueCommand(tsoCommandString);
		int indexStart = tsoResponse.indexOf("GENJCL(JOB");
		
		List<IZosBatchJob> jobs = zosBatch.getJobs("GENJCL", "IBMUSER");
		String jobSubstring = tsoResponse.substring(indexStart,indexStart+16);
		
		IZosBatchJob jobSubmitted = null;
		for (IZosBatchJob job : jobs) {
		    jobSubmitted = job;
			if (job.toString().equals(jobSubstring)) {		
				System.out.println(jobSubstring);
				jobSubmitted = job;
				break;
			}
		}
		
		jobSubmitted.waitForJob();	
		assertThat(jobSubmitted.getRetcode()).isEqualTo("CC 0000");
		System.out.println(jobSubmitted.getRetcode());

     	int rc = compareFiles("ADCDMST.SAMPLE.CUSTPERS","ADCDMST.SAMPLE.CUSTPERS.BKUP");
     	assertThat(rc).isEqualTo(0);
     	
     	rc = compareFiles("ADCDMST.SAMPLE.CUSTACCT","ADCDMST.SAMPLE.CUSTACCT.BKUP");
     	assertThat(rc).isEqualTo(0);
     	
     	rc = compareFiles("ADCDMST.SAMPLE.CUSTCARD","ADCDMST.SAMPLE.CUSTCARD.BKUP");
     	assertThat(rc).isEqualTo(0);
     	
     	rc = compareFiles("ADCDMST.SAMPLE.CONVRATE","ADCDMST.SAMPLE.CONVRATE.BKUP");
     	assertThat(rc).isEqualTo(0);
     	
     	rc = compareFiles("ADCDMST.SAMPLE.SEQNFILE","ADCDMST.SAMPLE.SEQNFILE.BKUP");
     	assertThat(rc).isEqualTo(0);

     	rc = compareFiles("ADCDMST.SAMPLE.CUSTTRAN","ADCDMST.SAMPLE.CUSTTRAN.BKUP");
     	assertThat(rc).isEqualTo(0);
     		
	}
	
	public int compareFiles(String actualFile, String expectedFile) throws TestBundleResourceException, IOException, ZosBatchException {
    	// Create the substitution parameters for the JCL
    	HashMap<String, Object> parameters = new HashMap<>();		
        parameters.put("ACTUAL-FILE", actualFile);
        parameters.put("EXPECTED-FILE", expectedFile);
        
        // Load the JCL with the given substitution parameters
     	String jcl = resources.retrieveSkeletonFileAsString("/resources/skeletons/COMPARE.jcl", parameters);
     	     		
     	// Submit the JCL
     	IZosBatchJob batchJob = zosBatch.submitJob(jcl, zosBatchJobname);
     		
     	// Wait for the batch job to complete
     	logger.info("batchJob.toString() = " +  batchJob.toString());
     	int rc = batchJob.waitForJob();
     	
     	if (rc != 0) {
         	batchJob.retrieveOutput().forEach(jobOutput ->
         	{ 
         	   if (jobOutput.getDdname().equals("SYSPRINT")) {
         		   int logIndex = jobOutput.getRecords().indexOf("Comparison summary:");
         		   logger.error("Output not as expected in: " + actualFile);
         		   logger.error("Expected data in: " + expectedFile);
    		       logger.error("batchJob.retrieveOutput(): " + jobOutput.getDdname() + "\n" + jobOutput.getRecords().substring(logIndex) + "\n");
         	   }
         	});
     	} else {
  		   logger.info("Output as expected in: " + actualFile);
     	}
     	
     	return(rc);		
	}
}