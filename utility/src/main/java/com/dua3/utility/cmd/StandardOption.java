package com.dua3.utility.cmd;

public class StandardOption extends AbstractOption {
    
    StandardOption(String... names) {
        super(names);
    }

    @Override
    public StandardOption description(String description) {
        super.description(description);
        return this;
    }

    public StandardOption occurence(int o) {
        return occurence(o, o);
    }

    @Override
    public StandardOption occurence(int min, int max) {
        super.occurence(min, max);
        return this;
    }

    public StandardOption arity(int a) {
        return arity(a,a);
    }
    
    public StandardOption arity(int minArity, int maxArity) {
        super.arity(minArity, maxArity);
        return this;
    }
}
