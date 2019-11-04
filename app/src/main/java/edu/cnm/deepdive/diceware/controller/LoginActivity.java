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
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;

/**
 * Simple {@link AppCompatActivity} subclass, displaying a button for&mdash;and transferring control
 * to&mdash;Google Sign In, switching to {@link MainActivity} on a successful login.
 */
public class LoginActivity extends AppCompatActivity {

  private static final int LOGIN_REQUEST_CODE = 1000;

  private GoogleSignInService service;

  /**
   * Attempts to refresh most recently logged-in account, switching to {@link MainActivity} if
   * successful, and displaying Google Sign In button otherwise.
   *
   * @param savedInstanceState previously saved state data (ignored).
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    service = GoogleSignInService.getInstance();
    service.refresh()
        .addOnSuccessListener((account) -> switchToMain())
        .addOnFailureListener((ex) -> {
          setContentView(R.layout.activity_login);
          findViewById(R.id.sign_in).setOnClickListener((view) ->
              service.startSignIn(this, LOGIN_REQUEST_CODE));
        });
  }

  /**
   * Receives result returned from {@link #startActivityForResult(Intent, int)}, checking Google
   * Sign In result if appropriate.
   *
   * @param requestCode code submitted with {@link #startActivityForResult(Intent, int)}.
   * @param resultCode abbreviated result (currently ignored).
   * @param data detailed payload of result {@link Intent}.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == LOGIN_REQUEST_CODE) {
     service.completeSignIn(data)
        .addOnSuccessListener((account) -> switchToMain())
        .addOnFailureListener((ex) ->
            Toast.makeText(this, R.string.login_failure_message, Toast.LENGTH_LONG).show());
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void switchToMain() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }

}
