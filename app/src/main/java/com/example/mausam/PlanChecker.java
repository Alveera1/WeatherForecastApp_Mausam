package com.example.mausam;

import java.util.Locale;

/**
 * Rule engine for the "Plans" feature. Pure Java (no Android classes) so it can be
 * unit-tested with hardcoded inputs before being wired to anything live.
 */
public class PlanChecker {

    public enum Verdict { SAFE, CAUTION, RISKY }

    public static class Result {
        public final Verdict verdict;
        public final String message;

        public Result(Verdict verdict, String message) {
            this.verdict = verdict;
            this.message = message;
        }

        /** Value to store in the Firestore "status" field: "safe" or "risky". */
        public String toStatus() {
            return verdict == Verdict.SAFE ? "safe" : "risky";
        }

        public boolean isRisky() {
            return verdict == Verdict.RISKY;
        }

        /** Badge for the Plans screen. */
        public String badge() {
            switch (verdict) {
                case RISKY:   return "❌";
                case CAUTION: return "⚠️";
                default:      return "✅";
            }
        }
    }

    /**
     * @param activity    e.g. "Outdoor Picnic", "Travel", "Trek"
     * @param condition   OpenWeatherMap "main" value: Clear, Clouds, Rain, Drizzle, Thunderstorm, Snow...
     * @param windSpeedKmh wind speed in km/h (convert from m/s with * 3.6)
     * @param temp        temperature in °C
     */
    public static Result evaluateDetailed(String activity, String condition, double windSpeedKmh, double temp) {
        String act = activity == null ? "" : activity.toLowerCase(Locale.ROOT);
        String cond = condition == null ? "" : condition.toLowerCase(Locale.ROOT);

        boolean outdoor = containsAny(act, "outdoor", "picnic", "trek", "hike", "hiking", "camp",
                "sport", "cricket", "football", "match", "run", "cycling", "wedding", "party", "walk");
        boolean travel = containsAny(act, "travel", "trip", "drive", "road", "flight", "journey");

        boolean stormy = cond.contains("thunderstorm");
        boolean wet = cond.contains("rain") || cond.contains("drizzle");
        boolean snow = cond.contains("snow");

        if (stormy && (outdoor || travel)) {
            return new Result(Verdict.RISKY, "Thunderstorm expected. Please reschedule your plan.");
        }
        if (outdoor && (wet || snow)) {
            return new Result(Verdict.RISKY, "Rain expected. Consider rescheduling or moving indoors.");
        }
        if (travel && windSpeedKmh > 40) {
            return new Result(Verdict.RISKY, "High winds. Travel may be affected.");
        }
        if (travel && (wet || snow)) {
            return new Result(Verdict.CAUTION, "Wet roads likely. Allow extra travel time.");
        }
        if (outdoor && windSpeedKmh > 40) {
            return new Result(Verdict.RISKY, "Strong winds. Outdoor plans may be affected.");
        }
        if (temp > 38) {
            return new Result(Verdict.CAUTION, "Very hot conditions. Stay hydrated.");
        }
        if (temp < 5 && (outdoor || travel)) {
            return new Result(Verdict.CAUTION, "Very cold conditions. Dress warmly.");
        }
        return new Result(Verdict.SAFE, "Conditions look fine for your plan.");
    }

    /**
     * String version agreed with Person 3's worker: returns "RISKY: ...", "CAUTION: ..." or "SAFE: ...".
     * Wind must be in km/h.
     */
    public static String evaluate(String activity, String condition, double windSpeedKmh, double temp) {
        Result r = evaluateDetailed(activity, condition, windSpeedKmh, temp);
        return r.verdict.name() + ": " + r.message;
    }

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}