package myPackage;

import java.io.File;  
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;  
 
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Program for reading in list of check ID's and values from excel and then 
 * finding which checks together add up to a target sum
 * @author Filip Kierzenka filip_kierzenka@brown.edu
 *
 */
public class SnapOnSubset {
	//the 0-indexed column number of the check ID and value, respectively
	final static int ID_COLUMN = 0;
	final static int VALUE_COLUMN = 12;
	
	//the upper and lower bound of subset sums that we will consider - note: unit is cents
	final static int UPPER_BOUND = 100000;
	final static int LOWER_BOUND = -100000;
	
	/*
	 * main method: takes input either formatted as:
	 *  - "::range [excel file path] [first row to consider] [last row to consider] [target sum]"
	 *  - "::all [excel file path] [target sum]"
	 */
	public static void main(String[] args) throws IOException {
		
		
		//mapping of check amount to transaction ID
		HashMap<Integer, ArrayList<String>> valToID = new HashMap<Integer, ArrayList<String>>();

		//path to excel file on computer
		String filePath ="";
		//the first row to consider
		int inputStart = 0;
		//the last row to consider
		int inputEnd = Integer.MAX_VALUE;
		//the dollar amount you want
		double target;
		  
		try {
			//if user chooses to consider only a range of rows
			if(args.length == 5 && args[0].equals("::range")) {
				filePath = args[1];
				inputStart = Integer.valueOf(args[2]) - 1;
				inputEnd = Integer.valueOf(args[3]) - 1;
				target = Double.valueOf(args[4]);
			//if user chooses to consider all rows
			}else if(args.length == 3 && args[0].equals("::all")) {
				filePath = args[1];
				target = Double.valueOf(args[2]);
			//if the input is invalid 
			}else {
				throw new IOException("Invalid input. Either \n- \"::range [filepath] [0-ind. row start] [row end] [target]\"\n-\" ::all [filepath] [target]\"");
			}
			  
			//Accessing the actual excel file and extracting relevant data using Apache POI 
			ArrayList<Integer> values = new ArrayList<Integer>();
			File f = new File(filePath);
			FileInputStream fis = new FileInputStream(f);  
			XSSFWorkbook wb = new XSSFWorkbook(fis);   
			XSSFSheet sheet = wb.getSheetAt(0);  
			  
			int rowStart = Math.max(0, inputStart);
			int rowEnd = Math.min(sheet.getLastRowNum(), inputEnd);
			  
			for(int curRow = rowStart; curRow<=rowEnd; curRow++) {
				//processing each row: making sure it has the right type of data and storing it in our hashmap & array
				Row row = sheet.getRow(curRow);
				if(row != null) {
					Cell idCell = row.getCell(ID_COLUMN);
					Cell valueCell = row.getCell(VALUE_COLUMN);
					if(idCell.getCellType() == Cell.CELL_TYPE_STRING && valueCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						String curID = idCell.getStringCellValue();
						//the DP approach used in this program only works with integer values - mult by 100 to preserve cents
						int curVal = (int) Math.ceil(valueCell.getNumericCellValue()*100.0);
						//math.ceil added b/c of strange rounding problem with Apache...
						if(!valToID.containsKey(curVal)) {
							valToID.put(curVal, new ArrayList<String>());
						}
						valToID.get(curVal).add(curID);
						values.add((int) Math.ceil(curVal));
					}
				}
			}

			//close readers in futile attempt to save memory
			fis.close();
			wb.close();

			//turn check values into array for easier access
			Integer[] n = values.toArray(new Integer[0]);
			
			int seek = (int) (target*100.0);
			System.out.println("Seeking Amount: $ "+target);
			ArrayList<Integer> ans = SnapOnSubset.subSetSum(n, seek);
			//print out check IDs and their dollar amount
			for(int l:ans) {
				System.out.printf("%-15s %-5s %-10s\n", "$ "+ l/100.0, ":::", valToID.get(l));
				//System.out.println("$ "+(double) l/100.0 + "\t\t:::\t"+ valToID.get(l));
			}
		}catch(IOException e) {
			System.out.println("Error in program arguments -");
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong -");
		}
	}

	/**
	 * The driving method - uses Dynamic Programming approach to find subsets which sum to the target
	 * @param in - int array which contains all of the check values
	 * @param target - the target sum
	 * @return - an arraylist of the check amounts that sum to the target, if any
	 */
	public static ArrayList<Integer> subSetSum(Integer[] in, int target) {
		
		//to catch if something goes wrong in the reading, probably not needed
		if (in == null) {
			System.out.println("Null target - Error");
			return new ArrayList<Integer>();
		}

		//DP approach relies on having the min possible and max possible sum
		int min = 0, max = 0;
		for (int e : in) {
			if (e < 0) {
				min += e;
			} else {
				max += e;
			}
		}
		
		//ensure target amount is possible with the given check amounts
		if (target > max || target < min) {
			System.out.println("No such set");
			return new ArrayList<Integer>();
		}
		int numChecks = in.length;
		

		//create matrix
		min = Math.max(min, LOWER_BOUND);
		max = Math.min(max, UPPER_BOUND);
		System.out.println("MIN: "+min+"   MAX: "+max);
		int numCols = (max - min + 1);
		//building the DP matrix
		boolean[][] matrix = new boolean[numChecks + 1][numCols];

		//first row represents empty set = cannot make subsets of any sum
		for (int s = 0; s < numCols; s++) {
			matrix[0][s] = false;
		}

		//filling out the matrix according to subset sum algorithm
		for (int i = 1; i <= numChecks; i++) {
			for (int s = 0; s < numCols; s++) {
				//avoid out of bounds
				if (s - in[i - 1] >= 0 && s - in[i - 1] < numCols) {
					//True if such a subset is already possible, if the most recently added element = target, or if most recent added element completes a subset
					matrix[i][s] = matrix[i - 1][s] || in[i - 1] == (s + min) || matrix[i - 1][s - in[i - 1]];
				} else {
					matrix[i][s] = matrix[i - 1][s] || in[i - 1] == (s + min);
				}
			}
		}

		//Finding the actual subset values after seeing if such a subset exists
		ArrayList<Integer> searchedSet = new ArrayList<>();
		if (matrix[numChecks][target + Math.abs(min)]) {
			for (int j = in.length - 1, i = target - min; j >= 0 && i + min != 0; j--) {
				/**
				 * "go up" the column until you find the number which made that sum possible, then
				 * add that number to the subset list and then look in the column which that number was
				 * added to and repeat until the subset sums to your goal
				 */
				while (matrix[j][i]) {
					j--;
				}
				searchedSet.add(in[j]);
				i = i - in[j];  
			}
		}
		System.out.println("SET EXISTS:::  " + matrix[numChecks][target + Math.abs(min)]);
		return searchedSet;
	}
  
	/**
	 * method for printing the DP matrix - extremely helpful for understanding whats happening under the hood
	 * @param m - the filled out matrix
	 * @param min - the min possible value
	 * @param max - the max possible value
	 */
	private static void print(boolean[][] m, int min, int max) {
		for (int q = min; q <= max; q++) {
			System.out.print(q+" ");
		}
		System.out.print("\n");
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				if (m[i][j]) {
					System.out.print("T");
				} else {
					System.out.print("F");
				}
			}
			System.out.print("\n");
		}
	}
}