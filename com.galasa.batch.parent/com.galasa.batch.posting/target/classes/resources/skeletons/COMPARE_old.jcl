//COMPARE JOB (ACCT#),                  
//             IBMUSER,MSGCLASS=A,      
//             NOTIFY=IBMUSER,CLASS=A,  
//             MSGLEVEL=(1,1)           
//*                                     
//FILEMGR  EXEC PGM=FMNMAIN
//STEPLIB  DD DISP=SHR,DSN=FMNE10.SFMNMODA
//*FMNCOB   DD DUMMY    Uncomment to force use of FM COBOL Compiler
//*FMNCLERR DD SYSOUT=* Uncomment to force output of Compiler listing
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//FMNTSPRT DD SYSOUT=*
//SYSTERM  DD SYSOUT=*
//SYSIN    DD *
$$FILEM SET HEADERPG=YES,PAGESIZE=60,PRTTRANS=ON
$$FILEM SET CCSID=00037
$$FILEM DSM TYPE=RECORD,
$$FILEM PACK=UNPACK,
$$FILEM SYNCH=ONETOONE,
$$FILEM LIST=SUMMARY,
$$FILEM IGNLEN=NO,
$$FILEM NUMDIFF=ALL,
$$FILEM DSNOLD=++ACTUAL-FILE++,
$$FILEM SKIPOLD=0,
$$FILEM CMPOLD=ALL,
$$FILEM SKIPNEW=0,
$$FILEM CMPNEW=ALL,
$$FILEM DSNNEW=++EXPECTED-FILE++
/*