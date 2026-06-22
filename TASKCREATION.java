import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

// title, desc, due date, category, priority
class createtask {
	private String title;
	private String desc;
	private int due;
	private String category;
	private String priority;
	private boolean iscomplete;

	public createtask(String title, String desc, int due, String category, String priority) {
		this.title = title;
		this.desc = desc;
		this.due = due;
		this.category = category;
		this.priority = priority;
		this.iscomplete = false;
	}

	// getters
	public String gettitle() {
		return title;
	}

	public String getdesc() {
		return desc;
	}

	public int getdue() {
		return due;
	}

	public String getcategory() {
		return category;
	}

	public String getpriority() {
		return priority;
	}

	public boolean getiscomplete() {
		return iscomplete;
	}

	// setters, used by the edit task option
	public void setiscomplete(boolean iscomplete) {
		this.iscomplete = iscomplete;
	}

	public void settitle(String title) {
		this.title = title;
	}

	public void setdesc(String desc) {
		this.desc = desc;
	}

	public void setdue(int due) {
		this.due = due;
	}

	public void setcategory(String category) {
		this.category = category;
	}

	public void setpriority(String priority) {
		this.priority = priority;
	}

	// Convert the object to a CSV-compatible string
	@Override
	public String toString() {
		return String.format("%s,%s,%d,%s,%s", title, desc, due, category, priority);
	}

	// Create a createtask object from a CSV string
	public static createtask fromCSV(String csvLine) {
		String[] parts = csvLine.split(",");
		String title = parts[0];
		String desc = parts[1];
		int due = Integer.parseInt(parts[2]);
		String category = parts[3];
		String priority = parts[4];
		return new createtask(title, desc, due, category, priority);
	}
}

class RecurringTask {
	private String title;
	private String desc;
	private String recurrence;

	public RecurringTask(String title, String desc, String recurrence) {
		this.title = title;
		this.desc = desc;
		this.recurrence = recurrence;
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s", title, desc, recurrence);
	}

	public String gettitle() {
		return title;
	}

	public String getdesc() {
		return desc;
	}

	public String getrecurrence() {
		return recurrence;
	}
}


public class TASKCREATION {

	// user can enter lots of tasks in an arraylist
	private static ArrayList<createtask> tasklist = new ArrayList<>();
	private static ArrayList<RecurringTask> recurringTaskList = new ArrayList<>();

	// for task dependency using HashMap
	private static HashMap<Integer, ArrayList<Integer>> dependencies = new HashMap<>();

	private static Scanner sc = new Scanner(System.in);

	private static int getpriorityvalue(String priority) {
		switch (priority.toLowerCase()) {
		case "high":
			return 3;
		case "medium":
			return 2;
		case "low":
			return 1;
		default:
			return 0; // Default for unknown priorities
		}
	}

	// Helper function to check for cycles
	private static boolean hasCycle(HashMap<Integer, ArrayList<Integer>> dependencies, int current, boolean[] visited, boolean[] stack) {
		if (stack[current]) {
			return true; // Cycle detected
		}
		if (visited[current]) {
			return false; // Already processed
		}

		visited[current] = true;
		stack[current] = true;

		// Check all dependencies of the current task
		if (dependencies.containsKey(current)) {
			for (int dep : dependencies.get(current)) {
				if (hasCycle(dependencies, dep, visited, stack)) {
					return true;
				}
			}
		}

		stack[current] = false;
		return false;
	}

	// search task method
	private static void searchtask(ArrayList<createtask> tasklist, String tasktosearch) {
		boolean found = false;
		for (int i = 0; i < tasklist.size(); i++) {
			createtask task = tasklist.get(i);
			if (task.getdesc().toLowerCase().contains(tasktosearch.toLowerCase())
					|| task.getcategory().toLowerCase().contains(tasktosearch.toLowerCase())) {
				System.out.println("TASK FOUND: " + task.gettitle());
				found = true;
			}
		}
		if (!found) {
			System.out.println("NO MATCHING TASK FOUND");
		}
	}

