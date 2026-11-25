import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    // ---- input ----
    private final DefaultTableModel inputModel =
            new DefaultTableModel(new String[]{"Name", "Arrival", "Burst", "Priority"}, 0);
    private final JTable inputTable = new JTable(inputModel);
    private final TableColumn priorityColumn;
    private boolean priorityVisible = true;

    private final JComboBox<String> algoBox = new JComboBox<>(new String[]{"SJF", "ACS"});
    private final JSpinner csSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner qSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));

    // ---- output ----
    private final GanttPanel gantt = new GanttPanel();
    private final DefaultTableModel resultModel =
            new DefaultTableModel(new String[]{"Process", "Arrival", "Finish", "TAT", "WT", "RT"}, 0);
    private final JLabel averagesLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea(8, 40);

    public MainFrame() {
        super("Cloud Cluster Scheduler Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        priorityColumn = inputTable.getColumnModel().getColumn(3);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        algoBox.addActionListener(e -> updateAlgoUI());
        loadTestCase(1);
        updateAlgoUI();

        setSize(950, 780);
        setLocationRelativeTo(null);
    }

    // ================= UI building =================

    private JComponent buildTopPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.add(new JLabel("Scheduler:"));
        p.add(algoBox);
        p.add(new JLabel("Context switch (CS):"));
        p.add(csSpinner);
        p.add(new JLabel("Quantum (Q):"));
        p.add(qSpinner);

        JButton run = new JButton("Run");
        run.addActionListener(e -> runSimulation());
        p.add(run);
        return p;
    }

    private JComponent buildCenter() {
        // --- input table + buttons ---
        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Processes"));
        inputPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove selected");
        JButton t1 = new JButton("Test case 1");
        JButton t2 = new JButton("Test case 2");

        add.addActionListener(e ->
                inputModel.addRow(new Object[]{"P" + (inputModel.getRowCount() + 1), 0, 1, 1}));
        remove.addActionListener(e -> {
            if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
            int row = inputTable.getSelectedRow();
            if (row >= 0) inputModel.removeRow(inputTable.convertRowIndexToModel(row));
        });
        t1.addActionListener(e -> loadTestCase(1));
        t2.addActionListener(e -> loadTestCase(2));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(t1);
        buttons.add(t2);
        inputPanel.add(buttons, BorderLayout.SOUTH);

        // --- gantt ---
        JPanel ganttWrap = new JPanel(new BorderLayout());
        ganttWrap.setBorder(BorderFactory.createTitledBorder("Gantt chart"));
        gantt.setPreferredSize(new Dimension(800, 100));
        ganttWrap.add(gantt, BorderLayout.CENTER);

        // --- results table + averages ---
        JTable resultTable = new JTable(resultModel);
        resultTable.setEnabled(false);
        JPanel resultPanel = new JPanel(new BorderLayout(4, 4));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        resultPanel.add(averagesLabel, BorderLayout.SOUTH);

        // --- log ---
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Simulation log"));

        JSplitPane resultsAndLog = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultPanel, logScroll);
        resultsAndLog.setResizeWeight(0.5);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.add(ganttWrap, BorderLayout.NORTH);
        bottom.add(resultsAndLog, BorderLayout.CENTER);

        JSplitPane all = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, bottom);
        all.setResizeWeight(0.3);
        return all;
    }

    // ================= logic =================

    private boolean isSjf() {
        return "SJF".equals(algoBox.getSelectedItem());
    }

    /** SJF ignores priority and quantum, so hide / disable them. */
    private void updateAlgoUI() {
        boolean sjf = isSjf();
        qSpinner.setEnabled(!sjf);
        if (sjf && priorityVisible) {
            inputTable.removeColumn(priorityColumn);
            priorityVisible = false;
        } else if (!sjf && !priorityVisible) {
            inputTable.addColumn(priorityColumn);
            priorityVisible = true;
        }
    }

    /** Same data as Main.testCaseProcesses(): test case 1 -> CS 0, test case 2 -> CS 1. */
    private void loadTestCase(int choice) {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
        inputModel.setRowCount(0);
        if (choice == 1) {
            inputModel.addRow(new Object[]{"P1", 0, 4, 2});
            inputModel.addRow(new Object[]{"P2", 1, 7, 3});
            inputModel.addRow(new Object[]{"P3", 3, 2, 1});
            csSpinner.setValue(0);
        } else {
            inputModel.addRow(new Object[]{"P1", 0, 5, 4});
            inputModel.addRow(new Object[]{"P2", 1, 6, 2});
            inputModel.addRow(new Object[]{"P3", 4, 4, 3});
            csSpinner.setValue(1);
        }
        qSpinner.setValue(3);
    }

    /** Builds FRESH Process objects every run (the schedulers modify them). */
    private ArrayList<Process> readProcesses(boolean sjf) {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();

        ArrayList<Process> list = new ArrayList<>();
        for (int i = 0; i < inputModel.getRowCount(); i++) {
            String name = String.valueOf(inputModel.getValueAt(i, 0)).trim();
            try {
                int arrival = Integer.parseInt(String.valueOf(inputModel.getValueAt(i, 1)).trim());
                int burst = Integer.parseInt(String.valueOf(inputModel.getValueAt(i, 2)).trim());
                if (name.isEmpty() || arrival < 0 || burst <= 0)
                    throw new NumberFormatException();

                if (sjf) {
                    list.add(new Process(name, arrival, burst));              // SJF constructor
                } else {
                    int priority = Integer.parseInt(String.valueOf(inputModel.getValueAt(i, 3)).trim());
                    list.add(new Process(name, arrival, burst, priority));    // ACS constructor
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Row " + (i + 1) + ": name required, arrival >= 0, burst > 0, numbers only.");
            }
        }
        if (list.isEmpty()) throw new IllegalArgumentException("Add at least one process.");
        return list;
    }

    private void runSimulation() {
        try {
            boolean sjf = isSjf();
            ArrayList<Process> processes = readProcesses(sjf);

            int cs = (Integer) csSpinner.getValue();
            int q = sjf ? 0 : (Integer) qSpinner.getValue();      // SJF: SimulationConfig(cs, 0)
            SimulationConfig config = new SimulationConfig(cs, q);

            IScheduler scheduler = sjf ? new SJF() : new ACS();
            List<SimulationResult> result = scheduler.simulate(processes, config);

            // ----- log (same format as Main) -----
            logArea.setText("");
            for (SimulationResult r : result) {
                if (sjf) {
                    String running = (r.getName() == null || r.getName().isEmpty()) ? "Idle" : r.getName();
                    logArea.append("Time " + r.getTime() + " | Running: " + running
                            + " | Ready: " + readyQueue(r.getTier1Processes()) + "\n");
                } else {
                    logArea.append(r + "\n");
                }
            }
            logArea.setCaretPosition(0);

            // ----- gantt -----
            gantt.setSlices(buildSlices(result, processes));

            // ----- results table + averages (same as Main.printMetrics) -----
            resultModel.setRowCount(0);
            double totalWT = 0, totalTAT = 0, totalRT = 0;
            for (Process p : processes) {
                resultModel.addRow(new Object[]{
                        p.getName(), p.getArrivalTime(), p.getFinishTime(),
                        p.getTurnAroundTime(), p.getWaitingTime(), p.getResponseTime()});
                totalWT += p.getWaitingTime();
                totalTAT += p.getTurnAroundTime();
                totalRT += p.getResponseTime();
            }
            int n = processes.size();
            averagesLabel.setText(String.format(
                    "  Avg TAT: %.2f    Avg WT: %.2f    Avg RT: %.2f",
                    totalTAT / n, totalWT / n, totalRT / n));

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Simulation failed: " + ex,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String readyQueue(List<Process> list) {
        if (list == null || list.isEmpty()) return "[Empty]";
        List<String> items = new ArrayList<>();
        for (Process p : list) {
            items.add(p.getName() + " (remaining: " + p.getRemainingTime() + ")");
        }
        return items.toString();
    }

    // ================= Gantt =================

    /** One bar on the chart: who ran, from when to when. */
    private static class Slice {
        final String name;
        final int start, end;
        Slice(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Each SimulationResult says "at time t, X is running". X keeps the CPU until the
     * next result's time; the last one runs until the latest process finish time.
     * Consecutive results with the same name are merged into one bar.
     */
    private List<Slice> buildSlices(List<SimulationResult> results, List<Process> processes) {
        int maxFinish = 0;
        for (Process p : processes) maxFinish = Math.max(maxFinish, p.getFinishTime());

        List<Slice> out = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            SimulationResult r = results.get(i);
            String name = r.getName() == null ? "" : r.getName();
            int start = r.getTime();
            int end = (i + 1 < results.size()) ? results.get(i + 1).getTime() : maxFinish;
            if (end <= start) continue;

            Slice last = out.isEmpty() ? null : out.get(out.size() - 1);
            if (last != null && last.name.equals(name) && last.end == start) {
                out.set(out.size() - 1, new Slice(name, last.start, end));
            } else {
                out.add(new Slice(name, start, end));
            }
        }
        return out;
    }

    private static class GanttPanel extends JPanel {
        private static final Color[] PALETTE = {
                new Color(0x8ECAE6), new Color(0xFFB703), new Color(0xB5E48C),
                new Color(0xF4A6A6), new Color(0xCDB4DB), new Color(0xFFD6A5),
                new Color(0x9BF6FF), new Color(0xFDFFB6), new Color(0xBDB2FF),
                new Color(0xCAFFBF)
        };

        private List<Slice> slices = new ArrayList<>();
        private final Map<String, Color> colors = new HashMap<>();

        GanttPanel() {
            setBackground(Color.WHITE);
        }

        void setSlices(List<Slice> slices) {
            this.slices = slices;
            colors.clear();
            int next = 0;
            for (Slice s : slices) {
                if (!s.name.isEmpty() && !colors.containsKey(s.name)) {
                    colors.put(s.name, PALETTE[next++ % PALETTE.length]);
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (slices.isEmpty()) {
                g.setColor(Color.GRAY);
                g.drawString("Press Run to see the timeline.", 20, 40);
                return;
            }

            int left = 20, top = 15, barH = 40;
            int total = slices.get(slices.size() - 1).end;
            double unit = (getWidth() - 2 * left) / (double) total;
            FontMetrics fm = g.getFontMetrics();

            for (Slice s : slices) {
                int x = left + (int) Math.round(s.start * unit);
                int w = Math.max(1, (int) Math.round((s.end - s.start) * unit));
                boolean idle = s.name.isEmpty();

                g.setColor(idle ? new Color(0xDDDDDD) : colors.get(s.name));
                g.fillRect(x, top, w, barH);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, top, w, barH);

                String label = idle ? "Idle" : s.name;
                if (fm.stringWidth(label) + 4 <= w) {
                    g.drawString(label, x + (w - fm.stringWidth(label)) / 2, top + barH / 2 + 5);
                }
                g.drawString(String.valueOf(s.start), x, top + barH + 16);
            }
            g.drawString(String.valueOf(total),
                    left + (int) Math.round(total * unit), top + barH + 16);
        }
    }
}
