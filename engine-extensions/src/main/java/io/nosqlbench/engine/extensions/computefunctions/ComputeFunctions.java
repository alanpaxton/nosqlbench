/*
 * Copyright (c) 2023 nosqlbench
 *
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

package io.nosqlbench.engine.extensions.computefunctions;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;

/**
 * <P>A collection of compute functions related to vector search relevancy.
 * These are based on arrays of indices of vectors, where the expected data is from known KNN test data,
 * and the actual data is from a vector search query.</P>
 *
 * <P>Variations of these functions have a limit parameter, which allows for derivation of relevancy
 * measurements for a smaller query without having to run a separate test for each K value.
 * If you are using test vectors from a computed KNN test data with for K=100, you can compute
 * metrics "@K" for any size up to and including K=100. This simply uses a partial view of the result
 * to do exactly what would have been done for a test where you actually query for that K limit.
 * <STRONG>This assumes that the result rank is stable irrespective of the limit AND the results
 * are passed to these functions as ranked in results.</STRONG></P> Some of the methods apply K to the
 * expected (relevant) indices, others to the actual (response) indices, depending on what is appropriate.
 *
 * <P>The array indices passed to these functions should not be sorted before-hand as a general rule.</P>
 * Yet, no provision is made for duplicate entries. If you have duplicate indices in either array,
 * these methods will yield incorrect results as they rely on the <EM>two-pointer</EM> method and do not
 * elide duplicates internally.
 */
public class ComputeFunctions {