	// dependency function, so it can just be called
	private static void addDependancy() {
		System.out.println("Enter the task number that depends on another task:");
		int dependentTask = sc.nextInt();

		System.out.println("Enter the task number it depends on:");
		int precedingTask = sc.nextInt();
		sc.nextLine(); // consume leftover newline after the two nextInt() calls

		// Validate task numbers
		if (dependentTask <= 0 || dependentTask > tasklist.size() || precedingTask <= 0 || precedingTask > tasklist.size()) {
			System.out.println("ERROR: Task number is invalid. Please enter a task number between 1 and " + tasklist.size() + ".");
			return;
		}

		// Add the dependency
		dependencies.putIfAbsent(dependentTask, new ArrayList<>());
		dependencies.get(dependentTask).add(precedingTask);

		// Detect cycles
		int maxTaskNumber = tasklist.size();
		boolean[] visited = new boolean[maxTaskNumber + 1];
		boolean[] stack = new boolean[maxTaskNumber + 1];
		if (hasCycle(dependencies, dependentTask, visited, stack)) {
			// Remove the dependency if it creates a cycle
			dependencies.get(dependentTask).remove(Integer.valueOf(precedingTask));
			System.out.println("ERROR: Adding this dependency would create a cycle. Dependency not added.");
		} else {
			System.out.println("Task " + dependentTask + " now depends on Task " + precedingTask);
		}

		if (!dependencies.get(dependentTask).contains(precedingTask)) {
			System.out.println("Dependency added successfully.");
		} else {
			System.out.println("This dependency already exists.");
		}
		saveTasksToFile();
	}

	// saving tasks to a file
	private static void saveTasksToFile() {
		try (FileWriter writer = new FileWriter("tasks.csv")) {
			// Save regular tasks
			writer.write("# Regular Tasks\n");
			for (createtask task : tasklist) {
				writer.write(task.toString() + "\n");
			}

			// Save dependencies
			writer.write("# Dependencies\n");
			for (Map.Entry<Integer, ArrayList<Integer>> entry : dependencies.entrySet()) {
				for (int prereq : entry.getValue()) {
					writer.write(entry.getKey() + ":" + prereq + "\n");
				}
			}

			// Save recurring tasks
			writer.write("# Recurring Tasks\n");
			for (RecurringTask rTask : recurringTaskList) {
				writer.write(rTask.toString() + "\n");
			}

			System.out.println("Tasks, dependencies, and recurring tasks saved successfully!");
		} catch (IOException e) {
			System.out.println("Error saving tasks: " + e.getMessage());
		}
	}

	// Load tasks if there are previously saved tasks
	private static void loadTasksFromFile() {
		try (BufferedReader reader = new BufferedReader(new FileReader("tasks.csv"))) {
			String line;
			String section = "";
			while ((line = reader.readLine()) != null) {
				// Check for section headers
				if (line.startsWith("#")) {
					section = line;
					continue;
				}
				if (line.isBlank()) {
					continue;
				}

				// Load regular tasks
				if (section.equals("# Regular Tasks")) {
					createtask task = createtask.fromCSV(line);
					tasklist.add(task);
				}

				// Load dependencies
				if (section.equals("# Dependencies")) {
					String[] parts = line.split(":");
					int dependentTask = Integer.parseInt(parts[0]);
					int prereq = Integer.parseInt(parts[1]);

					dependencies.putIfAbsent(dependentTask, new ArrayList<>());
					dependencies.get(dependentTask).add(prereq);
				}

				// Load recurring tasks
				if (section.equals("# Recurring Tasks")) {
					String[] parts = line.split(",");
					String rTitle = parts[0];
					String rDesc = parts[1];
					String recurrence = parts[2];

					RecurringTask rTask = new RecurringTask(rTitle, rDesc, recurrence);
					recurringTaskList.add(rTask);
				}
			}
			System.out.println("Tasks, dependencies, and recurring tasks loaded successfully!");
		} catch (FileNotFoundException e) {
			System.out.println("No saved tasks found. Starting fresh!");
		} catch (IOException e) {
			System.out.println("Error loading tasks: " + e.getMessage());
		}
	}

