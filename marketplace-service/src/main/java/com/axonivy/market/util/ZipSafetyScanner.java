package com.axonivy.market.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class ZipSafetyScanner {
    // Giới hạn kích thước
    public static final long maxTotalUncompressedBytes = 500L * 1024 * 1024; // 500MB
    public static final long maxSingleUncompressedBytes = 200L * 1024 * 1024; // 200MB
    public static final int maxEntries = 50;
    public static final double maxCompressionRatioPerEntry = 200.0;
    public static final double maxGlobalCompressionRatio = 100.0;
    public static final int maxNestedArchiveDepth = 3;
    public static final Set<String> dangerousExtensions = new HashSet<>(Arrays.asList(
            ".exe", ".dll", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar",
            ".php", ".phps", ".phtml", ".py", ".rb", ".pl", ".class", ".com"
    ));

    // Giới hạn đọc thử nội dung để kiểm nested (chỉ đọc vài byte đầu entry)
    public static int nestedHeaderProbeBytes = 8;

    /* ===================== KẾT QUẢ / ISSUE ===================== */
    public static class Issue {
        public final String code;
        public final String message;
        public final String entryName;

        public Issue(String code, String message, String entryName) {
            this.code = code;
            this.message = message;
            this.entryName = entryName;
        }

        @Override
        public String toString() {
            return "[" + code + "] " + message + (entryName != null ? " (entry=" + entryName + ")" : "");
        }
    }

    /**
     * Analyze a ZIP file without unzipping
     */
    public static boolean analyze(MultipartFile zipFile) throws IOException {
        if (zipFile == null || zipFile.isEmpty()) {
            throw new FileNotFoundException("File upload rỗng hoặc null");
        }

        File tempFile = File.createTempFile("upload-", ".zip");
        try (InputStream input = zipFile.getInputStream();
             OutputStream output = new FileOutputStream(tempFile)) {
            input.transferTo(output);
        }
      System.out.println(tempFile.getAbsolutePath());

        List<Issue> issues = new ArrayList<>();
        int entries = 0;
        long totalCompressed = 0;
        long totalUncompressed = 0;
        double maxEntryRatio = 0.0;
        int nestedArchiveCount = 0;

        try (ZipFile zf = new ZipFile(tempFile)) {
            Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                entries++;

                String name = e.getName();
                long compressedSize = e.getCompressedSize();
                long uncompressedSize = e.getSize();

                // Stop if there are too much sub files in zip file
                if (entries > maxEntries) {
                    issues.add(new Issue("TOO_MANY_ENTRIES", "Số lượng entry vượt giới hạn: " + entries, null));
//                    return false;
                }
                // Purpose: Detect files inside ZIP with names like ../../etc/passwd.
                if (isTraversal(name)) {
                    issues.add(new Issue("PATH_TRAVERSAL", "Tên entry có dấu hiệu traversal: " + name, name));
//                    return false;
                }
                // Prevent entries with paths starting from the root (e.g. /usr/bin/file or C:\Windows\...).
                // Avoid overwriting system files when extracting.
                if (isAbsolutePathLike(name)) {
                    issues.add(new Issue("ABSOLUTE_PATH", "Entry có đường dẫn tuyệt đối: " + name, name));
//                    return false;
                }

                // Purpose: Prevent files like .bashrc, .gitignore, .ssh/authorized_keys , .env
                // Protect the system from overwriting hidden configuration files when unzipping.
                if (isHiddenDotFile(name)) {
                    issues.add(new Issue("HIDDEN_DOT_FILE", "Entry là file ẩn (.) và bị chặn: " + name, name));
//                    return false;
                }

                if (hasDangerousExtension(name, dangerousExtensions)) {
                    issues.add(new Issue("DANGEROUS_EXT", "Đuôi file nguy hiểm: " + name, name));
//                    return false;
                }

                // Purpose: Prevent single files from being too large.
                if (uncompressedSize > maxSingleUncompressedBytes) {
                    issues.add(
                            new Issue("ENTRY_TOO_LARGE", "Entry quá lớn (uncompressed=" + uncompressedSize + "): " + name, name));
//                    return false;
                }

                // Purpose: Calculate the ratio between compressed and decompressed size.
                // Reason: If the ratio is too high (e.g. 1 byte compressed → 1GB decompressed), it could be a ZIP bomb.
                if (compressedSize > 0 && uncompressedSize > 0) {
                    double ratio = (double) uncompressedSize / (double) compressedSize;
                    if (ratio > maxEntryRatio) maxEntryRatio = ratio;
                    if (ratio > maxCompressionRatioPerEntry) {
                        issues.add(new Issue("HIGH_COMPRESSION_RATIO",
                                "Tỉ lệ nén bất thường (" + ratio + "): " + name, name));
//                        return false;
                    }
                }

                if (compressedSize > 0) totalCompressed += compressedSize;
                if (uncompressedSize > 0) totalUncompressed += uncompressedSize;

                // Purpose: Check the total size when extracting the entire ZIP.
                // Avoid ZIP bombs that compress many small files but have a very large total size.
                if (totalUncompressed > maxTotalUncompressedBytes) {
                    issues.add(new Issue("TOTAL_UNCOMPRESSED_LIMIT",
                            "Tổng uncompressed vượt giới hạn: " + totalUncompressed, null));
//                    return false;
                }

                // Purpose: Detect ZIP files that contain other ZIP files inside.
                if (looksLikeNestedArchive(name)) {
                    if (isZipSignature(zf, e, nestedHeaderProbeBytes)) {
                        nestedArchiveCount++;
                        if (nestedArchiveCount > maxNestedArchiveDepth) {
                            issues.add(new Issue("NESTED_DEPTH",
                                    "Số archive lồng nhau vượt giới hạn: " + nestedArchiveCount, name));
//                            return false;
                        }
                    }
                }
            }
        }

        double globalRatio = (totalCompressed > 0 && totalUncompressed > 0)
                ? (double) totalUncompressed / (double) totalCompressed
                : 0.0;

        if (globalRatio > maxGlobalCompressionRatio) {
            issues.add(new Issue("GLOBAL_COMPRESSION_RATIO",
                    "Tỉ lệ nén tổng thể bất thường: " + globalRatio, null));
            return false;
        }
        return true;
    }

    private static boolean isTraversal(String name) {
        return name.contains(".." + "/") || name.contains("../") || name.contains("..\\") || name.startsWith("../")
                || name.startsWith("..\\");
    }

    private static boolean isAbsolutePathLike(String name) {
        return name.startsWith("/") || name.startsWith("\\");
    }

    private static boolean isHiddenDotFile(String name) {
        String simple = name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name;
        return simple.startsWith(".") && simple.length() > 1;
    }

    private static boolean hasDangerousExtension(String name, Set<String> exts) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String ext : exts) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    private static boolean looksLikeNestedArchive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".war") || lower.endsWith(".ear");
    }

    private static boolean isZipSignature(ZipFile zf, ZipEntry entry, int probeBytes) {
        if (entry.isDirectory()) return false;
        try (InputStream in = zf.getInputStream(entry)) {
            byte[] buf = new byte[Math.min(probeBytes, 8)];
            int read = in.read(buf);
            if (read < 4) return false;
            return buf[0] == 'P' && buf[1] == 'K'; // Đơn giản
        } catch (IOException e) {
            return false;
        }
    }
}
