package dobarpos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserSession — Menyimpan data user yang sedang login secara global.
 * Akses dari frame manapun: UserSession.getInstance().getUsername() dll.
 */
public class UserSession {

    private static UserSession instance;

    private int    idUser;
    private String username;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void setUser(int idUser, String username, String role) {
        this.idUser   = idUser;
        this.username = username;
        this.role     = role;
    }

    public void clearSession() { idUser = 0; username = null; role = null; }

    public int    getIdUser()   { return idUser; }
    public String getUsername() { return username; }
    public String getRole()     { return role; }

    // ── Static helper: Autentikasi Login ────────────────────────
    /**
     * Memvalidasi username & password ke tabel 'user' di MySQL.
     * Password dibandingkan menggunakan SHA2-256 (sesuai skema DB di DBConnection).
     *
     * @return true jika valid, false jika salah / tidak ditemukan.
     */
    public static boolean login(String username, String password) {
        String sql = "SELECT id_user, username, role FROM user "
                   + "WHERE username = ? AND password = SHA2(?, 256)";
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return false;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                getInstance().setUser(
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                System.out.println("[UserSession] Login berhasil: " + username + " (" + rs.getString("role") + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserSession] Error saat login: " + e.getMessage());
        }
        return false;
    }
}
