package edu.umkc.sce_med.fh504;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;

public class BarGraph {

	public Intent getIntent (Context context, int [] values,boolean [] selections){
		
		Vaccines table = new Vaccines();
		
		double [] range = new double[4];
		range[0]=range[1]=range[2]=range[3]=0;
		for(int r=0;r<values.length;r++)
			if(range[3]<=values[r])//max y
				range[3]=values[r];
		CategorySeries series =new CategorySeries ("Patients");
		for (int i=0; i<selections.length;i++){
			if(selections[i]==true){
				series.add("Bar ", values[i]);
				range[1]++;//max x
			}
			
		}
		
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series.toXYSeries());
		
		XYSeriesRenderer renderer=new XYSeriesRenderer();
		renderer.setDisplayChartValues(true);
		//renderer.setChartValuesSpacing((float) 5.0);
		renderer.setColor(Color.BLUE);
		renderer.setChartValuesTextSize(24);
	
		
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.setChartTitle("Vaccinations Count");
		mRenderer.setXTitle("Vaccine");
		mRenderer.setYTitle("Number of Patients");
		mRenderer.setBarSpacing(1.0);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setXAxisMax(range[1]);
		mRenderer.setYAxisMax(range[3]);
		mRenderer.setInitialRange(range);
		mRenderer.setLegendTextSize(20);
		mRenderer.setBackgroundColor(Color.WHITE);
		mRenderer.setMarginsColor(Color.WHITE);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setLabelsTextSize(14);
		mRenderer.setLabelsColor(Color.BLACK);
		
		
		mRenderer.clearXTextLabels();
		mRenderer.setXLabels(0);
		mRenderer.setXLabelsColor(Color.BLACK);
		int q=1;
		for(int k=0;k<table.size;k++){
			if(selections[k]==true){
				mRenderer.addXTextLabel(q++, table.sName[k]);
			}
		}

		
		Intent intent = ChartFactory.getBarChartIntent(context, dataset, mRenderer,Type.DEFAULT);		
		
		return intent;
		
	}
}
