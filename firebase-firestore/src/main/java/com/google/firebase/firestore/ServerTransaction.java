package com.google.firebase.firestore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.annotations.PublicApi;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.firestore.util.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@PublicApi
public class ServerTransaction {

  private final com.google.firebase.firestore.core.ServerTransaction transaction;
  private final FirebaseFirestore firestore;

  public ServerTransaction(
      com.google.firebase.firestore.core.ServerTransaction transaction,
      FirebaseFirestore firestore) {

    this.transaction = transaction;
    this.firestore = firestore;
  }

  private Task<DocumentSnapshot> getAsync(DocumentReference documentRef) {
    return transaction
        .lookup(documentRef.getKey())
        .continueWith(
            Executors.DIRECT_EXECUTOR,
            task -> {
              if (!task.isSuccessful()) {
                throw task.getException();
              }
              Document doc = task.getResult();
              return DocumentSnapshot.fromDocument(
                  firestore, doc, /*fromCache=*/ false, /*hasPendingWrites=*/ false);
            });
  }

  /**
   * Reads the document referenced by this DocumentReference
   *
   * @param documentRef The DocumentReference to read.
   * @return The contents of the Document at this DocumentReference.
   */
  @NonNull
  @PublicApi
  public DocumentSnapshot get(@NonNull DocumentReference documentRef)
      throws FirebaseFirestoreException {
    firestore.validateReference(documentRef);
    try {
      return Tasks.await(getAsync(documentRef));
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof FirebaseFirestoreException) {
        throw ((FirebaseFirestoreException) ee.getCause());
      }
      throw new RuntimeException(ee.getCause());
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
  }

  private Task<List<DocumentSnapshot>> queryAsync(Query query) {
    return transaction
        .query(query.query)
        .continueWith(
            Executors.DIRECT_EXECUTOR,
            task -> {
              if (!task.isSuccessful()) {
                throw task.getException();
              }

              List<Document> results = task.getResult();
              List<DocumentSnapshot> snapshots = new ArrayList<>();
              for (Document doc : results) {
                snapshots.add(
                    DocumentSnapshot.fromDocument(
                        firestore, doc, /*fromCache=*/ false, /*hasPendingWrites=*/ false));
              }
              return snapshots;
            });
  }

  @NonNull
  @PublicApi
  public List<DocumentSnapshot> query(@NonNull Query query) throws FirebaseFirestoreException {
    try {
      return Tasks.await(queryAsync(query));
    } catch (ExecutionException ee) {
      if (ee.getCause() instanceof FirebaseFirestoreException) {
        throw ((FirebaseFirestoreException) ee.getCause());
      }
      throw new RuntimeException(ee.getCause());
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
  }

  /**
   * An interface for providing code to be executed within a transaction context.
   *
   * @see FirebaseFirestore#runServerTransaction(ServerTransaction.Function)
   */
  @PublicApi
  public interface Function<TResult> {
    @Nullable
    @PublicApi
    TResult apply(@NonNull ServerTransaction transaction) throws FirebaseFirestoreException;
  }
}
