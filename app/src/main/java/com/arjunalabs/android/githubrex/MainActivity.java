package com.arjunalabs.android.githubrex;

import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.arjunalabs.android.githubrex.model.Contributor;
import com.arjunalabs.android.githubrex.model.GitHubApi;
import com.arjunalabs.android.githubrex.model.User;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private final String REPO_USER = "ReactiveX";
    private final String REPO_NAME = "RxJava";
    private TextView repoTextView;
    private TextView contributionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        repoTextView = (TextView) findViewById(R.id.text_repos);
        contributionTextView = (TextView) findViewById(R.id.text_contributors);

        repoTextView.setText("Fetching from : " + REPO_USER + "/" + REPO_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setClient(new OkClient())
                .build();

        final GitHubApi gitHubApi = restAdapter.create(GitHubApi.class);

        /*

        // ini ga mungkin jalan kan, krn di main thread
        List<Contributor> contributorList = gitHubApi.contributors(REPO_USER,REPO_NAME);
        for (Contributor contributor: contributorList) {
            contributionTextView.append(contributor.contributions + "\t" +contributor.login);
            contributionTextView.append("\n");
        }
        */

        /*

        // basic version
        Observable<List<Contributor>> obserVableContributorList = gitHubApi.observableContributors(REPO_USER, REPO_NAME);
        obserVableContributorList
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Contributor>>() {
                    @Override
                    public void call(List<Contributor> contributors) {
                        contributionTextView.setText("");
                        for (Contributor contributor: contributors) {
                            contributionTextView.append(contributor.contributions + "\t" +contributor.login);
                            contributionTextView.append("\n");
                        }
                    }
                });
         */



        Observable<List<Contributor>> observableContributorList = gitHubApi.observableContributors(REPO_USER, REPO_NAME);
        observableContributorList
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .lift(new Observable.Operator<Contributor, List<Contributor>>() {
                    @Override
                    public Subscriber<? super List<Contributor>> call(final Subscriber<? super Contributor> subscriber) {

                        return new Subscriber<List<Contributor>>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(List<Contributor> contributors) {
                                for (Contributor contributor: contributors) {
                                    subscriber.onNext(contributor);
                                }
                            }
                        };
                    }
                })
                .forEach(new Action1<Contributor>() {
                    @Override
                    public void call(Contributor contributor) {
                        Log.i("contributor", contributor.login);
                        contributionTextView.append(contributor.contributions + "\t" + contributor.login);
                        contributionTextView.append("\n");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        contributionTextView.setText(throwable.getMessage());
                    }
                });


        /*
        more advanced but sometimes is forbidden by the gitHub API
        403

        Observable<List<Contributor>> observableContributorList = gitHubApi.observableContributors(REPO_USER, REPO_NAME);
        observableContributorList
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .lift(new Observable.Operator<Contributor, List<Contributor>>() {
                    @Override
                    public Subscriber<? super List<Contributor>> call(final Subscriber<? super Contributor> subscriber) {

                        return new Subscriber<List<Contributor>>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(List<Contributor> contributors) {
                                for (Contributor contributor : contributors) {
                                    subscriber.onNext(contributor);
                                }
                            }
                        };
                    }
                })
                .flatMap(new Func1<Contributor, Observable<User>>() {
                    @Override
                    public Observable<User> call(Contributor contributor) {
                        Log.i("contributor", contributor.login);
                        return gitHubApi.user(contributor.login);
                    }
                })

                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {
                        contributionTextView.append("Done");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", e.toString());
                        contributionTextView.append(e.toString());
                        onCompleted();
                    }

                    @Override
                    public void onNext(User user) {
                        Log.i("user", user.name);
                        contributionTextView.append(user.name);
                        contributionTextView.append("\n");
                    }
                });
                */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
