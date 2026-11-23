import java.util.ArrayList;
import java.util.Scanner;
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void menu(){
        System.out.println("=====================================");
        System.out.println("  Cloud Cluster Scheduler Simulator  ");
        System.out.println("=====================================");
        System.out.println("1) SFJ Scheduler");
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

    public static void main(String[] args){
        while(true){
            menu();
            int option = scanner.nextInt();

            if(option == 1){
                IScheduler scheduler = new SJF();
            }

            else if(option == 2){
                int choice = acsChoice();
                IScheduler scheduler = new ACS();
                ArrayList<Process> processes = testCaseProcesses(choice);
                SimulationConfig config;
                if(choice == 1){
                    config = new SimulationConfig(0, 3);
                }
                else if(choice == 2){
                    config = new SimulationConfig(1, 3);
                }
                else config = config();

                scheduler.simulate(processes, config);
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