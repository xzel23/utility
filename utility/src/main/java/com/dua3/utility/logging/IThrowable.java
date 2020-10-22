package com.dua3.utility.logging;

import java.util.Objects;

public interface IThrowable {
    IThrowable getCause();

    IStackTraceElement[] getStackTrace();

    interface IStackTraceElement {
    }

    class JavaThrowable implements IThrowable {
        private final Throwable t;
        private IStackTraceElement[] ist = null;
        
        JavaThrowable(Throwable t) {
            this.t= Objects.requireNonNull(t);
        }

        @Override
        public IThrowable getCause() {
            Throwable cause = t.getCause();
            return cause == null ? null : new JavaThrowable(cause);
        }

        @Override
        public IStackTraceElement[] getStackTrace() {
            if (ist==null) {
                StackTraceElement[] st = t.getStackTrace();
                IStackTraceElement[] ist_ = new IStackTraceElement[st.length];
                for (int i = 0; i < st.length; i++) {
                    ist_[i] = new JavaStackTraceElement(st[i]);
                }
                ist = ist_;
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
