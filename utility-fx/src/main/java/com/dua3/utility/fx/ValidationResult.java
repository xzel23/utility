package com.dua3.utility.fx;

import com.dua3.utility.lang.LangUtil;
import javafx.scene.control.Control;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ValidationResult(Control control, Level level, String message) {
    public static ValidationResult ok(Control c) {
        return new ValidationResult(c, Level.OK, "");
    }

    public static ValidationResult error(Control c, String message) {
        return new ValidationResult(c, Level.ERROR, message);
    }

    public boolean isOk() {
        return level == Level.OK;
    }

    public ValidationResult merge(ValidationResult other) {
        LangUtil.check(other.control() == control(), "trying to merge results for different controls");
        if (isOk()) {
            return other;
        }
        if (other.isOk()) {
            return this;
        }
        return new ValidationResult(
                control(),
                Level.ERROR,
                Stream.of(message(), other.message()).filter(s -> !s.isBlank()).collect(Collectors.joining("\n"))
        );
    }

    public enum Level {
        OK,
        ERROR
    }
}
