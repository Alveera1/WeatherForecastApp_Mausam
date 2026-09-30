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
     * Bridge method for Person 2 (API / PlanChecker) & Person 3 (Alert Dispatch)
     */
    private void checkPlanAndNotify(Plan plan) {
        /*
         * Person 2 integration note:
         * Once Person 2 adds PlanChecker.java:
         *
         * String recommendation = PlanChecker.evaluate(plan.getActivity(), condition, windSpeed, temp);
         * if (recommendation.startsWith("RISKY") || recommendation.startsWith("CAUTION")) {
         *     NotificationHelper.sendWeatherAlert(
         *             getApplicationContext(),
         *             "⚠️ Plan Alert: " + plan.getActivity(),
         *             recommendation + " in " + plan.getLocation(),
         *             (int) (plan.getDateTime() % Integer.MAX_VALUE)
         *     );
         * }
         */

        if ("risky".equalsIgnoreCase(plan.getStatus())) {
            NotificationHelper.sendWeatherAlert(
                    getApplicationContext(),
                    "⚠️ Weather Alert: " + plan.getActivity(),
                    "Adverse weather predicted for your plan in " + plan.getLocation() + ".",
                    (int) (plan.getDateTime() % Integer.MAX_VALUE)
            );
        }
    }

    /**
     * Schedules periodic background checking every 3 hours.
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
