// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.test.db;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.dua3.utility.db.DbUtil;
import com.dua3.utility.db.JdbcDriverInfo;

/**
 * Test the FileSystemView class.
 */
public class DbUtilTest {

    @Test
    public void testGetDrivers() {
        Collection<JdbcDriverInfo> drivers = DbUtil.getJdbcDrivers();
        for (var d : drivers) {
            System.out.println(d);
        }
    }

}
