/*
 * Copyright 2016 Axel Howind.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.text;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A class for rich text, i.e. text together with attributes like color, font etc.
 * <p>
 * Sequences of characters that share the same formatting attributes form a {@link Run}.
 */
public class RichText
        implements Iterable<Run>, ToRichText {

    private static final RichText EMPTY_TEXT = RichText.valueOf("");

    /**
     * Returns the empty String as RichText.
     *
     * @return the empty text
     */
    public static RichText emptyText() {
        return EMPTY_TEXT;
    }

    public static RichText valueOf(Object o) {
        return valueOf(String.valueOf(o));
    }

    /**
     * Convert String to RichText.
     *
     * @param s
     *            String to convert
     * @return RichText representation of s
     */
    public static RichText valueOf(String s) {
        return new RichText(Arrays.asList(new Run(s, 0, s.length(), TextAttributes.none())));
    }

    private final CharSequence text;
    private final List<Run> runs;

    RichText(List<Run> runs) {
        this.text = runs.isEmpty() ? "" : runs.get(0).base();
        this.runs = runs;        
        
        assert checkAllRunsHaveTextAsBase(runs);
    }

    private boolean checkAllRunsHaveTextAsBase(List<Run> runs) {
        boolean ok = true;
        for (Run run: runs) {
            ok &= run.base()==text;
        }
        return ok;
    }

    RichText(Run[] runs) {
        this(Arrays.asList(runs));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RichText other = (RichText) obj;
        return runs.equals(other.runs);
    }

    @Override
    public int hashCode() {
        return text.hashCode() + 17 * runs.size();
    }

    /**
     * Test if empty.
     *
     * @return true, if the text is empty.
     */
    public boolean isEmpty() {
        return text.length()==0;
    }

    @Override
    public Iterator<Run> iterator() {
        return runs.iterator();
    }

    /**
     * Length of text in characters.
     *
     * @return the text length
     */
    public int length() {
        return text.length();
    }

    /**
     * A stream of the Runs this text consists of.
     *
     * @return stream of Runs
     */
    public Stream<Run> stream() {
        return runs.stream();
    }

    @Override
    public String toString() {
        return text.toString();
    }

	@Override
	public void appendTo(RichTextBuilder builder) {
        builder.ensureCapacity(builder.length()+this.length());
        stream().forEach(builder::appendRun);
	}

	@Override
	public RichText toRichText() {
		return this;
	}
}