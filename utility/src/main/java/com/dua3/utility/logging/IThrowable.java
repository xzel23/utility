package com.dua3.utility.logging;

import java.util.*;

public interface IThrowable {
    IThrowable getCause();

    List<IStackTraceElement> getStackTrace();

    interface IStackTraceElement {
    }

    class JavaThrowable implements IThrowable {
        private final Throwable t;
        private List<IStackTraceElement> ist = null;
        
        JavaThrowable(Throwable t) {
            this.t= Objects.requireNonNull(t);
        }

        @Override
        public IThrowable getCause() {
            Throwable cause = t.getCause();
            return cause == null ? null : new JavaThrowable(cause);
        }

        @Override
        public List<IStackTraceElement> getStackTrace() {
            if (ist==null) {
                StackTraceElement[] st = t.getStackTrace();
                List<IStackTraceElement> ist_ = new ArrayList(st.length);
                for (StackTraceElement ste: st) {
                    ist_.add(new JavaStackTraceElement(ste));
                }
                ist = Collections.unmodifiableList(ist_);
            }
            return ist;
        }

        @Override
        public String toString() {
            return t.toString();
        }
    }

    class JavaStackTraceElement implements IStackTraceElement {
        private final StackTraceElement ste;
        
        JavaStackTraceElement(StackTraceElement ste) {
            this.ste = ste;
        }

        @Override
        public String toString() {
            return ste.toString();
        }
    }
}
