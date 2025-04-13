/*
 * Copyright © 2024 Manas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * ByteSize token parses a string like "10KB" or "1.5MB" into a canonical
 * value in bytes.
 */
public class ByteSize implements Token {
    private final double bytes;
    private final String raw;

    public ByteSize(String raw) {
        this.raw = raw.trim();
        this.bytes = parseToBytes(this.raw);
    }

    private double parseToBytes(String input) {
        input = input.toUpperCase();
        if (input.endsWith("KB")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 1024;
        } else if (input.endsWith("MB")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 1024 * 1024;
        } else if (input.endsWith("GB")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 1024 * 1024 * 1024;
        } else if (input.endsWith("TB")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 1024L * 1024 * 1024 * 1024;
        } else if (input.endsWith("B")) {
            return Double.parseDouble(input.substring(0, input.length() - 1));
        } else {
            throw new IllegalArgumentException("Invalid byte size format: " + input);
        }
    }

    /**
     * Returns the parsed value in bytes.
     */
    public long getBytes() {
        return (long) bytes;
    }

    @Override
    public Object value() {
        return getBytes();
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(bytes);
}
    public static long parse(String input) {
        return new ByteSize(input).getBytes();
}

}