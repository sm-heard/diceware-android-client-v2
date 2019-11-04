/*
 *  Copyright 2019 Nicholas Bennett & Deep Dive Coding/CNM Ingenuity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.diceware.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cnm.deepdive.diceware.BuildConfig;
import edu.cnm.deepdive.diceware.model.Passphrase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Declaration of proxy methods used to connect to Diceware server application, with
 * singleton-pattern-based instantiation of Retrofit-generated implementation.
 */
public interface DicewareService {

  /**
   * Requests all passphrases associated with the currently logged-in user.
   *
   * @param token OAuth2.0 token.
   * @return observable list of passphrases.
   */
  @GET("passphrases/")
  Observable<List<Passphrase>> getAll(@Header("Authorization") String token);

  /**
   * Requests a single passphrase of the currently logged-in user, with the specified ID.
   *
   * @param token OAuth2.0 token.
   * @param id unique numeric identifier of passphrase.
   * @return observable result.
   */
  @GET("passphrases/{id}")
  Single<Passphrase> get(@Header("Authorization") String token,
      @Path("id") long id);

  /**
   * Requests a single passphrase of the currently logged-in user, with the specified key.
   *
   * @param token OAuth2.0 token.
   * @param key unique {@link String} identifier of passphrase.
   * @return observable result.
   */
  @GET("passphrases/{key}")
  Single<Passphrase> get(@Header("Authorization") String token,
      @Path("key") String key);

  /**
   * Requests deletion of the specified passphrase associated with the currently logged-in user.
   *
   * @param token OAuth2.0 token.
   * @param id unique numeric identifier of passphrase.
   * @return observable success/failure result.
   */
  @DELETE("passphrases/{id}")
  Completable delete(@Header("Authorization") String token, @Path("id") long id);

  /**
   * Sends an updated passphrase, associated with the currently logged-in user, to the server.
   *
   * @param token OAuth2.0 token.
   * @param id unique numeric identifier of passphrase.
   * @param passphrase updated {@link Passphrase} instance.
   * @return observable result.
   */
  @PUT("passphrases/{id}")
  Single<Passphrase> put(@Header("Authorization") String token, @Path("id") long id, @Body Passphrase passphrase);

  /**
   * Sends a new {@link Passphrase} to the server, for adding to the collection associated with the
   * currently logged-in user.
   *
   * @param token OAuth2.0 token.
   * @param passphrase new {@link Passphrase} instance.
   * @return observable result.
   */
  @POST("passphrases/")
  Single<Passphrase> post(@Header("Authorization") String token, @Body Passphrase passphrase);

  /**
   * Returns (constructing as necessary) the singleton instance of the Retrofit-generated instance
   * of this interface.
   *
   * @return singleton instance.
   */
  static DicewareService getInstance() {
    return InstanceHolder.INSTANCE;
  }

  class InstanceHolder {

    private static final DicewareService INSTANCE;

    static {
      // TODO Investigate logging interceptor issues.
      Gson gson = new GsonBuilder()
          .excludeFieldsWithoutExposeAnnotation()
          .create();
      Retrofit retrofit = new Retrofit.Builder()
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .addConverterFactory(GsonConverterFactory.create(gson))
          .baseUrl(BuildConfig.BASE_URL)
          .build();
      INSTANCE = retrofit.create(DicewareService.class);
    }

  }

}
