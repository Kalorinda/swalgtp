/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.sprayandwait;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Kalorinda pada kelas ini algoritma diberi tambahan interval waktu
 * node dianggap hidup
 */
public class SprayAndWaitRouterWithTaxiProblemDiTambah implements RoutingDecisionEngine, CountingTaxiProblem {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouterWithTaxiProblemDiTambah";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";
    public static final String LIVE_INTERVAL = "live_Interval";
    private static double IntervalLive = 21600;
    private double interval;
    protected int initialNrofCopies;
    protected boolean isBinary;
    private int totalEstimationOfTheNode;
    protected Map<DTNHost, Double> theCollections;
    protected Set<DTNHost> theTomstonenodes;

    public SprayAndWaitRouterWithTaxiProblemDiTambah(Settings s) {

        if (s.contains(LIVE_INTERVAL)) {
            interval = s.getInt(LIVE_INTERVAL);
        } else {
            interval = IntervalLive;
        }
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            isBinary = false; // default value
        }

        if (s.contains(NROF_COPIES)) {
            initialNrofCopies = s.getInt(NROF_COPIES);
        }

        theCollections = new HashMap<DTNHost, Double>();
        theTomstonenodes = new HashSet<>();

    }

    public SprayAndWaitRouterWithTaxiProblemDiTambah(SprayAndWaitRouterWithTaxiProblemDiTambah proto) {
        this.initialNrofCopies = proto.initialNrofCopies;
        this.isBinary = proto.isBinary;
        theCollections = new HashMap<DTNHost, Double>();
        theTomstonenodes = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        SprayAndWaitRouterWithTaxiProblemDiTambah partner = getOtherDecisionEngine(peer);

        if (thisHost.isRadioActive() == true && peer.isRadioActive() == true) {
            //sebelum melakukan perhitungan estimasi maka ke dua node akan melakukan pengecekan terlebih dahulu
            //node-node mana saja yang melebihi interval waktu node dianggap hidup maka node tersebut akan dianggap mati
            for (Map.Entry<DTNHost, Double> entry : this.theCollections.entrySet()) {
                DTNHost key = entry.getKey();
                Double lastRecord = entry.getValue();
                if (SimClock.getTime() - lastRecord > IntervalLive) {
                    this.theTomstonenodes.add(key);
                } else {
                    //jika waktu node bertemu kurang dari interval maka node tersebut akan dianggap hidup kembali
                    this.theTomstonenodes.remove(key);
                }
            }

            //sebelum melakukan perhitungan estimasi maka ke dua node akan melakukan pengecekan terlebih dahulu
            //node-node mana saja yang melebihi interval waktu node dianggap hidup maka node tersebut akan dianggap mati
            for (Map.Entry<DTNHost, Double> entry : partner.theCollections.entrySet()) {
                DTNHost key = entry.getKey();
                Double lastRecord = entry.getValue();
                if (SimClock.getTime() - lastRecord > IntervalLive) {
                    partner.theTomstonenodes.add(key);
                } else {
                    //jika waktu node bertemu kurang dari interval maka node tersebut akan dianggap hidup kembali
                    partner.theTomstonenodes.remove(key);
                }
            }

            //hasil estimasi akan dikurangkan dengan jumlah node dianggap mati
            this.totalEstimationOfTheNode = this.CountTotalEstimationOfTheNode() - this.theTomstonenodes.size();
            partner.totalEstimationOfTheNode = partner.CountTotalEstimationOfTheNode() - partner.theTomstonenodes.size();

        }

        System.out.println(thisHost.getAddress() + " Counting = " + this.totalEstimationOfTheNode);
        System.out.println(peer.getAddress() + " Counting = " + partner.totalEstimationOfTheNode);
        System.out.println("");
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost thisHost = con.getOtherNode(peer);
        SprayAndWaitRouterWithTaxiProblemDiTambah partner = getOtherDecisionEngine(peer);
        //jika kedua interface aktif maka keduanya akan melakukan pengecekan 
        if (thisHost.isRadioActive() == true && peer.isRadioActive() == true) {
            //jika kedua node sudah pernah bertemu maka informasi akan diupdate dengan informasi yang baru
            if (this.theCollections.containsKey(peer) && partner.theCollections.containsKey(thisHost)) {
                this.theCollections.replace(peer, SimClock.getTime());
                partner.theCollections.replace(thisHost, SimClock.getTime());
            } else {
                //jika kedua node baru pertama kali bertemu maka keduanya akan langsung mencatat unique id dan waktu bertemu
                this.theCollections.put(peer, SimClock.getTime());
                partner.theCollections.put(thisHost, SimClock.getTime());
            }

            //this ngecek partner    
            //setelah selesai mencata maka kedua node akan bertkar informasi
            for (Map.Entry<DTNHost, Double> entry : partner.theCollections.entrySet()) {
                DTNHost key = entry.getKey();
                Double value = entry.getValue();
                //jika node belum ada dipenyimpanan maka langsung memasukan tetangga dan waktu bertemu
                if (!this.theCollections.containsKey(key)) {
                    this.theCollections.put(key, value);

                } else {
                    Double myValue = this.theCollections.get(key);
                    //jika node sudah ada dipenyimpanan maka akan dicek waktu bertemu, waktu bertemu terbaru yang akan dipakai
                    if (myValue < value) {
                        this.theCollections.put(key, value);
                    }
                }
            }

            //partner ngecek this
            for (Map.Entry<DTNHost, Double> entry : this.theCollections.entrySet()) {
                DTNHost key = entry.getKey();
                Double value = entry.getValue();
                //jika node belum ada dipenyimpanan maka langsung memasukan tetangga dan waktu bertemu
                if (!partner.theCollections.containsKey(key)) {
                    partner.theCollections.put(key, value);

                } else {
                    Double myValue = partner.theCollections.get(key);
                    //jika node sudah ada dipenyimpanan maka akan dicek waktu bertemu, waktu bertemu terbaru yang akan dipakai
                    if (myValue < value) {
                        partner.theCollections.put(key, value);
                    }
                }
            }
        }

    }

    @Override
    public boolean newMessage(Message m) {
        //Hasil dari estimasi akan digunakan sebagai L copy dengan 1/2 dari jumlah node hasil estimasi
        initialNrofCopies = (int) Math.ceil(this.totalEstimationOfTheNode / 2);
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            nrofCopies = 1;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (nrofCopies > 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return false;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public RoutingDecisionEngine replicate() {

        return new SprayAndWaitRouterWithTaxiProblemDiTambah(this);
    }

    private SprayAndWaitRouterWithTaxiProblemDiTambah getOtherDecisionEngine(DTNHost h) {

        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + "with other routers of same type";

        return (SprayAndWaitRouterWithTaxiProblemDiTambah) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    //fungsi method ini adalah untuk perhitungan rumus taxi problem
    //pertama kali mencari unique id tertinggi yang ada dipenyimpanan
    //selanjutnya menghitung dengan rumus taxi problem
    private int CountTotalEstimationOfTheNode() {
        int tracehold = Integer.MIN_VALUE;
        for (Map.Entry<DTNHost, Double> entry : theCollections.entrySet()) {
            DTNHost key = entry.getKey();
            Double value = entry.getValue();
            if (key.getAddress() > tracehold) {
                tracehold = key.getAddress();
            }
        }
        double atas = Math.pow(tracehold, (theCollections.size() + 1)) - Math.pow((tracehold - 1), (theCollections.size() + 1));
        double bawah = Math.pow(tracehold, theCollections.size()) - Math.pow((tracehold - 1), theCollections.size());
        return (int) (Math.floor(atas / bawah));

    }

    //method yang digunakan untuk report
    @Override
    public int getCountTotalEstimationOfTheNode() {
        return this.totalEstimationOfTheNode;
    }

}
