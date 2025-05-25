// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test the FileSystemView class.
 */
public class DbUtilTest {

    @Test
    void testGetDrivers() {
        Collection<JdbcDriverInfo> drivers = DbUtil.getJdbcDrivers();
        for (JdbcDriverInfo d : drivers) {
            System.out.println(d);
        }
        assertFalse(drivers.isEmpty());
    }

}
