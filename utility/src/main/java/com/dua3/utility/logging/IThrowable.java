package com.dua3.utility.logging;

import java.util.*;

/**
 * A wrapper interface to abstract handling of {@link Throwable} in different logging frameworks. 
 */
public interface IThrowable {
    /**
     * Get the cause.
     * @return cause
     */
    IThrowable getCause();

    /**
     * Get stack trace.
     * @return list of {@link IStackTraceElement}
     */
    List<IStackTraceElement> getStackTrace();

    /**
     * A wrapper interface to abstract handling of {@link StackTraceElement} in different logging frameworks. 
     */
    interface IStackTraceElement {
    }

    /**
     * An implementation of {@link IThrowable} that encapsulates an instance of {@link Throwable}.
     */
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
                List<IStackTraceElement> ist_ = new ArrayList<>(st.length);
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

    /**
     * An implementation of {@link IStackTraceElement} that encapsulates an instance of {@link StackTraceElement}.
     */
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
