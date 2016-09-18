package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Conf;
import util.RDao;


public class Command {
	private static final Logger LOG = LogManager.getLogger(Command.class);
	public static String VERSION = "0.1";	
	//agjobs        : AutoGent.bash Autogent.command.conf        //query current user's running job & 1 week
	//agjobs  -id jobid : list of current host status
	//agjobs  -u all: AutoGent.bash Autogent.command.conf -u all //query current all running job
	public static long jobid=0;
	public static String user=null;
	static void checkArgs(String[] args){
		if (args.length<1){
			printUsage();
		}
		try{
			for (int i =0; i<args.length ;i++){
				if (args[i].matches("-id")){
					try {
						jobid=Long.parseLong(args[++i]);
					}catch(NumberFormatException e){
						System.out.println("ID:"+args+" must be number.");
						System.exit(0);
					}
				}
				if (args[i].matches("-u")){
					user=args[++i];
				}
			}
		}catch(ArrayIndexOutOfBoundsException e){
			
			System.out.println("please check your args");
			printUsage();
			System.exit(0);
		}
	}
	static void printUsage(){
		System.out.println("agjobs - autogent job list\n"
				+ "SYNOPSIS\n"
				+ "    agcmd [OPTION]... [USER]...\n"
				+ "DESCRIPTION\n"
				+ "    List  information  about  the AutoGent submiteed job\n"
				+ "    Mandatory arguments to long options are  mandatory  for  short  options too.\n"
				+ "    -u [user_name|all]\n"
				+ "        job list of specific user\n" 
				+ "    -id [job_id]\n"
				+ "        job_list of specific job_id\n");
	}
	@SuppressWarnings("null")
	public static void main(String[] args) {
		
		checkArgs(args);
		Command cmd=new Command();
		
		Conf cf = new Conf();
		cf.setConfFile(args[0]);
		//String CMD=args[1];
		
		String userName=System.getProperty("user.name");
		
		RDao rDao=new RDao();
		rDao.setRdbPasswd(cf.getSingleString("password"));
		rDao.setRdbUrlNdbType(cf.getDbURL());
		rDao.setRdbUser(cf.getSingleString("user"));
		Connection con=rDao.getConnection();
		if (user==null){
			user=userName;
		}
		if (jobid==0){
			ArrayList <String> jobList= rDao.getTotalJobList(con,user);
			boolean head=true;
			for (String jobStr:jobList){
				if (head==true){
					System.out.println("JOBID\tUSER_NAME\tSTATUS\tCOMMAND");
					head=false;
				}
				System.out.println(jobStr);
			}
		}else{
			ArrayList <String> subJobList= rDao.getSubJobList(con,jobid);
			boolean head=true;
			for (String subStr:subJobList){
				if (head==true){
					System.out.println("JOBID\tHOSTNAME\tSTATUS\tSTART_TIME\tEND_TIME\tRESULT");
					head=false;
				}
				System.out.println(subStr);
			}
		}
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}