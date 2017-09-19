package com.dua3.utility.cmd;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the FileSystemView class.
 */
public class CmdOptionsTest {
    
    static class TestHelper {
        TestHelper(String[] args) {
            CmdOptionsInEffect opts = new CmdOptionsInEffect(availableOptions,args);
            opts.apply();
            this.residualArgs = opts.getResidualArgs();
        }

        List<String> residualArgs;
        
        boolean t = false;
        CmdOptions availableOptions = new CmdOptions(
                CmdOption.createShortFlag("t", "set test flag", () -> t=true)
           );
    }
    
    @Test
    public void testNoArgs() {
        String[] args = {};
        TestHelper t = new TestHelper(args);
        Assert.assertFalse(t.t);
    }
    
    @Test
    public void testNoOptions() {
        String[] args = {"123", "abc"};
        TestHelper t = new TestHelper(args);
        Assert.assertFalse(t.t);
        Assert.assertEquals(Arrays.asList(args), t.residualArgs);
    }
    
    @Test
    public void testShortFlag() {
        String[] args = {"-t"};
        TestHelper t = new TestHelper(args);
        Assert.assertTrue(t.t);
    }
    
    @Test
    public void testUnknownFlag() {
        String[] args = {"-u"};
        boolean passed = false;
        try {
            new TestHelper(args);
        } catch (CmdException e) {
            passed = true;
        }
        Assert.assertTrue(passed);
    }
    
    @Test
    public void testShortFlagWithArgs() {
        String[] args = {"-t","--","abc","123"};
        TestHelper t = new TestHelper(args);
        Assert.assertTrue(t.t);
        Assert.assertEquals(Arrays.asList("abc","123"), t.residualArgs);
    }
    
}
