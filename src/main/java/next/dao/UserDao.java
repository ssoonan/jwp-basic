package next.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import core.jdbc.ConnectionManager;
import next.model.User;


abstract class JDBCTemplate {
    Connection con;
    PreparedStatement pstmt;

    JDBCTemplate() { // 인스턴스를 만들며 세팅
        con = ConnectionManager.getConnection();
    }

    public abstract void setValues() throws SQLException;

    public void executeUpdate(String sql) throws SQLException {
        try {
            pstmt = con.prepareStatement(sql);
            setValues();
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
}


// 그냥 확장해서 만드려니 또 확장성이 꺠지네,, 흠.. 익명클래스도 일회성이랄까 -> 다시 추상클래스로 만들면 됐군
abstract class SelectJDBCTemplate<T> extends JDBCTemplate {
    ResultSet rs;
    public abstract T mapRow(ResultSet rs) throws SQLException; // rs의 결과를 매핑해서 ArrayList로 반환하는 함수. 이 때 타입이정해지지 않으니 Object로 return

    public List<T> executeQuery(String sql) throws SQLException {
        try {
            pstmt = con.prepareStatement(sql);
            setValues();
            rs = pstmt.executeQuery();
            ArrayList<T> results = new ArrayList<>();

            while (rs.next()) {
                T result = mapRow(rs);
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
        JDBCTemplate jdbcTemplate = new JDBCTemplate() {
            @Override
            public void setValues() throws SQLException {
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getName());
                pstmt.setString(4, user.getEmail());
            }
        };
        jdbcTemplate.executeUpdate("INSERT INTO USERS VALUES (?, ?, ?, ?)");
    }


    public void update(User user) throws SQLException {
        JDBCTemplate jdbcTemplate = new JDBCTemplate() {
            @Override
            public void setValues() throws SQLException {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getUserId());
            }
        };
        jdbcTemplate.executeUpdate("UPDATE USERS SET name=?, password=?, email=? WHERE userId=?");
    }

    public List<User> findAll() throws SQLException {
        SelectJDBCTemplate<User> selectJDBCTemplate = new SelectJDBCTemplate<User>() {
            @Override
            public User mapRow(ResultSet rs) throws SQLException {
                return new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"), rs.getString("email"));
            }

            @Override
            public void setValues() throws SQLException {

            }
        };
        return selectJDBCTemplate.executeQuery("SELECT * FROM USERS");
    }

    public User findByUserId(String userId) throws SQLException {
        SelectJDBCTemplate<User> selectJDBCTemplate = new SelectJDBCTemplate<User>() {
            @Override
            public User mapRow(ResultSet rs) throws SQLException {
                return new User(rs.getString("userId"), rs.getString("password"), rs.getString("name"),
                        rs.getString("email"));
            }

            @Override
            public void setValues() throws SQLException {
                pstmt.setString(1, userId);
            }
        };
        List<User> users = selectJDBCTemplate.executeQuery("SELECT userId, password, name, email FROM USERS WHERE userid=?");
        if (users.isEmpty()) return null;
        return users.get(0);
    }
}
