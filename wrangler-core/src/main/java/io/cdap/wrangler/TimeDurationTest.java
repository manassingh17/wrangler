/*
 * Copyright © 2024 Manas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package io.cdap.wrangler;

import org.junit.Test;
import static org.junit.Assert.*;
import io.cdap.wrangler.api.parser.TimeDuration;

public class TimeDurationTest {

    @Test
    public void testParseTimeDuration() {
        assertEquals(10 * 1000, TimeDuration.parse("10s").getMilliseconds());
        assertEquals(2 * 60 * 1000, TimeDuration.parse("2m").getMilliseconds());
        assertEquals((long)(1.5 * 60 * 60 * 1000), TimeDuration.parse("1.5h").getMilliseconds());
        assertEquals(5L, TimeDuration.parse("5ms").getMilliseconds());
        assertEquals(0, TimeDuration.parse("0s").getNanos());
        assertEquals(60L * 1_000_000_000, TimeDuration.parse("1min").getNanos());
        //assertThrows(IllegalArgumentException.class, () -> TimeDuration.parse("20lightyears"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        TimeDuration.parse("10lightyears");
}
}