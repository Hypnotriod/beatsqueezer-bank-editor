
package com.hypnotriod.beatsqueezereditor.tools;

import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author ipikin
 */
public class WaveDrawingTool 
{
    public static void drawWave16Bit(Canvas canvas, byte[] data, int channels, SustainLoopVO loopVO, int framePosition)
    {
        if(canvas == null || data == null) return;
        
        int i;
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        float pointX = 0.0f;
        float pointY;
        float pointXStep = (float)canvas.getWidth() / ((float)data.length / 2.0f);
        float pointYScale;
        float previewY1;
        float previewY2;
        float sample;
        int   arrayStep = (int)(1.0f * (1.0f / pointXStep));
        for(i = 0; i < 6 && arrayStep >= 4; i++) { arrayStep /= 2; }
        pointXStep *= arrayStep; 
        arrayStep  *= 2;
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        
        if(channels == 2)
        {
            pointYScale = (float)canvas.getHeight() / 65536.0f / 2.0f;
            previewY1 = (float)canvas.getHeight() / 4 * 1 - 32768.0f * pointYScale;
            previewY2 = (float)canvas.getHeight() / 4 * 3 - 32768.0f * pointYScale;
            
            for(i = 0; i < data.length; i += arrayStep)
            {
                sample = (short)((data[i+1] << 8) | data[i]);
                pointY = (float)canvas.getHeight() / 4 * 1 - pointYScale * sample;
                pointX += pointXStep;

                gc.strokeLine(pointX - pointXStep, previewY1, pointX, pointY);
                previewY1 = pointY;
                
                sample = (short)((data[i+3] << 8) | data[i+2]);
                pointY = (float)canvas.getHeight() / 4 * 3 - pointYScale * sample;
                
                gc.strokeLine(pointX - pointXStep, previewY2, pointX, pointY);
                previewY2 = pointY;
            }
        }
        else
        {
            pointYScale = (float)canvas.getHeight() / 65536.0f;
            previewY1 = (float)canvas.getHeight() / 2 - 32768.0f * pointYScale;
            
            for(i = 0; i < data.length; i += arrayStep)
            {
                sample = (short)((data[i+1] << 8) | data[i]);
                pointY = (float)canvas.getHeight() / 2 - pointYScale * sample;
                pointX += pointXStep;

                gc.strokeLine(pointX - pointXStep, previewY1, pointX, pointY);
                previewY1 = pointY;
            }
        }
        
        
        if(loopVO != null)
        {
            gc.setGlobalAlpha(0.5);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            
            pointX = ((float)loopVO.start / ((float)data.length / 2.0f)) * (float)canvas.getWidth();

            gc.fillRect(pointX, 0, canvas.getWidth() - pointX, canvas.getHeight());

            gc.setGlobalAlpha(1.0);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            
            gc.strokeLine(pointX, 0, pointX, canvas.getHeight());
            //gc.strokeLine(canvas.getWidth() - 1, 0, canvas.getWidth() - 1, canvas.getHeight());
        }
        
        if(framePosition != -1)
        {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            
            pointX = ((float)framePosition * channels / ((float)data.length / 2.0f)) * (float)canvas.getWidth();
            
            gc.strokeLine(pointX, 0, pointX, canvas.getHeight());
        }
    }
}
