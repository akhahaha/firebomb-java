package firebomb;

public interface FirebombCallback<T> {
    void onComplete(T result);

    void onException(Exception e);
}
