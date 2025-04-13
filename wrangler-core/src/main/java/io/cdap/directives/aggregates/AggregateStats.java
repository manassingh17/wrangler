/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.Text;

import java.util.Collections;
import java.util.List;

/**
 * AggregateStats directive aggregates a column of byte sizes and a column of time durations.
 * It accepts four arguments:
 *   1. Source column for byte sizes.
 *   2. Source column for time durations.
 *   3. Target column for aggregated size.
 *   4. Target column for aggregated time.
 *
 * Optionally, it accepts an aggregation argument ("total" or "average").
 */
public class AggregateStats implements Directive {

    private String sizeCol;
    private String timeCol;
    private String outSizeCol;
    private String outTimeCol;
    private String aggregation;  // "total" (default) or "average"

    // Accumulators.
    private long totalBytes = 0;
    private long totalNanos = 0;
    private int count = 0;

    /**
     * Defines the directive's usage signature.
     */
    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("time-col", TokenType.COLUMN_NAME);
        builder.define("size-col", TokenType.COLUMN_NAME);
        builder.define("name-col", TokenType.COLUMN_NAME);
        return builder.build();

    }

    /**
     * Initializes the directive with the provided arguments.
     */
    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeCol = ((ColumnName) args.value("size-col")).value();
        this.timeCol = ((ColumnName) args.value("time-col")).value();
        this.outSizeCol = ((ColumnName) args.value("out-size-col")).value();
        this.outTimeCol = ((ColumnName) args.value("out-time-col")).value();

        // Check for an optional aggregation argument.
        if (args.value("aggregation") != null) {
            this.aggregation = ((Text) args.value("aggregation")).value();
        } else {
            this.aggregation = "total";
        }
    }

    /**
     * Processes the list of rows and produces an aggregated result.
     * This method now does not declare a broad throws Exception clause since the interface does not allow it.
     */
    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws ErrorRowException {
        // Reset accumulators in case execute is called multiple times.
        totalBytes = 0;
        totalNanos = 0;
        count = 0;

        for (Row row : rows) {
            Object sizeObj = row.getValue(sizeCol);
            Object timeObj = row.getValue(timeCol);
            if (sizeObj != null && timeObj != null) {
                String sizeStr = sizeObj.toString();
                String timeStr = timeObj.toString();
                try {
                    ByteSize sizeToken = new ByteSize(sizeStr);
                    TimeDuration timeToken = new TimeDuration(timeStr);
                    totalBytes += sizeToken.getBytes();
                    totalNanos += timeToken.getNanos();
                    count++;
                } catch (NumberFormatException e) {
                    throw new ErrorRowException("Error parsing numeric values: " + e.getMessage(), 400);
                }
            }
        }

        if ("average".equalsIgnoreCase(aggregation) && count > 0) {
            totalBytes /= count;
            totalNanos /= count;
        }

        double totalMB = totalBytes / (1024.0 * 1024.0);
        double totalSec = totalNanos / 1_000_000_000.0;

        Row result = new Row();
        result.add(outSizeCol, totalMB);
        result.add(outTimeCol, totalSec);

        return Collections.singletonList(result);
    }

    /**
     * Clean up resources if needed.
     */
    @Override
    public void destroy() {
        // No special cleanup required.
}
}