	private static void tasksorting(ArrayList<createtask> tasklist, boolean highToLow) {
		for (int i = 0; i < tasklist.size() - 1; i++) {
			for (int j = 0; j < tasklist.size() - i - 1; j++) {
				int priority1 = getpriorityvalue(tasklist.get(j).getpriority());
				int priority2 = getpriorityvalue(tasklist.get(j + 1).getpriority());
				boolean swap = false;

				// Sort by priority
				if (highToLow && priority1 < priority2) {
					swap = true; // High to Low
				} else if (!highToLow && priority1 > priority2) {
					swap = true; // Low to High
				} else if (priority1 == priority2) {
					// If priorities are equal, sort by due date (ascending)
					if (tasklist.get(j).getdue() > tasklist.get(j + 1).getdue()) {
						swap = true;
					}
				}

				if (swap) {
					// Swap tasks
					createtask temp = tasklist.get(j);
					tasklist.set(j, tasklist.get(j + 1));
					tasklist.set(j + 1, temp);
				}
			}
		}

		// Display sorted tasks
		System.out.println(highToLow ? "TASKS SORTED BY PRIORITY (HIGH TO LOW):" : "TASKS SORTED BY PRIORITY (LOW TO HIGH):");
		for (int i = 0; i < tasklist.size(); i++) {
			createtask task = tasklist.get(i);
			System.out.println((i + 1) + ". " + task.gettitle() + " PRIORITY: " + task.getpriority() + " DUE DATE: " + task.getdue());
		}
	}

	public static void dataanalytic(ArrayList<createtask> tasklist, int countercomplete) {
		System.out.println("TOTAL TASK: " + tasklist.size());
		System.out.println("COMPLETED: " + countercomplete);
		System.out.println("PENDING: " + (tasklist.size() - countercomplete));

		if (tasklist.isEmpty()) {
			System.out.println("COMPLETION RATE: N/A (no tasks yet)");
		} else {
			double completionrate = (double) countercomplete / tasklist.size() * 100.0;
			System.out.println("COMPLETION RATE: " + completionrate + "%");
		}

		int hw = 0;
		int per = 0;
		int work = 0;
		for (int i = 0; i < tasklist.size(); i++) {
			createtask categories = tasklist.get(i);
			if (categories.getcategory().toLowerCase().contains("homework")) {
				hw++;
			} else if (categories.getcategory().toLowerCase().contains("personal")) {
				per++;
			} else {
				work++;
			}
		}
		System.out.println("TASK CATEGORIES: HOMEWORK: " + hw + " PERSONAL: " + per + " WORK: " + work);
	}

