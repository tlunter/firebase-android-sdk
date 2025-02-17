// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.storage.network;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.google.firebase.FirebaseApp;

/** Cancels an upload request in progress. */
public class ResumableUploadCancelRequest extends ResumableNetworkRequest {
  @VisibleForTesting public static boolean cancelCalled = false;

  private final String uploadURL;

  public ResumableUploadCancelRequest(
      @NonNull Uri gsUri, @NonNull FirebaseApp app, @NonNull String uploadURL) {
    super(gsUri, app);
    cancelCalled = true;
    if (TextUtils.isEmpty(uploadURL)) {
      super.mException = new IllegalArgumentException("uploadURL is null or empty");
    }
    this.uploadURL = uploadURL;
    super.setCustomHeader(PROTOCOL, "resumable");
    super.setCustomHeader(COMMAND, "cancel");
  }

  @NonNull
  @Override
  protected String getAction() {
    return POST;
  }

  @NonNull
  @Override
  protected String getURL() {
    return uploadURL;
  }
}
