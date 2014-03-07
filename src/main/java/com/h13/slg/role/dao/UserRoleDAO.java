package com.h13.slg.role.dao;

import com.h13.slg.role.co.UserRoleCO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: sunbo
 * Date: 14-2-21
 * Time: 下午5:58
 * To change this template use File | Settings | File Templates.
 */
@Repository
public class UserRoleDAO {

    @Autowired
    JdbcTemplate j;

    public long insert(final long roleId, final long uid,
                       final long weapon,
                       final long armor, final long accessory) {
        KeyHolder holder = new GeneratedKeyHolder();
        final String sql = "insert into user_role (role_id,uid,weapon,armor,accessory,createtime) " +
                "values (?,?,?,?,?,now())";
        j.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                pstmt.setLong(1, roleId);
                pstmt.setLong(2, uid);
                pstmt.setLong(3, weapon);
                pstmt.setLong(4, armor);
                pstmt.setLong(5, accessory);
                return pstmt;
            }
        }, holder);
        return holder.getKey().longValue();
    }

    public UserRoleCO get(long urId) {
        String sql = "select id,role_id,uid,weapon,armor,accessory from user_role where id=?";
        return j.queryForObject(sql, new Object[]{urId}, new BeanPropertyRowMapper<UserRoleCO>(UserRoleCO.class));
    }


    public void update(long urId, long weapon, long armor, long accessory) {
        String sql = "update user_role set weapon=?,armor=?,accessory=? " +
                "where id=?";
        j.update(sql, new Object[]{weapon, armor, accessory, urId});
    }


}
