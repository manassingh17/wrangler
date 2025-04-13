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
 * TimeDuration token parses a string like "150ms" or "2s" into a canonical 
 * value in nanoseconds.
 */
public class TimeDuration implements Token {
    private final double nanos;
    private final String raw;

    public TimeDuration(String raw) {
        this.raw = raw.trim();
        this.nanos = parseToNanos(this.raw);
    }

    private double parseToNanos(String input) {
        input = input.toLowerCase();
        if (input.endsWith("ns")) {
            return Double.parseDouble(input.substring(0, input.length() - 2));
        } else if (input.endsWith("us")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 1_000;
        } else if (input.endsWith("ms")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 1_000_000;
        } else if (input.endsWith("sec")) {
            return Double.parseDouble(input.substring(0, input.length() - 3)) * 1_000_000_000;
        } else if (input.endsWith("s")) {
            return Double.parseDouble(input.substring(0, input.length() - 1)) * 1_000_000_000;
        } else if (input.endsWith("min")) {
            return Double.parseDouble(input.substring(0, input.length() - 3)) * 60 * 1_000_000_000L;
        } else if (input.endsWith("m")) {
            return Double.parseDouble(input.substring(0, input.length() - 1)) * 60 * 1_000_000_000L;
        } else if (input.endsWith("hr")) {
            return Double.parseDouble(input.substring(0, input.length() - 2)) * 3600 * 1_000_000_000L;
        } else if (input.endsWith("h")) {
            return Double.parseDouble(input.substring(0, input.length() - 1)) * 3600 * 1_000_000_000L;
        } else if (input.endsWith("d")) {
            return Double.parseDouble(input.substring(0, input.length() - 1)) * 86400 * 1_000_000_000L;
        } else {
            throw new IllegalArgumentException("Invalid time duration format: " + input);
        }
    }
    public static TimeDuration parse(String input) {
        return new TimeDuration(input);
    }
    public long getMilliseconds() {
        return getNanos() / 1_000_000;
    }

    /**
     * Returns the parsed value in nanoseconds.
     */
    public long getNanos() {
        return (long) nanos;
    }

    @Override
    public Object value() {
        return getNanos();
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(nanos);
}
}