	// checks whether a task is due within 24 hours. Used to print a console
	// reminder (the original version emailed this; that feature was removed
	// because it had a hardcoded Gmail app password committed in source).
	public static void checktask() {
		LocalDate now = LocalDate.now();

		for (createtask task : tasklist) {
			int duedateint = task.getdue();
			String duedatestr = String.valueOf(duedateint);

			if (duedatestr.length() != 8) {
				// skip malformed due dates instead of throwing
				continue;
			}

			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				LocalDate taskduedate = LocalDate.parse(duedatestr, formatter);
				long daysbetween = ChronoUnit.DAYS.between(now, taskduedate);

				if (daysbetween <= 1 && daysbetween > 0) {
					System.out.println("REMINDER: \"" + task.gettitle() + "\" IS DUE WITHIN 24 HOURS — PLEASE COMPLETE IT SOON!");
				}
			} catch (DateTimeParseException e) {
				continue;
			}
		}
	}

	public static void main(String[] args) {
		Scanner sc = TASKCREATION.sc;
		loadTasksFromFile();
		int countercomplete = 0;

		while (true) {
			boolean cont = true;

			System.out.println("1. TASK CREATION");
			System.out.println("2. TASK MANAGEMENT");
			System.out.println("3. TASK DELETION");
			System.out.println("4. TASK SORTING");
			System.out.println("5. TASK SEARCHING");
			System.out.println("6. SET RECURRING TASK");
			System.out.println("7. SET TASK DEPENDENCIES");
			System.out.println("8. EDIT TASK");
			System.out.println("9. STORAGE SYSTEM");
			System.out.println("10. CHECK FOR TASKS DUE SOON");
			System.out.println("11. TASK ANALYTICS");
			System.out.println("0. EXIT THE PROGRAM");
			System.out.print("ENTER YOUR CHOICE: ");

			// Validate input
			if (!sc.hasNextInt()) {
				System.out.println("INVALID INPUT. PLEASE ENTER A NUMBER BETWEEN 0 AND 11.");
				sc.nextLine(); // Clear the invalid input
				continue; // Restart the loop
			}

			int mainchoice = sc.nextInt();
			sc.nextLine(); // consume leftover newline so the next nextLine() call gets real input

			while (cont) {
				switch (mainchoice) {
				case 0:
					System.out.println("EXITING PROGRAM. HAVE A GREAT DAY!");
					return;

				case 1: {
					System.out.println("CREATE YOUR TASK:");
					System.out.print("1. ENTER TITLE: ");
					String title = sc.nextLine();

					System.out.print("2. ENTER DESCRIPTION: ");
					String desc = sc.nextLine();

					System.out.print("3. ENTER DUE DATE (YYYYMMDD): ");
					String dueStr;
					LocalDate due;

					while (true) {
						dueStr = sc.nextLine(); // Read as string for validation
						if (dueStr.matches("\\d{8}")) {
							try {
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
								due = LocalDate.parse(dueStr, formatter);
								break;
							} catch (DateTimeParseException e) {
								System.out.println("INVALID DATE! PLEASE ENTER DATE IN YYYYMMDD FORMAT");
							}
						} else {
							System.out.println("INVALID FORMAT! PLEASE ENTER DATE IN YYYYMMDD FORMAT");
						}
						System.out.print("ENTER DUE DATE AGAIN: ");
					}

					int dueInt = Integer.parseInt(due.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
					System.out.println("Due Date (as int): " + dueInt);

					System.out.print("4. ENTER CATEGORY (HOMEWORK/PERSONAL/WORK): ");
					String category;
					while (true) {
						category = sc.nextLine().toLowerCase();
						if (category.equals("homework") || category.equals("work") || category.equals("personal")) {
							break;
						} else {
							System.out.println("PLEASE ENTER A VALID CATEGORY!");
							System.out.print("ENTER CATEGORY: ");
						}
					}

					System.out.print("5. ENTER PRIORITY(HIGH/MEDIUM/LOW): ");
					String priority;
					while (true) {
						priority = sc.nextLine().toLowerCase();
						if (priority.equals("high") || priority.equals("medium") || priority.equals("low")) {
							break;
						} else {
							System.out.println("PLEASE ENTER A VALID PRIORITY: ");
							System.out.print("ENTER PRIORITY: ");
						}
					}

					createtask todo = new createtask(title, desc, dueInt, category, priority);
					tasklist.add(todo);

					System.out.println("TASK " + title + " HAS BEEN CREATED SUCCESSFULLY!");
					saveTasksToFile();

					while (true) {
						System.out.print("DO YOU WANT TO CONTINUE CREATING A TASK? (YES/NO): ");
						String choice = sc.nextLine();
						if (choice.equalsIgnoreCase("no")) {
							cont = false;
							System.out.println("NUMBER OF TASK CREATED: " + tasklist.size());
							break;
						} else if (choice.equalsIgnoreCase("yes")) {
							System.out.println("NUMBER OF TASK CREATED: " + tasklist.size());
							break;
						} else {
							System.out.println("ENTER A VALID INPUT");
						}
					}
					break;
				}

				// task management (mark complete)
				case 2: {
					System.out.println("TASK LIST:");
					for (int i = 0; i < tasklist.size(); i++) {
						System.out.println((i + 1) + ". " + tasklist.get(i).gettitle());
					}
					System.out.println();
					System.out.print("ENTER THE TASK NUMBER THAT YOU WANT TO MARK AS COMPLETE: ");
					int n = sc.nextInt();
					sc.nextLine(); // consume leftover newline

					if (n <= 0 || n > tasklist.size()) {
						System.out.println("INVALID TASK NUMBER. PLEASE TRY AGAIN.");
					} else {
						boolean canComplete = true;

						if (dependencies.containsKey(n)) {
							ArrayList<Integer> prereqs = dependencies.get(n);
							for (int prereq : prereqs) {
								if (prereq > 0 && prereq <= tasklist.size()) {
									canComplete = false;
									System.out.println("WARNING: TASK " + n + " CANNOT BE MARKED AS COMPLETE BECAUSE IT DEPENDS ON TASK "
											+ prereq + ". PLEASE COMPLETE TASK " + prereq + " FIRST.");
									break;
								}
							}
						}

						if (canComplete) {
							createtask completed = tasklist.get(n - 1);
							System.out.println("TASK " + completed.gettitle() + " MARKED AS COMPLETE! HOORAY");
							tasklist.remove(n - 1);
							saveTasksToFile();

							countercomplete++;
							System.out.println("TOTAL TASK COMPLETED SO FAR: " + countercomplete);

							// Clean up dependencies
							dependencies.remove(n);
							for (ArrayList<Integer> prereqList : dependencies.values()) {
								prereqList.remove(Integer.valueOf(n));
							}

							// Adjust remaining dependencies to account for shifted task indices
							Map<Integer, ArrayList<Integer>> updatedDependencies = new HashMap<>();
							for (Map.Entry<Integer, ArrayList<Integer>> entry : dependencies.entrySet()) {
								int updatedKey = entry.getKey() > n ? entry.getKey() - 1 : entry.getKey();
								ArrayList<Integer> updatedPrereqs = new ArrayList<>();
								for (int prereq : entry.getValue()) {
									updatedPrereqs.add(prereq > n ? prereq - 1 : prereq);
								}
								updatedDependencies.put(updatedKey, updatedPrereqs);
							}
							dependencies.clear();
							dependencies.putAll(updatedDependencies);
						}
					}

					while (true) {
						System.out.print("DO YOU WANT TO CONTINUE MANAGING YOUR TASK? (YES/NO): ");
						String user = sc.nextLine();
						if (user.equalsIgnoreCase("yes")) {
							System.out.println("PROCEED MANAGE TASK...");
							break;
						} else if (user.equalsIgnoreCase("no")) {
							System.out.println("ALRIGHT, BYE BYE!");
							cont = false;
							break;
						} else {
							System.out.println("ENTER A VALID INPUT");
						}
					}
					break;
				}

				// task deletion
				case 3: {
					System.out.println("TASK LIST:");
					if (tasklist.isEmpty() && recurringTaskList.isEmpty()) {
						System.out.println("NO TASK AVAILABLE TO DELETE.");
						cont = false;
						break;
					}

					int normalcounter = 0, counter = 0;
					System.out.println("Normal tasks:");
					if (!tasklist.isEmpty()) {
						for (int i = 0; i < tasklist.size(); i++) {
							System.out.println((i + 1) + ". " + tasklist.get(i).gettitle());
							counter++;
							normalcounter++;
						}
					} else {
						System.out.println("No normal tasks found!\n");
					}

					System.out.println("Recurring tasks:");
					if (!recurringTaskList.isEmpty()) {
						for (int i = 0; i < recurringTaskList.size(); i++) {
							System.out.println((counter + 1) + ". " + recurringTaskList.get(i).gettitle());
							counter++;
						}
					} else {
						System.out.println("No Recurring tasks found!\n");
					}

					System.out.print("ENTER THE TASK NUMBER YOU WANT TO DELETE: ");
					int m = sc.nextInt();
					sc.nextLine(); // consume leftover newline

					if (m <= 0 || m > counter) {
						System.out.println("INVALID TASK NUMBER. PLEASE TRY AGAIN.");
					} else if (m <= normalcounter) {
						createtask remove = tasklist.get(m - 1);
						System.out.println("TASK " + remove.gettitle() + " HAS DELETED SUCCESSFULLY!");
						tasklist.remove(m - 1);
						saveTasksToFile();
					} else {
						int recurringIndex = m - normalcounter - 1;
						RecurringTask removerecurring = recurringTaskList.get(recurringIndex);
						System.out.println("TASK " + removerecurring.gettitle() + " HAS DELETED SUCCESSFULLY!");
						recurringTaskList.remove(recurringIndex);
						saveTasksToFile();
					}

					boolean incorrectinput = true;
					while (incorrectinput) {
						System.out.print("DO YOU STILL WANT TO DELETE TASKS? (YES/NO): ");
						String yesno = sc.nextLine();
						if (yesno.equalsIgnoreCase("yes")) {
							incorrectinput = false;
						} else if (yesno.equalsIgnoreCase("no")) {
							incorrectinput = false;
							cont = false;
						} else {
							System.out.println("ENTER A VALID STATEMENT\n");
						}
					}
					break;
				}

				// sorting task
				case 4: {
					System.out.println("HOW WOULD YOU WANT TO SORT YOUR TASK?");
					System.out.println("1. DUE DATE ASCENDING");
					System.out.println("2. DUE DATE DESCENDING");
					System.out.println("3. PRIORITY HIGH TO LOW");
					System.out.println("4. PRIORITY LOW TO HIGH");
					System.out.print("5. CHOOSE ONE: ");
					int sortinput = sc.nextInt();
					sc.nextLine(); // consume leftover newline

					switch (sortinput) {
					case 1:
						for (int i = 0; i < tasklist.size() - 1; i++) {
							for (int j = 0; j < tasklist.size() - i - 1; j++) {
								if (tasklist.get(j).getdue() > tasklist.get(j + 1).getdue()) {
									createtask temp = tasklist.get(j);
									tasklist.set(j, tasklist.get(j + 1));
									tasklist.set(j + 1, temp);
								}
							}
						}
						System.out.println("TASKS SORTED BY DUE DATE (ASCENDING):");
						for (int i = 0; i < tasklist.size(); i++) {
							createtask dueascending = tasklist.get(i);
							System.out.println((i + 1) + ". " + dueascending.gettitle() + " DUE DATE: " + dueascending.getdue());
						}
						break;

					case 2:
						for (int i = 0; i < tasklist.size() - 1; i++) {
							for (int j = 0; j < tasklist.size() - i - 1; j++) {
								if (tasklist.get(j).getdue() < tasklist.get(j + 1).getdue()) {
									createtask temp = tasklist.get(j);
									tasklist.set(j, tasklist.get(j + 1));
									tasklist.set(j + 1, temp);
								}
							}
						}
						System.out.println("TASK SORTED BY DUE DATE (DESCENDING)");
						for (int i = 0; i < tasklist.size(); i++) {
							createtask duedescending = tasklist.get(i);
							System.out.println((i + 1) + ". " + duedescending.gettitle() + " DUE DATE: " + duedescending.getdue());
						}
						break;

					case 3:
						tasksorting(tasklist, true);
						break;

					case 4:
						tasksorting(tasklist, false);
						break;

					default:
						System.out.println("PLEASE ENTER A VALID OPTION, THANK YOU");
					}

					System.out.print("DO YOU STILL WANT TO SORT TASKS? (YES/NO): ");
					String sorting = sc.nextLine();
					if (sorting.equalsIgnoreCase("no")) {
						cont = false;
						System.out.println("ALRIGHT THEN, BYE BYE!");
					}
					break; // fixes fallthrough into case 5
				}

				// search task
				case 5: {
					System.out.print("ENTER KEYWORD TO SEARCH YOUR TASK: ");
					String search = sc.nextLine().toLowerCase();
					boolean found = false;

					System.out.println();
					System.out.println("SEARCH RESULTS:");
					for (int i = 0; i < tasklist.size(); i++) {
						createtask task = tasklist.get(i);
						if (task.gettitle().toLowerCase().contains(search) || task.getdesc().toLowerCase().contains(search)) {
							System.out.println((i + 1) + ". [" + (task.getiscomplete() ? "COMPLETE" : "INCOMPLETE") + "] "
									+ task.gettitle() + " DUE: " + task.getdue());
							found = true;
						}
					}
					if (!found) {
						System.out.println("NO TASK FOUND WITH THE KEYWORD " + search);
					}

					boolean incorrectinput = true;
					while (incorrectinput) {
						System.out.print("DO YOU STILL WANT TO SEARCH? (YES/NO): ");
						String yesno = sc.nextLine();
						if (yesno.equalsIgnoreCase("yes")) {
							incorrectinput = false;
						} else if (yesno.equalsIgnoreCase("no")) {
							incorrectinput = false;
							cont = false;
						} else {
							System.out.println("ENTER A VALID STATEMENT\n");
						}
					}
					break;
				}

				case 6: {
					System.out.println("\n=== ADD A RECURRING TASK ===");
					System.out.print("Enter task title: ");
					String rTitle = sc.nextLine();

					System.out.print("Enter description for this recurring task: ");
					String rDesc = sc.nextLine();

					System.out.print("Enter recurrence interval (daily/weekly/monthly): ");
					String recurrence = sc.nextLine();

					RecurringTask recurringTask = new RecurringTask(rTitle, rDesc, recurrence);
					recurringTaskList.add(recurringTask);
					saveTasksToFile();
					System.out.println("Recurring Task \"" + rTitle + "\" created successfully!");

					boolean incorrectinput = true;
					while (incorrectinput) {
						System.out.print("DO YOU STILL WANT TO MAKE A RECURRING TASK? (YES/NO): ");
						String yesno = sc.nextLine();
						if (yesno.equalsIgnoreCase("yes")) {
							incorrectinput = false;
						} else if (yesno.equalsIgnoreCase("no")) {
							incorrectinput = false;
							cont = false;
						} else {
							System.out.println("ENTER A VALID STATEMENT\n");
						}
					}
					break;
				}

				// task dependency
				case 7: {
					if (dependencies.size() == 0) {
						addDependancy();
					} else {
						System.out.println("Would you like to create a new task dependency or view all your task dependencies?");
						System.out.println("1. Create new task dependency");
						System.out.println("2. View all my task dependencies");
						int dependancyChoice = sc.nextInt();
						sc.nextLine(); // consume leftover newline

						switch (dependancyChoice) {
						case 1:
							addDependancy();
							break;
						case 2:
							System.out.println("CURRENT TASK DEPENDENCIES:");
							for (Map.Entry<Integer, ArrayList<Integer>> entry : dependencies.entrySet()) {
								int task = entry.getKey();
								ArrayList<Integer> prereqs = entry.getValue();
								System.out.print("Task " + task + " (" + tasklist.get(task - 1).gettitle() + ") depends on: ");
								if (prereqs.isEmpty()) {
									System.out.println("No dependencies yet");
								} else {
									StringBuilder sb = new StringBuilder();
									for (int prereq : prereqs) {
										sb.append(prereq).append(" (").append(tasklist.get(prereq - 1).gettitle()).append("), ");
									}
									System.out.println(sb);
								}
							}
							break;
						default:
							System.out.println("Invalid choice, returning to the main menu.");
						}
					}

					System.out.print("DO YOU WANT TO CONTINUE TO ADD MORE TASK DEPENDENCIES? (YES/NO): ");
					String yesno = sc.nextLine();
					if (yesno.equalsIgnoreCase("no")) {
						System.out.println("ALRIGHT, BYE BYE!");
						cont = false;
					}
					break;
				}

				case 8: {
					if (tasklist.size() == 0) {
						System.out.println("THERE ARE NO TASK AVAILABLE");
						break;
					}

					System.out.println("DISPLAY ALL TASK: ");
					for (int i = 0; i < tasklist.size(); i++) {
						System.out.println((i + 1) + ". " + tasklist.get(i).gettitle());
					}

					System.out.println();
					System.out.print("ENTER THE TASK NUMBER YOU WANT TO EDIT: ");
					int taskNum = sc.nextInt();
					sc.nextLine(); // consume leftover newline

					if (taskNum <= 0 || taskNum > tasklist.size()) {
						System.out.println("INVALID TASK NUMBER.");
						break;
					}

					createtask edit = tasklist.get(taskNum - 1);
					System.out.println();
					System.out.println("WHAT WOULD YOU LIKE TO EDIT? ");
					System.out.println("1. TITLE");
					System.out.println("2. DESCRIPTION");
					System.out.println("3. DUE DATE");
					System.out.println("4. CATEGORY");
					System.out.println("5. PRIORITY");
					System.out.print("ENTER YOUR CHOICE: ");
					int editChoice = sc.nextInt();
					sc.nextLine(); // consume leftover newline

					switch (editChoice) {
					case 1:
						System.out.print("ENTER YOUR NEW TITLE: ");
						String newtitle = sc.nextLine();
						System.out.println(edit.gettitle() + " HAS SUCCESSFULLY CHANGED TO " + newtitle);
						edit.settitle(newtitle);
						break;

					case 2:
						System.out.print("ENTER YOUR NEW DESCRIPTION: ");
						String newdesc = sc.nextLine();
						System.out.println(edit.getdesc() + " HAS SUCCESSFULLY CHANGED TO " + newdesc);
						edit.setdesc(newdesc);
						break;

					case 3:
						System.out.print("ENTER YOUR NEW DUE DATE (YYYYMMDD): ");
						String newDueStr = sc.nextLine();
						while (!newDueStr.matches("\\d{8}")) {
							System.out.print("DUE DATE MUST BE EXACTLY 8 DIGITS (YYYYMMDD). PLEASE RE-ENTER: ");
							newDueStr = sc.nextLine();
						}
						int newdue = Integer.parseInt(newDueStr);
						System.out.println(edit.getdue() + " HAS SUCCESSFULLY CHANGED TO " + newdue);
						edit.setdue(newdue);
						break;

					case 4:
						System.out.print("ENTER YOUR NEW CATEGORY (HOMEWORK/PERSONAL/WORK): ");
						String newcategory = sc.nextLine().toLowerCase();
						while (!newcategory.equals("homework") && !newcategory.equals("personal") && !newcategory.equals("work")) {
							System.out.print("ENTER (HOMEWORK/PERSONAL/WORK) ONLY!: ");
							newcategory = sc.nextLine().toLowerCase();
						}
						System.out.println(edit.getcategory() + " HAS SUCCESSFULLY CHANGED TO " + newcategory);
						edit.setcategory(newcategory);
						break;

					case 5:
						System.out.print("ENTER YOUR NEW PRIORITY (HIGH/MEDIUM/LOW): ");
						String newpriority = sc.nextLine().toLowerCase();
						while (!newpriority.equals("high") && !newpriority.equals("medium") && !newpriority.equals("low")) {
							System.out.print("ENTER (HIGH/MEDIUM/LOW) ONLY!: ");
							newpriority = sc.nextLine().toLowerCase();
						}
						System.out.println(edit.getpriority() + " HAS SUCCESSFULLY CHANGED TO " + newpriority);
						edit.setpriority(newpriority);
						break;

					default:
						System.out.println("ENTER A NUMBER FROM (1-5)");
						break;
					}

					saveTasksToFile();

					System.out.print("DO YOU STILL WANT TO EDIT ANOTHER TASK? (YES/NO): ");
					String editAgain = sc.nextLine();
					if (editAgain.equalsIgnoreCase("no")) {
						cont = false;
					}
					break;
				}

				case 9: {
					System.out.println("STORAGE SYSTEM");
					System.out.println("Tasks are saved to tasks.csv automatically after every change.");
					saveTasksToFile();
					cont = false;
					break;
				}

				case 10: {
					System.out.println();
					checktask();
					System.out.println("CHECK COMPLETE.");
					System.out.println();
					cont = false;
					break;
				}

				case 11: {
					dataanalytic(tasklist, countercomplete);
					cont = false;
					break;
				}

				default:
					System.out.println("PLEASE ENTER A VALID OPTION FROM THE MENU.");
					cont = false;
				}
			}
		}
	}
}
