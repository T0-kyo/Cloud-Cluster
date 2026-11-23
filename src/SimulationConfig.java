public class SimulationConfig{
    
    private final int cs;
    private final int q;

    public SimulationConfig(int cs, int q){
        this.cs = cs;
        this.q = q;
    }

    public int getCS(){
        return cs;
    }

    public int getQ(){
        return q;
    }
}