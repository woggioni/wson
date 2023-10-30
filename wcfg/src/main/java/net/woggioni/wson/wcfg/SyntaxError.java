package net.woggioni.wson.wcfg;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SyntaxError extends RuntimeException {
    public SyntaxError(String message, int line, int column) {
        super(message + String.format(" at %d:%d", line, column));
    }
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
