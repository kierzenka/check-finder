Filip Kierzenka - filip_kierzenka@brown.edu 08/28/2020
Snap-On Audit BCSI Project

--Description
This program reads in checking data from an Excel spreadsheet, including the unique check IDs and 
their dollar amount (negative for reserves), and finds sets of checks whose dollar amounts sum to a 
specified total. To run the program, you will need to download the Apache POI Library files (found 
here: https://www.javatpoint.com/how-to-read-excel-file-in-java). As of 08/28/2020, the program returns only one subset (out of potentially many) which sums to the desired total.

--Using the program
It is run with initial user input in one of two formats:
"::all [Excel file path] [desired sum]" 
"::range [Excel file path] [first row #] [last row #] [desired sum]"
The "::all" setting uses all of the checks in the given Excel sheet, the "::range" setting 
considers only the specified range of checks. The "desired sum" is the dollar amount you are 
interested in. The program will print out the result, including the individual check amounts as 
well as all of the IDs associated with checks of that amount.


--Code Explanation
The program starts by reading in the relevant information from the Excel file. It then calls the 
subsetSum method which uses a dynamic programming approach to find if a subset of the desired value 
is possible to construct with the given checks. Below is an example to illustrate what is happening 
using this approach.

Each row in the completed dynamic programming matrix represents the possible subsets you could make 
with the checks *up to that point* from the input, with the first row representing the possible 
subsets you could make from an empty input list (no checks = no possible subsets). The columns 
correspond to the possible sum of the subset (ranging from min to max).

For example, if my checks are [5, 3, -1], it will go as follows:
-the min possible sum = -1, the max possible = 8, each row consists of 10 booleans corresponding to the 10 potential sums (ranging from -1 thru 8).

First row: represents input list of []. 
	- it will look like "F F F F F F F F F F" because none of the possible sums (-1 thru 8) are 
	achievable.
Second row: represents input list of [5].
 - it will look like "F F F F F F T F F F" because, now that we have a check of value 5, 5 is an 
 achievable sum.
Third row: represents input list of [5, 3].
 - it will look like "F F F F T F T F F T" because all of 3,5, and 8 are achievable sums
Fourth row: represents input list of [5, 3, -1].
 - it will look like "T F F T T T T F F T" because -1, 2, 3, 4, 5, 8, are all achievable.
 
thus, the full matrix will be:
-1 0  1  2  3  4  5  6  7  8
F  F  F  F  F  F  F  F  F  F
F  F  F  F  F  F  T  F  F  F
F  F  F  F  T  F  T  F  F  T
T  F  F  T  T  T  T  F  F  T
if we are interested in a subset with sum 7, we would want to look at the bottom row in column 8
since that position corresponds to if its possible to reach 7 with all of the inputted checks.


The subset sum problem is generally difficult to solve (NP-Complete) - it is also 
very space-consuming and grows with the _size_ of the inputs rather than the number 
of inputs (grows rapidly with increasing value of checks rather than volume of checks),
since it considers every possible sum from the min possible value up to the max possible.

A rigorous implementation of this method would have to consider every possible dollar amount 
(and in this case every cent amount, since it works only with integers) from the minimum possible 
to the maximum possible sum. When there are many-hundred checks, and some of a considerable size, 
it becomes simply too much to handle. As a result, there is an upper and lower bound parameter 
which essentially says "I don't expect to get above or below this amount on my way to the solution 
set". Note that this is not the upper and lower limit on the individual checks, but rather on the 
sum of the subset on its way to the target (if the upper bound is 1,000 , then it will not be 
able to find a subset that looks like [+500, +750, -800] because that subset crosses the $1,000 
border on its way to the final sum). This, unfortunately, seems unavoidable, but I'm hoping that 
the subsets hover around pretty reasonable numbers rather than veering way off. 
