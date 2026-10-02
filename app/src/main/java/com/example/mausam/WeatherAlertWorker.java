package com.example.mausam;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mausam.models.Plan;
import com.example.mausam.utils.NotificationHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class WeatherAlertWorker extends Worker {

    public static final String WORK_NAME = "weather_alert_check";

    // Reuses Person 2's forecast + rule-engine logic instead of duplicating it here
    private final PlanWeatherCheck planWeatherCheck = new PlanWeatherCheck();

    public WeatherAlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // No user logged in; nothing to check
            return Result.success();
        }

        String uid = auth.getCurrentUser().getUid();
        long now = System.currentTimeMillis();

        try {
            // Synchronously fetch upcoming plans for the logged-in user
            QuerySnapshot snapshot = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("plans")
                            .whereEqualTo("userId", uid)
                            .whereGreaterThanOrEqualTo("dateTime", now)
                            .get()
            );

            for (Plan plan : snapshot.toObjects(Plan.class)) {
                checkPlanAndNotify(plan);
            }

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    /**
     * Fetches the forecast for this plan's location/time, runs PlanChecker on it,
     * and fires a notification if the verdict is RISKY or CAUTION.
     */
    private void checkPlanAndNotify(Plan plan) {
        PlanChecker.Result result = planWeatherCheck.check(
                plan.getActivity(), plan.getLocation(), plan.getDateTime());

        if (result == null) {
            // Forecast couldn't be fetched (too far out, network issue, etc.) — skip silently
            return;
        }

        if (result.verdict == PlanChecker.Verdict.RISKY
                || result.verdict == PlanChecker.Verdict.CAUTION) {
            NotificationHelper.sendWeatherAlert(
                    getApplicationContext(),
                    "⚠️ Weather Alert: " + plan.getActivity(),
                    result.message + " in " + plan.getLocation(),
                    (int) (plan.getDateTime() % Integer.MAX_VALUE)
            );
        }
    }

    /**
     * Schedules periodic background checking every 3 hours.
     * Call this once, e.g. from MainActivity.onCreate().
     */
    public static void schedulePeriodicWork(Context context) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeatherAlertWorker.class,
                3, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}