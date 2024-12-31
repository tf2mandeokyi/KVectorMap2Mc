package com.mndk.scjdmc.util.function;

import org.opengis.feature.simple.SimpleFeature;

import java.util.function.Predicate;

public interface FeatureFilter extends Predicate<SimpleFeature> {
}
