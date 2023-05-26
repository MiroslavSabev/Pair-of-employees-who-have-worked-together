package PairOfEmployees;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 This class implements the functionality to read data from a CSV file,
 and calculate the pair of employees with the longest duration of common projects,
 along with the duration of each common project.
 */
public class PairOfEmployeesWhoHaveWorkedTogether {

    /**
     The main method of the program. Reads data from a CSV file, stores it in a Map,
     and calculates the pair of employees with the longest duration of common projects,
     along with the duration of each common project.
     @param args The command-line arguments passed to the program.
     */
    public static void main(String[] args) {
        String csvFile = "Book1.csv";
        Map<String, Map<String, LocalDate[]>> employeeProjects = new HashMap<>();
        csvReader(employeeProjects, csvFile);
        pairWithLongestDurationOfCommonProjects(employeeProjects);
    }

    /**
     Reads data from a CSV file, and stores it in a Map.
     The data contains the ID of an employee, the ID of a project the employee worked on,
     and the start and end dates of the employee's work on the project.
     @param employeeProjects A Map to store the projects of each employee.
     @param csvFile The path to the CSV file containing the employee-project data.
     */
    private static void csvReader(Map<String, Map<String, LocalDate[]>> employeeProjects, String csvFile) {
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] row = line.split(cvsSplitBy);
                String empId = row[0];
                String projectId = row[1];

                LocalDate dateFrom = parseDate(row[2]);
                LocalDate dateTo = row[3].equals("NULL") ? LocalDate.now() : parseDate(row[3]);

                Map<String, LocalDate[]> projectDates = employeeProjects.getOrDefault(empId, new HashMap<>());
                projectDates.put(projectId, new LocalDate[]{dateFrom, dateTo});
                employeeProjects.put(empId, projectDates);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     Finds the pair of employees with the longest duration of common projects,
     along with the duration of each common project, and outputs the result.
     @param employeeProjects A Map containing the projects of each employee.
     */
    private static void pairWithLongestDurationOfCommonProjects(Map<String, Map<String, LocalDate[]>> employeeProjects) {
        String empId1 = "";
        String empId2 = "";
        long maxDuration = 0;

        List<String> employeeList = new ArrayList<>(employeeProjects.keySet());
        int n = employeeList.size();

        for (int i = 0; i < n - 1; i++) {
            String e1 = employeeList.get(i);
            for (int j = i + 1; j < n; j++) {
                String e2 = employeeList.get(j);
                long duration = getCommonProjectDuration(employeeProjects.get(e1), employeeProjects.get(e2));
                if (duration > maxDuration) {
                    empId1 = e1;
                    empId2 = e2;
                    maxDuration = duration;
                }
            }
        }

        System.out.println(empId1 + ", " + empId2 + ", " + maxDuration);
        outputOfEachProject(employeeProjects, empId1, empId2);
    }

    /**
     Calculates the duration of overlap between two sets of project dates.
     @param projects1 a map of the form {projectId -> [dateFrom, dateTo]}
     @param projects2 a map of the form {projectId -> [dateFrom, dateTo]}
     @return the duration of overlap between the two sets of project dates
     */
    private static long getCommonProjectDuration(Map<String, LocalDate[]> projects1, Map<String, LocalDate[]> projects2) {
        long duration = 0;
        for (String projectId : projects1.keySet()) {
            if (projects2.containsKey(projectId)) {
                duration += getDurationOverlap(projects1.get(projectId), projects2.get(projectId));
            }
        }

        return duration;
    }

    /**
     Calculates the duration of overlap between two date ranges.
     @param range1 an array containing the start and end dates of the first range
     @param range2 an array containing the start and end dates of the second range
     @return the duration of overlap between the two date ranges
     */
    private static long getDurationOverlap(LocalDate[] range1, LocalDate[] range2) {
        LocalDate start = range1[0].isBefore(range2[0]) ? range2[0] : range1[0];
        LocalDate end = range1[1].isBefore(range2[1]) ? range1[1] : range2[1];
        if (ChronoUnit.DAYS.between(start, end) < 0) {
            return 0;
        }
        else {
            return ChronoUnit.DAYS.between(start, end);
        }
    }

    /**
     Prints the project IDs and durations of overlap for all projects that two employees have worked on together.
     @param employeeProjects the map containing the employee project data
     @param empId1 the ID of the first employee
     @param empId2 the ID of the second employee
     */
    private static void outputOfEachProject(Map<String, Map<String, LocalDate[]>> employeeProjects, String empId1, String empId2) { //?
        Map<String, LocalDate[]> projects1 = employeeProjects.get(empId1);
        Map<String, LocalDate[]> projects2 = employeeProjects.get(empId2);
        for (String projectId : projects1.keySet()) {
            if (projects2.containsKey(projectId)) {
                long duration = getDurationOverlap(projects1.get(projectId), projects2.get(projectId));
                if (duration > 0) {
                    System.out.println(projectId + ", " + duration);
                }
            }
        }
    }

    /**
     Parses a date string into a LocalDate object. The method tries to parse the input string using several date patterns.
     If none of the patterns match the input string, a DateTimeParseException is thrown.
     @param dateString the date string to parse
     @return a LocalDate object representing the parsed date
     @throws DateTimeParseException if the input string cannot be parsed into a LocalDate using any of the supported patterns
     */
    private static LocalDate parseDate(String dateString) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("d/M/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("d-M-yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("M/d/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("M-d-yyyy"))
                .toFormatter();

        return LocalDate.parse(dateString, formatter);
    }
}
