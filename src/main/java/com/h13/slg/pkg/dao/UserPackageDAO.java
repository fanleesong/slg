package com.h13.slg.pkg.dao;

import com.alibaba.fastjson.JSON;
import com.h13.slg.pkg.co.UserPackageCO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sunbo
 * Date: 14-2-26
 * Time: 下午4:46
 * To change this template use File | Settings | File Templates.
 */
@Repository
public class UserPackageDAO {

    @Autowired
    JdbcTemplate j;

    public void insert(long id, Map<String, Integer> roleCard, Map<String, List<Long>> equip,
                       Map<String, Integer> gem,
                       Map<String, Integer> material) {

        String sql = "insert into user_package " +
                "(id,role_card,equip,gem,material,createtime) " +
                "values" +
                "(?,?,?,?,?,now())";
        j.update(sql, new Object[]{id,
                JSON.toJSONString(roleCard),
                JSON.toJSONString(equip),
                JSON.toJSONString(gem),
                JSON.toJSONString(material)});
    }


    public UserPackageCO get(long id) {
        String sql = "select id,role_card,equip,gem,material,createtime from user_package where id=?";
        return j.queryForObject(sql, new Object[]{id}, new RowMapper<UserPackageCO>() {
            @Override
            public UserPackageCO mapRow(ResultSet resultSet, int i) throws SQLException {
                UserPackageCO userPackageCO = new UserPackageCO();
                userPackageCO.setId(resultSet.getInt(1));
                userPackageCO.setRoleCard(JSON.parseObject(resultSet.getString(2), Map.class));
                userPackageCO.setEquip(JSON.parseObject(resultSet.getString(3), Map.class));
                userPackageCO.setGem(JSON.parseObject(resultSet.getString(4), Map.class));
                userPackageCO.setMaterial(JSON.parseObject(resultSet.getString(5), Map.class));
                return userPackageCO;
            }
        });
    }


    public void updateRoleCard(long id, Map<String, Integer> roleCard) {
        String sql = "update user_package set role_card=? where id=?";
        j.update(sql, new Object[]{JSON.toJSONString(roleCard), id});
    }

    public void updateEquip(long id, Map<String, List<Long>> equip) {
        String sql = "update user_package set equip=? where id=?";
        j.update(sql, new Object[]{JSON.toJSONString(equip), id});
    }

    public void updateGem(long id, Map<String, Integer> gem) {
        String sql = "update user_package set gem=? where id=?";
        j.update(sql, new Object[]{JSON.toJSONString(gem), id});
    }


    public void updateMaterial(long id, Map<String, Integer> material) {
        String sql = "update user_package set material=? where id=?";
        j.update(sql, new Object[]{JSON.toJSONString(material), id});
    }

}

