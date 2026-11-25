import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void menu(){
        System.out.println("=====================================");
        System.out.println("  Cloud Cluster Scheduler Simulator  ");
        System.out.println("=====================================");
        System.out.println("1) SJF Scheduler");
        System.out.println("2) ACS Scheduler");        
        System.out.println("3) Exit");
        System.out.print("Choose Option: ");
    }

    public static int acsChoice(){
        System.out.println("1) Test Case 1");
        System.out.println("2) Test Case 2");
        System.out.println("3) Manual Test case");
        int choice = scanner.nextInt();
        return choice;
    }

    public static ArrayList<Process> testCaseProcesses(int choice){
        ArrayList<Process> processes = new ArrayList<>();

        if(choice == 1){
            Process P1 = new Process("P1", 0, 4, 2);
            Process P2 = new Process("P2", 1, 7, 3);
            Process P3 = new Process("P3", 3, 2, 1);
            processes.add(P1);
            processes.add(P2);
            processes.add(P3);
        }

        else if(choice == 2){
            Process P1 = new Process("P1", 0, 5, 4);
            Process P2 = new Process("P2", 1, 6, 2);
            Process P3 = new Process("P3", 4, 4, 3);
            processes.add(P1);
            processes.add(P2);
            processes.add(P3);
        }

        else if(choice == 3){
            System.out.println("Enter number of processes: ");
            int n = scanner.nextInt();
            for(int i = 0; i < n; i++){
                System.out.println("Enter next fields per process by order: (Arrival Time, Burst Time, Initial Priority)");
                String name = "P" + (i+1);
                System.out.print(name + ": ");
                int arrivalTime = scanner.nextInt();
                int burstTime = scanner.nextInt();
                int priority = scanner.nextInt();
                Process P = new Process(name, arrivalTime, burstTime, priority);
                processes.add(P);
            }
        }
        return processes;
    }

    public static SimulationConfig config(){
        System.out.println("Enter configurations (CS and Q): ");
        int cs = scanner.nextInt();
        int q = scanner.nextInt();
        SimulationConfig config = new SimulationConfig(cs, q);
        return config;
    }

    public static ArrayList<Process> sjfManualProcesses(){
        ArrayList<Process> processes = new ArrayList<>();
        System.out.println("Enter number of processes: ");
        int n = scanner.nextInt();
        for(int i = 0; i < n; i++){
            System.out.println("Enter next fields per process by order: (Arrival Time, Burst Time)");
            String name = "P" + (i+1);
            System.out.print(name + ": ");
            int arrivalTime = scanner.nextInt();
            int burstTime = scanner.nextInt();
            Process P = new Process(name, arrivalTime, burstTime);
            processes.add(P);
        }
        return processes;
    }

    public static SimulationConfig sjfConfig(){
        System.out.println("Enter configuration (CS): ");
        int cs = scanner.nextInt();
        SimulationConfig config = new SimulationConfig(cs, 0);
        return config;
    }

    public static String readyQueue(List<Process> list){
        if(list.isEmpty()) return "[Empty]";
        List<String> items = new ArrayList<>();
        for(Process p : list){
            items.add(p.getName() + " (remaining: " + p.getRemainingTime() + ")");
        }
        return items.toString();
    }

    public static void printMetrics(List<Process> processes){
        double totalWT = 0, totalTAT = 0, totalRT = 0;
        System.out.println("\nProcess | Arrival | Finish | TAT | WT | RT");
        for (Process proc : processes) {
            totalWT += proc.getWaitingTime();
            totalTAT += proc.getTurnAroundTime();
            totalRT += proc.getResponseTime();
            System.out.printf("%s | %d | %d | %d | %d | %d%n",
                    proc.getName(), proc.getArrivalTime(), proc.getFinishTime(),
                    proc.getTurnAroundTime(), proc.getWaitingTime(), proc.getResponseTime());
        }
        if (!processes.isEmpty()) {
            System.out.printf("Average Turnaround Time: %.2f%n", totalTAT / processes.size());
            System.out.printf("Average Waiting Time: %.2f%n", totalWT / processes.size());
            System.out.printf("Average Response Time: %.2f%n", totalRT / processes.size());
        }
    }

    public static void main(String[] args){
        while(true){
            menu();
            int option = scanner.nextInt();

            if(option == 1){
                int choice = acsChoice();
                if (choice != 1 && choice != 2 && choice != 3) {
                    System.out.println("Invalid test case choice.");
                    continue;
                }
                IScheduler scheduler = new SJF();
                ArrayList<Process> processes = (choice == 3) ? sjfManualProcesses() : testCaseProcesses(choice);
                SimulationConfig config;
                if(choice == 1){
                    config = new SimulationConfig(0, 0);
                }
                else if(choice == 2){
                    config = new SimulationConfig(1, 0);
                }
                else config = sjfConfig();

                List<SimulationResult> result = scheduler.simulate(processes, config);
                for(SimulationResult R : result){
                    String running = R.getName().isEmpty() ? "Idle" : R.getName();
                    System.out.println("Time " + R.getTime() + " | Running: " + running + " | Ready: " + readyQueue(R.getTier1Processes()));
                }

                printMetrics(processes);
            }

            else if(option == 2){
                int choice = acsChoice();
                IScheduler scheduler = new ACS();
                ArrayList<Process> processes = testCaseProcesses(choice);
                if (choice != 1 && choice != 2 && choice != 3) {
                    System.out.println("Invalid test case choice.");
                    continue;
                }
                SimulationConfig config;
                if(choice == 1){
                    config = new SimulationConfig(0, 3);
                }
                else if(choice == 2){
                    config = new SimulationConfig(1, 3);
                }
                else config = config();
                
                List<SimulationResult> result = scheduler.simulate(processes, config);
                for(SimulationResult R : result){
                    System.out.println(R);
                }

                double totalWT = 0, totalTAT = 0, totalRT = 0;
                System.out.println("\nProcess | Arrival | Finish | TAT | WT | RT");
                for (Process proc : processes) {
                    totalWT += proc.getWaitingTime();
                    totalTAT += proc.getTurnAroundTime();
                    totalRT += proc.getResponseTime();
                    System.out.printf("%s | %d | %d | %d | %d | %d%n",
                            proc.getName(), proc.getArrivalTime(), proc.getFinishTime(),
                            proc.getTurnAroundTime(), proc.getWaitingTime(), proc.getResponseTime());
                }
                if (!processes.isEmpty()) {
                    System.out.printf("Average Turnaround Time: %.2f%n", totalTAT / processes.size());
                    System.out.printf("Average Waiting Time: %.2f%n", totalWT / processes.size());
                    System.out.printf("Average Response Time: %.2f%n", totalRT / processes.size());
                }
            }

            else if(option == 3){
                scanner.close();
                break;
            }

            else{
                System.out.println("Invalid Choice.");
            }
        }
    }
}