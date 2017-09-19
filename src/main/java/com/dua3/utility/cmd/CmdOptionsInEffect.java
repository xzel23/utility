package com.dua3.utility.cmd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class CmdOptionsInEffect {
    private final CmdOptions availableOptions;
    LinkedHashMap<CmdOption, List<String>> arguments = new LinkedHashMap<>();
    
    public CmdOptionsInEffect(CmdOptions availableOptions, String[] cmdArgs) {
        this.availableOptions = availableOptions;
        
        initArgs(cmdArgs);
    }

    private void initArgs(String[] cmdArgs) {
        CmdOption opt = null;
        List<String> optArgs = new LinkedList<>();
        boolean residual = false;
        for (String arg: cmdArgs) {            
            if (residual) {
                optArgs.add(arg);
                continue;
            }
            if (arg.startsWith("-")) {
                // sanitize
                if (opt==null && !optArgs.isEmpty()) {
                    throw new CmdException("Command options must be specified before other arguments.", CmdOption.none());
                }
                // store current option
                if (opt!=null) {
                    storeOption(opt, optArgs);
                }
                // get new option
                if (arg.equals("--")) {
                    // special treatment for residual marker
                    residual = true;
                    opt = null;
                } else {
                    // get option
                    opt = availableOptions.get(arg);                    
                }
                optArgs.clear();
                continue;
            }
            // add to current args
            optArgs.add(arg);
        }
        // store remainder of args
        storeOption(opt, optArgs);
    }

    private void storeOption(CmdOption opt, List<String> optArgs) {
        List<String> old = arguments.put(opt, new ArrayList<>(optArgs));
        if (old!=null) {
            throw new CmdException("Option was passed multiple times.", opt);
        }
    }
    
    public void apply() {
        checkAndApply(false);
    }
    
    public void check() {
        checkAndApply(true);
    }
    
    private void checkAndApply(boolean testOnly) {
        for (CmdOption op: availableOptions) {
            if (!arguments.containsKey(op)) {
                // option is not set => make sure it is optional
                if (!op.isOptional()) {
                    throw new CmdException.MissingOptionException(op);
                }
            } else {
                // option is set => check arity
                List<String> args = arguments.get(op);
                if (args.size()<op.getMinArgs()) {
                    throw new CmdException.MissingArgumentException(op, args.size());
                }
                if (args.size()>op.getMaxArgs()) {
                    throw new CmdException.ExcessiveArgumentException(op, args.size());
                }
                // execute action
                if (!testOnly) {
                    op.call(args);
                }
            }
        }
    }

    public List<String> getResidualArgs() {
        return arguments.computeIfAbsent(null, opt -> new ArrayList<>());
    }
}
