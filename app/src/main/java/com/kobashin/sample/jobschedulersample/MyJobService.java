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
import android.widget.Toast;

import hugo.weaving.DebugLog;

public class MyJobService extends JobService {
    public MyJobService() {
    }

    private final static ComponentName JOB_SERVICE_NAME =
            new ComponentName("com.kobashin.sample.jobschedulersample",
                    "com.kobashin.sample.jobschedulersample.MyJobService");

    private final static int JOB_ID = 0x01;

    public static void cancelJobs(Context context){
        // TODO: schedule job
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);

        //scheduler.cancelAll();
    }



    @DebugLog
    public static void schedule(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, JOB_SERVICE_NAME);

        // setBackOffCriteria(long initialBackoffMillis, int backoffPolicy)
        //     initialbackoffMillis: バックオフ時間算出の基準値
        //     backoffPolicy: BACKOFF_POLICY_LINEARかBACKOFF_POLICY_EXPONENTIALを指定
        //                    LINEARの時は current+initial * fail_count
        //                    EXPONENTIALの時は current + initial * 2 ^ (fail_count -1)
        //                    後にリトライされる。defaultは30sec, EXPONENTIAL。最長バックオフは5hr
        builder.setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR);

        // setExtras(PersistableBundle)
        // PersistableBundleを利用して、onStartJob時に取り出すbundleを用意できる
        PersistableBundle bundle = new PersistableBundle();
        builder.setExtras(bundle);

        // setMinimumLatency()
        //     実行可能になってからの最低遅延時間を設定する
        //     定期実行Jobには必要ないため、build()時にエラー扱いとなる
//        builder.setMinimumLatency(5000);

        // setOverrideDeadline()
        //     実行可能になってからの最大遅延時間を設定する
        //     定期実行Jobには必要ないため、build()時にエラー扱いとなる
//        builder.setOverrideDeadline(20000);


        // setPeriodic()
        //     定期実行を設定する。前のJobが終わってからの経過時間(Millis)を指定する
        //     Priodic指定した場合は、状態が変更されても継続実行される
//        builder.setPeriodic(10000);

        // setPersisted()
        //     再起動時にJobを実行継続させるかどうか。
        //     trueを設定した場合は、BOOT_COMPLETEDが無いとエラー扱いとなる
        builder.setPersisted(true);


        // setRequiredNetworkType()
        //      Jobの実行に必要なネットワーク形態を設定する
        //      NETWORK_TYPE_NONE: 指定なし（ありでもなしでも）
        //      NETWORK_TYPE_ANY: なんらかのネットワーク
        //      NETWORK_TYPE_UNMETERD: 従量制でないネットワーク
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);

        // Deviceがidle maintenance windowの時に実行するかどうか
        // idolかつbackoffCriteriaを設定するとexceptionを拾う
//        builder.setRequiresDeviceIdle(true);


        // Deviceが給電状態かどうかを設定する
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
                    Thread.sleep(10000);
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
        }).start();


        // 処理が継続している場合はtrueを返す。trueを返した場合は、処理終了時にjobFinished()をコールすること
        // 特に何もしていない場合はfalseを返す。
        return true;
    }

    @Override
    @DebugLog
    public boolean onStopJob(JobParameters params) {
        // 要求したJobの実行中に条件を満たさなくなった場合に呼び出される
        // これが呼び出された場合は jobFinished() を呼び出すべきである
        jobFinished(params, false);

        // trueの場合、jobをback-off設定値に合わせてrescheduleする
        // returnした後の処理実行は保証されない
        return false;
    }
}
