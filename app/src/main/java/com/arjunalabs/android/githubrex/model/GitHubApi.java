package com.arjunalabs.android.githubrex.model;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by bobbyadiprabowo on 2/3/15.
 */
public interface GitHubApi {

    @GET("/repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);

    @GET("/repos/{owner}/{repo}/contributors")
    Observable<List<Contributor>> observableContributors(
            @Path("owner") String owner,
            @Path("repo") String repo);

    @GET("/users/{user}")
    Observable<User> user(
      @Path("user") String user
    );

}
