package org.clas12.analysisTools.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

public class HipoReader {

	/**
	 * List of file (absolute) paths to be read.
	 */
	private List<String> fileList;
	
	/**
	 * Number of the current file being read.
	 * 
	 *	<p> Starts at 0. </p>
	 */
	private int currentFile;
	
	/**
	 * Hipo file being read.
	 */
	private HipoDataSource hipoFile;
	
	/**
	 * Number of the current event of the file being read.
	 * 
	 * <p> Starts at 0. </p>
	 */
	private int currentEvent;
	
	
	
	
	
	/**
	 * @return the fileList
	 */
	public List<String> getFileList() {
		return fileList;
	}

	
	/**
	 * @return the currentFile
	 */
	public int getCurrentFile() {
		return currentFile;
	}

	
	/**
	 * @return the hipoFile
	 */
	public HipoDataSource getHipoFile() {
		return hipoFile;
	}

	
	/**
	 * @return the currentEvent
	 */
	public int getCurrentEvent() {
		return currentEvent;
	}
	

	/**
	 * Create Hipo reader and add with a list of files
	 * 
	 * @param fileList  path of files to analyze
	 */
	public HipoReader(List<String> fileList) {
		this.checkFileList(fileList);
		this.fileList=fileList;
		this.hipoFile = new HipoDataSource();
		this.currentFile=0;
		this.currentEvent=0;
		if (fileList!=null){
			System.out.println("NOT NULL");
		}else{
			System.out.println("NULL");
		}
		System.out.println("File to open: "+fileList.get(currentFile));
		this.hipoFile.open(fileList.get(currentFile));
	}
	
	/**
	 * Create Hipo reader and parse a given directory looking for a given run number.
	 * 
	 * @param directory  directory to parse
	 * @param runNumber  run number to look for (use wildcard * to get all run numbers)
	 */
	public HipoReader(String directory, String runNumber){
		this( HipoReader.getFileListFromPath(directory, runNumber) );
	}
	
	/**
	 * Get a list of all hipo files inside a directory and with a given run number
	 * 
	 * @param directory  directory to parse
	 * @param runNumber  run number to look for (use wildcard * to get all run numbers)
	 * @return a list of hipo files (absolute path)
	 */
	public static List<String> getFileListFromPath(String directory, String runNumber){
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles!=null && listOfFiles.length!=0){
			System.out.println(listOfFiles.length+"FILES FOUND IN DIRECTORY");
		}else{
			System.out.println("NO FILES FOUND");
		}
		
		
		List<String> fileList = new ArrayList<>();
		for (int fileIterator=0 ; fileIterator<listOfFiles.length ; fileIterator++){
			String path = listOfFiles[fileIterator].getAbsolutePath();
			String fileExtension = path.substring(Math.max(0, path.length() - 5));
			if (fileExtension.equals(".hipo") && runNumber.equals("*")){
				fileList.add(path);
			}else if ( fileExtension.equals(".hipo") && path.contains(runNumber) ){
				fileList.add(path);
			}
		}
		System.out.println(fileList.size()+"HIPO FILES FOUND IN DIRECTORY FOR RUN "+runNumber);
		return fileList;
	}
	
	/**
	 * Check if the list is non-null and if the files are .hipo
	 * 
	 * @param pathList  path of files to check
	 */
	public void checkFileList(List<String> pathList){
		if (pathList==null){
			throw new IllegalArgumentException("File list can't be null");
		}
		for (String path: pathList){
			String fileExtension = path.substring(Math.max(0, path.length() - 5));
			if (!fileExtension.equals(".hipo")){
				throw new IllegalArgumentException("Files has to be .hipo");
			}
		}
	}

	/**
	 * Get the next event, from the current file if exists, or from the next file
	 * 
	 * @return the next event or null if event was the last
	 */
	public DataEvent getNextEvent(){
		this.currentEvent ++;
		DataEvent newEvent = null;
		if (this.currentEvent<this.hipoFile.getSize()){
			newEvent = this.hipoFile.getNextEvent();
		} else if (this.currentFile<(this.fileList.size()-1)){
			this.openNextFile();
			this.currentEvent=1;
			newEvent = this.hipoFile.getNextEvent();
		}
		return newEvent;
	}
	
	public void goToEvent(int eventNumber){
		this.hipoFile.gotoEvent(eventNumber);
		this.currentEvent=eventNumber;
	}
	
	public DataEvent getRelativeEvent(int eventDistance){
		
//		DataEvent newEvent = null;
//		
//		int iterateFile = currentFile;
//		int iterateEvent = currentEvent;
//		
//		while (iterateEvent+eventDistance < 1 || iterateEvent+eventDistance>this.hipoFile.getSize()){
//			if (currentEvent+eventDistance > this.hipoFile.getSize()){
//				iterateFile++;
//				HipoDataSource hipoNextFile = new HipoDataSource();
//				if (iterateFile<this.fileList.size()){
//					this.hipoFile.open(this.fileList.get(iterateFile));
//				}else{
//					return null;
//				}
//			}else if (currentEvent+eventDistance<1){
//				
//			}
//		}
//		newEvent = this.hipoFile.gotoEvent(iterateEvent+eventDistance);
//		return newEvent;
		
		DataEvent newEvent = null;
		if (currentEvent+eventDistance >= 1 && currentEvent+eventDistance<this.hipoFile.getSize()){
			newEvent = this.hipoFile.gotoEvent(currentEvent+eventDistance);
		}
		this.hipoFile.gotoEvent(currentEvent);
		return newEvent;
	}
	
	/**
	 * Open the next file if exists
	 */
	public void openNextFile(){
		if (this.fileList == null || this.fileList.isEmpty() || this.hipoFile==null){
			return;
		}
		this.hipoFile.close();
		this.currentFile++;
		if (this.currentFile<this.fileList.size()){
			System.out.println("Opening File :"+this.fileList.get(this.currentFile)+" ("+this.currentFile+"/"+this.fileList.size()+")");
			this.hipoFile.open(this.fileList.get(this.currentFile));
		}else{
			System.out.println("HipoReader: No more files");
		}
	}
	
	/**
	 * Check if the current file has more events or if there are more files
	 * 
	 * @return false is the current event and the current file were the last ones
	 */
	public boolean hasNextEvent(){
		if (this.currentEvent+1>=this.hipoFile.getSize() && this.currentFile+1>=this.fileList.size()){
			System.out.println("HipoReader: No more events and files");
			return false;
		}else{
			return true;
		}
	}
	
	
}
