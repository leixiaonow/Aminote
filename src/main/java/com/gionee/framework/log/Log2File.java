package com.gionee.framework.log;

import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import com.gionee.framework.storage.StorageMgr;
import com.gionee.framework.utils.StringUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

final class Log2File implements ILog {
    private static final String BASE_FILENAME = ".log";
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String LOG_DIRECTORY = StorageMgr.getInstance().getSdcardRootPath();
    private static final String LOG_LINE_CONNECTOR = " : ";
    private static final String LOG_SEPARATOR = "   ";
    private static final String PACKAGE_NAME = "amigo/AmiNote/log";
    private static final int SIZE = 2048;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private boolean mIsOpen = false;
    private Writer mWriter;

    Log2File() {
    }

    public void println(Message msg) {
        if (!this.mIsOpen) {
            open();
        }
        Bundle bundle = msg.getData();
        String tag = bundle.getString("tag");
        String log = bundle.getString("log");
        String threadid = bundle.getString("thread_id");
        StringBuffer buffer = new StringBuffer();
        buffer.append(getCurrentTimeString()).append(LOG_SEPARATOR).append(Process.myPid()).append(LOG_SEPARATOR).append(threadid).append(LOG_SEPARATOR).append(tag).append(LOG_LINE_CONNECTOR).append(log);
        writeLine(buffer.toString());
    }

    public void printStack(Message msg) {
        PrintWriter pw;
        Throwable th;
        if (!this.mIsOpen) {
            open();
        }
        Bundle bundle = msg.getData();
        String tag = bundle.getString("tag");
        String log = bundle.getString("log");
        String threadid = bundle.getString("thread_id");
        Throwable throwable = msg.obj;
        StringBuffer buffer = new StringBuffer();
        buffer.append(getCurrentTimeString()).append(LOG_SEPARATOR).append(Process.myPid()).append(LOG_SEPARATOR).append(threadid).append(LOG_SEPARATOR).append(tag).append(LOG_LINE_CONNECTOR).append(log);
        writeLine(buffer.toString());
        StringWriter sw = null;
        PrintWriter pw2 = null;
        try {
            StringWriter sw2 = new StringWriter();
            try {
                pw = new PrintWriter(sw2, true);
            } catch (Exception e) {
                sw = sw2;
                if (sw != null) {
                    try {
                        sw.close();
                    } catch (IOException e2) {
                    }
                }
                if (pw2 == null) {
                    pw2.close();
                }
            } catch (Throwable th2) {
                th = th2;
                sw = sw2;
                if (sw != null) {
                    try {
                        sw.close();
                    } catch (IOException e3) {
                    }
                }
                if (pw2 != null) {
                    pw2.close();
                }
                throw th;
            }
            try {
                throwable.printStackTrace(pw);
                pw.flush();
                sw2.flush();
                writeLine(tag + LOG_LINE_CONNECTOR + sw2.toString());
                if (sw2 != null) {
                    try {
                        sw2.close();
                    } catch (IOException e4) {
                    }
                }
                if (pw != null) {
                    pw.close();
                    pw2 = pw;
                    sw = sw2;
                    return;
                }
                sw = sw2;
            } catch (Exception e5) {
                pw2 = pw;
                sw = sw2;
                if (sw != null) {
                    sw.close();
                }
                if (pw2 == null) {
                    pw2.close();
                }
            } catch (Throwable th3) {
                th = th3;
                pw2 = pw;
                sw = sw2;
                if (sw != null) {
                    sw.close();
                }
                if (pw2 != null) {
                    pw2.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            if (sw != null) {
                sw.close();
            }
            if (pw2 == null) {
                pw2.close();
            }
        } catch (Throwable th4) {
            th = th4;
            if (sw != null) {
                sw.close();
            }
            if (pw2 != null) {
                pw2.close();
            }
            throw th;
        }
    }

    private static synchronized String getCurrentDate() {
        String format;
        synchronized (Log2File.class) {
            format = DAY_FORMAT.format(new Date());
        }
        return format;
    }

    private static synchronized String getCurrentTimeString() {
        String format;
        synchronized (Log2File.class) {
            format = TIME_FORMAT.format(new Date());
        }
        return format;
    }

    private static String getLogDirectory() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(LOG_DIRECTORY).append(File.separator).append(PACKAGE_NAME).append(File.separator).append(getCurrentDate()).append(BASE_FILENAME);
        return buffer.toString();
    }

    private void open() {
        File file = new File(getLogDirectory());
        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
            } catch (Exception e) {
                throw new LogIOException("create log dirs error!");
            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e2) {
                throw new LogIOException("create log file error!");
            }
        }
        try {
            this.mWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StringUtils.ENCODING_UTF8), 2048);
            this.mIsOpen = true;
        } catch (IOException e3) {
            throw new LogIOException("open log file error!");
        }
    }

    private void writeLine(String message) {
        try {
            this.mWriter.append(message);
            this.mWriter.append('\n');
            this.mWriter.flush();
        } catch (Exception e) {
            throw new LogIOException();
        }
    }

    public void dispose() {
        if (this.mWriter != null) {
            try {
                this.mWriter.close();
            } catch (IOException e) {
            }
        }
    }
}
