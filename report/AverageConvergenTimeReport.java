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
import routing.sprayandwait.CountingTaxiProblem;

/**
 * Report untuk melihat hasi rata-rata hitung semua node per interval waktu
 */

public class AverageConvergenTimeReport extends Report implements UpdateListener {

    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";
    public static final int DEFAULT_WAKTU = 1800;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    public AverageConvergenTimeReport() {
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
        double rata = 0;
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            CountingTaxiProblem n = (CountingTaxiProblem) de;

            int temp = (int) n.getCountTotalEstimationOfTheNode();
            rata += temp;
        }
        double AV_Rata = rata / hosts.size();
        String output = format((int) SimClock.getTime()) + " \t " + format(AV_Rata);
        write(output);
    }
}
