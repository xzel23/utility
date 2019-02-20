// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.test.db;

import com.dua3.utility.db.DbUtil;

import org.junit.jupiter.api.Test;

/**
 * Test the FileSystemView class.
 */
public class DbUtilTest {

    @Test
    public void testGetDrivers() {
    	System.out.println(DbUtil.getJdbcDrivers());
    }

}
