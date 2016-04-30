package com.gionee.framework.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {
    private static final String FILE_EXTENSION_SEPARATOR = ".";

    private FileUtils() {
    }

    public static StringBuilder readFile(String filePath, String charsetName) {
        IOException e;
        Throwable th;
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return null;
        }
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (!fileContent.toString().equals("")) {
                        fileContent.append("\r\n");
                    }
                    fileContent.append(line);
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            reader.close();
            if (reader == null) {
                return fileContent;
            }
            try {
                reader.close();
                return fileContent;
            } catch (IOException e3) {
                throw new RuntimeException("IOException", e3);
            }
        } catch (IOException e4) {
            e3 = e4;
            try {
                throw new RuntimeException("IOException occurred. ", e3);
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32) {
                        throw new RuntimeException("IOException", e32);
                    }
                }
                throw th;
            }
        }
    }

    public static boolean writeFile(String filePath, String content, boolean append) {
        IOException e;
        Throwable th;
        OutputStreamWriter osWriter = null;
        try {
            makeDirs(filePath);
            OutputStreamWriter osWriter2 = new OutputStreamWriter(new FileOutputStream(filePath, append), StringUtils.ENCODING_UTF8);
            try {
                osWriter2.write(content);
                if (osWriter2 != null) {
                    try {
                        osWriter2.close();
                    } catch (IOException e2) {
                        throw new RuntimeException("IOException", e2);
                    }
                }
                return true;
            } catch (IOException e3) {
                e2 = e3;
                osWriter = osWriter2;
                try {
                    throw new RuntimeException("IOException", e2);
                } catch (Throwable th2) {
                    th = th2;
                    if (osWriter != null) {
                        try {
                            osWriter.close();
                        } catch (IOException e22) {
                            throw new RuntimeException("IOException", e22);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                osWriter = osWriter2;
                if (osWriter != null) {
                    osWriter.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            throw new RuntimeException("IOException", e22);
        }
    }

    public static boolean writeFile(String filePath, InputStream stream) {
        return writeFile(filePath, stream, false);
    }

    public static boolean writeFile(String filePath, InputStream stream, boolean append) {
        if (filePath != null) {
            return writeFile(new File(filePath), stream, append);
        }
        throw new RuntimeException("filePath == null");
    }

    public static boolean writeFile(File file, InputStream stream) {
        return writeFile(file, stream, false);
    }

    public static boolean writeFile(File file, InputStream stream, boolean append) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        OutputStream outputStream = null;
        try {
            makeDirs(file.getAbsolutePath());
            OutputStream o = new FileOutputStream(file, append);
            try {
                byte[] data = new byte[1024];
                while (true) {
                    int length = stream.read(data);
                    if (length == -1) {
                        break;
                    }
                    o.write(data, 0, length);
                }
                o.flush();
                if (o != null) {
                    try {
                        o.close();
                        stream.close();
                    } catch (IOException e3) {
                        throw new RuntimeException("IOException", e3);
                    }
                }
                return true;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                outputStream = o;
                try {
                    throw new RuntimeException("FileNotFoundException", e2);
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                            stream.close();
                        } catch (IOException e32) {
                            throw new RuntimeException("IOException", e32);
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e32 = e5;
                outputStream = o;
                throw new RuntimeException("IOException ", e32);
            } catch (Throwable th3) {
                th = th3;
                outputStream = o;
                if (outputStream != null) {
                    outputStream.close();
                    stream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            throw new RuntimeException("FileNotFoundException", e2);
        } catch (IOException e7) {
            e32 = e7;
            throw new RuntimeException("IOException ", e32);
        }
    }

    public static boolean copyFile(String sourceFilePath, String destFilePath) {
        try {
            return writeFile(destFilePath, new FileInputStream(sourceFilePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException", e);
        }
    }

    public static List<String> readFileToList(String filePath, String charsetName) {
        IOException e;
        Throwable th;
        File file = new File(filePath);
        List<String> fileContent = new ArrayList();
        if (file == null || !file.isFile()) {
            return null;
        }
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    fileContent.add(line);
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            reader.close();
            if (reader == null) {
                return fileContent;
            }
            try {
                reader.close();
                return fileContent;
            } catch (IOException e3) {
                throw new RuntimeException("IOException", e3);
            }
        } catch (IOException e4) {
            e3 = e4;
            try {
                throw new RuntimeException("IOException", e3);
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32) {
                        throw new RuntimeException("IOException", e32);
                    }
                }
                throw th;
            }
        }
    }

    private static boolean isNull(String filePath) {
        return filePath == null || "".endsWith(filePath);
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (isNull(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            if (extenPosi != -1) {
                return filePath.substring(0, extenPosi);
            }
            return filePath;
        } else if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        } else {
            return filePosi < extenPosi ? filePath.substring(filePosi + 1, extenPosi) : filePath.substring(filePosi + 1);
        }
    }

    public static String getFileName(String filePath) {
        if (isNull(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return filePosi != -1 ? filePath.substring(filePosi + 1) : filePath;
    }

    public static String getFolderName(String filePath) {
        if (isNull(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return filePosi == -1 ? "" : filePath.substring(0, filePosi);
    }

    public static String getFileExtension(String filePath) {
        if (isNull(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return filePosi >= extenPosi ? "" : filePath.substring(extenPosi + 1);
    }

    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (isNull(filePath)) {
            return false;
        }
        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }

    public static boolean makeFolders(String filePath) {
        return makeDirs(filePath);
    }

    public static boolean isFileExist(String filePath) {
        if (isNull(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }

    public static boolean isFolderExist(String directoryPath) {
        if (isNull(directoryPath)) {
            return false;
        }
        File dire = new File(directoryPath);
        if (dire.exists() && dire.isDirectory()) {
            return true;
        }
        return false;
    }

    public static boolean deleteFile(String path) {
        if (isNull(path)) {
            return true;
        }
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                if (!f.delete()) {
                    return false;
                }
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    public static long getFileSize(String path) {
        if (isNull(path)) {
            return -1;
        }
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return -1;
    }
}
