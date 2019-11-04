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
package edu.cnm.deepdive.diceware.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.model.Passphrase;
import edu.cnm.deepdive.diceware.service.DicewareService;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Collections;
import java.util.List;

/**
 * Supplier of {@link LiveData} intended to be consumed by an instance of {@link
 * edu.cnm.deepdive.diceware.controller.MainActivity} (and any hosted fragments within).
 */
public class MainViewModel extends AndroidViewModel implements LifecycleObserver {

  private final DicewareService dicewareService;
  private final MutableLiveData<List<Passphrase>> passphrases;
  private final MutableLiveData<GoogleSignInAccount> account;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  /**
   * Initializes the {@link LiveData} and {@link CompositeDisposable} containers used by this
   * instance.
   *
   * @param application {@link Application} context.
   */
  public MainViewModel(@NonNull Application application) {
    super(application);
    dicewareService = DicewareService.getInstance();
    passphrases = new MutableLiveData<>();
    account = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
  }

  /**
   * Returns the observable list of {@link Passphrase} instances from the server-based collection.
   */
  public LiveData<List<Passphrase>> getPassphrases() {
    return passphrases;
  }

  /**
   * Returns the most recently thrown exception or error.
   */
  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  /**
   * Sets the currently logged-in user.
   */
  public void setAccount(GoogleSignInAccount account) {
    this.account.setValue(account);
    refreshPassphrases();
  }

  /**
   * Deletes the specified {@link Passphrase} from the server-based collection.
   */
  public void deletePassphrase(Passphrase passphrase) {
    GoogleSignInAccount account = this.account.getValue();
    if (passphrase != null && passphrase.getId() > 0 && account != null) {
      String token = getAuthorizationHeader(account);
      pending.add(
          dicewareService.delete(token, passphrase.getId())
              .subscribeOn(Schedulers.io())
              .subscribe(() -> refreshPassphrases(account), this.throwable::postValue)
      );
    }
  }

  /**
   * Request a refresh from the server of the collection of {@link Passphrase} instances.
   */
  public void refreshPassphrases() {
    GoogleSignInAccount account = this.account.getValue();
    if (account != null) {
      refreshPassphrases(account);
    } else {
      passphrases.setValue(Collections.EMPTY_LIST);
    }
  }

  /**
   * Adds the specified {@link Passphrase} instance to the server-based collection.
   */
  public void addPassphrase(Passphrase passphrase) {
    GoogleSignInAccount account = this.account.getValue();
    if (account != null) {
      String token = getAuthorizationHeader(account);
      pending.add(
          dicewareService.post(token, passphrase)
              .subscribeOn(Schedulers.io())
              .subscribe((p) -> refreshPassphrases(account), this.throwable::postValue)
      );
    }
  }

  /**
   * Updates the specified {@link Passphrase} instance in the server-based collection.
   */
  public void updatePassphrase(Passphrase passphrase) {
    GoogleSignInAccount account = this.account.getValue();
    if (account != null) {
      String token = getAuthorizationHeader(account);
      pending.add(
          dicewareService.put(token, passphrase.getId(), passphrase)
              .subscribeOn(Schedulers.io())
              .subscribe((p) -> refreshPassphrases(account), this.throwable::postValue)
      );
    }
  }

  private void refreshPassphrases(GoogleSignInAccount account) {
    String token = getAuthorizationHeader(account);
    pending.add(
        dicewareService.getAll(token)
            .subscribeOn(Schedulers.io())
            .subscribe(this.passphrases::postValue, this.throwable::postValue)
    );
  }

  private String getAuthorizationHeader(GoogleSignInAccount account) {
    String token = getApplication().getString(R.string.oauth_header, account.getIdToken());
    Log.d("OAuth2.0 token", token); // FIXME Remove before shipping.
    return token;
  }

  @OnLifecycleEvent(Event.ON_STOP)
  private void clearPending() {
    pending.clear();
  }

}
