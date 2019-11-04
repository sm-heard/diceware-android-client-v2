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
package edu.cnm.deepdive.diceware.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.controller.PassphraseFragment.OnCompleteListener;
import edu.cnm.deepdive.diceware.model.Passphrase;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter.OnClickListener;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter.OnContextListener;
import edu.cnm.deepdive.diceware.viewmodel.MainViewModel;
import java.util.List;

/**
 * Main user interface for accessing a passphrase storage service, providing creation (including
 * random generation using a diceware word list), listing, updating, and deletion of passphrases.
 */
public class MainActivity extends AppCompatActivity
    implements OnClickListener, OnContextListener, OnCompleteListener {

  private ProgressBar waiting;
  private RecyclerView passphraseList;
  private MainViewModel viewModel;
  private GoogleSignInService signInService;

  /**
   * Initializes UI, sets up observers for backing ViewModel data, and sets up an observer for
   * active {@link com.google.android.gms.auth.api.signin.GoogleSignInAccount}.
   *
   * @param savedInstanceState state saved prior to configuration change (currently ignored).
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupUI();
    setupViewModel();
    setupSignIn();
  }

  /**
   * Inflates options (action bar and overflow) menu resource, attaching the inflated items to the
   * specified {@link Menu}.
   *
   * @param menu instance to which inflated items will be attached.
   * @return {@code true}, indicating that options menu should be displayed.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  /**
   * Handles selections from the options (action bar and overflow) menu.
   *
   * @param item selected menu item.
   * @return {@code true} if item was handled (by this method or the superclass implementation),
   * {@code false} otherwise.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean handled = true;
    switch (item.getItemId()) {
      case R.id.refresh:
        refreshSignIn(() -> viewModel.refreshPassphrases());
        break;
      case R.id.action_settings:
        break;
      case R.id.sign_out:
        signOut();
        break;
      default:
        handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  /**
   * Handles click on item in list by opening {@link PassphraseFragment} edit dialog.
   *
   * @param view item in {@link RecyclerView} list.
   * @param position index of clicked item in list.
   * @param passphrase instance backing clicked item.
   */
  @Override
  public void onClick(View view, int position, Passphrase passphrase) {
    editPassphrase(passphrase);
  }

  /**
   * Handles context (long) press on item in list by inflating a context menu. Currently, the only
   * option in the context menu is <strong>Delete</strong>, which when clicked, causes deletion of
   * the selected passphrase.
   *
   * @param menu object to which inflated menu resource is attached.
   * @param position index of pressed item in list.
   * @param passphrase instance backing clicked item.
   */
  @Override
  public void onLongPress(Menu menu, int position, Passphrase passphrase) {
    getMenuInflater().inflate(R.menu.passphrase_context, menu);
    menu.findItem(R.id.delete_passphrase).setOnMenuItemClickListener(
        (item) -> deletePassphrase(passphrase));
  }

  /**
   * Requests addition or update of specified passphrase, depending on its identifier (an ID of zero
   * indicates a new passphrase to be added).
   *
   * @param passphrase instance to be added or updated.
   */
  @Override
  public void updatePassphrase(Passphrase passphrase, boolean regenerate, int length) {
    refreshSignIn(() -> {
      if (passphrase.getId() == 0) {
        viewModel.addPassphrase(passphrase);
      } else {
        viewModel.updatePassphrase(passphrase, regenerate, length);
      }
    });
  }

  private void setupViewModel() {
    viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    getLifecycle().addObserver(viewModel);
    viewModel.getPassphrases().observe(this, this::refreshList);
    viewModel.getThrowable().observe(this, this::showError);
  }

  private void setupSignIn() {
    signInService = GoogleSignInService.getInstance();
    signInService.getAccount().observe(this, (account) -> viewModel.setAccount(account));
  }

  private void setupUI() {
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> editPassphrase(null));
    waiting = findViewById(R.id.waiting);
    passphraseList = findViewById(R.id.keyword_list);
  }

  private void refreshList(List<Passphrase> passphrases) {
    PassphraseAdapter adapter = new PassphraseAdapter(this, passphrases, this, this);
    passphraseList.setAdapter(adapter);
    waiting.setVisibility(View.GONE);
  }

  private void showError(Throwable throwable) {
    if (throwable != null) {
      waiting.setVisibility(View.GONE);
      Toast.makeText(this, getString(R.string.connection_error, throwable.getMessage()),
          Toast.LENGTH_LONG).show();
    }
  }

  private void editPassphrase(Passphrase passphrase) {
    PassphraseFragment fragment = PassphraseFragment.newInstance(passphrase);
    fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
  }

  private boolean deletePassphrase(Passphrase passphrase) {
    refreshSignIn(() -> viewModel.deletePassphrase(passphrase));
    return true;
  }

  private void refreshSignIn(Runnable runnable) {
    waiting.setVisibility(View.VISIBLE);
    signInService.refresh()
        .addOnSuccessListener((account) -> runnable.run())
        .addOnFailureListener((e) -> signOut());
  }

  private void signOut() {
    signInService.signOut()
        .addOnCompleteListener((task) -> {
          Intent intent = new Intent(this, LoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        });
  }

}
