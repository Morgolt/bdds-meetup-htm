package com.epam.bdcc.meetup.htm;

import org.numenta.nupic.Parameters;
import org.numenta.nupic.algorithms.Anomaly;

import java.util.HashMap;
import java.util.Map;

public class HTMParameters {

    private static Parameters getSpatialPoolerParameters() {
        Parameters spParams = Parameters.getSpatialDefaultParameters();
        spParams.set(Parameters.KEY.MAX_BOOST, 1.0);
        spParams.set(Parameters.KEY.COLUMN_DIMENSIONS, new int[]{2048});
        spParams.set(Parameters.KEY.GLOBAL_INHIBITION, true);
        spParams.set(Parameters.KEY.INPUT_DIMENSIONS, new int[]{2048});
        spParams.set(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0);
        spParams.set(Parameters.KEY.POTENTIAL_PCT, 0.8);
        spParams.set(Parameters.KEY.SEED, 1956);
        spParams.set(Parameters.KEY.SYN_PERM_ACTIVE_INC, 0.0001);
        spParams.set(Parameters.KEY.SYN_PERM_CONNECTED, 0.1);
        spParams.set(Parameters.KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
        return spParams;
    }

    private static Parameters getTemporalMemoryParameters() {
        Parameters tmParams = Parameters.getTemporalDefaultParameters();
        tmParams.set(Parameters.KEY.ACTIVATION_THRESHOLD, 6);
        tmParams.set(Parameters.KEY.CELLS_PER_COLUMN, 32);
        tmParams.set(Parameters.KEY.COLUMN_DIMENSIONS, new int[]{2048});
        tmParams.set(Parameters.KEY.CONNECTED_PERMANENCE, 0.5);
        tmParams.set(Parameters.KEY.INITIAL_PERMANENCE, 0.21);
        tmParams.set(Parameters.KEY.INPUT_DIMENSIONS, new int[]{2048});
        tmParams.set(Parameters.KEY.MAX_SEGMENTS_PER_CELL, 128);
        tmParams.set(Parameters.KEY.MAX_SYNAPSES_PER_SEGMENT, 32);
        tmParams.set(Parameters.KEY.MIN_THRESHOLD, 3);
        tmParams.set(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, 20);
        tmParams.set(Parameters.KEY.PERMANENCE_DECREMENT, 0.1);
        tmParams.set(Parameters.KEY.PERMANENCE_INCREMENT, 0.1);
        tmParams.set(Parameters.KEY.SEED, 1960);
        return tmParams;
    }

    public static Map<String, Object> getAnomalyParameters() {
        Map<String, Object> anomalyParams = new HashMap<>();
        anomalyParams.put(Anomaly.KEY_HIST_VALUES, 2184);
        anomalyParams.put(Anomaly.KEY_MODE, Anomaly.Mode.PURE);
        return anomalyParams;
    }

    public static Parameters getNetworkParameters() {
        return getSpatialPoolerParameters()
                .union(getTemporalMemoryParameters());
    }

    private static Map<String, Map<String, Object>> setupMap(
            Map<String, Map<String, Object>> map,
            int n, int w, double min, double max, double radius, double resolution, Boolean periodic,
            Boolean clip, Boolean forced, String fieldName, String fieldType, String encoderType) {

        if (map == null) {
            map = new HashMap<>();
        }
        Map<String, Object> inner = null;
        if ((inner = map.get(fieldName)) == null) {
            map.put(fieldName, inner = new HashMap<>());
        }

        inner.put("n", n);
        inner.put("w", w);
        inner.put("minVal", min);
        inner.put("maxVal", max);
        inner.put("radius", radius);
        inner.put("resolution", resolution);

        if (periodic != null) inner.put("periodic", periodic);
        if (clip != null) inner.put("clipInput", clip);
        if (forced != null) inner.put("forced", forced);
        if (fieldName != null) inner.put("fieldName", fieldName);
        if (fieldType != null) inner.put("fieldType", fieldType);
        if (encoderType != null) inner.put("encoderType", encoderType);

        return map;
    }
}
