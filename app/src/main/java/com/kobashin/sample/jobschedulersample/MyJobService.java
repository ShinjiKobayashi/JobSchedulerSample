package com.kobashin.sample.jobschedulersample;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import hugo.weaving.DebugLog;

public class MyJobService extends JobService {
    public MyJobService() {
    }

    private final static ComponentName JOB_SERVICE_NAME =
            new ComponentName("com.kobashin.sample.jobschedulersample",
                    "com.kobashin.sample.jobschedulersample.MyJobService");

    private final static int JOB_ID = 0x01;


    @DebugLog
    public static void schedule(Context context) {
        // TODO: schedule job
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, JOB_SERVICE_NAME);

        // setBackOffCriteria(long initialBackoffMillis, int backoffPolicy)
        //     initialbackoffMillis: TODO
        //     backoffPolicy: 指定した間隔通りか、指数的に増えていくか
        builder.setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR);

        // setExtras(PersistableBundle)
        // PersistableBundleを利用して、onStartJob時に取り出すbundleを用意できる
        PersistableBundle bundle = new PersistableBundle();
        builder.setExtras(bundle);

        // setMinimumLatency()
        //     遅延可能時間?
        builder.setMinimumLatency(5000);

        // deadline?
        builder.setOverrideDeadline(20000);

//        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(true);

        scheduler.schedule(builder.build());
    }


    private JobParameters mParams;


    // ---- scheduled job interface

    @Override
    @DebugLog
    public boolean onStartJob(JobParameters params) {
        // 登録したJob実行タイミングで呼び出される
        // Jobの実処理を書く。ただし、メインスレッドで呼び出されるので、
        // 重たい処理は別Threadで行うこと。

        mParams = params;
        new Thread(new Runnable() {

            @Override
            @DebugLog
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mParams != null) {


                    Log.i("koba", "jobFinished");

                    // jobFinished(JobParameters params, boolean needsReschedule)
                    // params: Job開始時にonStartJob()の引数で受け取ったparamsを指定
                    // needsReschedule:
                    //     true JobInfo.Builder.setBackoffCriteria()で指定したback-off criteriaに従って
                    //          JobをRescheduleする
                    //     false Resucheduleしない
                    jobFinished(mParams, false);
                }
            }
        }).run();


        // 処理が継続している場合はtrueを返す。trueを返した場合は、処理終了時にjobFinished()をコールすること
        // 特に何もしていない場合はfalseを返す。
        return true;
    }

    @Override
    @DebugLog
    public boolean onStopJob(JobParameters params) {
        // 要求したJobの実行条件を満たさなくなった場合に呼び出される
        // これが呼び出された場合は jobFinished() を呼び出すべきである


        // trueの場合、jobをrescheduleする
        // returnした後の処理実行は保証されない
        return false;
    }
}
