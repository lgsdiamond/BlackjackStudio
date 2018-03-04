package com.lgsdiamond.outsource.mpchart.interfaces;

import com.lgsdiamond.outsource.mpchart.components.YAxis;
import com.lgsdiamond.outsource.mpchart.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
