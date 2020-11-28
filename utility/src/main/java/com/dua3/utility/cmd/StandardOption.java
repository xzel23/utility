package com.dua3.utility.cmd;

import com.dua3.utility.data.DataUtil;

import java.util.function.Function;

public class StandardOption<T> extends Option<T> {
    
    StandardOption(Class<T> type, String... names) {
        this(s -> DataUtil.convert(s, type), names);
    }

    StandardOption(Function<String,T> mapper, String... names) {
        super(mapper, names);
    }

    @Override
    public StandardOption<T> description(String description) {
        super.description(description);
        return this;
    }

    public StandardOption<T> occurence(int o) {
        return occurence(o, o);
    }

    @Override
    public StandardOption<T> occurence(int min, int max) {
        super.occurence(min, max);
        return this;
    }

    public StandardOption<T> arity(int a) {
        return arity(a,a);
    }
    
    public StandardOption<T> arity(int minArity, int maxArity) {
        super.arity(minArity, maxArity);
        return this;
    }
}
