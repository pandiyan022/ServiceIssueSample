package background.location.service.td;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class MainThreadExecutor implements Executor {
    private Handler handler = new Handler(Looper.getMainLooper());
    public static String ThreadID;
    public static boolean threadComplete;
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }
}