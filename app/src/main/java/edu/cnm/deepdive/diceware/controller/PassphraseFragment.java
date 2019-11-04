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

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.model.Passphrase;
import java.util.Arrays;

/**
 * Alert dialog (modal) user interface component presenting the properties (keyword and word list)
 * of a passphrase for editing.
 *
 * @author Nicholas Bennett, Todd Nordquist, Brian Bleck, Deep Dive Coding Java + Android Cohort 8
 */
public class PassphraseFragment extends DialogFragment {

  private Passphrase passphrase;
  private EditText passphraseKey;
  private EditText passphraseWords;

  /**
   * Creates and returns an instance of {@link PassphraseFragment} for editing a new passphrase.
   *
   * @return {@link PassphraseFragment} for display.
   */
  public static PassphraseFragment newInstance() {
    return newInstance(null);
  }

  /**
   * Creates and returns an instance of {@link PassphraseFragment} for editing a new or existing
   * passphrase.
   *
   * @param passphrase instance to be edited; null for a new passphrase.
   * @return {@link PassphraseFragment} for display.
   */
  public static PassphraseFragment newInstance(Passphrase passphrase) {
    PassphraseFragment fragment = new PassphraseFragment();
    Bundle args = new Bundle();
    if (passphrase != null) {
      args.putSerializable("passphrase", passphrase);
    }
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * Constructs and returns an {@link AlertDialog} containing edit controls for the properties (key
   * and word list) of a passphrase. This method is invoked implicitly by {@link
   * DialogFragment#show(FragmentManager, String)}.
   *
   * @param savedInstanceState state data saved prior to a configuration change (currently ignored).
   * @return dialog for modal interaction.
   */
  @NonNull
  @Override
  public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_passphrase, null);
    passphraseKey = view.findViewById(R.id.passphrase_key);
    passphraseWords = view.findViewById(R.id.passphrase_words);
    passphrase = (Passphrase) getArguments().getSerializable("passphrase");
    if (passphrase == null) {
      passphrase = new Passphrase();
    }
    if (savedInstanceState == null) {
      populateFields();
    }
    return new Builder(getContext())
        .setTitle(getString(R.string.passphrase_details))
        .setView(view)
        .setNegativeButton(getString(R.string.cancel), (dialog, button) -> {
        })
        .setPositiveButton(getString(R.string.ok), (dialog, button) -> populatePassphrase())
        .create();
  }

  private void populateFields() {
    if (passphrase.getKey() != null) {
      passphraseKey.setText(passphrase.getKey());
    }
    if (passphrase.getWords() != null) {
      String words = passphrase.getWords().toString();
      passphraseWords.setText(words
          .replaceAll("^\\[|\\]$", "")
          .trim()
          .replaceAll("\\s*,\\s+", " "));
    }
  }

  private void populatePassphrase() {
    passphrase.setKey(passphraseKey.getText().toString().trim());
    String words = passphraseWords.getText().toString().trim();
    passphrase.setWords(words.isEmpty() ? null : Arrays.asList(words.split("\\s+")));
    ((OnCompleteListener) getActivity()).updatePassphrase(passphrase);
  }

  /**
   * Declares a {@link #updatePassphrase(Passphrase)} method that receives the {@link Passphrase}
   * instance on completion of editing. The host activity for the {@link PassphraseFragment}
   * instance <strong>must</strong> implement this interface.
   */
  @FunctionalInterface
  public interface OnCompleteListener {

    /**
     * Performs any necessary model updates for the specified passphrase.
     *
     * @param passphrase edited {@link Passphrase} instance.
     */
    void updatePassphrase(Passphrase passphrase);

  }

}
