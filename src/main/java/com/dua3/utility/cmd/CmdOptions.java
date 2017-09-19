package com.dua3.utility.cmd;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CmdOptions implements Iterable<CmdOption> {

    private final List<CmdOption> options = new LinkedList<>();
    
    public CmdOptions(CmdOption... options) {
        add(options);
    }

    public void add(CmdOption... options) {
        this.options.addAll(Arrays.asList(options));
    }

    public String usageHint() {
        return options.stream().map(CmdOption::toString).collect(Collectors.joining(" "));
    }

    public CmdOption get(String arg) {
        for (CmdOption op: options) {
            if (Objects.equals(arg, op.getShortOption()) || Objects.equals(arg, op.getLongOption())) {
                return op;
            }
        }
        throw new CmdException("Unknown option '"+arg+"'.", CmdOption.none());
    }

    @Override
    public Iterator<CmdOption> iterator() {
        return options.iterator();
    }
}
