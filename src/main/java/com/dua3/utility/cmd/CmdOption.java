package com.dua3.utility.cmd;

import java.util.List;
import java.util.function.Consumer;

public class CmdOption implements Consumer<List<String>>{

    private static final CmdOption NONE = new CmdOption("--", null, "", s -> {}, false, 0, Integer.MAX_VALUE);
    
    private final String shortOption;
    private final String longOption;
    private final boolean optional;
    private final String description;
    private final int minArgs;
    private final int maxArgs;

    private final Consumer<List<String>> action;
    
    public static CmdOption createShortFlag(String shortOption, String description, Runnable action) {
        String shortOption_ = shortOption;
        String longOption_ = null;
        boolean optional_ = true;
        String description_ = description;
        Consumer<List<String>> action_ = args -> { action.run(); };
        int minArgs_ = 0;
        int maxArgs_ = 0;
        return new CmdOption(
                shortOption_, 
                longOption_, 
                description_, 
                action_, 
                optional_, 
                minArgs_, 
                maxArgs_);
    }

    public static CmdOption createFlag(String shortOption, String longOption, String description, Runnable action) {
        String shortOption_ = shortOption;
        String longOption_ = longOption;
        boolean optional_ = true;
        String description_ = description;
        Consumer<List<String>> action_ = args -> { action.run(); };
        int minArgs_ = 0;
        int maxArgs_ = 0;
        return new CmdOption(
                shortOption_, 
                longOption_, 
                description_, 
                action_, 
                optional_, 
                minArgs_, 
                maxArgs_);
    }

    public static CmdOption createOption(
            String shortOption,
            String longOption,
            String description,
            Consumer<List<String>> action,
            int minArgs,
            int maxArgs) {
        String shortOption_ = shortOption;
        String longOption_ = longOption;
        boolean optional_ = minArgs==0;
        String description_ = description;
        Consumer<List<String>> action_ = action;
        int minArgs_ = minArgs;
        int maxArgs_ = maxArgs;
        return new CmdOption(
                shortOption_, 
                longOption_, 
                description_, 
                action_, 
                optional_, 
                minArgs_, 
                maxArgs_);
    }
    
    private CmdOption(String shortOption, String longOption, String description, Consumer<List<String>> action, boolean required, int minArgs, int maxArgs) {
        this.shortOption=shortOption;
        this.longOption=longOption;
        this.description=description;
        this.optional = required;
        this.action = action;
        this.minArgs=minArgs;
        this.maxArgs = maxArgs;
    }
    
    @Override
    public void accept(List<String> t) {
        action.accept(t);
    }
    
    public String usageHint() {
        String hint;
        
        if (hasShortOption() && hasLongOption()) {
            hint = getShortOption() + " | " + getLongOption();
        } else if (hasShortOption()) {
            hint = getShortOption();
        } else if (hasLongOption()) {
            hint = getLongOption();
        } else {
            throw new IllegalStateException();
        }
        
        return isOptional() ? "[ " + hint + " ]" : hint;
    }

    public boolean hasShortOption() {
        return shortOption!=null;
    }
    
    public boolean hasLongOption() {
        return longOption!=null;
    }
    
    public boolean isOptional() {
        return optional;
    }
    
    public void call(List<String> args) {
        if (args.size()<minArgs) {
            throw new CmdException.MissingArgumentException(this, args.size());
        }
        if (args.size()>maxArgs) {
            throw new CmdException.ExcessiveArgumentException(this, args.size());
        }
        action.accept(args);
    }

    public int getMinArgs() {
        return minArgs;
    }
    
    public int getMaxArgs() {
        return maxArgs;
    }
    
    public String getName() {
        return hasLongOption() ? getLongOption() : getShortOption();
    }

    public String getShortOption() {
        return "-"+shortOption;
    }
    public String getLongOption() {
        return "--"+longOption;
    }

    public static CmdOption none() {
        return NONE;
    }
    
    public String getDescription() {
        return description;
    }
}
