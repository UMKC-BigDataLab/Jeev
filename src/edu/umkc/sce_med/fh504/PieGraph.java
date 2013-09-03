package edu.umkc.sce_med.fh504;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class PieGraph {
	public Intent getIntent(Context context,int []values,boolean [] selection){
		
		Vaccines table = new Vaccines();
		CategorySeries series = new CategorySeries("Vaccine Distribution");
		int countVaccines=0;
		
		for (int i=0; i<values.length;i++){
			if(selection[i]==true)
					{series.add(table.sName[i], values[i]);
					countVaccines++;
					}
		}
		
		
		DefaultRenderer dRenderer = new DefaultRenderer();
		dRenderer.setChartTitle("Selected Vaccines Count");
		dRenderer.setBackgroundColor(Color.WHITE);
		dRenderer.setApplyBackgroundColor(true);
		dRenderer.setLabelsTextSize(20);
		 Random numGen = new Random();

		for(int c =0; c<countVaccines;c++){
			
			SimpleSeriesRenderer ssRenderer = new SimpleSeriesRenderer();
			int randomColor = Color.rgb(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
			ssRenderer.setColor(randomColor);
			
			dRenderer.addSeriesRenderer(ssRenderer);
			dRenderer.setShowLabels(true);
			dRenderer.setLabelsColor(Color.BLACK);
		}
		
		Intent intent = ChartFactory.getPieChartIntent(context, series, dRenderer, "Jeev - Server");
		
		
		return intent;
		
	}

}
