
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @author fancy
 */
public class LogUtil {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");
    public static void logOut(Object info){
        String threadName=Thread.currentThread().getName();
        System.out.println(dtf.format(LocalDateTime.now())+"--线程名"+threadName+":::"+info);
    }
    public static void log_print(String functionName, Object result) {
        Gson gson = new Gson();
        LogUtil.logOut("-------------------------------" + functionName + "-------------------------------");
        LogUtil.logOut(gson.toJson(result));
    }
}
