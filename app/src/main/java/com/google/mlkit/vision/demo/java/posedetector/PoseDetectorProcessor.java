/*
 * Copyright 2020 Google LLC. All rights reserved.
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

package com.google.mlkit.vision.demo.java.posedetector;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.demo.java.posedetector.classification.PoseClassifierProcessor;
import com.google.mlkit.vision.demo.video.VideoBaseActivity;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A processor to run pose detector.
 */
public class PoseDetectorProcessor extends VisionProcessorBase<Pose> {
    private static final String TAG = "PoseDetectorProcessor";

    private final PoseDetector detector;

    private final boolean showInFrameLikelihood;
    private final boolean visualizeZ;
    private final boolean rescaleZForVisualization;
    private final boolean runClassification;
    private final boolean isStreamMode;
    private final Context context;
    private final Executor classificationExecutor;
    private final CaptureFramesForTest captureFramesForTest;

    private PoseClassifierProcessor poseClassifierProcessor;


    /**
     * Internal class to hold Pose and classification results.
     */
    protected static class PoseWithClassification {
        private final Pose pose;
        private final List<String> classificationResult;

        public PoseWithClassification(Pose pose, List<String> classificationResult) {
            this.pose = pose;
            this.classificationResult = classificationResult;
        }

        public Pose getPose() {
            return pose;
        }

        public List<String> getClassificationResult() {
            return classificationResult;
        }
    }

    ContentResolver resolver, normResolver;
    Uri uri, normUri;
    public PoseDetectorProcessor(
            Context context,
            PoseDetectorOptionsBase options,
            boolean showInFrameLikelihood,
            boolean visualizeZ,
            boolean rescaleZForVisualization,
            boolean runClassification,
            boolean isStreamMode) {
        super(context);
        this.showInFrameLikelihood = showInFrameLikelihood;
        this.visualizeZ = visualizeZ;
        this.rescaleZForVisualization = rescaleZForVisualization;
        detector = PoseDetection.getClient(options);
        this.runClassification = runClassification;
        this.isStreamMode = isStreamMode;
        this.context = context;
        classificationExecutor = Executors.newSingleThreadExecutor();

        resolver = context.getContentResolver();
        normResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, VideoBaseActivity.chosenFileName + "_" + "framesFull.txt");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
        uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

        ContentValues normContentValues = new ContentValues();
        normContentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, VideoBaseActivity.chosenFileName + "_" + "normFramesFull.txt");
        normContentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        normContentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
        normUri = resolver.insert(MediaStore.Files.getContentUri("external"), normContentValues);

        captureFramesForTest = new CaptureFramesForTest(context, uri, resolver, normUri, normResolver);
    }

    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    @Override
    protected Task<Pose> detectInImage(InputImage image) {
        return detector
                .process(image);
    }

    @Override
    protected Task<Pose> detectInImage(MlImage image) {
        return detector
                .process(image);
    }

    @Override
    protected void onSuccess(
            @NonNull Pose pose,
            @NonNull GraphicOverlay graphicOverlay) {

        captureFramesForTest.captureFullSessionFrames(AIUtils.extractPoseLandmarks(pose), pose, "");
        List<PointF3D> normalizedLandmarks = AIUtils.normalize(
                AIUtils.extractPoseLandmarks(pose), graphicOverlay.getImageWidth(), graphicOverlay.getImageHeight()
        );
        captureFramesForTest.captureFullSessionFrames(normalizedLandmarks, pose, "norm");
        graphicOverlay.add(
                new PoseGraphic(
                        graphicOverlay,
                        pose,
                        showInFrameLikelihood,
                        visualizeZ,
                        rescaleZForVisualization,
                        new ArrayList<>()));


    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Pose detection failed!", e);
    }

    @Override
    protected boolean isMlImageEnabled(Context context) {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return true;
    }
}
