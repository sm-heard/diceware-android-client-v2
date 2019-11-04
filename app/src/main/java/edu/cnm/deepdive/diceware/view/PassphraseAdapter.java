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
package edu.cnm.deepdive.diceware.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.model.Passphrase;
import edu.cnm.deepdive.diceware.view.PassphraseAdapter.Holder;
import java.util.List;

/**
 * Simple subclass of {@link RecyclerView.Adapter} that displays {@link Passphrase#getKey()} values,
 * with the consumer providing listeners for click and long (context) press events.
 */
public class PassphraseAdapter extends RecyclerView.Adapter<Holder> {

  private final Context context;
  private final List<Passphrase> passphrases;
  private final OnClickListener clickListener;
  private final OnContextListener contextListener;

  /**
   * Initializes the adapter with the specified passphrases and event listeners.
   *
   * @param context {@link Context} object used for obtaining a {@link LayoutInflater}.
   * @param passphrases {@link List List&lt;Passphrase&gt;} to display in {@link RecyclerView}.
   * @param clickListener {@link OnClickListener} to handle click events; may be {@code null}.
   * @param contextListener {@link OnContextListener} to handle long press events; may be {@code null}.
   */
  public PassphraseAdapter(Context context, List<Passphrase> passphrases,
      OnClickListener clickListener, OnContextListener contextListener) {
    this.context = context;
    this.passphrases = passphrases;
    this.clickListener = clickListener;
    this.contextListener = contextListener;
  }

  /**
   * Creates and returns a {@link Holder} that can be bound to any {@link
   * Passphrase} in this instance's list of items.
   *
   * @param parent enclosing {@link RecyclerView}.
   * @param viewType desired view type (ignored in this implementation).
   * @return {@link Holder} referencing inflated layout.
   */
  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.passphrase_item, parent, false);
    return new Holder(view);
  }

  /**
   * Binds the specified {@link Holder} to the {@link Passphrase} at the specified position in this
   * adapter instance.
   *
   * @param holder {@link Holder} referencing a bindable {@link View}.
   * @param position index of item in the adapter's list to bind to {@code holder}.
   */
  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    Passphrase passphrase = passphrases.get(position);
    holder.bind(position, passphrase);
  }

  /**
   * Returns the number of items in the this instance's list of passphrases.
   *
   * @return count.
   */
  @Override
  public int getItemCount() {
    return passphrases.size();
  }

  /**
   * Listener for {@link PassphraseAdapter} item clicks.
   */
  @FunctionalInterface
  public interface OnClickListener {

    /**
     * Handles a click on a specified {@link View} in the {@link RecyclerView}, corresponding to
     * the {@link Passphrase} at index {@code position} in the {@link PassphraseAdapter}.
     *
     * @param view clicked {@link View}.
     * @param position selected item index of the {@link PassphraseAdapter}.
     * @param passphrase {@link Passphrase} instance bound to the {@link Holder} at {code position}.
     */
    void onClick(View view, int position, Passphrase passphrase);

  }

  /**
   * Listener for {@link PassphraseAdapter} context (long) presses.
   */
  @FunctionalInterface
  public interface OnContextListener {

    /**
     * Handles a long press on a specified {@code position} in the {@link PassphraseAdapter}.
     *
     * @param menu {@link Menu} instance to which context items may be attached.
     * @param position index of pressed item in {@link PassphraseAdapter}.
     * @param passphrase pressed instance of {@link Passphrase}.
     */
    void onLongPress(Menu menu, int position, Passphrase passphrase);

  }

  /**
   * Binder for {@link View} items in a {@link RecyclerView} and {@link Passphrase} items in a
   * {@link PassphraseAdapter}.
   */
  public class Holder extends RecyclerView.ViewHolder {

    private final View view;

    private Holder(@NonNull View itemView) {
      super(itemView);
      view = itemView;
    }

    private void bind(int position, Passphrase passphrase) {
      ((TextView) view).setText(passphrase.getKey());
      if (clickListener != null) {
        view.setOnClickListener((v) -> clickListener.onClick(v, position, passphrase));
      }
      if (contextListener != null) {
        view.setOnCreateContextMenuListener((menu, v, menuInfo) ->
            contextListener.onLongPress(menu, position, passphrase));
      }
    }

  }

}
