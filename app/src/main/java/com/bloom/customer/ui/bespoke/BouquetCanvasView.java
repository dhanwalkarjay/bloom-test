package com.bloom.customer.ui.bespoke;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BouquetCanvasView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<ItemNode> items = new ArrayList<>();
    private final Random random = new Random();
    private Map<String, Bitmap> bitmapCache = new HashMap<>();

    // 2.5D Projection variables
    private final float focalLength = 800f;

    public BouquetCanvasView(Context context) {
        super(context);
    }

    public BouquetCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Bitmap getBitmap(String resName) {
        if (bitmapCache.containsKey(resName)) {
            return bitmapCache.get(resName);
        }
        int resId = getContext().getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
        if (resId != 0) {
            Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), resId);
            bitmapCache.put(resName, b);
            return b;
        }
        return null;
    }

    public void addItem(String resName, String type) {
        ItemNode node = new ItemNode();
        node.resName = resName;
        node.type = type;
        
        // Spawn from center base
        node.currentX = 0;
        node.currentY = 200;
        node.currentZ = 0;
        
        node.targetX = node.currentX;
        node.targetY = node.currentY;
        node.targetZ = node.currentZ;

        items.add(node);
        invalidate();
    }

    public void removeItem(String resName) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).resName.equals(resName)) {
                items.remove(i);
                break;
            }
        }
        invalidate();
    }

    public void autoArrange() {
        if (items.isEmpty()) return;

        List<ItemNode> flowers = new ArrayList<>();
        for (ItemNode n : items) {
            if (n.type.equals("STEM") || n.type.equals("FILLER")) {
                flowers.add(n);
            }
        }

        int n = flowers.size();
        if (n > 0) {
            float maxRadius = 350f;
            for (int i = 0; i < n; i++) {
                ItemNode node = flowers.get(i);
                
                // Conical fan spread
                float angle = (float) Math.PI / 2; // Straight up if only 1
                if (n > 1) {
                    float spread = (float) (Math.PI * 0.85); // Wider Spread angle
                    float startAngle = (float) (Math.PI / 2 + spread / 2);
                    angle = startAngle - (i / (float) (n - 1)) * spread;
                }
                
                float r = maxRadius * (0.5f + random.nextFloat() * 0.5f); // Randomize height slightly
                
                node.targetX = (float) Math.cos(angle) * r;
                node.targetY = -(float) Math.sin(angle) * r; 
                node.targetZ = (random.nextFloat() - 0.5f) * 200f; // Random depth
            }
        }

        for (ItemNode itemNode : items) {
            if (itemNode.type.equals("WRAPPER") || itemNode.type.equals("RIBBON")) {
                itemNode.targetX = 0;
                itemNode.targetY = 0;
                itemNode.targetZ = 0;
            }
        }

        ValueAnimator arrangeAnimator = ValueAnimator.ofFloat(0f, 1f);
        arrangeAnimator.setDuration(1200);
        arrangeAnimator.setInterpolator(new OvershootInterpolator(1.1f));
        arrangeAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            for (ItemNode node : items) {
                node.currentX = node.startX + (node.targetX - node.startX) * progress;
                node.currentY = node.startY + (node.targetY - node.startY) * progress;
                node.currentZ = node.startZ + (node.targetZ - node.startZ) * progress;
            }
            invalidate();
        });

        for (ItemNode node : items) {
            node.startX = node.currentX;
            node.startY = node.currentY;
            node.startZ = node.currentZ;
        }

        arrangeAnimator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) return;

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        ItemNode wrapperNode = null;
        ItemNode ribbonNode = null;
        List<ProjectedNode> projectedNodes = new ArrayList<>();

        for (ItemNode node : items) {
            if (node.type.equals("WRAPPER")) {
                wrapperNode = node;
                continue;
            }
            if (node.type.equals("RIBBON")) {
                ribbonNode = node;
                continue;
            }

            // Stems & Fillers
            float distance = focalLength + node.currentZ;
            if (distance <= 0) distance = 0.1f;
            float scale = focalLength / distance;
            
            float projX = cx + (node.currentX * scale);
            float projY = cy + (node.currentY * scale);

            projectedNodes.add(new ProjectedNode(node.resName, node.type, projX, projY, scale, node.currentZ));
        }

        // Painter's Algorithm: Furthest Z first
        // If Z is depth (positive is far away, negative is close). 
        // distance = focalLength + Z. So larger Z = further away.
        // We want to draw furthest first, so sort descending by Z.
        Collections.sort(projectedNodes, (a, b) -> Float.compare(b.z, a.z)); 

        // 1. Draw Wrapper (Behind everything)
        if (wrapperNode != null) {
            Bitmap b = getBitmap(wrapperNode.resName);
            if (b != null) {
                Matrix matrix = new Matrix();
                matrix.postTranslate(-b.getWidth() / 2f, -b.getHeight() / 2f);
                float wrapperScale = 1.2f; // Make wrapper large enough
                matrix.postScale(wrapperScale, wrapperScale);
                matrix.postTranslate(cx, cy);
                paint.setColorFilter(null);
                canvas.drawBitmap(b, matrix, paint);
            }
        }

        // 2. Draw Blooms & Fillers
        for (ProjectedNode pNode : projectedNodes) {
            Bitmap b = getBitmap(pNode.resName);
            if (b != null) {
                Matrix matrix = new Matrix();
                matrix.postTranslate(-b.getWidth() / 2f, -b.getHeight() / 2f);
                
                // Photorealistic Scale 
                float baseScale = pNode.type.equals("FILLER") ? 0.35f : 0.45f;
                matrix.postScale(baseScale * pNode.scale, baseScale * pNode.scale);
                
                // Natural Rotation (fan outward from center)
                float dx = pNode.x - cx;
                float rotAngle = (dx / (getWidth() / 2f)) * 45f; // Rotate up to 45 deg
                matrix.postRotate(rotAngle);
                
                matrix.postTranslate(pNode.x, pNode.y);

                // Depth Shadowing: Stems further back (z > 0) are darkened
                if (pNode.z > 0) {
                    float darkenAmount = 1f - Math.min(1f, (pNode.z / 250f) * 0.4f); // Up to 40% darker
                    ColorMatrix cm = new ColorMatrix();
                    cm.setScale(darkenAmount, darkenAmount, darkenAmount, 1f);
                    paint.setColorFilter(new ColorMatrixColorFilter(cm));
                } else {
                    paint.setColorFilter(null);
                }
                
                canvas.drawBitmap(b, matrix, paint);
            }
        }
        paint.setColorFilter(null);

        // 3. Draw Ribbon (In front of everything, at binding point)
        if (ribbonNode != null) {
            Bitmap b = getBitmap(ribbonNode.resName);
            if (b != null) {
                Matrix matrix = new Matrix();
                matrix.postTranslate(-b.getWidth() / 2f, -b.getHeight() / 2f);
                float ribbonScale = 0.5f;
                matrix.postScale(ribbonScale, ribbonScale);
                matrix.postTranslate(cx, cy + 200f); // Draw at the binding point
                canvas.drawBitmap(b, matrix, paint);
            }
        }
    }

    private static class ItemNode {
        String resName;
        String type;
        float currentX, currentY, currentZ;
        float targetX, targetY, targetZ;
        float startX, startY, startZ;
    }

    private static class ProjectedNode {
        String resName;
        String type;
        float x, y;
        float scale;
        float z;

        public ProjectedNode(String resName, String type, float x, float y, float scale, float z) {
            this.resName = resName;
            this.type = type;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.z = z;
        }
    }
}
