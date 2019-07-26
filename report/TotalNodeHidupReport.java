package report;

/**
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * <Simulation time> <average buffer occupancy % [0..100]> <variance>
 * </p>
 *
 */
import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 * Report untuk melihat jumlah node hidup per interval waktu
 */
public class TotalNodeHidupReport extends Report implements UpdateListener {
    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";
    public static final int DEFAULT_WAKTU = 1800;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    public TotalNodeHidupReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(NODE_PERWAKTU)) {
            interval = settings.getInt(NODE_PERWAKTU);
        } else {
            interval = DEFAULT_WAKTU;
        }
    }

    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }

  
    private void printLine(List<DTNHost> hosts) {
        double active = 0;
        for (DTNHost host : hosts) {
            MessageRouter r = host.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            boolean inter = host.isRadioActive();
            if (inter == true) {
                active++;
            }
        }
        String print = format((int) SimClock.getTime()) + " \t " + active;
        write(print);
    }
}
