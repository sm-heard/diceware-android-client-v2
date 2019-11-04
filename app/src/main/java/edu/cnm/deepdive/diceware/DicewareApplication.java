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
package edu.cnm.deepdive.diceware;

import android.app.Application;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;

/**
 * Class containing main (non-UI) entry point for this app.
 */
public class DicewareApplication extends Application {

  /**
   * Initializes the app by passing this instance (as the context) to {@link GoogleSignInService}.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    GoogleSignInService.setApplicationContext(this);
  }

}
