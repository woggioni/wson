package net.woggioni.wson.wcfg;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParseError extends RuntimeException {
    public ParseError(String message, int line, int column) {
        super(message + String.format(" at %d:%d", line, column));
    }
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
