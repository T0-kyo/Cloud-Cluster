import java.util.*;

public interface IScheduler{

    public List<SimulationResult> simulate(List<Process> processes, SimulationConfig config);
}