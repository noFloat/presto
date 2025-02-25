/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator.scalar.sql;

import com.facebook.presto.common.type.ArrayType;
import com.facebook.presto.common.type.RowType;
import com.facebook.presto.operator.scalar.AbstractTestFunctions;
import com.facebook.presto.spi.StandardErrorCode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static com.facebook.presto.block.BlockAssertions.createMapType;
import static com.facebook.presto.common.type.BigintType.BIGINT;
import static com.facebook.presto.common.type.BooleanType.BOOLEAN;
import static com.facebook.presto.common.type.DoubleType.DOUBLE;
import static com.facebook.presto.common.type.IntegerType.INTEGER;
import static com.facebook.presto.common.type.VarcharType.VARCHAR;
import static java.util.Collections.singletonList;

public class TestArraySqlFunctions
        extends AbstractTestFunctions
{
    @Test
    public void testArraySum()
    {
        assertFunction("array_sum(array[BIGINT '1', BIGINT '2'])", BIGINT, 3L);
        assertFunction("array_sum(array[INTEGER '1', INTEGER '2'])", BIGINT, 3L);
        assertFunction("array_sum(array[SMALLINT '1', SMALLINT '2'])", BIGINT, 3L);
        assertFunction("array_sum(array[TINYINT '1', TINYINT '2'])", BIGINT, 3L);

        assertFunction("array_sum(array[BIGINT '1', INTEGER '2'])", BIGINT, 3L);
        assertFunction("array_sum(array[INTEGER '1', SMALLINT '2'])", BIGINT, 3L);
        assertFunction("array_sum(array[SMALLINT '1', TINYINT '2'])", BIGINT, 3L);

        assertFunctionWithError("array_sum(array[DOUBLE '-2.0', DOUBLE '5.3'])", DOUBLE, 3.3);
        assertFunctionWithError("array_sum(array[DOUBLE '-2.0', REAL '5.3'])", DOUBLE, 3.3);
        assertFunctionWithError("array_sum(array[DOUBLE '-2.0', DECIMAL '5.3'])", DOUBLE, 3.3);
        assertFunctionWithError("array_sum(array[REAL '-2.0', DECIMAL '5.3'])", DOUBLE, 3.3);

        assertFunctionWithError("array_sum(array[BIGINT '-2', DOUBLE '5.3'])", DOUBLE, 3.3);
        assertFunctionWithError("array_sum(array[INTEGER '-2', REAL '5.3'])", DOUBLE, 3.3);
        assertFunctionWithError("array_sum(array[SMALLINT '-2', DECIMAL '5.3'])", DOUBLE, 3.3);
        assertFunctionWithError("array_sum(array[TINYINT '-2', DOUBLE '5.3'])", DOUBLE, 3.3);

        assertFunction("array_sum(null)", BIGINT, null);
        assertFunction("array_sum(array[])", BIGINT, 0L);
        assertFunction("array_sum(array[NULL])", BIGINT, 0L);
        assertFunction("array_sum(array[NULL, NULL, NULL])", BIGINT, 0L);
        assertFunction("array_sum(array[3, NULL, 5])", BIGINT, 8L);
        assertFunctionWithError("array_sum(array[NULL, double '1.2', double '2.3', NULL, -3])", DOUBLE, 0.5);
    }

    @Test
    public void testArrayAverage()
    {
        assertFunctionWithError("array_average(array[1, 2])", DOUBLE, 1.5);
        assertFunctionWithError("array_average(array[1, bigint '2', smallint '3', tinyint '4', 5.0])", DOUBLE, 3.0);

        assertFunctionWithError("array_average(array[1, null, 2, null])", DOUBLE, 1.5);
        assertFunctionWithError("array_average(array[null, null, 1])", DOUBLE, 1.0);

        assertFunction("array_average(array[null])", DOUBLE, null);
        assertFunction("array_average(array[null, null])", DOUBLE, null);
        assertFunction("array_average(null)", DOUBLE, null);
    }

    @Test
    public void testArrayFrequencyBigint()
    {
        assertFunction("array_frequency(cast(null as array(bigint)))", createMapType(BIGINT, INTEGER), null);
        assertFunction("array_frequency(cast(array[] as array(bigint)))", createMapType(BIGINT, INTEGER), ImmutableMap.of());
        assertFunction("array_frequency(array[cast(null as bigint), cast(null as bigint), cast(null as bigint)])", createMapType(BIGINT, INTEGER), ImmutableMap.of());
        assertFunction("array_frequency(array[cast(null as bigint), bigint '1'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 1));
        assertFunction("array_frequency(array[cast(null as bigint), bigint '1', bigint '3', cast(null as bigint), bigint '1', bigint '3', cast(null as bigint)])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 2, 3L, 2));
        assertFunction("array_frequency(array[bigint '1', bigint '1', bigint '2', bigint '2', bigint '3', bigint '1', bigint '3', bigint '2'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 3, 2L, 3, 3L, 2));
        assertFunction("array_frequency(array[bigint '45'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(45L, 1));
        assertFunction("array_frequency(array[bigint '-45'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(-45L, 1));
        assertFunction("array_frequency(array[bigint '1', bigint '3', bigint '1', bigint '3'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 2, 3L, 2));
        assertFunction("array_frequency(array[bigint '3', bigint '1', bigint '3',bigint '1'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 2, 3L, 2));
        assertFunction("array_frequency(array[bigint '4',bigint '3',bigint '3',bigint '2',bigint '2',bigint '2',bigint '1',bigint '1',bigint '1',bigint '1'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 4, 2L, 3, 3L, 2, 4L, 1));
        assertFunction("array_frequency(array[bigint '3', bigint '3', bigint '2', bigint '2', bigint '5', bigint '5', bigint '1', bigint '1'])", createMapType(BIGINT, INTEGER), ImmutableMap.of(1L, 2, 2L, 2, 3L, 2, 5L, 2));
    }

    @Test
    public void testArrayFrequencyVarchar()
    {
        assertFunction("array_frequency(cast(null as array(varchar)))", createMapType(VARCHAR, INTEGER), null);
        assertFunction("array_frequency(cast(array[] as array(varchar)))", createMapType(VARCHAR, INTEGER), ImmutableMap.of());
        assertFunction("array_frequency(array[cast(null as varchar), cast(null as varchar), cast(null as varchar)])", createMapType(VARCHAR, INTEGER), ImmutableMap.of());
        assertFunction("array_frequency(array[varchar 'z', cast(null as varchar)])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("z", 1));
        assertFunction("array_frequency(array[varchar 'a', cast(null as varchar), varchar 'b', cast(null as varchar), cast(null as varchar) ])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("a", 1, "b", 1));
        assertFunction("array_frequency(array[varchar 'a', varchar 'b', varchar 'a', varchar 'a', varchar 'a'])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("a", 4, "b", 1));
        assertFunction("array_frequency(array[varchar 'a', varchar 'b', varchar 'a', varchar 'b', varchar 'c'])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("a", 2, "b", 2, "c", 1));
        assertFunction("array_frequency(array[varchar 'y', varchar 'p'])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("p", 1, "y", 1));
        assertFunction("array_frequency(array[varchar 'a', varchar 'a', varchar 'p'])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("p", 1, "a", 2));
        assertFunction("array_frequency(array[varchar 'z'])", createMapType(VARCHAR, INTEGER), ImmutableMap.of("z", 1));
    }

    @Test
    public void testArrayFrequencyComplexTypes()
    {
        assertFunction("array_frequency(cast(null as array(array(varchar))))", createMapType(new ArrayType(VARCHAR), INTEGER), null);
        assertFunction("array_frequency(cast(array[] as array(array(varchar))))", createMapType(new ArrayType(VARCHAR), INTEGER), ImmutableMap.of());
        assertFunction("array_frequency(array[cast(null as array(varchar)), cast(null as array(varchar)), cast(null as array(varchar))])", createMapType(new ArrayType(VARCHAR), INTEGER), ImmutableMap.of());
        assertFunction("array_frequency(array[array[varchar 'z'], array[varchar 'z']])", createMapType(new ArrayType(VARCHAR), INTEGER), ImmutableMap.of(singletonList("z"), 2));
        assertFunction("array_frequency(array[array[varchar 'z'], array[varchar 't']])", createMapType(new ArrayType(VARCHAR), INTEGER), ImmutableMap.of(singletonList("z"), 1, singletonList("t"), 1));

        RowType rowType = RowType.from(ImmutableList.of(RowType.field(INTEGER), RowType.field(INTEGER)));
        String t = rowType.toString();
        assertFunction("array_frequency(array[(1, 2), (1, 3), (1, 2)])", createMapType(rowType, INTEGER), ImmutableMap.of(ImmutableList.of(1, 2), 2, ImmutableList.of(1, 3), 1));
        assertInvalidFunction("array_frequency(array[(1, null), (null, 2), (null, 1)])", StandardErrorCode.NOT_SUPPORTED, "ROW comparison not supported for fields with null elements");
        assertInvalidFunction("array_frequency(array[(null, 1), (1, null), (null, null)])", StandardErrorCode.NOT_SUPPORTED, "map key cannot be null or contain nulls");
    }

    @Test
    public void testArrayHasDuplicates()
    {
        assertFunction("array_has_duplicates(cast(null as array(varchar)))", BOOLEAN, null);
        assertFunction("array_has_duplicates(cast(array[] as array(varchar)))", BOOLEAN, false);

        assertFunction("array_has_duplicates(array[varchar 'a', varchar 'b', varchar 'a'])", BOOLEAN, true);
        assertFunction("array_has_duplicates(array[varchar 'a', varchar 'b'])", BOOLEAN, false);
        assertFunction("array_has_duplicates(array[varchar 'a', varchar 'a'])", BOOLEAN, true);

        assertFunction("array_has_duplicates(array[1, 2, 1])", BOOLEAN, true);
        assertFunction("array_has_duplicates(array[1, 2])", BOOLEAN, false);
        assertFunction("array_has_duplicates(array[1, 1, 1])", BOOLEAN, true);

        assertFunction("array_has_duplicates(array[0, null])", BOOLEAN, false);
        assertFunction("array_has_duplicates(array[0, null, null])", BOOLEAN, true);

        // Test legacy name.
        assertFunction("array_has_dupes(array[varchar 'a', varchar 'b', varchar 'a'])", BOOLEAN, true);

        assertFunction("array_has_duplicates(array[array[1], array[2], array[]])", BOOLEAN, false);
        assertFunction("array_has_duplicates(array[array[1], array[2], array[2]])", BOOLEAN, true);
        assertFunction("array_has_duplicates(array[(1, 2), (1, 2)])", BOOLEAN, true);
        assertFunction("array_has_duplicates(array[(1, 2), (2, 2)])", BOOLEAN, false);
        assertInvalidFunction("array_has_duplicates(array[(1, null), (null, 2), (null, 1)])", StandardErrorCode.NOT_SUPPORTED, "ROW comparison not supported for fields with null elements");
        assertInvalidFunction("array_has_duplicates(array[(1, null), (null, 2), (null, null)])", StandardErrorCode.NOT_SUPPORTED, "map key cannot be null or contain nulls");
    }

    @Test
    public void testArrayDuplicates()
    {
        assertFunction("array_duplicates(cast(null as array(varchar)))", new ArrayType(VARCHAR), null);
        assertFunction("array_duplicates(cast(array[] as array(varchar)))", new ArrayType(VARCHAR), ImmutableList.of());

        assertFunction("array_duplicates(array[varchar 'a', varchar 'b', varchar 'a'])", new ArrayType(VARCHAR), ImmutableList.of("a"));
        assertFunction("array_duplicates(array[varchar 'a', varchar 'b'])", new ArrayType(VARCHAR), ImmutableList.of());
        assertFunction("array_duplicates(array[varchar 'a', varchar 'a'])", new ArrayType(VARCHAR), ImmutableList.of("a"));

        assertFunction("array_duplicates(array[1, 2, 1])", new ArrayType(INTEGER), ImmutableList.of(1));
        assertFunction("array_duplicates(array[1, 2])", new ArrayType(INTEGER), ImmutableList.of());
        assertFunction("array_duplicates(array[1, 1, 1])", new ArrayType(INTEGER), ImmutableList.of(1));

        assertFunction("array_duplicates(array[0, null])", new ArrayType(INTEGER), ImmutableList.of());
        assertFunction("array_duplicates(array[0, null, null])", new ArrayType(INTEGER), singletonList(null));

        // Test legacy name.
        assertFunction("array_dupes(array[1, 2, 1])", new ArrayType(INTEGER), ImmutableList.of(1));

        RowType rowType = RowType.from(ImmutableList.of(RowType.field(INTEGER), RowType.field(INTEGER)));
        String t = rowType.toString();
        assertFunction("array_duplicates(array[array[1], array[2], array[]])", new ArrayType(new ArrayType(INTEGER)), ImmutableList.of());
        assertFunction("array_duplicates(array[array[1], array[2], array[2]])", new ArrayType(new ArrayType(INTEGER)), ImmutableList.of(ImmutableList.of(2)));
        assertFunction("array_duplicates(array[(1, 2), (1, 2)])", new ArrayType(rowType), ImmutableList.of(ImmutableList.of(1, 2)));
        assertFunction("array_duplicates(array[(1, 2), (2, 2)])", new ArrayType(rowType), ImmutableList.of());
        assertInvalidFunction("array_duplicates(array[(1, null), (null, 2), (null, 1)])", StandardErrorCode.NOT_SUPPORTED, "ROW comparison not supported for fields with null elements");
        assertInvalidFunction("array_duplicates(array[(1, null), (null, 2), (null, null)])", StandardErrorCode.NOT_SUPPORTED, "map key cannot be null or contain nulls");
    }
}
