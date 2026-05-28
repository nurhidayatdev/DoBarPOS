import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Replacer2 {
    public static void main(String[] args) throws Exception {
        Map<String, String> rep = new HashMap<>();
        rep.put("\"3 Items\"", "\"3 Item\"");
        rep.put("\"5 Items\"", "\"5 Item\"");
        rep.put("\"Completed\"", "\"Selesai\"");
        rep.put("\"Pending\"", "\"Tertunda\"");
        rep.put("\"-- Select an access level --\"", "\"-- Pilih tingkat akses --\"");
        rep.put("\"Registered Personnel\"", "\"Pengguna Terdaftar\"");
        rep.put("\"Clear Form\"", "\"Bersihkan Form\"");
        rep.put("\"Is Available\"", "\"Tersedia\"");
        rep.put("setText(\"Role\")", "setText(\"Peran\")");
        rep.put("value=\"Role\"", "value=\"Peran\""); // for form files
        rep.put("\"ID USER\", \"USERNAME\", \"ROLE\"", "\"ID USER\", \"USERNAME\", \"PERAN\""); // for the JTable columns

        Files.walk(Paths.get("c:/Users/ASUS/Documents/NetBeansProjects/DoBarPOS/src/dobarpos"))
            .filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".form"))
            .forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p), "UTF-8");
                    boolean mod = false;
                    for (Map.Entry<String, String> e : rep.entrySet()) {
                        if (content.contains(e.getKey())) {
                            content = content.replace(e.getKey(), e.getValue());
                            mod = true;
                        }
                    }
                    if (mod) {
                        Files.write(p, content.getBytes("UTF-8"));
                        System.out.println("Modified: " + p.getFileName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        System.out.println("Done 2.");
    }
}
