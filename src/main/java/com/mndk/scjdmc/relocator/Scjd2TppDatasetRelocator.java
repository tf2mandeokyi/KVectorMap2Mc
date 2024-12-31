package com.mndk.scjdmc.relocator;

import com.mndk.scjdmc.column.LayerDataType;
import com.mndk.scjdmc.reader.ScjdDatasetReader;
import com.mndk.scjdmc.util.*;
import com.mndk.scjdmc.util.file.ScjdFileInformation;
import com.mndk.scjdmc.util.io.SimpleFeatureJsonWriter;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Predicate;

public class Scjd2TppDatasetRelocator {


    public static void relocateMemoryIntensive(File sourceFile, Charset sourceEncoding,
                                               ScjdParsedType parsedType, ScjdDatasetReader reader,
                                               File tppDatasetFolder, Charset destinationEncoding,
                                               double buffer, Predicate<LayerDataType> cutFeatures) throws IOException {

        Map<Pair<TppTileCoordinate, LayerDataType>, Integer> coordinateTypeCountMap = new HashMap<>();
        ScjdFileInformation fileInformation = new ScjdFileInformation(sourceFile, parsedType);

        reader.read(sourceFile, sourceEncoding, parsedType, (featureCollection, layerDataType, typeCount) -> {
            Map<TppTileCoordinate, List<SimpleFeature>> featureMap = new HashMap<>();
            ProgressBar progressBar1 = ProgressBarUtils.createProgressBar(
                    "Relocating: %s - %s#%d".formatted(fileInformation.getNameForFile(), layerDataType.getEnglishName(), typeCount),
                    featureCollection.size()
            );

            SimpleFeatureIterator featureIterator = featureCollection.features();
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();

                BoundingBox featureBoundingBox = feature.getBounds();
                Set<TppTileCoordinate> tileCoordinateIntersections =
                        TppTileCoordinate.getBoundingBoxIntersections(featureBoundingBox, buffer);

                for (TppTileCoordinate intersectionCoord : tileCoordinateIntersections) {
                    SimpleFeature newFeature = cutFeatures.test(layerDataType)
                            ? FeatureGeometryUtils.getFeatureGeometryIntersection(feature, intersectionCoord.getTileGeometry(buffer))
                            : feature;
                    if (newFeature != null) {
                        featureMap.computeIfAbsent(intersectionCoord, c -> new ArrayList<>()).add(newFeature);
                    }
                }
                progressBar1.step();
            }
            featureIterator.close();
            progressBar1.close();

            ProgressBar progressBar2 = ProgressBarUtils.createProgressBar(
                    "Writing: %s - %s#%d".formatted(fileInformation.getNameForFile(), layerDataType.getEnglishName(), typeCount),
                    featureMap.size()
            );
//            String destinationFileName = "%s_%s_%d.txt".formatted(fileInformation.getNameForFile(), layerDataType.getLayerName(), typeCount);
//            File destinationFile = new File(tppDatasetFolder, destinationFileName);
//            FileWriter writer = new FileWriter(destinationFile, destinationEncoding);
//            for (Map.Entry<TppTileCoordinate, List<SimpleFeature>> entry : featureMap.entrySet()) {
//                TppTileCoordinate coordinate = entry.getKey();
//                List<SimpleFeature> features = entry.getValue();
//
//                writer.write("%d\t%d\t".formatted(coordinate.x(), coordinate.y()));
//                for (int i = 0; i < features.size(); ++i) {
//                    writer.write(Constants.FEATURE_JSON.toString(features.get(i)));
//                    if (i != features.size() - 1) writer.write("\t");
//                }
//                writer.write("\n");
//                progressBar2.step();
//            }
//            writer.close();
            for (Map.Entry<TppTileCoordinate, List<SimpleFeature>> entry : featureMap.entrySet()) {
                TppTileCoordinate coordinate = entry.getKey();
                List<SimpleFeature> features = entry.getValue();

                Pair<TppTileCoordinate, LayerDataType> pair = Pair.of(coordinate, layerDataType);
                int coordinateTypeCount = IntegerMapUtils.increment(coordinateTypeCountMap, pair, 0);
                SimpleFeatureJsonWriter writer = createWriterForCoordinate(
                        fileInformation, layerDataType, coordinate, coordinateTypeCount, tppDatasetFolder, destinationEncoding
                );

                for (SimpleFeature feature : features) {
                    writer.write(feature);
                }
                writer.close();
                progressBar2.step();
            }
            progressBar2.close();

            return null;
        });
    }


    public static void relocate(File sourceFile, Charset sourceEncoding,
                                ScjdParsedType parsedType, ScjdDatasetReader reader,
                                File tppDatasetFolder, Charset destinationEncoding,
                                double buffer, Predicate<LayerDataType> cutFeatures) throws IOException {

        Map<Pair<TppTileCoordinate, LayerDataType>, Integer> coordinateTypeCountMap = new HashMap<>();
        ScjdFileInformation fileInformation = new ScjdFileInformation(sourceFile, parsedType);

        reader.read(sourceFile, sourceEncoding, parsedType, (featureCollection, layerDataType, typeCount) -> {
            Map<TppTileCoordinate, SimpleFeatureJsonWriter> writerMap = new HashMap<>();
            ProgressBar progressBar1 = ProgressBarUtils.createProgressBar(
                    "Relocating: %s - %s#%d".formatted(fileInformation.getNameForFile(), layerDataType.getEnglishName(), typeCount),
                    featureCollection.size()
            );

            SimpleFeatureIterator featureIterator = featureCollection.features();
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();

                BoundingBox featureBoundingBox = feature.getBounds();
                Set<TppTileCoordinate> tileCoordinateIntersections =
                        TppTileCoordinate.getBoundingBoxIntersections(featureBoundingBox, buffer);

                for (TppTileCoordinate intersectionCoord : tileCoordinateIntersections) {
                    Pair<TppTileCoordinate, LayerDataType> pair = Pair.of(intersectionCoord, layerDataType);
                    SimpleFeatureJsonWriter writer = writerMap.computeIfAbsent(intersectionCoord, c -> {
                        int coordinateTypeCount = IntegerMapUtils.increment(coordinateTypeCountMap, pair, 0);
                        return createWriterForCoordinate(
                                fileInformation, layerDataType, c, coordinateTypeCount, tppDatasetFolder, destinationEncoding
                        );
                    });

                    SimpleFeature newFeature = cutFeatures.test(layerDataType)
                            ? FeatureGeometryUtils.getFeatureGeometryIntersection(feature, intersectionCoord.getTileGeometry(buffer))
                            : feature;
                    if (newFeature != null) {
                        writer.write(newFeature);
                        // writer.flush();
                    }
                }
                progressBar1.step();
            }
            featureIterator.close();
            progressBar1.close();

            ProgressBar progressBar2 = ProgressBarUtils.createProgressBar(
                    "Closing: %s - %s#%d".formatted(fileInformation.getNameForFile(), layerDataType.getEnglishName(), typeCount),
                    writerMap.size()
            );
            for (Map.Entry<TppTileCoordinate, SimpleFeatureJsonWriter> entry : writerMap.entrySet()) {
                entry.getValue().close();
                progressBar2.step();
            }
            progressBar2.close();

            return null;
        });
    }


    private static SimpleFeatureJsonWriter createWriterForCoordinate(ScjdFileInformation fileInformation,
                                                                     LayerDataType layerDataType,
                                                                     TppTileCoordinate tileCoordinate,
                                                                     int count,
                                                                     File datasetFolder, Charset encoding) {
        try {
            File coordinateFolder = tileCoordinate.getFolderLocation(datasetFolder, true);
            String fileName = "%s_%s_%d.json".formatted(fileInformation.getNameForFile(), layerDataType.getLayerName(), count);
            return new SimpleFeatureJsonWriter(new File(coordinateFolder, fileName), encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
