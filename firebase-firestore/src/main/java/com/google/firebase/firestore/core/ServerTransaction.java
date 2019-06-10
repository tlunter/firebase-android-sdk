package com.google.firebase.firestore.core;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreException.Code;
import com.google.firebase.firestore.core.Query;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.firestore.model.DocumentKey;
import com.google.firebase.firestore.remote.Datastore;
import com.google.firebase.firestore.util.Executors;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerTransaction {
  private final Datastore datastore;

  private ByteString transactionId;

  public ServerTransaction(Datastore d) {
    datastore = d;
  }

  public Task<Boolean> begin() {
    return datastore.beginReadOnlyTransaction()
        .continueWithTask(
            Executors.DIRECT_EXECUTOR,
            task -> {
              transactionId = task.getResult();

              return Tasks.forResult(transactionId != null && !transactionId.isEmpty());
            });
  }

  public Task<Document> lookup(DocumentKey key) {
    if (transactionId == null) {
      return Tasks.forException(
          new FirebaseFirestoreException(
              "Transaction has not been started", Code.FAILED_PRECONDITION));
    }

    return datastore.lookupWithTransaction(key, transactionId);
  }

  public Task<List<Document>> query(Query query) {
    if (transactionId == null) {
      return Tasks.forException(
          new FirebaseFirestoreException(
              "Transaction has not been started", Code.FAILED_PRECONDITION));
    }

    return datastore.query(query, transactionId);
  }

  public boolean hasPendingWrites() {
    return false;
  }

  private static final Executor defaultExecutor = createDefaultExecutor();

  private static Executor createDefaultExecutor() {
    // Create a thread pool with a reasonable size.
    int corePoolSize = 5;
    // maxPoolSize only gets used when queue is full, and queue size is MAX_INT, so this is a no-op.
    int maxPoolSize = corePoolSize;
    int keepAliveSeconds = 1;
    LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    ThreadPoolExecutor executor =
        new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, queue);
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  public static Executor getDefaultExecutor() {
    return defaultExecutor;
  }

  public Task<Boolean> start() {
    return Tasks.forResult(null);
  }
}
