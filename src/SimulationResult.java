import java.util.*;

public class SimulationResult{

    private final int time;
    private final String name;
    private final List<Process> tier1Processes;
    private final List<Process> tier2Processes;

    
    public SimulationResult(int time, String name, List<Process> tier1Processes, List<Process> tier2Processes){
        this.time = time;
        this.name = name;
        this.tier1Processes = tier1Processes;
        this.tier2Processes = tier2Processes;
    }

    public int getTime(){
        return time;
    }

    public String getName(){
        return name;
    }

    public List<Process> getTier1Processes(){
        return tier1Processes;
    }

    public List<Process> getTier2Processes(){
        return tier2Processes;
    }
}