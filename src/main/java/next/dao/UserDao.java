package next.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import core.jdbc.ConnectionManager;
import next.model.User;


interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}

interface PreparedStatementSetter {
    void setValues(PreparedStatement pstmt) throws SQLException;
}


class JDBCTemplate<T> {
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


    public T executeQueryOne(String sql, PreparedStatementSetter pss, RowMapper<T> rm) throws SQLException {
        pstmt = con.prepareStatement(sql);
        pss.setValues(pstmt);
        rs = pstmt.executeQuery();
        if (!rs.next()) return null;
        return rm.mapRow(rs);
    }

    public List<T> executeQuery(String sql, PreparedStatementSetter pss, RowMapper<T> rm) throws SQLException {
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
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getName());
                pstmt.setString(4, user.getEmail());
            }
        };
        JDBCTemplate jdbcTemplate = new JDBCTemplate();
        jdbcTemplate.executeUpdate("INSERT INTO USERS VALUES (?, ?, ?, ?)", pss);
    }


    public void update(User user) throws SQLException {
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getUserId());
            }
        };
        JDBCTemplate jdbcTemplate = new JDBCTemplate();
        jdbcTemplate.executeUpdate("UPDATE USERS SET name=?, password=?, email=? WHERE userId=?", pss);
    }

    public List<User> findAll() throws SQLException {

        RowMapper<User> rm = new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs) throws SQLException {
                return new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"), rs.getString("email"));
            }
        };

        JDBCTemplate<User> jdbcTemplate = new JDBCTemplate<>();
        return jdbcTemplate.executeQuery("SELECT * FROM USERS", pstmt -> {}, rm);
    }

    public User findByUserId(String userId) throws SQLException {
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, userId);
            }
        };
        RowMapper<User> rm = new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs) throws SQLException {
                return new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"),
                        rs.getString("email"));
            }
        };
        JDBCTemplate<User> jdbcTemplate = new JDBCTemplate<>();;
        return jdbcTemplate.executeQueryOne("SELECT userId, password, name, email FROM USERS WHERE userid=?", pss, rm);
    }
}
