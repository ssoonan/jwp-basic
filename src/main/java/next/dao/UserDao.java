package next.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import core.jdbc.ConnectionManager;
import next.model.User;


@FunctionalInterface
interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}
/* pss의 의미는 결국 무엇이냐? sql에서 세팅하는 게 매번 달라지니까, 그 때마다 인터페이스를 람다로 전달하여 깔끔히 하자는 것
* 이걸 가변인자로 대체함은?
* */
@FunctionalInterface
interface PreparedStatementSetter {
    void setValues(PreparedStatement pstmt) throws SQLException;
}


class JDBCTemplate {
    Connection con;
    PreparedStatement pstmt;
    ResultSet rs;

    JDBCTemplate() { // 인스턴스를 만들며 세팅
        con = ConnectionManager.getConnection();
    }

    public void executeUpdate(String sql, PreparedStatementSetter pss) throws SQLException {
        try {
            pstmt = con.prepareStatement(sql);
            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }

            if (con != null) {
                con.close();
            }
        }
    }

    public void executeUpdate(String sql, Object... args) throws SQLException {
        try {
            pstmt = con.prepareStatement(sql);
            for (int i=0; i < args.length; i++) {
                pstmt.setObject(i+1, args[i]);
            }
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }

            if (con != null) {
                con.close();
            }
        }
    }


    public <T> T executeQueryOne(String sql, RowMapper<T> rm, PreparedStatementSetter pss) throws SQLException {
        pstmt = con.prepareStatement(sql);
        pss.setValues(pstmt);
        rs = pstmt.executeQuery();
        if (!rs.next()) return null;
        return rm.mapRow(rs);
    }

    public <T> List<T> executeQuery(String sql, RowMapper<T> rm, PreparedStatementSetter pss) throws SQLException {
        try {
            pstmt = con.prepareStatement(sql);
            pss.setValues(pstmt);
            rs = pstmt.executeQuery();
            ArrayList<T> results = new ArrayList<>();

            while (rs.next()) {
                T result = rm.mapRow(rs);
                results.add(result);
            }
            return results;

        } finally {
            if (pstmt != null) {
                pstmt.close();
            }

            if (con != null) {
                con.close();
            }
        }
    }
}


public class UserDao {

    public void insert(User user) throws SQLException {
        JDBCTemplate jdbcTemplate = new JDBCTemplate();
        jdbcTemplate.executeUpdate("INSERT INTO USERS VALUES (?, ?, ?, ?)", user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
    }

    public void update(User user) throws SQLException {
        JDBCTemplate jdbcTemplate = new JDBCTemplate();
        jdbcTemplate.executeUpdate("UPDATE USERS SET name=?, password=?, email=? WHERE userId=?", user.getName(), user.getPassword(), user.getEmail(), user.getUserId());
    }

    public List<User> findAll() throws SQLException {
        RowMapper<User> rm = rs -> new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"), rs.getString("email"));
        JDBCTemplate jdbcTemplate = new JDBCTemplate();
        return jdbcTemplate.executeQuery("SELECT * FROM USERS", rm, pstmt -> {}); // 이게 되는 이유? 어차피 setValues가 아무것도 안하니까 그냥 빈 깡통인 메서드를 전달하는 것. 구현은 됐으니 상관 X
    }

    public User findByUserId(String userId) throws SQLException {
        PreparedStatementSetter pss =  pstmt -> pstmt.setString(1, userId);
        RowMapper<User> rm = rs -> new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"), rs.getString("email"));
        JDBCTemplate jdbcTemplate = new JDBCTemplate();;
        return jdbcTemplate.executeQueryOne("SELECT userId, password, name, email FROM USERS WHERE userid=?", rm, pss);
    }
}
