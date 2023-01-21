package com.google.mlkit.vision.demo.java.posedetector;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.demo.java.posedetector.classification.Utils;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.List;

public class AIUtils {

    public static double distance(int a, int b, List<PoseLandmark> currentLandmarks) {
        float valueX = (currentLandmarks.get(a).getPosition().x - currentLandmarks.get(b).getPosition().x);
        float valueY = (currentLandmarks.get(a).getPosition().y - currentLandmarks.get(b).getPosition().y);
        return Math.sqrt((Math.pow(valueX, 2) + Math.pow(valueY, 2)));
    }

    public static List<Float> extractLikelyHoods(Pose pose) {
        List<Float> inFrameLikelyHood = new ArrayList<>();
        for (PoseLandmark poseLandmark : pose.getAllPoseLandmarks()) {
            inFrameLikelyHood.add(poseLandmark.getInFrameLikelihood());
        }
        return inFrameLikelyHood;
    }

    public static List<PointF3D> extractPoseLandmarks(Pose pose) {
        List<PointF3D> landmarks = new ArrayList<>();
        for (PoseLandmark poseLandmark : pose.getAllPoseLandmarks()) {
            landmarks.add(poseLandmark.getPosition3D());
        }
        return landmarks;
    }

    public static List<PointF3D> normalize(List<PointF3D> landmarks, float width, float height) {
        List<PointF3D> normalizedLandmarks = new ArrayList<>(landmarks);
        // Normalize translation.
        Utils.divideAll(normalizedLandmarks, width, height);

        return normalizedLandmarks;
    }

}