    /**
     * Compute the recall as the proportion of matching indices divided by the expected indices
     *
     * @param referenceIndexes
     *     long array of indices
     * @param sampleIndexes
     *     long array of indices
     * @return a fractional measure of matching vs expected indices
     */
    public static double recall(long[] referenceIndexes, long[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = Intersections.find(referenceIndexes, sampleIndexes);
        return (double) intersection.length / (double) referenceIndexes.length;
    }

    public static double recall(long[] referenceIndexes, long[] sampleIndexes, int limit) {
        if (sampleIndexes.length < limit) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + sampleIndexes.length + ", limit=" + limit);
        }
        sampleIndexes = Arrays.copyOfRange(sampleIndexes, 0, limit);
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = Intersections.find(referenceIndexes, sampleIndexes);
        return (double) intersection.length / (double) referenceIndexes.length;
    }

    public static double precision(long[] referenceIndexes, long[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = Intersections.find(referenceIndexes, sampleIndexes);
        return (double) intersection.length / (double) sampleIndexes.length;
    }

    public static double precision(long[] referenceIndexes, long[] sampleIndexes, int limit) {
        if (sampleIndexes.length < limit) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + sampleIndexes.length + ", limit=" + limit);
        }
        sampleIndexes = Arrays.copyOfRange(sampleIndexes, 0, limit);
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        long[] intersection = Intersections.find(referenceIndexes, sampleIndexes);
        return (double) intersection.length / (double) sampleIndexes.length;
    }

    /**
     * Compute the recall as the proportion of matching indices divided by the expected indices
     *
     * @param referenceIndexes
     *     int array of indices
     * @param sampleIndexes
     *     int array of indices
     * @return a fractional measure of matching vs expected indices
     */
    public static double recall(int[] referenceIndexes, int[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int intersection = Intersections.count(referenceIndexes, sampleIndexes, referenceIndexes.length);
        return (double) intersection / (double) referenceIndexes.length;
    }

    public static double recall(int[] referenceIndexes, int[] sampleIndexes, int limit) {
        if (sampleIndexes.length < limit) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + sampleIndexes.length + ", limit=" + limit);
        }
        sampleIndexes = Arrays.copyOfRange(sampleIndexes, 0, limit);
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int intersection = Intersections.count(referenceIndexes, sampleIndexes, referenceIndexes.length);
        return (double) intersection / (double) referenceIndexes.length;
    }

    public static double precision(int[] referenceIndexes, int[] sampleIndexes) {
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int intersection = Intersections.count(referenceIndexes, sampleIndexes);
        return (double) intersection / (double) sampleIndexes.length;
    }

    public static double precision(int[] referenceIndexes, int[] sampleIndexes, int limit) {
        if (sampleIndexes.length < limit) {
            throw new RuntimeException("indices fewer than limit, invalid precision computation: index count=" + sampleIndexes.length + ", limit=" + limit);
        }
        sampleIndexes = Arrays.copyOfRange(sampleIndexes, 0, limit);
        Arrays.sort(referenceIndexes);
        Arrays.sort(sampleIndexes);
        int intersection = Intersections.count(referenceIndexes, sampleIndexes);
        return (double) intersection / (double) sampleIndexes.length;
    }

    /**
     * Compute the intersection of two long arrays
     */
    public static long[] intersection(long[] a, long[] b) {
        return Intersections.find(a, b);
    }

    public static long[] intersection(long[] a, long[] b, int limit) {
        return Intersections.find(a, b, limit);
    }

    /**
     * Compute the intersection of two int arrays
     */
    public static int[] intersection(int[] reference, int[] sample) {
        return Intersections.find(reference, sample);
    }

    public static int[] intersection(int[] reference, int[] sample, int limit) {
        return Intersections.find(reference, sample, limit);
    }

    /**
     * Compute the size of the intersection of two int arrays
     */
    public static int intersectionSize(int[] reference, int[] sample) {
        return Intersections.count(reference, sample);
    }

    public static int intersectionSize(int[] reference, int[] sample, int limit) {
        return Intersections.count(reference, sample, limit);
    }

    public static int intersectionSize(long[] reference, long[] sample) {
        return Intersections.count(reference, sample);
    }

    public static int intersectionSize(long[] reference, long[] sample, int limit) {
        return Intersections.count(reference, sample, limit);
    }

    public static double F1(int[] reference, int[] sample) {
        return F1(reference, sample, reference.length);
    }

    public static double F1(int[] reference, int[] sample, int limit) {
        double recallAtK = recall(reference, sample, limit);
        double precisionAtK = precision(reference, sample, limit);
        return 2.0d * ((recallAtK * precisionAtK) / (recallAtK + precisionAtK));
    }

    public static double F1(long[] reference, long[] sample) {
        return F1(reference, sample, reference.length);
    }

    public static double F1(long[] reference, long[] sample, int limit) {
        double recallAtK = recall(reference, sample, limit);
        double precisionAtK = precision(reference, sample, limit);
        return 2.0d * ((recallAtK * precisionAtK) / (recallAtK + precisionAtK));
    }

    /**
     * Reciprocal Rank - The multiplicative inverse of the first rank which is relevant.
     */
    public static double reciprocal_rank(long[] reference, long[] sample, int limit) {
        int firstRank = Intersections.firstMatchingIndex(reference, sample, limit);
        if (firstRank >= 0) {
            return 1.0d / (firstRank+1);
        } else {
            return 0.0;
        }
    }

    public static double reciprocal_rank(long[] reference, long[] sample) {
        return reciprocal_rank(reference, sample, reference.length);
    }

    public static double reciprocal_rank(int[] reference, int[] sample, int limit) {
        int firstRank = Intersections.firstMatchingIndex(reference, sample, limit);
        if (firstRank >= 0) {
            return 1.0d / (firstRank+1);
        } else {
            return 0.0;
        }
    }

    public static double reciprocal_rank(int[] reference, int[] sample) {
        return reciprocal_rank(reference, sample, reference.length);
    }

    public static double average_precision(int[] reference, int[] sample) {
        return average_precision(reference,sample,reference.length);
    }

    public static double average_precision(int[] reference, int[] sample, int k) {
        int maxK = Math.min(k,sample.length);
        HashSet<Integer> refset = new HashSet<>(reference.length);
        for (Integer i : reference) {
            refset.add(i);
        }
        int relevant=0;
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int i = 0; i < maxK; i++) {
            if (refset.contains(sample[i])){
                relevant++;
                double precisionAtIdx = (double) relevant / (i+1);
                stats.accept(precisionAtIdx);
            }
        }
        return stats.getAverage();
    }

    public static double average_precision(long[] reference, long[] sample, int k) {
        int maxK = Math.min(k,sample.length);
        HashSet<Long> refset = new HashSet<>(reference.length);
        for (Long i : reference) {
            refset.add(i);
        }
        int relevant=0;
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int i = 0; i < maxK; i++) {
            if (refset.contains(sample[i])){
                relevant++;
                double precisionAtIdx = (double) relevant / (i+1);
                stats.accept(precisionAtIdx);
            }
        }
        return stats.getAverage();
    }
}
