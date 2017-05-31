package com.mobileappscompany.training.nthreads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    TextView tV;
    Subscription myS;  //for RxJava

    IntentFilter iFilter; // for BR
    BroadcastReceiver br; // for BR

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tV = (TextView) findViewById(R.id.textView);

    }

    ////////////////////////  Thread  \\\\\\\\\\\\\\\\\\\\\\\\\\

    public void onThread(View view) {
        new Thread(){
            public void run(){
                try {
                    Thread.sleep(4000);
                    tV.post(new Runnable() {
                        @Override
                        public void run() {
                            tV.setText("Done with Thread");
                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    ////////////////////////  Thread  \\\\\\\\\\\\\\\\\\\\\\\\\\
    ////////////////////////  AsyncTask \\\\\\\\\\\\\\\\\\\\\\\\\\

    public void onAT(View view) {

        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                int tTS = params[0] *1000;
                try {
                    Thread.sleep(tTS);
                    return "Done with AsyncTask " +tTS;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "AsyncTask error";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tV.setText(s);
            }
        }.execute(3);

    }



    ////////////////////////  AsyncTask \\\\\\\\\\\\\\\\\\\\\\\\\\
    ////////////////////////  Event Bus \\\\\\\\\\\\\\\\\\\\\\\\\\
    public class  MyEvent{
        private  String mMessage;

        public MyEvent(String message) {
            mMessage = message;
        }

        public String getMessage() {
            return mMessage;
        }
    }


    public void onEventBus(View view) {

        new Thread(){
            public void run(){
                try {
                    Thread.sleep(4000);
                    EventBus.getDefault().post(new MyEvent("Done with EventBus"));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void myEvH(MyEvent e){
        tV.setText(e.getMessage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //////////////////////// Event Bus  \\\\\\\\\\\\\\\\\\\\\\\\\\
    ////////////////////////  RxJava \\\\\\\\\\\\\\\\\\\\\\\\\\


    public void onRxJava(View view) {
        Observable<Integer> myO = Observable.fromCallable(
                new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        Thread.sleep(4000);
                        return 4;
                    }});
        myS = myO
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onNext(Integer integer) {
                        tV.setText("Done with RxJava " + integer);
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!myS.isUnsubscribed()){
            myS.unsubscribe();
        }
    }

    ////////////////////////  RxJava \\\\\\\\\\\\\\\\\\\\\\\\\\
    //////////////////////// BR  \\\\\\\\\\\\\\\\\\\\\\\\\\


    public void onBR(View view) {

        iFilter = new IntentFilter("com.mobileappscompany.training.nthreads.BRThing");

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tV.setText(intent.getStringExtra("DoneBRKey"));
            }
        };

        registerReceiver(br, iFilter);

        new Thread(){
            public void run(){
                try {
                    Thread.sleep(5000);
                    Intent i = new Intent("com.mobileappscompany.training.nthreads.BRThing");
                    i.putExtra("DoneBRKey", "Done with BR");
                    sendBroadcast(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }

    ////////////////////////  BR \\\\\\\\\\\\\\\\\\\\\\\\\\
}
